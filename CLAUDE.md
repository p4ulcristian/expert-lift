# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## =
 Task Communication Protocol

### Audio Feedback with say.sh
After completing any task, changing approach, or reaching a checkpoint:

```bash
./say.sh "[component/file]: [brief description]"
```

**When to use:**
-  After completing any coding task
-  When changing your approach mid-task
-  At logical checkpoints during complex work
-  When encountering blockers or errors

**Guidelines:**
- Keep under 15 words
- Use present tense: "Added", "Fixed", "Updated", "Removed"
- Be specific about component/area modified
- Focus on WHAT changed, not HOW

**Examples:**
```bash
./say.sh "Fixed order loading: added workspace-id dependency"
./say.sh "Updated backend resolver: corrected parameter mapping"
./say.sh "Refactored user form: broke into 5 smaller functions"
./say.sh "Blocked: database connection error in orders module"
```

---

## =' Development Commands

### Start Development Server

```bash
./start-dev.sh app
```

**What it does:**
- Loads environment variables from `.env.local`
- Starts Shadow-CLJS compilation for the app module
- Runs backend Clojure server with hot reloading
- Launches Chrome with debugging enabled
- Streams console output from browser

### Debugging Frontend Code
**Always use `println` for debugging frontend ClojureScript code, not `js/console.log`.**

The output from `println` appears in the terminal logs where `./start-dev.sh` is running, making it easy to track and debug.

```clojure
;; ✅ CORRECT - Shows in terminal logs
(println "Debug value:" some-value)
(println "User data:" (:user/name user))

;; ❌ INCORRECT - Only shows in browser console
(js/console.log "Debug value:" some-value)
```

**Why use println:**
- Output appears in the same terminal as your development server
- Easy to track alongside backend logs and build output  
- No need to open browser dev tools
- Consistent logging experience across frontend and backend

---

## =� ParQuery Data Layer

ParQuery is the custom data fetching system that replaced Pathom. It provides a simple query interface between frontend and backend.

### Frontend Usage

#### Basic Query
```clojure
(parquery/send-queries
 {:queries {:orders/get-orders {:workspace-id workspace-id}}
  :parquery/context {:workspace-id workspace-id}
  :callback (fn [response]
              (let [orders (:orders/get-orders response)]
                (callback orders)))})
```

#### Query with Parameters
```clojure
(parquery/send-queries
 {:queries {:orders/get-order {:order/id order-id}}
  :parquery/context {:workspace-id workspace-id}
  :callback (fn [response]
              (let [order (:orders/get-order response)]
                (handle-order-response order)))})
```

#### Multiple Queries
```clojure
(parquery/send-queries
 {:queries {:user/current {}
            :workspaces/get-my-workspaces {}}
  :parquery/context {}
  :callback (fn [response]
              (let [user (:user/current response)
                    workspaces (:workspaces/get-my-workspaces response)]
                (handle-data user workspaces)))})
```

### Backend Handler Functions

Backend handlers receive a single map parameter with:
- `:parquery/context` - shared data (workspace-id, user-id)
- `:parquery/request` - full request object (session, cookies)
- Other keys are query parameters

#### Basic Handler
```clojure
(defn get-orders
  "Get orders for workspace"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id context)
        search-term (:search params)]
    (when workspace-id
      (db/get-orders workspace-id search-term))))
```

#### Handler with Parameters
```clojure
(defn get-order
  "Get single order by ID"
  [{:parquery/keys [context request] :as params}]
  (let [order-id (:order/id params)          ; from query params
        workspace-id (:workspace-id context)] ; from context
    (when (and order-id workspace-id)
      (db/get-order order-id workspace-id))))
```

### Query Registration
In `parquery/backend/config.clj`:

```clojure
(def read-queries
  {:orders/get-orders #'orders-read/get-my-orders
   :orders/get-order #'orders-read/get-order
   :user/current #'get-current-user})

(def write-queries
  {:orders/create-order #'orders-write/create-order
   :orders/update-order #'orders-write/update-order})
```

### Key Patterns
- **Context vs Parameters**: Use context for shared data (workspace-id), parameters for specific query data
- **Error Handling**: Return `nil` for missing data, throw exceptions for actual errors
- **Parameter Access**: Use keyword keys exactly as sent from frontend (`:order/id`, not `:order-id`)

---

## =� Clojure Code Organization

### File and Namespace Conventions

#### File Names (lowercase with underscores)
```
user_profile.cljs
order_history.cljs  
workspace_settings.clj
```

#### Namespace Names (dashes)
```clojure
(ns features.app.user-profile.frontend.view)
(ns features.app.order-history.backend.db)
(ns features.app.workspace-settings.frontend.components)
```

#### Documentation Placement
Place docstrings **after** function name, **before** parameter list:

```clojure
;;  CORRECT
(defn process-user-data
  "Validates and transforms user input data for storage"
  [user-data]
  (-> user-data
      validate-required-fields
      normalize-phone-number
      hash-password))

;; L INCORRECT
(defn process-user-data [user-data]
  "Validates and transforms user input data for storage"
  ...)
```

#### Function Organization
```clojure
(ns features.app.orders.frontend.view
  "Order list and detail views"
  (:require
   [features.app.orders.frontend.request :as orders-request]
   [ui.button :as button]
   [zero.frontend.react :as zero-react]))

;; Private helper functions first
(defn- format-order-date [date-str]
  "Formats ISO date string for display"
  (when date-str
    (-> date-str js/Date. (.toLocaleDateString))))

(defn- calculate-order-total [line-items]
  "Sums all line item totals"
  (reduce + (map :total line-items)))

;; Public interface functions last
(defn order-summary-card
  "Displays order summary information"
  [order]
  [:div.order-summary
   [:h3 (str "Order #" (:id order))]
   [:p "Date: " (format-order-date (:created-at order))]
   [:p "Total: $" (calculate-order-total (:line-items order))]])
```

### Namespaced Keywords
**Always use namespaced keywords for data modeling and API interfaces.**

#### Domain Entity Keys
Use namespace that matches the domain entity:
```clojure
;; ✅ CORRECT
{:order/id 123
 :order/status "pending"
 :order/total 29.99
 :order/line-items [...]}

{:user/id 456
 :user/email "user@example.com"
 :user/workspace-id 789}

;; ❌ INCORRECT
{:id 123
 :status "pending"
 :total 29.99
 :line-items [...]}
```

#### ParQuery Keys
Use descriptive namespaces for query operations:
```clojure
;; ✅ CORRECT - Query names
:orders/get-orders
:orders/create-order
:users/get-current-user
:workspaces/get-my-workspaces

;; ✅ CORRECT - System keys
:parquery/context
:parquery/request
:parquery/response
```

#### Component State
Use component namespace for local state:
```clojure
;; ✅ CORRECT
(defn order-form []
  (let [state (atom {:order-form/loading? false
                     :order-form/errors {}
                     :order-form/data {:order/status "draft"}})]
    ...))
```

#### Benefits
- **Prevents key collisions** between different domains
- **Improves code readability** by making data relationships clear  
- **Enables better tooling** (spec validation, IDE completion)
- **Matches Clojure idioms** and community conventions

### Key Guidelines
- Keep files focused on single domain/feature
- Use descriptive function names that explain intent
- Extract complex logic into named helper functions
- Group related functions together
- Always include docstrings for public functions

---

## Feature Module Structure

For complex features (>300 lines), split into multiple files following this pattern:

```
features/app/{feature}/frontend/
├── view.cljs          # Main component, page layout, orchestration
├── events.cljs        # All rf/reg-event-db, rf/reg-event-fx handlers
├── subscriptions.cljs # All rf/reg-sub subscriptions
├── components.cljs    # Reusable UI components (forms, modals, etc.)
├── table.cljs         # Table column renderers and config (if applicable)
├── queries.cljs       # ParQuery operations (load, save, delete)
└── utils.cljs         # Validation, helpers, constants
```

### File Responsibilities

| File | Contains | Imports |
|------|----------|---------|
| `view.cljs` | Main `view` fn, page structure | All other modules |
| `events.cljs` | State mutations, side effects | `parquery`, `rf` |
| `subscriptions.cljs` | Data selectors from app-db | `rf` only |
| `components.cljs` | Form fields, modals, sections | `utils`, `queries`, `rf` |
| `table.cljs` | Column renderers, table config | `utils`, `tr` |
| `queries.cljs` | `parquery/send-queries` calls | `parquery` only |
| `utils.cljs` | Pure functions, constants | Minimal deps |

### Example: Worksheets Feature

```clojure
;; view.cljs - slim orchestration layer
(ns features.app.worksheets.frontend.view
  (:require
   [features.app.worksheets.frontend.utils :as utils]
   [features.app.worksheets.frontend.queries :as queries]
   [features.app.worksheets.frontend.table :as table]
   [features.app.worksheets.frontend.components :as components]
   [features.app.worksheets.frontend.events]        ; load to register
   [features.app.worksheets.frontend.subscriptions] ; load to register
   ...))

(defn view []
  (let [workspace-id (utils/get-workspace-id)
        ...]
    [:div
     [page-header ...]
     [data-table {:headers (table/get-columns) ...}]
     (when @modal-open?
       [components/worksheet-modal ...])]))
```

### When to Split

**Split into modules when:**
- File exceeds 300 lines
- Multiple re-frame events/subscriptions (>5 each)
- Complex forms with validation
- Signature/canvas handling
- Nested state management (materials, line items)

**Keep single file when:**
- Simple CRUD with <300 lines
- Few re-frame events (<5)
- No complex UI components

---

## =📋 Log Analysis

### Reading Development Logs
Always check the end of `log.txt` for recent activity when debugging or understanding system state:

```bash
tail -50 log.txt
```

**When to check logs:**
- Before starting any debugging task
- When encountering unexpected behavior
- After making changes that might affect system state
- When investigating error reports

**Focus on:**
- Recent error messages and stack traces
- Database connection issues
- Failed HTTP requests
- Shadow-CLJS compilation warnings/errors
- ParQuery resolver failures