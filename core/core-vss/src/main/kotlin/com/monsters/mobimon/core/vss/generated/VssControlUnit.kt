// Generated from docs/vss.csv.
// Regenerate with: python3 core/core-vss/tools/generate_vss_signals.py

package com.monsters.mobimon.core.vss.generated

data class VssControlUnit(
    val central: Central = Central(),
    val frontLeft: FrontLeft = FrontLeft(),
    val frontRight: FrontRight = FrontRight(),
    val rearLeft1: RearLeft1 = RearLeft1(),
    val rearLeft2: RearLeft2 = RearLeft2(),
    val trunk: Trunk = Trunk(),
) {
    data class Central(
        val health: Health = Health(),
        val id: Int = 0,
    ) {
        data class Health(
            val network: Network = Network(),
            val resources: Resources = Resources(),
            val sWSupervision: SWSupervision = SWSupervision(),
        ) {
            data class Network(
                val can: CAN = CAN(),
                val eth: ETH = ETH(),
            ) {
                data class CAN(
                    val isNetworkOK: Boolean = false,
                )

                data class ETH(
                    val isNetworkOK: Boolean = false,
                )
            }

            data class Resources(
                val power: Float = 0f,
                val temperature: Float = 0f,
                val utilization: Utilization = Utilization(),
            ) {
                data class Utilization(
                    val cpu: Float = 0f,
                    val memory: Float = 0f,
                )
            }

            data class SWSupervision(
                val isAliveTriggered: Boolean = false,
                val isDeadlineTriggered: Boolean = false,
                val isLogicalTriggered: Boolean = false,
                val isWatchdogTriggered: Boolean = false,
            )
        }
    }

    data class FrontLeft(
        val health: Health = Health(),
        val id: Int = 0,
    ) {
        data class Health(
            val network: Network = Network(),
            val resources: Resources = Resources(),
            val sWSupervision: SWSupervision = SWSupervision(),
        ) {
            data class Network(
                val can: CAN = CAN(),
                val eth: ETH = ETH(),
            ) {
                data class CAN(
                    val isNetworkOK: Boolean = false,
                )

                data class ETH(
                    val isNetworkOK: Boolean = false,
                )
            }

            data class Resources(
                val power: Float = 0f,
                val temperature: Float = 0f,
                val utilization: Utilization = Utilization(),
            ) {
                data class Utilization(
                    val cpu: Float = 0f,
                    val memory: Float = 0f,
                )
            }

            data class SWSupervision(
                val isAliveTriggered: Boolean = false,
                val isDeadlineTriggered: Boolean = false,
                val isLogicalTriggered: Boolean = false,
                val isWatchdogTriggered: Boolean = false,
            )
        }
    }

    data class FrontRight(
        val health: Health = Health(),
        val id: Int = 0,
    ) {
        data class Health(
            val network: Network = Network(),
            val resources: Resources = Resources(),
            val sWSupervision: SWSupervision = SWSupervision(),
        ) {
            data class Network(
                val can: CAN = CAN(),
                val eth: ETH = ETH(),
            ) {
                data class CAN(
                    val isNetworkOK: Boolean = false,
                )

                data class ETH(
                    val isNetworkOK: Boolean = false,
                )
            }

            data class Resources(
                val power: Float = 0f,
                val temperature: Float = 0f,
                val utilization: Utilization = Utilization(),
            ) {
                data class Utilization(
                    val cpu: Float = 0f,
                    val memory: Float = 0f,
                )
            }

            data class SWSupervision(
                val isAliveTriggered: Boolean = false,
                val isDeadlineTriggered: Boolean = false,
                val isLogicalTriggered: Boolean = false,
                val isWatchdogTriggered: Boolean = false,
            )
        }
    }

    data class RearLeft1(
        val health: Health = Health(),
        val id: Int = 0,
    ) {
        data class Health(
            val network: Network = Network(),
            val resources: Resources = Resources(),
            val sWSupervision: SWSupervision = SWSupervision(),
        ) {
            data class Network(
                val can: CAN = CAN(),
                val eth: ETH = ETH(),
            ) {
                data class CAN(
                    val isNetworkOK: Boolean = false,
                )

                data class ETH(
                    val isNetworkOK: Boolean = false,
                )
            }

            data class Resources(
                val power: Float = 0f,
                val temperature: Float = 0f,
                val utilization: Utilization = Utilization(),
            ) {
                data class Utilization(
                    val cpu: Float = 0f,
                    val memory: Float = 0f,
                )
            }

            data class SWSupervision(
                val isAliveTriggered: Boolean = false,
                val isDeadlineTriggered: Boolean = false,
                val isLogicalTriggered: Boolean = false,
                val isWatchdogTriggered: Boolean = false,
            )
        }
    }

    data class RearLeft2(
        val health: Health = Health(),
        val id: Int = 0,
    ) {
        data class Health(
            val network: Network = Network(),
            val resources: Resources = Resources(),
            val sWSupervision: SWSupervision = SWSupervision(),
        ) {
            data class Network(
                val can: CAN = CAN(),
                val eth: ETH = ETH(),
            ) {
                data class CAN(
                    val isNetworkOK: Boolean = false,
                )

                data class ETH(
                    val isNetworkOK: Boolean = false,
                )
            }

            data class Resources(
                val power: Float = 0f,
                val temperature: Float = 0f,
                val utilization: Utilization = Utilization(),
            ) {
                data class Utilization(
                    val cpu: Float = 0f,
                    val memory: Float = 0f,
                )
            }

            data class SWSupervision(
                val isAliveTriggered: Boolean = false,
                val isDeadlineTriggered: Boolean = false,
                val isLogicalTriggered: Boolean = false,
                val isWatchdogTriggered: Boolean = false,
            )
        }
    }

    data class Trunk(
        val health: Health = Health(),
        val id: Int = 0,
    ) {
        data class Health(
            val network: Network = Network(),
            val resources: Resources = Resources(),
            val sWSupervision: SWSupervision = SWSupervision(),
        ) {
            data class Network(
                val can: CAN = CAN(),
                val eth: ETH = ETH(),
            ) {
                data class CAN(
                    val isNetworkOK: Boolean = false,
                )

                data class ETH(
                    val isNetworkOK: Boolean = false,
                )
            }

            data class Resources(
                val power: Float = 0f,
                val temperature: Float = 0f,
                val utilization: Utilization = Utilization(),
            ) {
                data class Utilization(
                    val cpu: Float = 0f,
                    val memory: Float = 0f,
                )
            }

            data class SWSupervision(
                val isAliveTriggered: Boolean = false,
                val isDeadlineTriggered: Boolean = false,
                val isLogicalTriggered: Boolean = false,
                val isWatchdogTriggered: Boolean = false,
            )
        }
    }
}
