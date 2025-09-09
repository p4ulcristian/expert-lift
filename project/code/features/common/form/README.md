# Form Module Documentation

## Overview

The Form Module is a flexible, reusable form system built with ClojureScript and Re-frame. It provides a declarative way to create dynamic forms with various input types, conditional rendering, and automatic state management.

## Architecture

```
form/
├── backend/
│   ├── resolver.clj      # Pathom resolvers for form data
│   └── queries.sql       # Database queries
└── frontend/
    ├── reader.cljs       # Main form rendering logic
    └── inputs/           # Input type implementations
        ├── digit.cljs    # Number input with increment/decrement
        ├── select.cljs   # Dropdown selection
        └── switch.cljs   # Toggle switch
```

## Core Components

### 1. Form Reader (`reader.cljs`)

The main component that renders forms based on templates and manages form state.

#### Key Functions:

- `view` - Main entry point for rendering forms
- `input-broker` - Routes input rendering based on type
- `read-by-template-data` - Renders form from template data

#### Usage:

```clojure
;; Basic usage with form template
[form-reader/view {:form-template template-data
                   :value-path [:customizer :parts "part-id"]
                   :init-data initial-data
                   :on-change callback-fn}]

;; Usage with form ID (fetches from database)
[form-reader/view form-id {:value-path [:customizer :parts "part-id"]
                          :init-data initial-data}]
```

### 2. Input Types

#### Digit Input (`digit.cljs`)
Number input with increment/decrement buttons and quantity tracking.

**Features:**
- Increment/decrement buttons
- Quantity and value tracking
- Min/max validation
- Service ID association
- Prefix support

**Template Example:**
```clojure
{:id :quantity
 :title "Quantity"
 :type "digit_input"
 :value-path [:formdata :quantity]
 :value 1
 :min {:type "num" :value 1}
 :max {:type "num" :value 99}
 :prefix "$"
 :service-id "quantity-service"}
```

#### Select Input (`select.cljs`)
Dropdown selection component.

**Template Example:**
```clojure
{:id :size
 :title "Size"
 :type "select"
 :value-path [:formdata :size]
 :options [{:label "13\"" :value 1}
           {:label "14\"" :value 1.1}
           {:label "15\"" :value 1.2}]}
```

#### Switch Input (`switch.cljs`)
Toggle switch component.

**Template Example:**
```clojure
{:id :enabled
 :title "Enable Feature"
 :type "switch"
 :value-path [:formdata :enabled]
 :on-change callback-fn}
```

#### Group Input
Container for grouping related inputs.

**Template Example:**
```clojure
{:id :repair
 :title "Repair Options"
 :type "group"
 :inputs [{:id :curb_rash_repair
           :title "Curb Rash Repair"
           :type "digit_input"
           :value-path [:formdata :repair :curb_rash_repair]
           :value 40}
          {:id :welding
           :title "Welding"
           :type "digit_input"
           :value-path [:formdata :repair :welding]
           :value 60}]}
```

## Form Template Structure

A form template is a vector of input definitions:

```clojure
[{:id :quantity
  :title "Quantity"
  :type "digit_input"
  :value-path [:formdata :quantity]
  :value 1
  :min {:type "num" :value 1}
  :max {:type "num" :value 99}
  :description "Number of items to order"}
 
 {:id :size
  :title "Size"
  :type "select"
  :value-path [:formdata :size]
  :options [{:label "13\"" :value 1}
            {:label "14\"" :value 1.1}]}
 
 {:id :repair
  :title "Repair Options"
  :type "group"
  :inputs [{:id :curb_rash_repair
            :title "Curb Rash Repair"
            :type "digit_input"
            :value-path [:formdata :repair :curb_rash_repair]
            :value 40}]}]
```

## Input Properties

### Common Properties

| Property | Type | Description |
|----------|------|-------------|
| `id` | keyword/string | Unique identifier for the input |
| `title` | string | Display label |
| `type` | string | Input type ("digit_input", "select", "switch", "group") |
| `value-path` | vector | Re-frame path for storing the value |
| `condition` | boolean/string | Conditional rendering expression |
| `description` | string | Help text |

### Type-Specific Properties

#### Digit Input
| Property | Type | Description |
|----------|------|-------------|
| `value` | number | Initial value |
| `min` | map | Minimum value configuration |
| `max` | map | Maximum value configuration |
| `prefix` | string | Value prefix (e.g., "$") |
| `service-id` | string | Associated service identifier |

#### Select Input
| Property | Type | Description |
|----------|------|-------------|
| `options` | vector | Available options `[{:label "text" :value value}]` |

#### Group Input
| Property | Type | Description |
|----------|------|-------------|
| `inputs` | vector | Nested input definitions |

## Conditional Rendering

Inputs can be conditionally rendered using the `condition` property:

```clojure
{:id :repair_cost
 :title "Repair Cost"
 :type "digit_input"
 :value-path [:formdata :repair_cost]
 :condition "[:quantity] > 1"  ; Only show if quantity > 1
 :value 10}
```

Conditions use a simple expression syntax that references other form values.

## State Management

### Form Data Structure

Form data is stored in Re-frame DB under the specified `value-path`:

```clojure
{:customizer :parts "part-id" :formdata {:quantity {:qty 2 :value 2 :prefix "$"}
                                        :size {:value 1.3 :label "16\""}
                                        :enabled true}}
```

### Re-frame Events

The module provides several events for managing form state:

- `:form.digit-input/inc!` - Increment digit input
- `:form.digit-input/dec!` - Decrement digit input
- `:form.digit-input/on-change!` - Handle digit input changes
- `:form.reader/fetch!` - Fetch form template from database

### Re-frame Subscriptions

- `:form.digit-input/value` - Get digit input value
- `:form.input.condition/validate!` - Validate conditional expressions

## Usage Examples

### Basic Form

```clojure
(ns my-app.forms
  (:require [features.common.form.frontend.reader :as form-reader]))

(def my-form-template
  [{:id :name
    :title "Name"
    :type "digit_input"
    :value-path [:formdata :name]
    :value 1}
   
   {:id :category
    :title "Category"
    :type "select"
    :value-path [:formdata :category]
    :options [{:label "Basic" :value "basic"}
              {:label "Premium" :value "premium"}]}])

(defn my-form []
  [form-reader/view {:form-template my-form-template
                     :value-path [:my-form]
                     :init-data {:name {:qty 1 :value 1}
                                :category {:value "basic"}}
                     :on-change #(println "Form changed")}])
```

### Form with Database Template

```clojure
(defn form-with-db-template []
  [form-reader/view "form-uuid-123"
   {:value-path [:my-form]
    :init-data {:quantity {:qty 1 :value 1}}
    :on-change #(println "Form updated")}])
```

### Custom Input Components

```clojure
(defn form-with-custom-inputs []
  [form-reader/view {:form-template my-form-template
                     :value-path [:my-form]
                     :inputs {:digit_input my-custom-digit-input
                             :select my-custom-select-input}
                     :on-change #(println "Form changed")}])
```

## Backend Integration

### Database Schema

Forms are stored in the `forms` table:

```sql
CREATE TABLE forms (
    id UUID PRIMARY KEY,
    title VARCHAR(255),
    template JSONB,
    price_formula TEXT,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
);
```

### Pathom Resolvers

The module provides a resolver for fetching forms:

```clojure
;; Fetch form by ID
{:form.reader/get-by-id {:id "form-uuid"}}
```

## Best Practices

1. **Use meaningful IDs** - Use descriptive keywords for input IDs
2. **Group related inputs** - Use group inputs to organize related fields
3. **Validate conditions** - Test conditional expressions thoroughly
4. **Handle form changes** - Always provide an `on-change` callback
5. **Initialize data** - Provide proper initial data structure
6. **Use consistent paths** - Follow a consistent naming convention for value paths

## Troubleshooting

### Common Issues

1. **Input not rendering** - Check the `type` property matches available input types
2. **Condition not working** - Verify the condition expression syntax
3. **Value not updating** - Ensure the `value-path` is correct
4. **Form data not persisting** - Check if `clear?` is set to `false` when needed

### Debug Tips

- Use browser dev tools to inspect Re-frame DB state
- Add `println` statements in `on-change` callbacks
- Check the `value-path` structure matches your expectations
- Verify template data structure is correct

## Extending the Module

### Adding New Input Types

1. Create a new input component in `inputs/`
2. Add the input type to `input-broker` in `reader.cljs`
3. Update this documentation

### Custom Validation

Add validation logic in the `on-change` callback or create custom re-frame events.

### Styling

Input components can be styled by modifying the inline styles or adding CSS classes. 