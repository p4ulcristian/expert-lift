# Customizer Pricing Structure

## Overview

The customizer needs a hierarchical pricing system where:
- **Packages** contain parts and can have services
- **Parts** can have paint jobs and services
- **Package price** = sum of all part prices + package services
- **Total price** = package price + all additional services

## Current Structure Analysis

### What Works Well:
- ✅ Price calculation with formulas
- ✅ Part-level pricing
- ✅ Package total calculation
- ✅ Form data inheritance

### What Needs Improvement:
- ❌ No clear service separation (package vs part services)
- ❌ Paint job pricing not clearly defined
- ❌ Missing service pricing structure
- ❌ No clear total calculation hierarchy

## Proposed Improved Structure

### 1. Package Structure

```clojure
{:packages {"package-id" 
           {:id "package-id"
            :name "Three piece rim"
            :type "package"
            :part-ids ["part1" "part2" "part3"]
            
            ;; Package-level pricing
            :price 0  ; Base package price (assembly, etc.)
            :services {"service1" {:id "service1"
                                  :name "Assembly Service"
                                  :price 50
                                  :enabled true}}
            
            ;; Form data for package-level options
            :formdata {:material {:value 1.5 :label "Alloy"}
                      :assembly {:enabled true}}
            
            ;; Price calculation
            :price-formula "([:assembly] * 50) + [:material] * 20"
            
            ;; Total calculation
            :total 0  ; Will be calculated: price + parts_total + services_total
            }}
```

### 2. Part Structure

```clojure
{:parts {"part-id"
         {:id "part-id"
          :name "Face"
          :type "part"
          :package-id "package-id"
          
          ;; Part-level pricing
          :price 0  ; Base part price
          
          ;; Paint job pricing
          :paint-job {:look-id "look-id"
                     :price 25  ; Paint job cost
                     :enabled true}
          
          ;; Part-specific services
          :services {"repair" {:id "repair"
                              :name "Curb Rash Repair"
                              :price 40
                              :enabled false}
                    "welding" {:id "welding"
                              :name "Welding Service"
                              :price 60
                              :enabled false}}
          
          ;; Form data
          :formdata {:quantity {:qty 2 :value 2}
                    :size {:value 1.1 :label "14\""}
                    :condition {:value 1 :label "Newly manufactured"}}
          
          ;; Price calculation
          :price-formula "([:quantity] * ([:look-cost] * [:size] * [:condition]))"
          
          ;; Total for this part
          :total 0  ; Will be calculated: price + paint_job + services_total
          }}
```

### 3. Look/Paint Job Structure

```clojure
{:looks {"look-id"
         {:id "look-id"
          :name "Fancy Blue"
          :basecolor "#1434d7"
          :price-group-key "basic"
          :price-groups {"basic" 25
                        "basic+" 35
                        "pro" 45
                        "pro+" 55}
          
          ;; Texture data (simplified)
          :texture {:material {:color "#1434d7"
                              :metalness 0.911
                              :roughness 1}}}
```

### 4. Services Structure

```clojure
{:services {"service-id"
           {:id "service-id"
            :name "Service Name"
            :type "package"  ; or "part"
            :price 50
            :enabled false
            :description "Service description"
            :category "repair"  ; or "assembly", "finishing", etc.
            }}
```

## Price Calculation Hierarchy

### 1. Part Price Calculation
```clojure
(defn calculate-part-price [part-data]
  (let [base-price (calc-formula (:price-formula part-data) (:formdata part-data))
        paint-price (if (:enabled (:paint-job part-data))
                     (:price (:paint-job part-data))
                     0)
        services-price (->> (:services part-data)
                           (filter :enabled)
                           (map :price)
                           (reduce + 0))]
    (+ base-price paint-price services-price)))
```

### 2. Package Price Calculation
```clojure
(defn calculate-package-price [package-data parts-data]
  (let [package-base-price (calc-formula (:price-formula package-data) (:formdata package-data))
        parts-total (->> (:part-ids package-data)
                        (map #(get-in parts-data [% :total]))
                        (reduce + 0))
        package-services-price (->> (:services package-data)
                                   (filter :enabled)
                                   (map :price)
                                   (reduce + 0))]
    (+ package-base-price parts-total package-services-price)))
```

### 3. Total Price Calculation
```clojure
(defn calculate-total-price [package-data]
  (:total package-data))  ; Already calculated in package price
```

## Implementation Strategy

### Phase 1: Update Data Structure
1. **Add service fields** to packages and parts
2. **Add paint-job structure** to parts
3. **Update price calculation** functions
4. **Add total calculation** logic

### Phase 2: Update UI Components
1. **Service toggles** for packages and parts
2. **Paint job selection** with pricing
3. **Price breakdown** display
4. **Total price** updates

### Phase 3: Form Integration
1. **Service form fields** in templates
2. **Paint job form** integration
3. **Price formula** updates
4. **Validation** and error handling

## Example Usage

### Creating a Package with Parts
```clojure
;; Initialize package
{:packages {"three-piece-rim"
           {:id "three-piece-rim"
            :name "Three piece rim"
            :type "package"
            :part-ids ["face" "barrel" "lip"]
            :price 0
            :services {"assembly" {:id "assembly"
                                  :name "Professional Assembly"
                                  :price 75
                                  :enabled true}}
            :formdata {:material {:value 1.5 :label "Alloy"}}
            :price-formula "([:material] * 30)"
            :total 0}}

;; Initialize parts
{:parts {"face" {:id "face"
                 :name "Face"
                 :type "part"
                 :package-id "three-piece-rim"
                 :price 0
                 :paint-job {:look-id "fancy-blue"
                            :price 25
                            :enabled true}
                 :services {"repair" {:id "repair"
                                     :name "Curb Rash Repair"
                                     :price 40
                                     :enabled false}}
                 :formdata {:quantity {:qty 2 :value 2}
                           :size {:value 1.1 :label "14\""}}
                 :price-formula "([:quantity] * ([:look-cost] * [:size]))"
                 :total 0}}}
```

### Price Calculation Example
```clojure
;; Part calculation
;; Base: (2 * (25 * 1.1)) = 55
;; Paint: 25
;; Services: 0 (repair disabled)
;; Part total: 80

;; Package calculation
;; Base: (1.5 * 30) = 45
;; Parts: 80 + 80 + 80 = 240 (assuming all parts same price)
;; Services: 75 (assembly enabled)
;; Package total: 360
```

## Benefits of This Structure

1. **Clear separation** of package vs part services
2. **Explicit paint job** pricing
3. **Hierarchical calculation** that's easy to understand
4. **Flexible service** system
5. **Clear total breakdown** for customers
6. **Easy to extend** with new service types

## Migration Path

1. **Keep current structure** working
2. **Add new fields** gradually
3. **Update calculations** one at a time
4. **Test thoroughly** before removing old code
5. **Update UI** to show new pricing breakdown

This structure will give you a robust, scalable pricing system that clearly shows customers what they're paying for at each level. 