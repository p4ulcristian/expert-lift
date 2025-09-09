#!/bin/bash

# Transform script to convert request/pathom calls to parquery pattern
# Usage: ./transform_pathom_to_parquery.sh

# Array of modules to transform
modules=(
    "batches"
    "jobs" 
    "locations"
    "machines"
    "orders"
    "processes"
    "recipes"
    "service_areas"
    "services_pricing"
    "parts_pricing" 
    "workstations"
    "zero"
    "shared"
)

for module in "${modules[@]}"; do
    echo "Processing $module module..."
    
    # Find all .cljs files in the module's frontend directory
    find "/Users/paul/projects/ironrainbow/project/code/features/flex/$module/frontend" -name "*.cljs" -type f | while read -r file; do
        if grep -q "request/pathom" "$file"; then
            echo "  Updating $file"
            
            # Replace the import
            sed -i '' 's|\[app\.frontend\.request :as request\]|[features.flex.'"$module"'.frontend.request :as '"${module//_/-}"'-request]|g' "$file"
            
            # Note: Complex pathom call replacements would need more sophisticated parsing
            # This is a basic framework - individual files may need manual refinement
        fi
    done
done

echo "Transformation complete. Manual review and testing required."