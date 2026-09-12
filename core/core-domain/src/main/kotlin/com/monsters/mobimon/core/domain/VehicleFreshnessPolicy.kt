package com.monsters.mobimon.core.domain

/** Evaluates parking and battery timestamps separately; neither refreshes the other. */
class VehicleFreshnessPolicy(
    private val maxAgeMillis: Long,
) {
    init {
        require(maxAgeMillis >= 0)
    }

    fun displaySnapshot(
        snapshot: VehicleSnapshot,
        expectedSource: SignalSource,
        nowMillis: Long,
    ): VehicleSnapshot {
        val sourceMatches = snapshot.source == expectedSource
        val parkingQuality =
            qualityAt(
                snapshot.quality,
                snapshot.receivedAtMillis,
                nowMillis,
                sourceMatches && snapshot.id.isNotBlank() && snapshot.epoch.isNotBlank() && snapshot.sequence >= 0,
            )
        val batteryQuality =
            if (snapshot.batteryPercent == null) {
                SignalQuality.UNAVAILABLE
            } else {
                qualityAt(
                    snapshot.batteryQuality ?: snapshot.quality,
                    snapshot.batteryReceivedAtMillis ?: snapshot.receivedAtMillis,
                    nowMillis,
                    sourceMatches && snapshot.batteryPercent in 0..100,
                )
            }
        val warnings =
            snapshot.warnings.map { warning ->
                warning.copy(
                    quality =
                        qualityAt(
                            warning.quality,
                            warning.observedAtMillis,
                            nowMillis,
                            sourceMatches && warning.item.isNotBlank() && warning.description.isNotBlank(),
                        ),
                )
            }
        return snapshot.copy(
            quality = parkingQuality,
            batteryQuality = batteryQuality,
            warnings = warnings,
            parkingAgeMillis = ageAt(snapshot.receivedAtMillis, nowMillis, sourceMatches),
            batteryAgeMillis =
                ageAt(snapshot.batteryReceivedAtMillis ?: snapshot.receivedAtMillis, nowMillis, sourceMatches),
        )
    }

    private fun ageAt(
        receivedAt: Long,
        now: Long,
        sourceMatches: Boolean,
    ): Long? = if (sourceMatches && receivedAt > 0 && now >= receivedAt) now - receivedAt else null

    private fun qualityAt(
        quality: SignalQuality,
        receivedAt: Long,
        now: Long,
        valid: Boolean,
    ): SignalQuality =
        when {
            !valid || receivedAt < 0 || now < 0 || receivedAt > now -> SignalQuality.UNAVAILABLE
            quality != SignalQuality.VALID -> quality
            now - receivedAt > maxAgeMillis -> SignalQuality.STALE
            else -> SignalQuality.VALID
        }
}
