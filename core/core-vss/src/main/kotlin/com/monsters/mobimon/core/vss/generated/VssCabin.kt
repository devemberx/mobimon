// Generated from docs/vss.csv.
// Regenerate with: python3 core/core-vss/tools/generate_vss_signals.py

package com.monsters.mobimon.core.vss.generated

data class VssCabin(
    val convertible: Convertible = Convertible(),
    val door: Door = Door(),
    val doorCount: Int = 4,
    val driverPosition: String = "",
    val hvac: HVAC = HVAC(),
    val infotainment: Infotainment = Infotainment(),
    val isAutoPowerOptimize: Boolean = false,
    val isWindowChildLockEngaged: Boolean = false,
    val light: Light = Light(),
    val powerOptimizeLevel: Int = 0,
    val rearShade: RearShade = RearShade(),
    val rearviewMirror: RearviewMirror = RearviewMirror(),
    val seat: Seat = Seat(),
    val seatPosCount: List<Int> = emptyList(),
    val seatRowCount: Int = 2,
    val sunroof: Sunroof = Sunroof(),
) {
    data class Convertible(
        val status: String = "",
    )

    data class Door(
        val row1: Row1 = Row1(),
        val row2: Row2 = Row2(),
    ) {
        data class Row1(
            val driverSide: DriverSide = DriverSide(),
            val passengerSide: PassengerSide = PassengerSide(),
        ) {
            data class DriverSide(
                val isChildLockActive: Boolean = false,
                val isLocked: Boolean = false,
                val isOpen: Boolean = false,
                val position: Int = 0,
                val shade: Shade = Shade(),
                val switch: String = "",
                val window: Window = Window(),
            ) {
                data class Shade(
                    val isOpen: Boolean = false,
                    val position: Int = 0,
                    val switch: String = "",
                )

                data class Window(
                    val isOpen: Boolean = false,
                    val position: Int = 0,
                    val switch: String = "",
                )
            }

            data class PassengerSide(
                val isChildLockActive: Boolean = false,
                val isLocked: Boolean = false,
                val isOpen: Boolean = false,
                val position: Int = 0,
                val shade: Shade = Shade(),
                val switch: String = "",
                val window: Window = Window(),
            ) {
                data class Shade(
                    val isOpen: Boolean = false,
                    val position: Int = 0,
                    val switch: String = "",
                )

                data class Window(
                    val isOpen: Boolean = false,
                    val position: Int = 0,
                    val switch: String = "",
                )
            }
        }

        data class Row2(
            val driverSide: DriverSide = DriverSide(),
            val passengerSide: PassengerSide = PassengerSide(),
        ) {
            data class DriverSide(
                val isChildLockActive: Boolean = false,
                val isLocked: Boolean = false,
                val isOpen: Boolean = false,
                val position: Int = 0,
                val shade: Shade = Shade(),
                val switch: String = "",
                val window: Window = Window(),
            ) {
                data class Shade(
                    val isOpen: Boolean = false,
                    val position: Int = 0,
                    val switch: String = "",
                )

                data class Window(
                    val isOpen: Boolean = false,
                    val position: Int = 0,
                    val switch: String = "",
                )
            }

            data class PassengerSide(
                val isChildLockActive: Boolean = false,
                val isLocked: Boolean = false,
                val isOpen: Boolean = false,
                val position: Int = 0,
                val shade: Shade = Shade(),
                val switch: String = "",
                val window: Window = Window(),
            ) {
                data class Shade(
                    val isOpen: Boolean = false,
                    val position: Int = 0,
                    val switch: String = "",
                )

                data class Window(
                    val isOpen: Boolean = false,
                    val position: Int = 0,
                    val switch: String = "",
                )
            }
        }
    }

    data class HVAC(
        val ambientAirTemperature: Float = 0f,
        val isAirConditioningActive: Boolean = false,
        val isAutoPowerOptimize: Boolean = false,
        val isFrontDefrosterActive: Boolean = false,
        val isRearDefrosterActive: Boolean = false,
        val isRecirculationActive: Boolean = false,
        val powerOptimizeLevel: Int = 0,
        val station: Station = Station(),
    ) {
        data class Station(
            val row1: Row1 = Row1(),
            val row2: Row2 = Row2(),
            val row3: Row3 = Row3(),
            val row4: Row4 = Row4(),
        ) {
            data class Row1(
                val driver: Driver = Driver(),
                val passenger: Passenger = Passenger(),
            ) {
                data class Driver(
                    val airDistribution: String = "",
                    val fanSpeed: Int = 0,
                    val temperature: Float = 0f,
                )

                data class Passenger(
                    val airDistribution: String = "",
                    val fanSpeed: Int = 0,
                    val temperature: Float = 0f,
                )
            }

            data class Row2(
                val driver: Driver = Driver(),
                val passenger: Passenger = Passenger(),
            ) {
                data class Driver(
                    val airDistribution: String = "",
                    val fanSpeed: Int = 0,
                    val temperature: Float = 0f,
                )

                data class Passenger(
                    val airDistribution: String = "",
                    val fanSpeed: Int = 0,
                    val temperature: Float = 0f,
                )
            }

            data class Row3(
                val driver: Driver = Driver(),
                val passenger: Passenger = Passenger(),
            ) {
                data class Driver(
                    val airDistribution: String = "",
                    val fanSpeed: Int = 0,
                    val temperature: Float = 0f,
                )

                data class Passenger(
                    val airDistribution: String = "",
                    val fanSpeed: Int = 0,
                    val temperature: Float = 0f,
                )
            }

            data class Row4(
                val driver: Driver = Driver(),
                val passenger: Passenger = Passenger(),
            ) {
                data class Driver(
                    val airDistribution: String = "",
                    val fanSpeed: Int = 0,
                    val temperature: Float = 0f,
                )

                data class Passenger(
                    val airDistribution: String = "",
                    val fanSpeed: Int = 0,
                    val temperature: Float = 0f,
                )
            }
        }
    }

    data class Infotainment(
        val hmi: HMI = HMI(),
        val isAutoPowerOptimize: Boolean = false,
        val media: Media = Media(),
        val navigation: Navigation = Navigation(),
        val powerOptimizeLevel: Int = 0,
        val smartphoneProjection: SmartphoneProjection = SmartphoneProjection(),
        val smartphoneScreenMirroring: SmartphoneScreenMirroring = SmartphoneScreenMirroring(),
    ) {
        data class HMI(
            val brightness: Float = 0f,
            val currentLanguage: String = "",
            val dateFormat: String = "",
            val dayNightMode: String = "",
            val displayOffDuration: Int = 0,
            val distanceUnit: String = "",
            val eVEconomyUnits: String = "",
            val eVEnergyUnits: String = "",
            val fontSize: String = "",
            val fuelEconomyUnits: String = "",
            val fuelVolumeUnit: String = "",
            val isScreenAlwaysOn: Boolean = false,
            val lastActionTime: String = "",
            val speedUnit: String = "",
            val temperatureUnit: String = "",
            val timeFormat: String = "",
            val tirePressureUnit: String = "",
        )

        data class Media(
            val action: String = "",
            val declinedURI: String = "",
            val played: Played = Played(),
            val selectedURI: String = "",
            val volume: Int = 0,
        ) {
            data class Played(
                val album: String = "",
                val artist: String = "",
                val genre: String = "",
                val playbackRate: Float = 0f,
                val source: String = "",
                val track: String = "",
                val uri: String = "",
            )
        }

        data class Navigation(
            val destinationSet: DestinationSet = DestinationSet(),
            val guidanceVoice: String = "",
            val map: Map = Map(),
            val mute: String = "",
            val volume: Int = 0,
        ) {
            data class DestinationSet(
                val latitude: Float = 0f,
                val longitude: Float = 0f,
            )

            data class Map(
                val isAutoScaleModeUsed: Boolean = false,
            )
        }

        data class SmartphoneProjection(
            val active: String = "",
            val source: String = "",
            val supportedMode: List<String> = emptyList(),
        )

        data class SmartphoneScreenMirroring(
            val active: String = "",
            val source: String = "",
        )
    }

    data class Light(
        val ambientLight: AmbientLight = AmbientLight(),
        val interactiveLightBar: InteractiveLightBar = InteractiveLightBar(),
        val isDomeOn: Boolean = false,
        val isGloveBoxOn: Boolean = false,
        val perceivedAmbientLight: Int = 0,
        val spotlight: Spotlight = Spotlight(),
    ) {
        data class AmbientLight(
            val row1: Row1 = Row1(),
            val row2: Row2 = Row2(),
        ) {
            data class Row1(
                val driverSide: DriverSide = DriverSide(),
                val passengerSide: PassengerSide = PassengerSide(),
            ) {
                data class DriverSide(
                    val color: String = "",
                    val intensity: Int = 0,
                    val isLightOn: Boolean = false,
                )

                data class PassengerSide(
                    val color: String = "",
                    val intensity: Int = 0,
                    val isLightOn: Boolean = false,
                )
            }

            data class Row2(
                val driverSide: DriverSide = DriverSide(),
                val passengerSide: PassengerSide = PassengerSide(),
            ) {
                data class DriverSide(
                    val color: String = "",
                    val intensity: Int = 0,
                    val isLightOn: Boolean = false,
                )

                data class PassengerSide(
                    val color: String = "",
                    val intensity: Int = 0,
                    val isLightOn: Boolean = false,
                )
            }
        }

        data class InteractiveLightBar(
            val color: String = "",
            val effect: String = "",
            val intensity: Int = 0,
            val isLightOn: Boolean = false,
        )

        data class Spotlight(
            val row1: Row1 = Row1(),
            val row2: Row2 = Row2(),
            val row3: Row3 = Row3(),
            val row4: Row4 = Row4(),
        ) {
            data class Row1(
                val driverSide: DriverSide = DriverSide(),
                val passengerSide: PassengerSide = PassengerSide(),
            ) {
                data class DriverSide(
                    val color: String = "",
                    val intensity: Int = 0,
                    val isLightOn: Boolean = false,
                )

                data class PassengerSide(
                    val color: String = "",
                    val intensity: Int = 0,
                    val isLightOn: Boolean = false,
                )
            }

            data class Row2(
                val driverSide: DriverSide = DriverSide(),
                val passengerSide: PassengerSide = PassengerSide(),
            ) {
                data class DriverSide(
                    val color: String = "",
                    val intensity: Int = 0,
                    val isLightOn: Boolean = false,
                )

                data class PassengerSide(
                    val color: String = "",
                    val intensity: Int = 0,
                    val isLightOn: Boolean = false,
                )
            }

            data class Row3(
                val driverSide: DriverSide = DriverSide(),
                val passengerSide: PassengerSide = PassengerSide(),
            ) {
                data class DriverSide(
                    val color: String = "",
                    val intensity: Int = 0,
                    val isLightOn: Boolean = false,
                )

                data class PassengerSide(
                    val color: String = "",
                    val intensity: Int = 0,
                    val isLightOn: Boolean = false,
                )
            }

            data class Row4(
                val driverSide: DriverSide = DriverSide(),
                val passengerSide: PassengerSide = PassengerSide(),
            ) {
                data class DriverSide(
                    val color: String = "",
                    val intensity: Int = 0,
                    val isLightOn: Boolean = false,
                )

                data class PassengerSide(
                    val color: String = "",
                    val intensity: Int = 0,
                    val isLightOn: Boolean = false,
                )
            }
        }
    }

    data class RearShade(
        val isOpen: Boolean = false,
        val position: Int = 0,
        val switch: String = "",
    )

    data class RearviewMirror(
        val dimmingLevel: Int = 0,
    )

    data class Seat(
        val row1: Row1 = Row1(),
        val row2: Row2 = Row2(),
    ) {
        data class Row1(
            val driverSide: DriverSide = DriverSide(),
            val middle: Middle = Middle(),
            val passengerSide: PassengerSide = PassengerSide(),
        ) {
            data class DriverSide(
                val airbag: Airbag = Airbag(),
                val backrest: Backrest = Backrest(),
                val headrest: Headrest = Headrest(),
                val heatingCooling: Int = 0,
                val height: Int = 0,
                val isBackwardSwitchEngaged: Boolean = false,
                val isBelted: Boolean = false,
                val isCoolerSwitchEngaged: Boolean = false,
                val isDecreaseMassageLevelSwitchEngaged: Boolean = false,
                val isDownSwitchEngaged: Boolean = false,
                val isForwardSwitchEngaged: Boolean = false,
                val isIncreaseMassageLevelSwitchEngaged: Boolean = false,
                val isTiltBackwardSwitchEngaged: Boolean = false,
                val isTiltForwardSwitchEngaged: Boolean = false,
                val isUpSwitchEngaged: Boolean = false,
                val isWarmerSwitchEngaged: Boolean = false,
                val massage: Massage = Massage(),
                val neckScarf: NeckScarf = NeckScarf(),
                val occupancyStatus: String = "",
                val position: Int = 0,
                val seatBeltHeight: Int = 0,
                val seating: Seating = Seating(),
                val tilt: Float = 0f,
            ) {
                data class Airbag(
                    val isDeployed: Boolean = false,
                    val isEnabled: Boolean = false,
                )

                data class Backrest(
                    val bottomLumbarSupport: Float = 0f,
                    val heatingCooling: Int = 0,
                    val isLessLumbarSupportSwitchEngaged: Boolean = false,
                    val isLessSideBolsterSupportSwitchEngaged: Boolean = false,
                    val isLumbarDownSwitchEngaged: Boolean = false,
                    val isLumbarUpSwitchEngaged: Boolean = false,
                    val isMoreLumbarSupportSwitchEngaged: Boolean = false,
                    val isMoreSideBolsterSupportSwitchEngaged: Boolean = false,
                    val isReclineBackwardSwitchEngaged: Boolean = false,
                    val isReclineForwardSwitchEngaged: Boolean = false,
                    val lumbarHeight: Int = 0,
                    val lumbarSupport: Float = 0f,
                    val midLumbarSupport: Float = 0f,
                    val recline: Float = 0f,
                    val sideBolsterSupport: Float = 0f,
                    val sideBolsterSupportLeft: Float = 0f,
                    val sideBolsterSupportRight: Float = 0f,
                    val topLumbarSupport: Float = 0f,
                    val upperShoulderSupport: Float = 0f,
                )

                data class Headrest(
                    val angle: Float = 0f,
                    val height: Int = 0,
                    val isBackwardSwitchEngaged: Boolean = false,
                    val isDownSwitchEngaged: Boolean = false,
                    val isForwardSwitchEngaged: Boolean = false,
                    val isUpSwitchEngaged: Boolean = false,
                )

                data class Massage(
                    val isAvailable: Boolean = false,
                    val level: Int = 0,
                    val status: String = "",
                    val supportedTypes: List<String> = emptyList(),
                    val typeActive: String = "",
                )

                data class NeckScarf(
                    val fanSpeed: Int = 0,
                    val heatingCooling: Int = 0,
                )

                data class Seating(
                    val heatingCooling: Int = 0,
                    val isBackwardSwitchEngaged: Boolean = false,
                    val isForwardSwitchEngaged: Boolean = false,
                    val length: Int = 0,
                    val sideBolsterSupportLeft: Float = 0f,
                    val sideBolsterSupportRight: Float = 0f,
                )
            }

            data class Middle(
                val airbag: Airbag = Airbag(),
                val backrest: Backrest = Backrest(),
                val headrest: Headrest = Headrest(),
                val heatingCooling: Int = 0,
                val height: Int = 0,
                val isBackwardSwitchEngaged: Boolean = false,
                val isBelted: Boolean = false,
                val isCoolerSwitchEngaged: Boolean = false,
                val isDecreaseMassageLevelSwitchEngaged: Boolean = false,
                val isDownSwitchEngaged: Boolean = false,
                val isForwardSwitchEngaged: Boolean = false,
                val isIncreaseMassageLevelSwitchEngaged: Boolean = false,
                val isTiltBackwardSwitchEngaged: Boolean = false,
                val isTiltForwardSwitchEngaged: Boolean = false,
                val isUpSwitchEngaged: Boolean = false,
                val isWarmerSwitchEngaged: Boolean = false,
                val massage: Massage = Massage(),
                val neckScarf: NeckScarf = NeckScarf(),
                val occupancyStatus: String = "",
                val position: Int = 0,
                val seatBeltHeight: Int = 0,
                val seating: Seating = Seating(),
                val tilt: Float = 0f,
            ) {
                data class Airbag(
                    val isDeployed: Boolean = false,
                    val isEnabled: Boolean = false,
                )

                data class Backrest(
                    val bottomLumbarSupport: Float = 0f,
                    val heatingCooling: Int = 0,
                    val isLessLumbarSupportSwitchEngaged: Boolean = false,
                    val isLessSideBolsterSupportSwitchEngaged: Boolean = false,
                    val isLumbarDownSwitchEngaged: Boolean = false,
                    val isLumbarUpSwitchEngaged: Boolean = false,
                    val isMoreLumbarSupportSwitchEngaged: Boolean = false,
                    val isMoreSideBolsterSupportSwitchEngaged: Boolean = false,
                    val isReclineBackwardSwitchEngaged: Boolean = false,
                    val isReclineForwardSwitchEngaged: Boolean = false,
                    val lumbarHeight: Int = 0,
                    val lumbarSupport: Float = 0f,
                    val midLumbarSupport: Float = 0f,
                    val recline: Float = 0f,
                    val sideBolsterSupport: Float = 0f,
                    val sideBolsterSupportLeft: Float = 0f,
                    val sideBolsterSupportRight: Float = 0f,
                    val topLumbarSupport: Float = 0f,
                    val upperShoulderSupport: Float = 0f,
                )

                data class Headrest(
                    val angle: Float = 0f,
                    val height: Int = 0,
                    val isBackwardSwitchEngaged: Boolean = false,
                    val isDownSwitchEngaged: Boolean = false,
                    val isForwardSwitchEngaged: Boolean = false,
                    val isUpSwitchEngaged: Boolean = false,
                )

                data class Massage(
                    val isAvailable: Boolean = false,
                    val level: Int = 0,
                    val status: String = "",
                    val supportedTypes: List<String> = emptyList(),
                    val typeActive: String = "",
                )

                data class NeckScarf(
                    val fanSpeed: Int = 0,
                    val heatingCooling: Int = 0,
                )

                data class Seating(
                    val heatingCooling: Int = 0,
                    val isBackwardSwitchEngaged: Boolean = false,
                    val isForwardSwitchEngaged: Boolean = false,
                    val length: Int = 0,
                    val sideBolsterSupportLeft: Float = 0f,
                    val sideBolsterSupportRight: Float = 0f,
                )
            }

            data class PassengerSide(
                val airbag: Airbag = Airbag(),
                val backrest: Backrest = Backrest(),
                val headrest: Headrest = Headrest(),
                val heatingCooling: Int = 0,
                val height: Int = 0,
                val isBackwardSwitchEngaged: Boolean = false,
                val isBelted: Boolean = false,
                val isCoolerSwitchEngaged: Boolean = false,
                val isDecreaseMassageLevelSwitchEngaged: Boolean = false,
                val isDownSwitchEngaged: Boolean = false,
                val isForwardSwitchEngaged: Boolean = false,
                val isIncreaseMassageLevelSwitchEngaged: Boolean = false,
                val isTiltBackwardSwitchEngaged: Boolean = false,
                val isTiltForwardSwitchEngaged: Boolean = false,
                val isUpSwitchEngaged: Boolean = false,
                val isWarmerSwitchEngaged: Boolean = false,
                val massage: Massage = Massage(),
                val neckScarf: NeckScarf = NeckScarf(),
                val occupancyStatus: String = "",
                val position: Int = 0,
                val seatBeltHeight: Int = 0,
                val seating: Seating = Seating(),
                val tilt: Float = 0f,
            ) {
                data class Airbag(
                    val isDeployed: Boolean = false,
                    val isEnabled: Boolean = false,
                )

                data class Backrest(
                    val bottomLumbarSupport: Float = 0f,
                    val heatingCooling: Int = 0,
                    val isLessLumbarSupportSwitchEngaged: Boolean = false,
                    val isLessSideBolsterSupportSwitchEngaged: Boolean = false,
                    val isLumbarDownSwitchEngaged: Boolean = false,
                    val isLumbarUpSwitchEngaged: Boolean = false,
                    val isMoreLumbarSupportSwitchEngaged: Boolean = false,
                    val isMoreSideBolsterSupportSwitchEngaged: Boolean = false,
                    val isReclineBackwardSwitchEngaged: Boolean = false,
                    val isReclineForwardSwitchEngaged: Boolean = false,
                    val lumbarHeight: Int = 0,
                    val lumbarSupport: Float = 0f,
                    val midLumbarSupport: Float = 0f,
                    val recline: Float = 0f,
                    val sideBolsterSupport: Float = 0f,
                    val sideBolsterSupportLeft: Float = 0f,
                    val sideBolsterSupportRight: Float = 0f,
                    val topLumbarSupport: Float = 0f,
                    val upperShoulderSupport: Float = 0f,
                )

                data class Headrest(
                    val angle: Float = 0f,
                    val height: Int = 0,
                    val isBackwardSwitchEngaged: Boolean = false,
                    val isDownSwitchEngaged: Boolean = false,
                    val isForwardSwitchEngaged: Boolean = false,
                    val isUpSwitchEngaged: Boolean = false,
                )

                data class Massage(
                    val isAvailable: Boolean = false,
                    val level: Int = 0,
                    val status: String = "",
                    val supportedTypes: List<String> = emptyList(),
                    val typeActive: String = "",
                )

                data class NeckScarf(
                    val fanSpeed: Int = 0,
                    val heatingCooling: Int = 0,
                )

                data class Seating(
                    val heatingCooling: Int = 0,
                    val isBackwardSwitchEngaged: Boolean = false,
                    val isForwardSwitchEngaged: Boolean = false,
                    val length: Int = 0,
                    val sideBolsterSupportLeft: Float = 0f,
                    val sideBolsterSupportRight: Float = 0f,
                )
            }
        }

        data class Row2(
            val driverSide: DriverSide = DriverSide(),
            val middle: Middle = Middle(),
            val passengerSide: PassengerSide = PassengerSide(),
        ) {
            data class DriverSide(
                val airbag: Airbag = Airbag(),
                val backrest: Backrest = Backrest(),
                val headrest: Headrest = Headrest(),
                val heatingCooling: Int = 0,
                val height: Int = 0,
                val isBackwardSwitchEngaged: Boolean = false,
                val isBelted: Boolean = false,
                val isCoolerSwitchEngaged: Boolean = false,
                val isDecreaseMassageLevelSwitchEngaged: Boolean = false,
                val isDownSwitchEngaged: Boolean = false,
                val isForwardSwitchEngaged: Boolean = false,
                val isIncreaseMassageLevelSwitchEngaged: Boolean = false,
                val isTiltBackwardSwitchEngaged: Boolean = false,
                val isTiltForwardSwitchEngaged: Boolean = false,
                val isUpSwitchEngaged: Boolean = false,
                val isWarmerSwitchEngaged: Boolean = false,
                val massage: Massage = Massage(),
                val neckScarf: NeckScarf = NeckScarf(),
                val occupancyStatus: String = "",
                val position: Int = 0,
                val seatBeltHeight: Int = 0,
                val seating: Seating = Seating(),
                val tilt: Float = 0f,
            ) {
                data class Airbag(
                    val isDeployed: Boolean = false,
                    val isEnabled: Boolean = false,
                )

                data class Backrest(
                    val bottomLumbarSupport: Float = 0f,
                    val heatingCooling: Int = 0,
                    val isLessLumbarSupportSwitchEngaged: Boolean = false,
                    val isLessSideBolsterSupportSwitchEngaged: Boolean = false,
                    val isLumbarDownSwitchEngaged: Boolean = false,
                    val isLumbarUpSwitchEngaged: Boolean = false,
                    val isMoreLumbarSupportSwitchEngaged: Boolean = false,
                    val isMoreSideBolsterSupportSwitchEngaged: Boolean = false,
                    val isReclineBackwardSwitchEngaged: Boolean = false,
                    val isReclineForwardSwitchEngaged: Boolean = false,
                    val lumbarHeight: Int = 0,
                    val lumbarSupport: Float = 0f,
                    val midLumbarSupport: Float = 0f,
                    val recline: Float = 0f,
                    val sideBolsterSupport: Float = 0f,
                    val sideBolsterSupportLeft: Float = 0f,
                    val sideBolsterSupportRight: Float = 0f,
                    val topLumbarSupport: Float = 0f,
                    val upperShoulderSupport: Float = 0f,
                )

                data class Headrest(
                    val angle: Float = 0f,
                    val height: Int = 0,
                    val isBackwardSwitchEngaged: Boolean = false,
                    val isDownSwitchEngaged: Boolean = false,
                    val isForwardSwitchEngaged: Boolean = false,
                    val isUpSwitchEngaged: Boolean = false,
                )

                data class Massage(
                    val isAvailable: Boolean = false,
                    val level: Int = 0,
                    val status: String = "",
                    val supportedTypes: List<String> = emptyList(),
                    val typeActive: String = "",
                )

                data class NeckScarf(
                    val fanSpeed: Int = 0,
                    val heatingCooling: Int = 0,
                )

                data class Seating(
                    val heatingCooling: Int = 0,
                    val isBackwardSwitchEngaged: Boolean = false,
                    val isForwardSwitchEngaged: Boolean = false,
                    val length: Int = 0,
                    val sideBolsterSupportLeft: Float = 0f,
                    val sideBolsterSupportRight: Float = 0f,
                )
            }

            data class Middle(
                val airbag: Airbag = Airbag(),
                val backrest: Backrest = Backrest(),
                val headrest: Headrest = Headrest(),
                val heatingCooling: Int = 0,
                val height: Int = 0,
                val isBackwardSwitchEngaged: Boolean = false,
                val isBelted: Boolean = false,
                val isCoolerSwitchEngaged: Boolean = false,
                val isDecreaseMassageLevelSwitchEngaged: Boolean = false,
                val isDownSwitchEngaged: Boolean = false,
                val isForwardSwitchEngaged: Boolean = false,
                val isIncreaseMassageLevelSwitchEngaged: Boolean = false,
                val isTiltBackwardSwitchEngaged: Boolean = false,
                val isTiltForwardSwitchEngaged: Boolean = false,
                val isUpSwitchEngaged: Boolean = false,
                val isWarmerSwitchEngaged: Boolean = false,
                val massage: Massage = Massage(),
                val neckScarf: NeckScarf = NeckScarf(),
                val occupancyStatus: String = "",
                val position: Int = 0,
                val seatBeltHeight: Int = 0,
                val seating: Seating = Seating(),
                val tilt: Float = 0f,
            ) {
                data class Airbag(
                    val isDeployed: Boolean = false,
                    val isEnabled: Boolean = false,
                )

                data class Backrest(
                    val bottomLumbarSupport: Float = 0f,
                    val heatingCooling: Int = 0,
                    val isLessLumbarSupportSwitchEngaged: Boolean = false,
                    val isLessSideBolsterSupportSwitchEngaged: Boolean = false,
                    val isLumbarDownSwitchEngaged: Boolean = false,
                    val isLumbarUpSwitchEngaged: Boolean = false,
                    val isMoreLumbarSupportSwitchEngaged: Boolean = false,
                    val isMoreSideBolsterSupportSwitchEngaged: Boolean = false,
                    val isReclineBackwardSwitchEngaged: Boolean = false,
                    val isReclineForwardSwitchEngaged: Boolean = false,
                    val lumbarHeight: Int = 0,
                    val lumbarSupport: Float = 0f,
                    val midLumbarSupport: Float = 0f,
                    val recline: Float = 0f,
                    val sideBolsterSupport: Float = 0f,
                    val sideBolsterSupportLeft: Float = 0f,
                    val sideBolsterSupportRight: Float = 0f,
                    val topLumbarSupport: Float = 0f,
                    val upperShoulderSupport: Float = 0f,
                )

                data class Headrest(
                    val angle: Float = 0f,
                    val height: Int = 0,
                    val isBackwardSwitchEngaged: Boolean = false,
                    val isDownSwitchEngaged: Boolean = false,
                    val isForwardSwitchEngaged: Boolean = false,
                    val isUpSwitchEngaged: Boolean = false,
                )

                data class Massage(
                    val isAvailable: Boolean = false,
                    val level: Int = 0,
                    val status: String = "",
                    val supportedTypes: List<String> = emptyList(),
                    val typeActive: String = "",
                )

                data class NeckScarf(
                    val fanSpeed: Int = 0,
                    val heatingCooling: Int = 0,
                )

                data class Seating(
                    val heatingCooling: Int = 0,
                    val isBackwardSwitchEngaged: Boolean = false,
                    val isForwardSwitchEngaged: Boolean = false,
                    val length: Int = 0,
                    val sideBolsterSupportLeft: Float = 0f,
                    val sideBolsterSupportRight: Float = 0f,
                )
            }

            data class PassengerSide(
                val airbag: Airbag = Airbag(),
                val backrest: Backrest = Backrest(),
                val headrest: Headrest = Headrest(),
                val heatingCooling: Int = 0,
                val height: Int = 0,
                val isBackwardSwitchEngaged: Boolean = false,
                val isBelted: Boolean = false,
                val isCoolerSwitchEngaged: Boolean = false,
                val isDecreaseMassageLevelSwitchEngaged: Boolean = false,
                val isDownSwitchEngaged: Boolean = false,
                val isForwardSwitchEngaged: Boolean = false,
                val isIncreaseMassageLevelSwitchEngaged: Boolean = false,
                val isTiltBackwardSwitchEngaged: Boolean = false,
                val isTiltForwardSwitchEngaged: Boolean = false,
                val isUpSwitchEngaged: Boolean = false,
                val isWarmerSwitchEngaged: Boolean = false,
                val massage: Massage = Massage(),
                val neckScarf: NeckScarf = NeckScarf(),
                val occupancyStatus: String = "",
                val position: Int = 0,
                val seatBeltHeight: Int = 0,
                val seating: Seating = Seating(),
                val tilt: Float = 0f,
            ) {
                data class Airbag(
                    val isDeployed: Boolean = false,
                    val isEnabled: Boolean = false,
                )

                data class Backrest(
                    val bottomLumbarSupport: Float = 0f,
                    val heatingCooling: Int = 0,
                    val isLessLumbarSupportSwitchEngaged: Boolean = false,
                    val isLessSideBolsterSupportSwitchEngaged: Boolean = false,
                    val isLumbarDownSwitchEngaged: Boolean = false,
                    val isLumbarUpSwitchEngaged: Boolean = false,
                    val isMoreLumbarSupportSwitchEngaged: Boolean = false,
                    val isMoreSideBolsterSupportSwitchEngaged: Boolean = false,
                    val isReclineBackwardSwitchEngaged: Boolean = false,
                    val isReclineForwardSwitchEngaged: Boolean = false,
                    val lumbarHeight: Int = 0,
                    val lumbarSupport: Float = 0f,
                    val midLumbarSupport: Float = 0f,
                    val recline: Float = 0f,
                    val sideBolsterSupport: Float = 0f,
                    val sideBolsterSupportLeft: Float = 0f,
                    val sideBolsterSupportRight: Float = 0f,
                    val topLumbarSupport: Float = 0f,
                    val upperShoulderSupport: Float = 0f,
                )

                data class Headrest(
                    val angle: Float = 0f,
                    val height: Int = 0,
                    val isBackwardSwitchEngaged: Boolean = false,
                    val isDownSwitchEngaged: Boolean = false,
                    val isForwardSwitchEngaged: Boolean = false,
                    val isUpSwitchEngaged: Boolean = false,
                )

                data class Massage(
                    val isAvailable: Boolean = false,
                    val level: Int = 0,
                    val status: String = "",
                    val supportedTypes: List<String> = emptyList(),
                    val typeActive: String = "",
                )

                data class NeckScarf(
                    val fanSpeed: Int = 0,
                    val heatingCooling: Int = 0,
                )

                data class Seating(
                    val heatingCooling: Int = 0,
                    val isBackwardSwitchEngaged: Boolean = false,
                    val isForwardSwitchEngaged: Boolean = false,
                    val length: Int = 0,
                    val sideBolsterSupportLeft: Float = 0f,
                    val sideBolsterSupportRight: Float = 0f,
                )
            }
        }
    }

    data class Sunroof(
        val position: Int = 0,
        val shade: Shade = Shade(),
        val switch: String = "",
    ) {
        data class Shade(
            val isOpen: Boolean = false,
            val position: Int = 0,
            val switch: String = "",
        )
    }
}
