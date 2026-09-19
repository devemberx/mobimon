// Generated from docs/vss.csv.
// Regenerate with: python3 core/core-vss/tools/generate_vss_signals.py

package com.monsters.mobimon.core.vss.generated

data class VssVehicleIdentification(
    val acrissCode: String = "",
    val bodyType: String = "",
    val brand: String = "",
    val dateVehicleFirstRegistered: String = "",
    val knownVehicleDamages: String = "",
    val licensePlate: String = "",
    val meetsEmissionStandard: String = "",
    val model: String = "",
    val optionalExtras: List<String> = emptyList(),
    val productionDate: String = "",
    val purchaseDate: String = "",
    val vin: String = "",
    val vehicleConfiguration: String = "",
    val vehicleExteriorColor: String = "",
    val vehicleInteriorColor: String = "",
    val vehicleInteriorType: String = "",
    val vehicleModelDate: String = "",
    val vehicleSeatingCapacity: Int = 0,
    val vehicleSpecialUsage: String = "",
    val wmi: String = "",
    val year: Int = 0,
)
