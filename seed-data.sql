-- ============================================================================
-- ExpertLift Database Seed Data
-- ============================================================================
-- Ez a script feltölti az adatbázist alap teszt adatokkal.
-- Futtatás: ./seed-database.sh
-- ============================================================================

-- ============================================================================
-- 1. TÖRLÉS (FK-k miatt fordított sorrendben)
-- ============================================================================

DELETE FROM expert_lift.worksheets;
DELETE FROM expert_lift.addresses;
DELETE FROM expert_lift.material_templates;
DELETE FROM expert_lift.users;
DELETE FROM expert_lift.workspaces;

-- ============================================================================
-- 2. WORKSPACES (1 db)
-- ============================================================================

INSERT INTO expert_lift.workspaces (id, name, description, active)
VALUES
  ('11111111-1111-1111-1111-111111111111', 'ExpertLift', 'Fő munkaterület lift karbantartáshoz', true);

-- ============================================================================
-- 3. USERS (5 db)
-- ============================================================================

INSERT INTO expert_lift.users (id, username, full_name, password_hash, role, email, phone, active, workspace_id)
VALUES
  -- Paul - superadmin, nincs workspace-hez kötve (mindent lát)
  ('22222222-2222-2222-2222-222222222201', 'paul', 'Paul Cristian', 'Kf8#mNq2$xL9', 'superadmin', 'paul@expertlift.hu', '+36301234567', true, NULL),

  -- Valentin - admin, ExpertLift workspace
  ('22222222-2222-2222-2222-222222222202', 'valentin', 'Valentin Varhegyi', 'Vr4&pWz7!jH3', 'admin', 'valentin@expertlift.hu', '+36302345678', true, '11111111-1111-1111-1111-111111111111'),

  -- János - employee, ExpertLift workspace
  ('22222222-2222-2222-2222-222222222203', 'janos', 'Kovács János', 'Jn6@tKs5#bY8', 'employee', 'janos@expertlift.hu', '+36303456789', true, '11111111-1111-1111-1111-111111111111'),

  -- Péter - employee, ExpertLift workspace
  ('22222222-2222-2222-2222-222222222204', 'peter', 'Nagy Péter', 'Pt9!cRm4&dF2', 'employee', 'peter@expertlift.hu', '+36304567890', true, '11111111-1111-1111-1111-111111111111'),

  -- Anna - employee, ExpertLift workspace
  ('22222222-2222-2222-2222-222222222205', 'anna', 'Szabó Anna', 'An3#gVx8$qE6', 'employee', 'anna@expertlift.hu', '+36305678901', true, '11111111-1111-1111-1111-111111111111');

-- ============================================================================
-- 4. ADDRESSES (5 db)
-- ============================================================================

INSERT INTO expert_lift.addresses (id, name, address_line1, address_line2, city, postal_code, country, contact_person, contact_phone, contact_email, elevators, workspace_id)
VALUES
  -- Budapest - irodaház
  ('33333333-3333-3333-3333-333333333301', 'Corvin Irodaház', 'Futó utca 35-37.', 'A épület', 'Budapest', '1082', 'Magyarország', 'Kiss László', '+36201234567', 'kiss.laszlo@corvin.hu',
   '["A1", "A2"]'::jsonb,
   '11111111-1111-1111-1111-111111111111'),

  -- Debrecen - bevásárlóközpont
  ('33333333-3333-3333-3333-333333333302', 'Forum Debrecen', 'Csapó utca 30.', NULL, 'Debrecen', '4024', 'Magyarország', 'Tóth Erzsébet', '+36302345678', 'toth.erzsebet@forum.hu',
   '["L1", "L2"]'::jsonb,
   '11111111-1111-1111-1111-111111111111'),

  -- Szeged - lakópark
  ('33333333-3333-3333-3333-333333333303', 'Napfény Lakópark', 'Kálvária sugárút 85.', 'B lépcsőház', 'Szeged', '6725', 'Magyarország', 'Varga Miklós', '+36303456789', 'varga.miklos@napfeny.hu',
   '["B1"]'::jsonb,
   '11111111-1111-1111-1111-111111111111'),

  -- Győr - gyár
  ('33333333-3333-3333-3333-333333333304', 'Audi Hungaria Gyár', 'Kardán utca 1.', 'K3 csarnok', 'Győr', '9027', 'Magyarország', 'Horváth Gábor', '+36304567890', 'horvath.gabor@audi.hu',
   '["K1", "K2"]'::jsonb,
   '11111111-1111-1111-1111-111111111111'),

  -- Pécs - kórház
  ('33333333-3333-3333-3333-333333333305', 'PTE Klinikai Központ', 'Ifjúság útja 13.', 'Főépület', 'Pécs', '7624', 'Magyarország', 'Dr. Fekete Zsolt', '+36305678901', 'fekete.zsolt@pte.hu',
   '["P1", "P2"]'::jsonb,
   '11111111-1111-1111-1111-111111111111');

-- ============================================================================
-- 5. MATERIAL TEMPLATES (5 db)
-- ============================================================================

INSERT INTO expert_lift.material_templates (id, name, unit, category, description, active, workspace_id)
VALUES
  -- Kábelek
  ('44444444-4444-4444-4444-444444444401', 'Acél sodronykötél 10mm', 'm', 'Kábelek', 'Tartókötél liftek számára, 10mm átmérő, EN 12385 szabványnak megfelelő', true, '11111111-1111-1111-1111-111111111111'),

  ('44444444-4444-4444-4444-444444444402', 'Vezérlő kábel 12x0.75mm²', 'm', 'Kábelek', 'Többeres vezérlőkábel, árnyékolt, lift vezérlőrendszerekhez', true, '11111111-1111-1111-1111-111111111111'),

  -- Alkatrészek
  ('44444444-4444-4444-4444-444444444403', 'Ajtózáró kontaktus', 'db', 'Alkatrészek', 'Biztonsági ajtózáró érzékelő, kabinés aknaajtóhoz', true, '11111111-1111-1111-1111-111111111111'),

  ('44444444-4444-4444-4444-444444444404', 'Vezetősín csúszóbetét', 'db', 'Alkatrészek', 'Kopásálló műanyag csúszóbetét T-profilú vezetősínhez', true, '11111111-1111-1111-1111-111111111111'),

  -- Kenőanyagok
  ('44444444-4444-4444-4444-444444444405', 'Vezetősín kenőolaj', 'l', 'Kenőanyagok', 'Speciális kenőolaj lift vezetősínekhez, ISO VG 68', true, '11111111-1111-1111-1111-111111111111');

-- ============================================================================
-- 6. WORKSHEETS (5 db)
-- ============================================================================

INSERT INTO expert_lift.worksheets (id, serial_number, creation_date, arrival_time, departure_time, work_duration_hours, work_type, service_type, work_description, material_usage, notes, status, created_by_user_id, assigned_to_user_id, address_id, elevator_id, elevator_identifier)
VALUES
  -- Befejezett karbantartás - Corvin Irodaház, A1
  ('55555555-5555-5555-5555-555555555501', '2026-01-15/001', '2026-01-15', '2026-01-15 08:00:00+01', '2026-01-15 11:30:00+01', 3.50, 'maintenance', 'normal',
   'Havi rendszeres karbantartás elvégzése. Vezetősínek ellenőrzése és kenése, fékpofák kopásának vizsgálata, ajtómechanizmus beállítása.',
   '[{"name": "Vezetősín kenőolaj", "unit": "l", "quantity": 0.5}, {"name": "Vezetősín csúszóbetét", "unit": "db", "quantity": 2}]'::jsonb,
   'Minden rendben, következő karbantartás 1 hónap múlva.',
   'completed',
   '22222222-2222-2222-2222-222222222203', -- Kovács János
   '22222222-2222-2222-2222-222222222203',
   '33333333-3333-3333-3333-333333333301', -- Corvin Irodaház
   NULL,
   'A1'),

  -- Folyamatban lévő javítás - Forum Debrecen, L1
  ('55555555-5555-5555-5555-555555555502', '2026-01-18/001', '2026-01-18', '2026-01-18 14:00:00+01', '2026-01-18 17:30:00+01', 3.50, 'repair', 'normal',
   'Ajtózáró kontaktus hibás, kabinajtó nem záródik megfelelően. Kontaktus cseréje szükséges.',
   '[{"name": "Ajtózáró kontaktus", "unit": "db", "quantity": 1}]'::jsonb,
   'Alkatrész megrendelve, holnap folytatjuk.',
   'in_progress',
   '22222222-2222-2222-2222-222222222204', -- Nagy Péter
   '22222222-2222-2222-2222-222222222204',
   '33333333-3333-3333-3333-333333333302', -- Forum Debrecen
   NULL,
   'L1'),

  -- Piszkozat - tervezett munka - Audi Gyár, K1
  ('55555555-5555-5555-5555-555555555503', '2026-01-20/001', '2026-01-20', '2026-01-20 08:00:00+01', '2026-01-20 12:00:00+01', 4.00, 'maintenance', 'normal',
   'Éves nagyjavítás - kötélcsere és teljes felülvizsgálat.',
   '[]'::jsonb,
   'Munkavégzés előtt egyeztetés szükséges az üzemeltetővel.',
   'draft',
   '22222222-2222-2222-2222-222222222202', -- Valentin
   '22222222-2222-2222-2222-222222222203',
   '33333333-3333-3333-3333-333333333304', -- Audi Gyár
   NULL,
   'K1'),

  -- Befejezett hétvégi munka - Napfény Lakópark, B1
  ('55555555-5555-5555-5555-555555555504', '2026-01-11/001', '2026-01-11', '2026-01-11 09:00:00+01', '2026-01-11 13:00:00+01', 4.00, 'repair', 'weekend',
   'Sürgős javítás - a lift leállt szombaton reggel. Vezérlőkábel szakadás miatt. Kábel cseréje és tesztelés.',
   '[{"name": "Vezérlő kábel 12x0.75mm²", "unit": "m", "quantity": 15}]'::jsonb,
   'Hétvégi pótdíjas munka. A lift újra működőképes.',
   'completed',
   '22222222-2222-2222-2222-222222222205', -- Szabó Anna
   '22222222-2222-2222-2222-222222222205',
   '33333333-3333-3333-3333-333333333303', -- Napfény Lakópark
   NULL,
   'B1'),

  -- Befejezett kórházi munka - PTE Klinikai Központ, P1
  ('55555555-5555-5555-5555-555555555505', '2026-01-10/001', '2026-01-10', '2026-01-10 06:00:00+01', '2026-01-10 08:30:00+01', 2.50, 'maintenance', 'normal',
   'Betegszállító lift ellenőrzése és karbantartása. Speciális figyelemmel a higiéniai követelményekre.',
   '[{"name": "Vezetősín kenőolaj", "unit": "l", "quantity": 0.3}]'::jsonb,
   'A lift megfelel az egészségügyi előírásoknak.',
   'completed',
   '22222222-2222-2222-2222-222222222203', -- Kovács János
   '22222222-2222-2222-2222-222222222203',
   '33333333-3333-3333-3333-333333333305', -- PTE Klinikai Központ
   NULL,
   'P1');

-- ============================================================================
-- Összegzés
-- ============================================================================
-- Létrehozott adatok:
--   - 1 workspace (ExpertLift)
--   - 5 user (paul, valentin, janos, peter, anna)
--   - 5 address (Budapest, Debrecen, Szeged, Győr, Pécs)
--   - 5 material template (kábelek, alkatrészek, kenőanyagok)
--   - 5 worksheet (különböző státuszok és munkatípusok)
-- ============================================================================
