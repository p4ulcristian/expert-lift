# Service Area Status Codes

This document describes the status codes used in the service areas system for managing ZIP Code Tabulation Areas (ZCTA).

## Status Code Definitions

| Status | Code | Description |
|--------|------|-------------|
| **Own** | `"r"` | ZIP code belongs to the current user's workspace (calculated in frontend, not stored) |
| **Reserved** | `"r"` | ZIP code is reserved for future use |
| **Disabled** | `"d"` | ZIP code is disabled/inactive |
| **Preserved** | `"p"` | ZIP code is preserved/protected |
| **Empty** | `"e"` | ZIP code has no status assigned |

## Usage Notes

- **Own status**: Calculated in frontend when `user-workspace-id` equals the `stored-workspace-id-in-zip-code` - not stored as a status code
- **String codes**: All other statuses use single character string codes stored in the database
- **Default**: New ZIP codes typically start with "e" (empty) status

## Examples

```clojure
;; Own status (user-workspace-id matches stored-workspace-id-in-zip-code)
{:zip-code "94102" :user-workspace-id "94102" :stored-workspace-id-in-zip-code "94102"}

;; Reserved status
{:zip-code "94103" :user-workspace-id "94102" :status "r"}

;; Disabled status
{:zip-code "94104" :user-workspace-id "94102" :status "d"}
```