# Customizer Notification Messages

## User Guidance Notifications

### Part Selection
- **"Select a part to customize"** - When no part is selected
- **"Choose your rim style"** - When viewing package options
- **"Pick a part to configure"** - General part selection prompt
- **"Select a part to see customization options"** - When hovering over parts

### Package Selection
- **"Choose your rim package"** - When no package is selected
- **"Select a package to get started"** - Initial package selection
- **"Pick your preferred rim style"** - Package selection prompt

### Configuration Steps
- **"Configure your part settings"** - When part is selected but not configured
- **"Adjust quantity and size"** - For parts that need basic configuration
- **"Select your preferred finish"** - When choosing paint/look options
- **"Choose your services"** - When service options are available

### Paint/Look Selection
- **"Pick your color and finish"** - When no look is selected
- **"Select a paint job"** - Look selection prompt
- **"Choose your preferred color"** - Color selection guidance
- **"Customize your finish"** - Finish customization prompt

### Services
- **"Add repair services if needed"** - When repair services are available
- **"Select additional services"** - Service selection prompt
- **"Choose assembly options"** - Package assembly services
- **"Add finishing touches"** - Final service options

### Form Completion
- **"Fill in the required details"** - When form fields are empty
- **"Complete your configuration"** - Form completion prompt
- **"Review your selections"** - Before finalizing
- **"Check your configuration"** - Final review prompt

## Success Messages

### Part Configuration
- **"Part configured successfully"** - When part is fully configured
- **"Settings saved"** - When form data is saved
- **"Configuration complete"** - When all required fields are filled
- **"Part ready for cart"** - When part can be added to cart

### Package Configuration
- **"Package configured"** - When package is complete
- **"All parts configured"** - When all parts in package are done
- **"Ready to add to cart"** - When package is complete
- **"Configuration saved"** - When package settings are saved

### Cart Actions
- **"Added to cart"** - When item is successfully added
- **"Item updated"** - When cart item is modified
- **"Quantity updated"** - When quantity changes
- **"Price updated"** - When price changes

## Warning Messages

### Incomplete Configuration
- **"Please complete all required fields"** - When form is incomplete
- **"Select a size before continuing"** - When size is missing
- **"Choose a quantity"** - When quantity is not set
- **"Pick a color/finish"** - When look is not selected

### Validation Errors
- **"Invalid quantity selected"** - When quantity is out of range
- **"Size not available"** - When selected size is unavailable
- **"Service not compatible"** - When service conflicts occur
- **"Configuration error"** - General validation error

### Price Changes
- **"Price updated based on selections"** - When price changes
- **"Additional cost for selected services"** - When services add cost
- **"Premium finish selected"** - When expensive look is chosen
- **"Bulk discount applied"** - When quantity discounts apply

## Information Messages

### Price Information
- **"Base price: $X"** - Show base price
- **"Service cost: +$X"** - Show service additions
- **"Total: $X"** - Show final price
- **"Price includes assembly"** - When assembly is included

### Feature Information
- **"Premium finish available"** - When premium options exist
- **"Repair services recommended"** - When repair might be needed
- **"Assembly included"** - When assembly is part of package
- **"Custom sizing available"** - When custom sizes are offered

### Process Information
- **"Processing your configuration"** - When calculating prices
- **"Loading customization options"** - When loading data
- **"Saving your preferences"** - When saving data
- **"Updating price calculation"** - When recalculating

## Context-Specific Messages

### Rim Customization
- **"Select your rim diameter"** - Size selection
- **"Choose your rim width"** - Width selection
- **"Pick your bolt pattern"** - Bolt pattern selection
- **"Select your offset"** - Offset selection

### Paint/Finish
- **"Choose your powder coat color"** - Color selection
- **"Select your finish type"** - Finish type selection
- **"Pick your texture"** - Texture selection
- **"Choose your gloss level"** - Gloss/matte selection

### Services
- **"Add curb rash repair"** - Repair service
- **"Include professional assembly"** - Assembly service
- **"Add balancing service"** - Balancing service
- **"Include mounting service"** - Mounting service

## Quick Tips

### User Guidance
- **"💡 Tip: Select a part to see customization options"**
- **"💡 Tip: Choose your color before adding services"**
- **"💡 Tip: Review all options before finalizing"**
- **"💡 Tip: Check compatibility before adding services"**

### Helpful Hints
- **"💡 Hover over parts to see details"**
- **"💡 Click on services to learn more"**
- **"💡 Use the preview to see your choices"**
- **"💡 Check the price breakdown for details"**

## Error Recovery

### Network Issues
- **"Connection lost. Retrying..."** - Network error
- **"Unable to load options. Please refresh."** - Loading error
- **"Configuration not saved. Please try again."** - Save error
- **"Price calculation failed. Please retry."** - Calculation error

### Data Issues
- **"Some options unavailable. Please refresh."** - Data sync issue
- **"Configuration outdated. Please reload."** - Stale data
- **"Price may have changed. Please review."** - Price update
- **"Options updated. Please check your selection."** - Option changes

## Usage Examples

### Implementation in Components
```clojure
;; Show notification when no part selected
(when (not selected-part)
  [:div.notification
   "Select a part to customize"])

;; Show success when part configured
(when part-complete?
  [:div.notification.success
   "Part configured successfully"])

;; Show warning for incomplete form
(when (and selected-part (not form-complete?))
  [:div.notification.warning
   "Please complete all required fields"])
```

### Notification Types
- **Info**: Blue background, general guidance
- **Success**: Green background, completion confirmations
- **Warning**: Yellow background, validation issues
- **Error**: Red background, error states

### Timing
- **Immediate**: Show right away for guidance
- **Delayed**: Show after 2-3 seconds of inactivity
- **On action**: Show when user performs specific actions
- **On error**: Show when validation fails 