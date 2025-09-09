📍 Location Manager – Overview
A unified interface where users can:

Create and organize locations within their operation

Define location types (e.g. rack, workstation, partner)

Assign location-specific properties (capacity, tags, order routing behavior)

View current status (e.g., parts in location, available space, alerts)

🧱 Location Types (built-in, can be expanded)
Type	Icon	Purpose
🧰 Workstation	🛠️	Places where work is performed (e.g. Sandblasting, Powder Booth)
🗃️ Rack	📦	Storage locations — “Awaiting coating”, “Cooling”, “Ready for pickup”
🏭 Area / Zone	📍	Spatial zones in the shop (e.g. Prep Area, QA Zone)
🔄 Service Partner	🔗	External locations — third-party vendors handling sub-services
🚛 Inbound/Outbound	📦↔️	Receiving & shipping locations

📋 Fields Per Location
Field	Type	Notes
Location Name	Text	e.g. “Prep Bench 1”, “North Wall Rack”, “Alpha Welding Co.”
Type	Dropdown	Choose from built-in types (above)
Status	Auto	(Live) Shows “Active”, “Idle”, “Over capacity”, “Has alerts”
Capacity	Number	Optional: how many items/jobs it can hold
Tags	Multi-tag input	e.g. “hot”, “manual only”, “rim work”
Linked Operator(s)	Dropdown (multi)	Assign employee roles (optional)
Workstation Processes	Multi-select	For workstations only — defines which operations are done here
Is Partner Location	Toggle	Marks this location as external
Geo Info (optional)	Address or GPS	For external/partner mapping later
Notes	Textarea	Internal info, usage notes

📦 Example Location Entries
Icon	Name	Type	Capacity	Tags	Linked Ops
🛠️	Powder Booth A	Workstation	1	“manual”, “powder”	Alex, Maria
📦	Rack Q	Rack	12	“hot”, “no stack”	—
📍	QA Zone	Area	—	“final”	QA Lead
🔗	Rim Welding Inc.	Service Partner	—	“rim repair”	—

🖥️ UX & Management Panel Features
Create / Edit / Delete locations

Drag & drop sorting (for grouping or spatial order)

Filter by type or tag

“Live status” icon (color-coded):

Green = Idle

Yellow = Processing

Red = Alert (over capacity, delay, etc.)

Show parts currently in location (with popover or mini-list)

Button to “View on Process Map” (for route visualization)

Optional toggle: “Show only used in current batch”

🔄 Integration Use Cases
Orders can be routed through defined location sequences

Workstation type locations trigger job state changes (e.g. "In Sandblast Booth")

Racks track where parts sit during cooldown, post-process, or staging

Service partner locations enable status sync once integrated