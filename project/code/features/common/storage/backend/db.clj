(ns features.common.storage.backend.db
  (:require 
    [zero.backend.state.postgres :as postgres]))

(defn create-table
  "Create the storage table if it doesn't exist"
  []
  (postgres/execute-sql 
   "CREATE TABLE IF NOT EXISTS storage(
      id        uuid PRIMARY KEY,
      alias     VARCHAR(50) NOT NULL,
      type      VARCHAR(255) NOT NULL,
      url       VARCHAR(255) UNIQUE,
      size      BIGINT,
      mime_type VARCHAR(255),
      path      uuid[],
      items     uuid[],
      wsid      uuid,
      added_by  uuid,
      added_at  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
    )"))

(defn search-by-alias-with-path
  "Search by alias with path using recursive CTE"
  [table-name search-term workspace-id is-superadmin mime-types]
  (postgres/execute-sql 
   "WITH RECURSIVE path_items AS (
      -- Get all items that match the search term
      SELECT 
          s.*,
          s.path as item_path,
          array_agg(p.id) as breadcrumb_ids,
          array_agg(p.alias) as breadcrumb_names
      FROM storage s
      LEFT JOIN LATERAL (
          SELECT id, alias
          FROM storage
          WHERE id = ANY(s.path)
          AND (CASE WHEN $4 
                    THEN wsid IS NULL 
                    ELSE wsid = $3::uuid 
               END)
          ORDER BY array_position(s.path, id)
      ) p ON true
      WHERE s.alias ILIKE '%' || $2 || '%'
      AND (CASE WHEN $4 
                THEN s.wsid IS NULL 
                ELSE s.wsid = $3::uuid 
           END)
      AND (s.type = 'directory'
           OR ($5::text[] is null 
               OR s.mime_type = ANY($5::text[])))
      GROUP BY s.id, s.alias, s.type, s.url, s.size, s.mime_type, s.path, s.items, s.wsid, s.added_by, s.added_at
    )
    SELECT 
      id,
      alias,
      type,
      url,
      size,
      mime_type,
      path as item_path,
      items,
      wsid,
      added_by,
      added_at,
      breadcrumb_ids,
      breadcrumb_names
    FROM path_items
    LIMIT 10"
   {:params [table-name search-term workspace-id is-superadmin mime-types]}))

(defn search-by-alias
  "Search by alias"
  [search-term workspace-id is-superadmin mime-types]
  (postgres/execute-sql 
   "SELECT * FROM storage
    WHERE alias ILIKE '%' || $1 || '%'
    AND (CASE WHEN $3 
              THEN wsid IS NULL 
              ELSE wsid = $2::uuid 
         END)
    AND (type = 'directory'
          OR ($4::text[] is null OR mime_type = ANY($4::text[])))"
   {:params [search-term workspace-id is-superadmin mime-types]}))

(defn open-home
  "Open home directory"
  [workspace-id is-superadmin mime-types]
  (postgres/execute-sql 
   "SELECT * FROM storage
    WHERE (path = '{}' OR path IS NULL)
    AND (CASE WHEN $2 
              THEN wsid IS NULL 
              ELSE wsid = $1::uuid 
         END)
    AND (type = 'directory'
         OR ($3::text[] is null OR mime_type = ANY($3::text[])))"
   {:params [workspace-id is-superadmin mime-types]}))

(defn open-dir
  "Open a specific directory"
  [dir-id workspace-id is-superadmin mime-types]
  (postgres/execute-sql 
   "SELECT dir_items.* FROM storage dir 
    JOIN storage dir_items 
    ON dir_items.id = ANY(dir.items) 
    WHERE dir.id = $1
    AND (CASE WHEN $3 
              THEN dir.wsid IS NULL 
              ELSE dir.wsid = $2::uuid 
         END)
    AND (CASE WHEN $3 
              THEN dir_items.wsid IS NULL 
              ELSE dir_items.wsid = $2::uuid 
         END)
    AND (dir_items.type = 'directory'
          OR ($4::text[] is null 
              OR dir_items.mime_type = ANY($4::text[])))"
   {:params [dir-id workspace-id is-superadmin mime-types]}))

(defn create-directory-to-dir
  "Create a directory within another directory"
  [id alias type added-by workspace-id is-superadmin dir-id]
  (first 
    (postgres/execute-sql 
     "WITH new_item AS (
        INSERT INTO storage (id, alias, type, added_by, added_at, wsid, path)
        VALUES ($1, $2, $3, $4, now(), 
                CASE WHEN $6 
                     THEN NULL 
                     ELSE $5::uuid 
                END,
                (SELECT path || $7::uuid FROM storage 
                 WHERE id = $7::uuid 
                 AND (CASE WHEN $6 
                           THEN wsid IS NULL 
                           ELSE wsid = $5::uuid 
                      END)))
        RETURNING *
      ),
      updated AS (
        UPDATE storage
        SET items = array_append(items, (SELECT id FROM new_item))
        WHERE id = $7::uuid 
        AND (CASE WHEN $6 
                  THEN wsid IS NULL 
                  ELSE wsid = $5::uuid 
             END)
      )
      SELECT * FROM new_item"
     {:params [id alias type added-by workspace-id is-superadmin dir-id]})))

(defn create-directory-to-root
  "Create a directory at the root level"
  [id alias type added-by workspace-id is-superadmin]
  (first 
    (postgres/execute-sql 
     "INSERT INTO storage (id, alias, type, added_by, added_at, wsid)
      VALUES($1, $2, $3, $4, now(), 
             CASE WHEN $6 
                  THEN NULL 
                  ELSE $5::uuid 
             END)
      RETURNING *"
     {:params [id alias type added-by workspace-id is-superadmin]})))

(defn delete-item
  "Delete an item and all its children recursively"
  [item-id workspace-id is-superadmin]
  (postgres/execute-sql 
   "WITH RECURSIVE items_in_dir AS (
      SELECT id, alias, items
      FROM storage
      WHERE id = $1
      AND (CASE WHEN $3 
                THEN wsid IS NULL 
                ELSE wsid = $2::uuid 
           END)

      UNION ALL

      SELECT s.id, s.alias, s.items
      FROM storage s
      INNER JOIN items_in_dir itd ON s.id = ANY(itd.items)
      WHERE (CASE WHEN $3 
                  THEN s.wsid IS NULL 
                  ELSE s.wsid = $2::uuid 
             END)
    ),
    updated_items AS(
      UPDATE storage
      SET items = array_remove(items, $1)
      WHERE $1 = ANY(items)
      AND (CASE WHEN $3 
                THEN wsid IS NULL 
                ELSE wsid = $2::uuid 
           END)
    )
    DELETE FROM storage
    WHERE id IN (select id from items_in_dir)
    AND (CASE WHEN $3 
              THEN wsid IS NULL 
              ELSE wsid = $2::uuid 
         END)
    RETURNING id, type, url"
   {:params [item-id workspace-id is-superadmin]}))

(defn file-insert-to-dir
  "Insert a file into a specific directory"
  [id alias type mime-type size url workspace-id added-by is-superadmin dir-id]
  (first 
    (postgres/execute-sql 
     "WITH new_item AS (
        INSERT INTO storage (id, alias, type, mime_type, size, url, wsid, added_by, added_at, path) 
        VALUES($1, $2, $3, $4, $5, $6, 
               CASE WHEN $9 
                    THEN NULL 
                    ELSE $7::uuid 
               END, 
               $8, now(),
               (SELECT path || $10::uuid FROM storage 
                WHERE id = $10::uuid 
                AND (CASE WHEN $9 
                          THEN wsid IS NULL 
                          ELSE wsid = $7::uuid 
                     END)))
        RETURNING *
      ),
      updated AS(
        UPDATE storage
        SET items = array_append(items, (SELECT id FROM new_item))
        WHERE id = $10::uuid 
        AND (CASE WHEN $9 
                  THEN wsid IS NULL 
                  ELSE wsid = $7::uuid 
             END)
      )
      SELECT * FROM new_item"
     {:params [id alias type mime-type size url workspace-id added-by is-superadmin dir-id]})))

(defn file-insert-to-root
  "Insert a file at the root level"
  [id alias type mime-type size url workspace-id added-by is-superadmin]
  (first 
    (postgres/execute-sql 
     "INSERT INTO storage (id, alias, type, mime_type, size, url, wsid, added_by, added_at, path) 
      VALUES($1, $2, $3, $4, $5, $6, 
             CASE WHEN $9 
                  THEN NULL 
                  ELSE $7::uuid 
             END, 
             $8, now(), '{}')
      RETURNING *"
     {:params [id alias type mime-type size url workspace-id added-by is-superadmin]})))

(defn rename-item
  "Rename an item"
  [new-alias item-id workspace-id is-superadmin]
  (first 
    (postgres/execute-sql 
     "UPDATE storage
      SET alias = $1
      WHERE id = $2
      AND (CASE WHEN $4 
                THEN wsid IS NULL 
                ELSE wsid = $3::uuid 
           END)
      RETURNING *"
     {:params [new-alias item-id workspace-id is-superadmin]})))