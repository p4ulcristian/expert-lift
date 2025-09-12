address "acme_hq" {
  street = "123 Main Street"
  city = "New York"
  state = "NY"
  zip = "10001"
  
  elevator_ids = [
    "elv_acme_hq_001",
    "elv_acme_hq_002",
    "elv_acme_hq_freight"
  ]
}

address "acme_warehouse" {
  street = "456 Industrial Drive"
  city = "Newark"
  state = "NJ"
  zip = "07102"
  
  elevator_ids = [
    "elv_acme_wh_freight_001",
    "elv_acme_wh_freight_002"
  ]
}

address "acme_branch_ny" {
  street = "789 Business Ave"
  city = "Buffalo"
  state = "NY"
  zip = "14201"
  
  elevator_ids = [
    "elv_acme_buf_001"
  ]
}

address "tech_main_office" {
  street = "321 Tech Blvd"
  city = "San Francisco"
  state = "CA"
  zip = "94105"
  
  elevator_ids = [
    "elv_tech_main_001",
    "elv_tech_main_002",
    "elv_tech_main_003"
  ]
}

address "tech_data_center" {
  street = "555 Server Farm Rd"
  city = "Fremont"
  state = "CA"
  zip = "94538"
  
  elevator_ids = [
    "elv_tech_dc_001",
    "elv_tech_dc_service"
  ]
}

address "retail_store_001" {
  street = "100 Shopping Plaza"
  city = "Chicago"
  state = "IL"
  zip = "60601"
  
  elevator_ids = [
    "elv_retail_001_passenger",
    "elv_retail_001_service"
  ]
}

address "retail_store_002" {
  street = "200 Mall Drive"
  city = "Dallas"
  state = "TX"
  zip = "75201"
  
  elevator_ids = [
    "elv_retail_002_001",
    "elv_retail_002_002"
  ]
}

address "retail_distribution_center" {
  street = "999 Logistics Way"
  city = "Atlanta"
  state = "GA"
  zip = "30309"
  
  elevator_ids = [
    "elv_retail_dc_freight_001",
    "elv_retail_dc_freight_002",
    "elv_retail_dc_freight_003"
  ]
}

address "retail_corporate_hq" {
  street = "1 Corporate Plaza"
  city = "Minneapolis"
  state = "MN"
  zip = "55401"
  
  elevator_ids = [
    "elv_retail_hq_001",
    "elv_retail_hq_002",
    "elv_retail_hq_executive"
  ]
}

elevator "elv_acme_hq_001" {
  type = "passenger"
  capacity = "2500 lbs"
  floors = 15
  manufacturer = "Otis"
  model = "Gen2"
  year_installed = 2018
}

elevator "elv_acme_hq_002" {
  type = "passenger"
  capacity = "2500 lbs"
  floors = 15
  manufacturer = "Otis"
  model = "Gen2"
  year_installed = 2018
}

elevator "elv_acme_hq_freight" {
  type = "freight"
  capacity = "4000 lbs"
  floors = 15
  manufacturer = "ThyssenKrupp"
  model = "Evolution"
  year_installed = 2019
}

elevator "elv_acme_wh_freight_001" {
  type = "freight"
  capacity = "6000 lbs"
  floors = 3
  manufacturer = "Schindler"
  model = "5500"
  year_installed = 2020
}

elevator "elv_acme_wh_freight_002" {
  type = "freight"
  capacity = "6000 lbs"
  floors = 3
  manufacturer = "Schindler"
  model = "5500"
  year_installed = 2020
}

elevator "elv_acme_buf_001" {
  type = "passenger"
  capacity = "2000 lbs"
  floors = 8
  manufacturer = "KONE"
  model = "MonoSpace"
  year_installed = 2017
}

elevator "elv_tech_main_001" {
  type = "passenger"
  capacity = "3000 lbs"
  floors = 25
  manufacturer = "Otis"
  model = "Gen2 Premier"
  year_installed = 2021
}

elevator "elv_tech_main_002" {
  type = "passenger"
  capacity = "3000 lbs"
  floors = 25
  manufacturer = "Otis"
  model = "Gen2 Premier"
  year_installed = 2021
}

elevator "elv_tech_main_003" {
  type = "passenger"
  capacity = "3000 lbs"
  floors = 25
  manufacturer = "Otis"
  model = "Gen2 Premier"
  year_installed = 2021
}

elevator "elv_tech_dc_001" {
  type = "freight"
  capacity = "5000 lbs"
  floors = 4
  manufacturer = "ThyssenKrupp"
  model = "Evolution Blue"
  year_installed = 2022
}

elevator "elv_tech_dc_service" {
  type = "service"
  capacity = "1500 lbs"
  floors = 4
  manufacturer = "KONE"
  model = "MiniSpace"
  year_installed = 2022
}

elevator "elv_retail_001_passenger" {
  type = "passenger"
  capacity = "2500 lbs"
  floors = 5
  manufacturer = "Schindler"
  model = "3300"
  year_installed = 2019
}

elevator "elv_retail_001_service" {
  type = "service"
  capacity = "2000 lbs"
  floors = 5
  manufacturer = "Schindler"
  model = "2400"
  year_installed = 2019
}

elevator "elv_retail_002_001" {
  type = "passenger"
  capacity = "2800 lbs"
  floors = 4
  manufacturer = "KONE"
  model = "EcoSpace"
  year_installed = 2020
}

elevator "elv_retail_002_002" {
  type = "passenger"
  capacity = "2800 lbs"
  floors = 4
  manufacturer = "KONE"
  model = "EcoSpace"
  year_installed = 2020
}

elevator "elv_retail_dc_freight_001" {
  type = "freight"
  capacity = "8000 lbs"
  floors = 6
  manufacturer = "ThyssenKrupp"
  model = "Evolution"
  year_installed = 2018
}

elevator "elv_retail_dc_freight_002" {
  type = "freight"
  capacity = "8000 lbs"
  floors = 6
  manufacturer = "ThyssenKrupp"
  model = "Evolution"
  year_installed = 2018
}

elevator "elv_retail_dc_freight_003" {
  type = "freight"
  capacity = "8000 lbs"
  floors = 6
  manufacturer = "ThyssenKrupp"
  model = "Evolution"
  year_installed = 2019
}

elevator "elv_retail_hq_001" {
  type = "passenger"
  capacity = "3500 lbs"
  floors = 20
  manufacturer = "Otis"
  model = "Gen2 Stream"
  year_installed = 2021
}

elevator "elv_retail_hq_002" {
  type = "passenger"
  capacity = "3500 lbs"
  floors = 20
  manufacturer = "Otis"
  model = "Gen2 Stream"
  year_installed = 2021
}

elevator "elv_retail_hq_executive" {
  type = "passenger"
  capacity = "2500 lbs"
  floors = 20
  manufacturer = "Otis"
  model = "Gen2 Premier"
  year_installed = 2021
}