# Customizer Panel TODO

## 🚨 Critical Issues to Address

### Data Duplication Problems

#### 1. Look Object Duplication
**Current Issue**: The same `look` object is duplicated in multiple places:
- `:selected-look` at top level
- Each part's `:look` field  
- Package parts' `:look` fields

**Example**:
```clojure
{:selected-look {:id "look1" :name "Fancy Blue" :texture {...}}
 :parts {"part1" {:look {:id "look1" :name "Fancy Blue" :texture {...}}}
         "part2" {:look {:id "look1" :name "Fancy Blue" :texture {...}}}}
```

**Problems**:
- Memory waste (same texture data stored multiple times)
- Data consistency issues (updating look requires multiple changes)
- State synchronization hell

#### 2. Part Data Duplication
**Current Issue**: Parts appear in multiple structures:
- `:parts` map
- `:packages` map with `:children`
- `:mesh->part` mapping

#### 3. Texture/Material Duplication
**Current Issue**: Entire texture objects with 20+ properties duplicated for each part.

## 🔧 Proposed Solutions

### Phase 1: Normalized Structure (Recommended)
```clojure
{:selected-look-id "fcd39b45-03c2-4413-8abe-95eaf278f4bb"
 :looks {"fcd39b45-03c2-4413-8abe-95eaf278f4bb" 
         {:id "fcd39b45-03c2-4413-8abe-95eaf278f4bb"
          :name "Fancy Blue"
          :basecolor "#1434d7"
          :texture {...}}}
 :parts {"3b53ddab-08ed-4bf1-bde2-578ed806b5f1" 
         {:id "3b53ddab-08ed-4bf1-bde2-578ed806b5f1"
          :name "One piece rim"
          :look-id "fcd39b45-03c2-4413-8abe-95eaf278f4bb"
          :formdata {...}}}
 :packages {"d250385a-a426-4631-8871-74833cc5df36"
           {:id "d250385a-a426-4631-8871-74833cc5df36"
            :name "Three piece rim"
            :part-ids ["c6092ebd-4daf-4f8e-b10b-7bc74a87db75"
                      "b18f2ccb-2e71-462d-acc3-2b5c27251bd1"]}}}
```

### Phase 2: Helper Functions
- Use `get-part-look` helper function for accessing look data
- Create `update-look-for-all-parts` for consistent updates
- Add re-frame subscriptions for clean API

## 📋 Implementation Tasks

### High Priority
- [ ] Refactor `:customizer/init-part!` to use look IDs instead of full objects
- [ ] Update effects to use helper functions
- [ ] Add look ID references to part data structure
- [ ] Create migration function for existing data

### Medium Priority  
- [ ] Normalize package structure to use part IDs
- [ ] Remove `:mesh->part` duplication
- [ ] Optimize texture data storage
- [ ] Add data validation for consistency

### Low Priority
- [ ] Performance optimization for large datasets
- [ ] Add data compression for texture objects
- [ ] Implement caching for frequently accessed looks

## 🎯 Benefits After Refactoring

1. **Memory Usage**: ~70% reduction in data size
2. **Performance**: Faster look updates and state changes
3. **Maintainability**: Single source of truth for look data
4. **Debugging**: Easier to track state changes
5. **Scalability**: Better performance with many parts/looks

## ⚠️ Breaking Changes to Consider

- Components expecting `(:look part)` will need updates
- Effects that modify look data need refactoring
- Database schema might need updates for look references

## 📝 Notes

- Current structure works but is not scalable
- Helper functions in `helpers.cljs` provide temporary solution
- Consider implementing gradually to avoid breaking existing functionality
- Test thoroughly after each phase of refactoring 