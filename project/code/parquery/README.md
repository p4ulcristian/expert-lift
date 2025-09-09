# Parquery

A query system for executing multiple backend operations in a single request with typed responses and error handling.

## Overview

Parquery allows you to batch multiple queries together and execute them on the backend in a single HTTP request. It supports both read and write operations with context sharing and comprehensive error handling.

## Backend Usage

### 1. Define Handler Functions

Create handler functions in your own namespace (see `example_handlers.clj` for examples):

```clojure
(ns my-app.handlers)

(defn for-you
  "Example handler function - returns personalized data"
  [{:parquery/keys [context] :as params}]
  (let [user-id (:user-id params)
        message (:message params "Hello")]
    {:user-id user-id
     :personalized-message (str message " just for you!")
     :timestamp (str (java.time.Instant/now))
     :parquery/context-data context}))
```

### 2. Register Query Handlers

Add your handlers to the query maps in `parquery.backend.config`:

```clojure
;; In parquery.backend.config
(def read-queries
  "Read operations - mapped to handler functions"
  {:example/for-you #'my-app.handlers/for-you
   :example/get-items #'my-app.handlers/get-items})

(def write-queries
  "Write operations - mapped to handler functions"
  {:example/create-item #'my-app.handlers/create-item})
```

### Handler Function Contract

- Receive a single map parameter containing:
  - `:parquery/context` - shared context data (destructured as `context`)
  - Other keys - individual function parameters
- Return a map with the query results
- Access context via `(:workspace-id context)` etc.

## Client-Side Usage

### 1. Basic Query Request

```clojure
(ns my-app.queries
  (:require [parquery.frontend.request :as parquery]))

;; Single query
(parquery/send-queries
  {:queries {:example/for-you {:user-id 123 :message "Welcome"}}
   :parquery/context {:workspace-id 456}
   :callback (fn [response]
               (if (:success response)
                 (let [result (get-in response [:data :example/for-you])]
                   (println "Result:" result))
                 (println "Error:" (:error response))))})
```

### 2. Multiple Queries in One Request

```clojure
;; Multiple queries executed together
(parquery/send-queries
  {:queries {:example/for-you {:user-id 123 :message "Hello"}
             :example/get-items {:limit 10 :offset 0 :category "electronics"}}
   :parquery/context {:workspace-id 456}
   :callback (fn [response]
               (when (:success response)
                 (let [personal (get-in response [:data :example/for-you])
                       items (get-in response [:data :example/get-items])]
                   (println "Personal:" personal)
                   (println "Items:" items))))})
```

### 3. Using the Interactive UI

Visit `/parquery` in your browser to use the interactive query interface:

```edn
{:queries {:example/for-you {:user-id 123 :message "Testing"}
           :example/get-items {:limit 5 :category "books"}}
 :parquery/context {:workspace-id 456}}
```

## Request/Response Format

### Request Structure
```edn
{:queries {:query-key-1 {:param1 value1 :param2 value2}
           :query-key-2 {:param1 value1}}
 :parquery/context {:shared-param1 value1 :shared-param2 value2}}
```

### Response Structure
```edn
{:query-key-1 {:result "data"}
 :query-key-2 {:result "data"}}
```

### Error Responses
```edn
{:query-key-1 {:error "Error message" :query :query-key-1}
 :query-key-2 {:result "success data"}}
```

## Query Types

- **Read Queries**: Safe operations that don't modify state
- **Write Queries**: Operations that modify state  
- **Unknown Queries**: Result in 400 error with list of unknown query keys

## Features

- **Batching**: Execute multiple queries in single HTTP request
- **Context Sharing**: Share common parameters across all queries
- **Type Validation**: Schema validation on requests and responses
- **Error Isolation**: Individual query errors don't affect other queries
- **Interactive UI**: Web interface for testing queries at `/parquery`

## Error Handling

- Schema validation errors return 400 with validation details
- Unknown query errors return 400 with list of unknown queries
- Individual query exceptions are caught and returned as error objects
- Client receives `{:success false :error "message"}` for HTTP failures

## Development

The interactive UI at `/parquery` provides:
- Syntax highlighting for EDN queries
- Real-time query execution
- Formatted response display
- Error visualization