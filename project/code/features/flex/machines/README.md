# 🔧 Machines Management System

## Overview

The Machines Management System provides comprehensive monitoring, maintenance tracking, and performance analytics for manufacturing equipment in the flex/coating facility.

## Features

### 📊 Machine Dashboard
- **Real-time monitoring** of all machines
- **OEE (Overall Equipment Effectiveness)** tracking
- **Energy consumption analytics** by type
- **Maintenance alerts** and scheduling
- **Performance metrics** and trends

### 🏭 Machine Management
- **Comprehensive machine profiles** with technical specifications
- **Energy consumption tracking** (Electrical, Gas, Compressed Air, Hydraulic)
- **Amortization calculations** (time-based or usage-based)
- **Maintenance scheduling** with wear parts tracking
- **Performance monitoring** (production rate, OEE targets, operating conditions)

### 🔍 Advanced Features
- **Search and filtering** by status, energy type, category
- **Status badges** with visual indicators
- **Category-based organization** (Washer, Oven, Booth, Sprayer, etc.)
- **Automated maintenance due date calculation**
- **Wear parts replacement tracking**

## Machine Categories

The system supports various machine types:

- **Washer** 🧽 - Parts cleaning equipment
- **Degreaser** - Chemical cleaning systems
- **Dryer** 💨 - Drying equipment
- **Blaster** 💥 - Abrasive blasting systems
- **Oven** 🔥 - Curing ovens
- **Booth** 🎨 - Spray booths
- **Sprayer** 🎯 - Paint application systems
- **Gun** - Manual spray guns
- **Compressor** 🔧 - Air compression systems
- **AirDryer** - Air drying systems
- **Polisher** ✨ - Surface polishing equipment
- **Grinder** ⚙️ - Material grinding systems
- **Buffer** - Surface buffing equipment
- **Sander** - Surface preparation tools
- **RimStraight** - Rim straightening equipment
- **RimWeld** - Rim welding systems
- **DiamondCut** - Diamond cutting equipment
- **Balance** - Tire balancing machines
- **TireMount** - Tire mounting equipment
- **DipTank** 🛁 - Chemical dip systems
- **Lift** - Material handling lifts
- **PaintTrap** - Overspray capture systems
- **CoolZone** - Part cooling areas
- **BatchRack** - Batch handling equipment
- **QCStation** - Quality control stations
- **Custom** - Custom equipment types

## Database Schema

### Machine Fields

```sql
CREATE TABLE machines (
  id UUID PRIMARY KEY,
  name VARCHAR NOT NULL,
  description TEXT,
  category VARCHAR DEFAULT 'Custom',
  status VARCHAR DEFAULT 'Idle', -- 'Idle', 'Active', 'Maintenance', 'Down'
  location VARCHAR,
  
  -- Energy Consumption
  energy_type VARCHAR DEFAULT 'Electrical',
  energy_consumption DECIMAL DEFAULT 0,
  consumption_mode VARCHAR DEFAULT 'manual', -- 'manual', 'automatic'
  sensor_id VARCHAR,
  
  -- Amortization
  amortization_type VARCHAR DEFAULT 'time-based', -- 'time-based', 'usage-based'
  amortization_rate DECIMAL DEFAULT 0,
  usage_unit VARCHAR,
  
  -- Performance
  production_rate DECIMAL DEFAULT 0,
  oee_target DECIMAL DEFAULT 85,
  operating_temp DECIMAL DEFAULT 0,
  operating_pressure DECIMAL DEFAULT 0,
  
  -- Maintenance
  last_maintenance DATE,
  maintenance_interval_days INTEGER DEFAULT 30,
  maintenance_due DATE,
  wear_parts JSONB DEFAULT '[]',
  
  -- Relationships
  workspace_id UUID NOT NULL,
  workstation_id UUID,
  
  -- Timestamps
  last_used TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Wear Parts JSON Structure

```json
[
  {
    "name": "Filter Element",
    "trigger": "Every 500 kg powder sprayed"
  },
  {
    "name": "Spray Nozzle",
    "trigger": "Every 200 operating hours"
  }
]
```

## API Endpoints

### GraphQL Resolvers

```clojure
;; Get all machines for workspace
[:machines/get-machines]

;; Get single machine by ID
[:machines/get-machine {:machine/id "uuid"}]

;; Get machines needing maintenance
[:machines/get-machines-needing-maintenance]

;; Get machine statistics
[:machines/get-machine-stats]
```

### GraphQL Mutations

```clojure
;; Create new machine
[(machines/create-machine {
  :name "Powder Booth A"
  :category "Booth"
  :energy_type "Electrical"
  :energy_consumption 15.5
  ;; ... other fields
})]

;; Update machine
[(machines/edit-machine {
  :id "uuid"
  :name "Updated Name"
  ;; ... other fields
})]

;; Delete machine
[(machines/delete-machine {:id "uuid"})]

;; Update machine usage timestamp
[(machines/update-machine-usage {:id "uuid"})]
```

## Usage Examples

### Frontend Components

```clojure
;; Machine list with filters
[machines/view]

;; Machine detail/edit form
[machine/view]

;; Dashboard with analytics
[dashboard/view]
```

### Energy Consumption Tracking

1. **Manual Mode**: Operators log energy usage manually
2. **Automatic Mode**: Connected to smart meters or sensors via:
   - MODBUS protocol
   - REST API integration
   - IoT sensors

### Maintenance Scheduling

The system automatically calculates maintenance due dates:

```clojure
;; Maintenance due = last_maintenance + maintenance_interval_days
maintenance_due = last_maintenance + INTERVAL '1 day' * maintenance_interval_days
```

### Amortization Calculations

**Time-based**: Cost per hour/month of operation
**Usage-based**: Cost per unit produced (kg powder, cycles, parts)

## Performance Metrics

### OEE Calculation
```
OEE = Availability × Performance × Quality

Where:
- Availability = (Operating Time / Planned Production Time) × 100%
- Performance = (Actual Output / Theoretical Output) × 100%  
- Quality = (Good Parts / Total Parts) × 100%
```

### Key Performance Indicators
- **Production Rate**: Parts per hour
- **Energy Efficiency**: kW per part produced
- **Uptime Percentage**: Operating time vs. total time
- **Maintenance Compliance**: On-time vs. overdue maintenance

## Integration Points

### Workstations
Machines can be assigned to specific workstations for:
- Task scheduling
- Operator assignments
- Workflow optimization

### Inventory System
Integration with spare parts inventory for:
- Automatic reorder points
- Maintenance planning
- Cost tracking

### Production Planning
Machine availability feeds into:
- Batch scheduling
- Capacity planning
- Bottleneck analysis

## Best Practices

### Machine Setup
1. **Complete Profile**: Fill all relevant fields for accurate tracking
2. **Realistic Targets**: Set achievable OEE and production targets
3. **Regular Updates**: Keep maintenance records current

### Energy Management
1. **Baseline Measurement**: Establish consumption baselines
2. **Peak Load Monitoring**: Track energy spikes and patterns
3. **Efficiency Optimization**: Regular calibration and maintenance

### Maintenance Strategy
1. **Preventive Scheduling**: Use interval-based maintenance
2. **Condition Monitoring**: Track wear patterns and performance
3. **Spare Parts Planning**: Maintain adequate inventory

## Troubleshooting

### Common Issues

**Machine not appearing in list**
- Check workspace assignment
- Verify database connection
- Refresh browser cache

**Maintenance dates incorrect**
- Verify last_maintenance date format
- Check maintenance_interval_days value
- Ensure timezone settings

**Energy consumption not tracking**
- Verify sensor_id configuration
- Check consumption_mode setting
- Test API connectivity for automatic mode

## Future Enhancements

### Planned Features
- **IoT Integration**: Real-time sensor data collection
- **Predictive Maintenance**: AI-driven failure prediction
- **Mobile App**: Field technician interface
- **Advanced Analytics**: Machine learning insights
- **Cost Optimization**: Automated scheduling recommendations

### Integration Roadmap
- **ERP Systems**: SAP, Oracle integration
- **SCADA Systems**: Industrial automation protocols
- **Cloud Platforms**: AWS IoT, Azure IoT Hub
- **Mobile Platforms**: iOS/Android applications

---

For technical support or feature requests, please contact the development team. 