// Generated from docs/vss.csv.
// Regenerate with: python3 core/core-vss/tools/generate_vss_signals.py

package com.monsters.mobimon.core.vss.generated

data class VssSignals(
    val adas: ADAS = ADAS(),
    val acceleration: Acceleration = Acceleration(),
    val angularVelocity: AngularVelocity = AngularVelocity(),
    val averageSpeed: Float = 0f,
    val body: Body = Body(),
    val cabin: Cabin = Cabin(),
    val cargoVolume: Float = 0f,
    val chassis: Chassis = Chassis(),
    val connectivity: Connectivity = Connectivity(),
    val controlUnit: ControlUnit = ControlUnit(),
    val curbWeight: Int = 0,
    val currentLocation: CurrentLocation = CurrentLocation(),
    val currentOverallWeight: Int = 0,
    val diagnostics: Diagnostics = Diagnostics(),
    val driver: Driver = Driver(),
    val emissionsCO2: Int = 0,
    val exterior: Exterior = Exterior(),
    val grossWeight: Int = 0,
    val height: Int = 0,
    val isAutoPowerOptimize: Boolean = false,
    val isBrokenDown: Boolean = false,
    val isMoving: Boolean = false,
    val length: Int = 0,
    val lowVoltageBattery: LowVoltageBattery = LowVoltageBattery(),
    val lowVoltageSystemState: String = "",
    val maxTowBallWeight: Int = 0,
    val maxTowWeight: Int = 0,
    val motionManagement: MotionManagement = MotionManagement(),
    val occupant: Occupant = Occupant(),
    val powerOptimizeLevel: Int = 0,
    val powertrain: Powertrain = Powertrain(),
    val roofLoad: Int = 0,
    val service: Service = Service(),
    val speed: Float = 0f,
    val startTime: String = "0000-01-01T00:00Z",
    val trailer: Trailer = Trailer(),
    val traveledDistance: Int = 0,
    val traveledDistanceSinceStart: Int = 0,
    val tripDuration: Float = 0f,
    val tripMeterReading: Int = 0,
    val turningDiameter: Int = 0,
    val vehicleIdentification: VehicleIdentification = VehicleIdentification(),
    val versionVSS: VersionVSS = VersionVSS(),
    val widthExcludingMirrors: Int = 0,
    val widthFoldedMirrors: Int = 0,
    val widthIncludingMirrors: Int = 0,
) {
    data class ADAS(
        val abs: ABS = ABS(),
        val activeAutonomyLevel: String = "",
        val cruiseControl: CruiseControl = CruiseControl(),
        val dms: DMS = DMS(),
        val eba: EBA = EBA(),
        val ebd: EBD = EBD(),
        val esc: ESC = ESC(),
        val isAutoPowerOptimize: Boolean = false,
        val laneDepartureDetection: LaneDepartureDetection = LaneDepartureDetection(),
        val obstacleDetection: ObstacleDetection = ObstacleDetection(),
        val powerOptimizeLevel: Int = 0,
        val supportedAutonomyLevel: String = "",
        val tcs: TCS = TCS(),
    ) {
        data class ABS(
            val isEnabled: Boolean = false,
            val isEngaged: Boolean = false,
            val isError: Boolean = false,
        )

        data class CruiseControl(
            val adaptiveDistanceSet: Float = 0f,
            val adaptiveIntervalSet: Int = 0,
            val isActive: Boolean = false,
            val isAdaptive: Boolean = false,
            val isEnabled: Boolean = false,
            val isError: Boolean = false,
            val speedSet: Float = 0f,
        )

        data class DMS(
            val isEnabled: Boolean = false,
            val isError: Boolean = false,
            val isWarning: Boolean = false,
        )

        data class EBA(
            val isEnabled: Boolean = false,
            val isEngaged: Boolean = false,
            val isError: Boolean = false,
        )

        data class EBD(
            val isEnabled: Boolean = false,
            val isEngaged: Boolean = false,
            val isError: Boolean = false,
        )

        data class ESC(
            val isEnabled: Boolean = false,
            val isEngaged: Boolean = false,
            val isError: Boolean = false,
            val isStrongCrossWindDetected: Boolean = false,
            val roadFriction: RoadFriction = RoadFriction(),
        ) {
            data class RoadFriction(
                val lowerBound: Float = 0f,
                val mostProbable: Float = 0f,
                val upperBound: Float = 0f,
            )
        }

        data class LaneDepartureDetection(
            val isEnabled: Boolean = false,
            val isError: Boolean = false,
            val isWarning: Boolean = false,
        )

        data class ObstacleDetection(
            val front: Front = Front(),
            val rear: Rear = Rear(),
        ) {
            data class Front(
                val center: Center = Center(),
                val left: Left = Left(),
                val right: Right = Right(),
            ) {
                data class Center(
                    val distance: Float = 0f,
                    val isEnabled: Boolean = false,
                    val isError: Boolean = false,
                    val isWarning: Boolean = false,
                    val timeGap: Int = 0,
                    val warningType: String = "",
                )

                data class Left(
                    val distance: Float = 0f,
                    val isEnabled: Boolean = false,
                    val isError: Boolean = false,
                    val isWarning: Boolean = false,
                    val timeGap: Int = 0,
                    val warningType: String = "",
                )

                data class Right(
                    val distance: Float = 0f,
                    val isEnabled: Boolean = false,
                    val isError: Boolean = false,
                    val isWarning: Boolean = false,
                    val timeGap: Int = 0,
                    val warningType: String = "",
                )
            }

            data class Rear(
                val center: Center = Center(),
                val left: Left = Left(),
                val right: Right = Right(),
            ) {
                data class Center(
                    val distance: Float = 0f,
                    val isEnabled: Boolean = false,
                    val isError: Boolean = false,
                    val isWarning: Boolean = false,
                    val timeGap: Int = 0,
                    val warningType: String = "",
                )

                data class Left(
                    val distance: Float = 0f,
                    val isEnabled: Boolean = false,
                    val isError: Boolean = false,
                    val isWarning: Boolean = false,
                    val timeGap: Int = 0,
                    val warningType: String = "",
                )

                data class Right(
                    val distance: Float = 0f,
                    val isEnabled: Boolean = false,
                    val isError: Boolean = false,
                    val isWarning: Boolean = false,
                    val timeGap: Int = 0,
                    val warningType: String = "",
                )
            }
        }

        data class TCS(
            val isEnabled: Boolean = false,
            val isEngaged: Boolean = false,
            val isError: Boolean = false,
        )
    }

    data class Acceleration(
        val lateral: Float = 0f,
        val longitudinal: Float = 0f,
        val vertical: Float = 0f,
    )

    data class AngularVelocity(
        val pitch: Float = 0f,
        val roll: Float = 0f,
        val yaw: Float = 0f,
    )

    data class Body(
        val bodyType: String = "",
        val hood: Hood = Hood(),
        val horn: Horn = Horn(),
        val isAutoPowerOptimize: Boolean = false,
        val lights: Lights = Lights(),
        val mirrors: Mirrors = Mirrors(),
        val powerOptimizeLevel: Int = 0,
        val raindetection: Raindetection = Raindetection(),
        val rearMainSpoilerPosition: Float = 0f,
        val trunk: Trunk = Trunk(),
        val windshield: Windshield = Windshield(),
    ) {
        data class Hood(
            val isOpen: Boolean = false,
            val position: Int = 0,
            val switch: String = "",
        )

        data class Horn(
            val isActive: Boolean = false,
        )

        data class Lights(
            val backup: Backup = Backup(),
            val beam: Beam = Beam(),
            val brake: Brake = Brake(),
            val directionIndicator: DirectionIndicator = DirectionIndicator(),
            val fog: Fog = Fog(),
            val hazard: Hazard = Hazard(),
            val isHighBeamSwitchOn: Boolean = false,
            val licensePlate: LicensePlate = LicensePlate(),
            val lightSwitch: String = "",
            val parking: Parking = Parking(),
            val running: Running = Running(),
        ) {
            data class Backup(
                val isDefect: Boolean = false,
                val isOn: Boolean = false,
            )

            data class Beam(
                val high: High = High(),
                val low: Low = Low(),
            ) {
                data class High(
                    val isDefect: Boolean = false,
                    val isOn: Boolean = false,
                )

                data class Low(
                    val isDefect: Boolean = false,
                    val isOn: Boolean = false,
                )
            }

            data class Brake(
                val isActive: String = "",
                val isDefect: Boolean = false,
            )

            data class DirectionIndicator(
                val left: Left = Left(),
                val right: Right = Right(),
            ) {
                data class Left(
                    val isDefect: Boolean = false,
                    val isSignaling: Boolean = false,
                )

                data class Right(
                    val isDefect: Boolean = false,
                    val isSignaling: Boolean = false,
                )
            }

            data class Fog(
                val front: Front = Front(),
                val rear: Rear = Rear(),
            ) {
                data class Front(
                    val isDefect: Boolean = false,
                    val isOn: Boolean = false,
                )

                data class Rear(
                    val isDefect: Boolean = false,
                    val isOn: Boolean = false,
                )
            }

            data class Hazard(
                val isDefect: Boolean = false,
                val isSignaling: Boolean = false,
            )

            data class LicensePlate(
                val isDefect: Boolean = false,
                val isOn: Boolean = false,
            )

            data class Parking(
                val isDefect: Boolean = false,
                val isOn: Boolean = false,
            )

            data class Running(
                val isDefect: Boolean = false,
                val isOn: Boolean = false,
            )
        }

        data class Mirrors(
            val driverSide: DriverSide = DriverSide(),
            val passengerSide: PassengerSide = PassengerSide(),
        ) {
            data class DriverSide(
                val isFolded: Boolean = false,
                val isHeatingOn: Boolean = false,
                val isLocked: Boolean = false,
                val pan: Int = 0,
                val tilt: Int = 0,
                val yaw: Int = 0,
            )

            data class PassengerSide(
                val isFolded: Boolean = false,
                val isHeatingOn: Boolean = false,
                val isLocked: Boolean = false,
                val pan: Int = 0,
                val tilt: Int = 0,
                val yaw: Int = 0,
            )
        }

        data class Raindetection(
            val intensity: Int = 0,
        )

        data class Trunk(
            val front: Front = Front(),
            val rear: Rear = Rear(),
        ) {
            data class Front(
                val isLightOn: Boolean = false,
                val isLocked: Boolean = false,
                val isOpen: Boolean = false,
                val position: Int = 0,
                val switch: String = "",
            )

            data class Rear(
                val isLightOn: Boolean = false,
                val isLocked: Boolean = false,
                val isOpen: Boolean = false,
                val position: Int = 0,
                val switch: String = "",
            )
        }

        data class Windshield(
            val front: Front = Front(),
            val rear: Rear = Rear(),
        ) {
            data class Front(
                val isHeatingOn: Boolean = false,
                val washerFluid: WasherFluid = WasherFluid(),
                val wiping: Wiping = Wiping(),
            ) {
                data class WasherFluid(
                    val isLevelLow: Boolean = false,
                    val level: Int = 0,
                )

                data class Wiping(
                    val intensity: Int = 0,
                    val isWipersWorn: Boolean = false,
                    val mode: String = "",
                    val system: System = System(),
                    val wiperWear: Int = 0,
                ) {
                    data class System(
                        val actualPosition: Float = 0f,
                        val driveCurrent: Float = 0f,
                        val frequency: Int = 0,
                        val isBlocked: Boolean = false,
                        val isEndingWipeCycle: Boolean = false,
                        val isOverheated: Boolean = false,
                        val isPositionReached: Boolean = false,
                        val isWiperError: Boolean = false,
                        val isWiping: Boolean = false,
                        val mode: String = "",
                        val targetPosition: Float = 0f,
                    )
                }
            }

            data class Rear(
                val isHeatingOn: Boolean = false,
                val washerFluid: WasherFluid = WasherFluid(),
                val wiping: Wiping = Wiping(),
            ) {
                data class WasherFluid(
                    val isLevelLow: Boolean = false,
                    val level: Int = 0,
                )

                data class Wiping(
                    val intensity: Int = 0,
                    val isWipersWorn: Boolean = false,
                    val mode: String = "",
                    val system: System = System(),
                    val wiperWear: Int = 0,
                ) {
                    data class System(
                        val actualPosition: Float = 0f,
                        val driveCurrent: Float = 0f,
                        val frequency: Int = 0,
                        val isBlocked: Boolean = false,
                        val isEndingWipeCycle: Boolean = false,
                        val isOverheated: Boolean = false,
                        val isPositionReached: Boolean = false,
                        val isWiperError: Boolean = false,
                        val isWiping: Boolean = false,
                        val mode: String = "",
                        val targetPosition: Float = 0f,
                    )
                }
            }
        }
    }

    data class Cabin(
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

    data class Chassis(
        val accelerator: Accelerator = Accelerator(),
        val axle: Axle = Axle(),
        val axleCount: Int = 2,
        val brake: Brake = Brake(),
        val parkingBrake: ParkingBrake = ParkingBrake(),
        val steeringWheel: SteeringWheel = SteeringWheel(),
        val wheelbase: Int = 0,
    ) {
        data class Accelerator(
            val pedalPosition: Int = 0,
        )

        data class Axle(
            val row1: Row1 = Row1(),
            val row2: Row2 = Row2(),
        ) {
            data class Row1(
                val axleWidth: Int = 0,
                val steeringAngle: Float = 0f,
                val tireAspectRatio: Int = 0,
                val tireDiameter: Float = 0f,
                val tireWidth: Int = 0,
                val trackWidth: Int = 0,
                val treadWidth: Int = 0,
                val wheel: Wheel = Wheel(),
                val wheelCount: Int = 0,
                val wheelDiameter: Float = 0f,
                val wheelWidth: Float = 0f,
            ) {
                data class Wheel(
                    val left: Left = Left(),
                    val right: Right = Right(),
                ) {
                    data class Left(
                        val angularSpeed: Float = 0f,
                        val brake: Brake = Brake(),
                        val speed: Float = 0f,
                        val tire: Tire = Tire(),
                    ) {
                        data class Brake(
                            val fluidLevel: Int = 0,
                            val isBrakesWorn: Boolean = false,
                            val isFluidLevelLow: Boolean = false,
                            val padWear: Int = 0,
                        )

                        data class Tire(
                            val airTemperature: Float = 0f,
                            val isPressureLow: Boolean = false,
                            val pressure: Int = 0,
                            val rubberTemperature: Float = 0f,
                            val temperature: Float = 0f,
                        )
                    }

                    data class Right(
                        val angularSpeed: Float = 0f,
                        val brake: Brake = Brake(),
                        val speed: Float = 0f,
                        val tire: Tire = Tire(),
                    ) {
                        data class Brake(
                            val fluidLevel: Int = 0,
                            val isBrakesWorn: Boolean = false,
                            val isFluidLevelLow: Boolean = false,
                            val padWear: Int = 0,
                        )

                        data class Tire(
                            val airTemperature: Float = 0f,
                            val isPressureLow: Boolean = false,
                            val pressure: Int = 0,
                            val rubberTemperature: Float = 0f,
                            val temperature: Float = 0f,
                        )
                    }
                }
            }

            data class Row2(
                val axleWidth: Int = 0,
                val steeringAngle: Float = 0f,
                val tireAspectRatio: Int = 0,
                val tireDiameter: Float = 0f,
                val tireWidth: Int = 0,
                val trackWidth: Int = 0,
                val treadWidth: Int = 0,
                val wheel: Wheel = Wheel(),
                val wheelCount: Int = 0,
                val wheelDiameter: Float = 0f,
                val wheelWidth: Float = 0f,
            ) {
                data class Wheel(
                    val left: Left = Left(),
                    val right: Right = Right(),
                ) {
                    data class Left(
                        val angularSpeed: Float = 0f,
                        val brake: Brake = Brake(),
                        val speed: Float = 0f,
                        val tire: Tire = Tire(),
                    ) {
                        data class Brake(
                            val fluidLevel: Int = 0,
                            val isBrakesWorn: Boolean = false,
                            val isFluidLevelLow: Boolean = false,
                            val padWear: Int = 0,
                        )

                        data class Tire(
                            val airTemperature: Float = 0f,
                            val isPressureLow: Boolean = false,
                            val pressure: Int = 0,
                            val rubberTemperature: Float = 0f,
                            val temperature: Float = 0f,
                        )
                    }

                    data class Right(
                        val angularSpeed: Float = 0f,
                        val brake: Brake = Brake(),
                        val speed: Float = 0f,
                        val tire: Tire = Tire(),
                    ) {
                        data class Brake(
                            val fluidLevel: Int = 0,
                            val isBrakesWorn: Boolean = false,
                            val isFluidLevelLow: Boolean = false,
                            val padWear: Int = 0,
                        )

                        data class Tire(
                            val airTemperature: Float = 0f,
                            val isPressureLow: Boolean = false,
                            val pressure: Int = 0,
                            val rubberTemperature: Float = 0f,
                            val temperature: Float = 0f,
                        )
                    }
                }
            }
        }

        data class Brake(
            val isDriverEmergencyBrakingDetected: Boolean = false,
            val pedalPosition: Int = 0,
        )

        data class ParkingBrake(
            val isAutoApplyEnabled: Boolean = false,
            val isEngaged: Boolean = false,
        )

        data class SteeringWheel(
            val angle: Int = 0,
            val extension: Int = 0,
            val heatingCooling: Int = 0,
            val tilt: Int = 0,
        )
    }

    data class Connectivity(
        val isConnectivityAvailable: Boolean = false,
    )

    data class ControlUnit(
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

    data class CurrentLocation(
        val altitude: Float = 0f,
        val gNSSReceiver: GNSSReceiver = GNSSReceiver(),
        val heading: Float = 0f,
        val horizontalAccuracy: Float = 0f,
        val latitude: Float = 0f,
        val longitude: Float = 0f,
        val timestamp: String = "",
        val verticalAccuracy: Float = 0f,
    ) {
        data class GNSSReceiver(
            val fixType: String = "",
            val mountingPosition: MountingPosition = MountingPosition(),
        ) {
            data class MountingPosition(
                val x: Int = 0,
                val y: Int = 0,
                val z: Int = 0,
            )
        }
    }

    data class Diagnostics(
        val dTCCount: Int = 0,
        val dTCList: List<String> = emptyList(),
    )

    data class Driver(
        val attentiveProbability: Float = 0f,
        val distractionLevel: Float = 0f,
        val fatigueLevel: Float = 0f,
        val heartRate: Int = 0,
        val isEyesOnRoad: Boolean = false,
        val isHandsOnWheel: Boolean = false,
    )

    data class Exterior(
        val airTemperature: Float = 0f,
        val humidity: Float = 0f,
        val lightIntensity: Float = 0f,
    )

    data class LowVoltageBattery(
        val currentCurrent: Float = 0f,
        val currentVoltage: Float = 0f,
        val nominalCapacity: Int = 0,
        val nominalVoltage: Int = 0,
    )

    data class MotionManagement(
        val brake: Brake = Brake(),
        val electricAxle: ElectricAxle = ElectricAxle(),
        val steering: Steering = Steering(),
        val suspension: Suspension = Suspension(),
    ) {
        data class Brake(
            val axle: Axle = Axle(),
            val vehicleForceDistributionFrontMaximum: Int = 0,
            val vehicleForceDistributionFrontMinimum: Int = 0,
            val vehicleForceElectric: Int = 0,
            val vehicleForceElectricMinimumArbitrated: Int = 0,
            val vehicleForceMaximum: Int = 0,
        ) {
            data class Axle(
                val row1: Row1 = Row1(),
                val row2: Row2 = Row2(),
            ) {
                data class Row1(
                    val torqueDistributionFrictionRightMaximum: Int = 0,
                    val torqueDistributionFrictionRightMinimum: Int = 0,
                    val torqueElectricMinimum: Int = 0,
                    val torqueFrictionDifferenceMaximum: Int = 0,
                    val wheel: Wheel = Wheel(),
                ) {
                    data class Wheel(
                        val left: Left = Left(),
                        val right: Right = Right(),
                    ) {
                        data class Left(
                            val omegaLower: Int = 0,
                            val omegaUpper: Int = 0,
                            val torque: Int = 0,
                            val torqueArbitrated: Int = 0,
                            val torqueFrictionMaximum: Int = 0,
                            val torqueFrictionMinimum: Int = 0,
                        )

                        data class Right(
                            val omegaLower: Int = 0,
                            val omegaUpper: Int = 0,
                            val torque: Int = 0,
                            val torqueArbitrated: Int = 0,
                            val torqueFrictionMaximum: Int = 0,
                            val torqueFrictionMinimum: Int = 0,
                        )
                    }
                }

                data class Row2(
                    val torqueDistributionFrictionRightMaximum: Int = 0,
                    val torqueDistributionFrictionRightMinimum: Int = 0,
                    val torqueElectricMinimum: Int = 0,
                    val torqueFrictionDifferenceMaximum: Int = 0,
                    val wheel: Wheel = Wheel(),
                ) {
                    data class Wheel(
                        val left: Left = Left(),
                        val right: Right = Right(),
                    ) {
                        data class Left(
                            val omegaLower: Int = 0,
                            val omegaUpper: Int = 0,
                            val torque: Int = 0,
                            val torqueArbitrated: Int = 0,
                            val torqueFrictionMaximum: Int = 0,
                            val torqueFrictionMinimum: Int = 0,
                        )

                        data class Right(
                            val omegaLower: Int = 0,
                            val omegaUpper: Int = 0,
                            val torque: Int = 0,
                            val torqueArbitrated: Int = 0,
                            val torqueFrictionMaximum: Int = 0,
                            val torqueFrictionMinimum: Int = 0,
                        )
                    }
                }
            }
        }

        data class ElectricAxle(
            val row1: Row1 = Row1(),
            val row2: Row2 = Row2(),
        ) {
            data class Row1(
                val rotationalSpeed: Int = 0,
                val rotationalSpeedMaximumLimit: Int = 0,
                val rotationalSpeedMinimumLimit: Int = 0,
                val rotationalSpeedTarget: Int = 0,
                val torque: Int = 0,
                val torqueMaximum: Int = 0,
                val torqueMaximumLimit: Int = 0,
                val torqueMinimum: Int = 0,
                val torqueMinimumLimit: Int = 0,
                val torqueTarget: Int = 0,
            )

            data class Row2(
                val rotationalSpeed: Int = 0,
                val rotationalSpeedMaximumLimit: Int = 0,
                val rotationalSpeedMinimumLimit: Int = 0,
                val rotationalSpeedTarget: Int = 0,
                val torque: Int = 0,
                val torqueMaximum: Int = 0,
                val torqueMaximumLimit: Int = 0,
                val torqueMinimum: Int = 0,
                val torqueMinimumLimit: Int = 0,
                val torqueTarget: Int = 0,
            )
        }

        data class Steering(
            val axle: Axle = Axle(),
            val steeringWheel: SteeringWheel = SteeringWheel(),
        ) {
            data class Axle(
                val row1: Row1 = Row1(),
                val row2: Row2 = Row2(),
            ) {
                data class Row1(
                    val positionOffsetTargetMode: Int = 0,
                    val positionTargetMode: Int = 0,
                    val rackPosition: Int = 0,
                    val rackPositionOffsetTarget: Int = 0,
                    val rackPositionTarget: Int = 0,
                    val steerAngle: Int = 0,
                    val steerAngleOffsetTarget: Int = 0,
                    val steerAngleTarget: Int = 0,
                )

                data class Row2(
                    val steerAngle: Int = 0,
                    val steerAngleTarget: Int = 0,
                    val steerAngleVelocityTarget: Int = 0,
                )
            }

            data class SteeringWheel(
                val angle: Int = 0,
                val angleTarget: Int = 0,
                val angleTargetMode: Int = 0,
                val torque: Int = 0,
                val torqueOffsetTarget: Int = 0,
                val torqueOffsetTargetMode: Int = 0,
                val torqueTarget: Int = 0,
                val torqueTargetMode: Int = 0,
            )
        }

        data class Suspension(
            val axle: Axle = Axle(),
            val dampingPrioTarget: Int = 0,
            val rollPrioTarget: Int = 0,
            val rollTorqueDistributionFrontMaximum: Int = 0,
            val rollTorqueDistributionFrontMinimum: Int = 0,
            val rollTorqueTarget: Int = 0,
        ) {
            data class Axle(
                val row1: Row1 = Row1(),
                val row2: Row2 = Row2(),
            ) {
                data class Row1(
                    val rollTorque: Int = 0,
                    val wheel: Wheel = Wheel(),
                ) {
                    data class Wheel(
                        val left: Left = Left(),
                        val right: Right = Right(),
                    ) {
                        data class Left(
                            val dampingForce: Int = 0,
                            val dampingForceTarget: Int = 0,
                            val dampingRate: Int = 0,
                            val dampingRateTarget: Int = 0,
                        )

                        data class Right(
                            val dampingForce: Int = 0,
                            val dampingForceTarget: Int = 0,
                            val dampingRate: Int = 0,
                            val dampingRateTarget: Int = 0,
                        )
                    }
                }

                data class Row2(
                    val rollTorque: Int = 0,
                    val wheel: Wheel = Wheel(),
                ) {
                    data class Wheel(
                        val left: Left = Left(),
                        val right: Right = Right(),
                    ) {
                        data class Left(
                            val dampingForce: Int = 0,
                            val dampingForceTarget: Int = 0,
                            val dampingRate: Int = 0,
                            val dampingRateTarget: Int = 0,
                        )

                        data class Right(
                            val dampingForce: Int = 0,
                            val dampingForceTarget: Int = 0,
                            val dampingRate: Int = 0,
                            val dampingRateTarget: Int = 0,
                        )
                    }
                }
            }
        }
    }

    data class Occupant(
        val row1: Row1 = Row1(),
        val row2: Row2 = Row2(),
    ) {
        data class Row1(
            val driverSide: DriverSide = DriverSide(),
            val middle: Middle = Middle(),
            val passengerSide: PassengerSide = PassengerSide(),
        ) {
            data class DriverSide(
                val headPosition: HeadPosition = HeadPosition(),
                val identifier: Identifier = Identifier(),
                val midEyeGaze: MidEyeGaze = MidEyeGaze(),
            ) {
                data class HeadPosition(
                    val pitch: Float = 0f,
                    val roll: Float = 0f,
                    val x: Int = 0,
                    val y: Int = 0,
                    val yaw: Float = 0f,
                    val z: Int = 0,
                )

                data class Identifier(
                    val issuer: String = "",
                    val subject: String = "",
                )

                data class MidEyeGaze(
                    val azimuth: Float = 0f,
                    val elevation: Float = 0f,
                )
            }

            data class Middle(
                val headPosition: HeadPosition = HeadPosition(),
                val identifier: Identifier = Identifier(),
                val midEyeGaze: MidEyeGaze = MidEyeGaze(),
            ) {
                data class HeadPosition(
                    val pitch: Float = 0f,
                    val roll: Float = 0f,
                    val x: Int = 0,
                    val y: Int = 0,
                    val yaw: Float = 0f,
                    val z: Int = 0,
                )

                data class Identifier(
                    val issuer: String = "",
                    val subject: String = "",
                )

                data class MidEyeGaze(
                    val azimuth: Float = 0f,
                    val elevation: Float = 0f,
                )
            }

            data class PassengerSide(
                val headPosition: HeadPosition = HeadPosition(),
                val identifier: Identifier = Identifier(),
                val midEyeGaze: MidEyeGaze = MidEyeGaze(),
            ) {
                data class HeadPosition(
                    val pitch: Float = 0f,
                    val roll: Float = 0f,
                    val x: Int = 0,
                    val y: Int = 0,
                    val yaw: Float = 0f,
                    val z: Int = 0,
                )

                data class Identifier(
                    val issuer: String = "",
                    val subject: String = "",
                )

                data class MidEyeGaze(
                    val azimuth: Float = 0f,
                    val elevation: Float = 0f,
                )
            }
        }

        data class Row2(
            val driverSide: DriverSide = DriverSide(),
            val middle: Middle = Middle(),
            val passengerSide: PassengerSide = PassengerSide(),
        ) {
            data class DriverSide(
                val headPosition: HeadPosition = HeadPosition(),
                val identifier: Identifier = Identifier(),
                val midEyeGaze: MidEyeGaze = MidEyeGaze(),
            ) {
                data class HeadPosition(
                    val pitch: Float = 0f,
                    val roll: Float = 0f,
                    val x: Int = 0,
                    val y: Int = 0,
                    val yaw: Float = 0f,
                    val z: Int = 0,
                )

                data class Identifier(
                    val issuer: String = "",
                    val subject: String = "",
                )

                data class MidEyeGaze(
                    val azimuth: Float = 0f,
                    val elevation: Float = 0f,
                )
            }

            data class Middle(
                val headPosition: HeadPosition = HeadPosition(),
                val identifier: Identifier = Identifier(),
                val midEyeGaze: MidEyeGaze = MidEyeGaze(),
            ) {
                data class HeadPosition(
                    val pitch: Float = 0f,
                    val roll: Float = 0f,
                    val x: Int = 0,
                    val y: Int = 0,
                    val yaw: Float = 0f,
                    val z: Int = 0,
                )

                data class Identifier(
                    val issuer: String = "",
                    val subject: String = "",
                )

                data class MidEyeGaze(
                    val azimuth: Float = 0f,
                    val elevation: Float = 0f,
                )
            }

            data class PassengerSide(
                val headPosition: HeadPosition = HeadPosition(),
                val identifier: Identifier = Identifier(),
                val midEyeGaze: MidEyeGaze = MidEyeGaze(),
            ) {
                data class HeadPosition(
                    val pitch: Float = 0f,
                    val roll: Float = 0f,
                    val x: Int = 0,
                    val y: Int = 0,
                    val yaw: Float = 0f,
                    val z: Int = 0,
                )

                data class Identifier(
                    val issuer: String = "",
                    val subject: String = "",
                )

                data class MidEyeGaze(
                    val azimuth: Float = 0f,
                    val elevation: Float = 0f,
                )
            }
        }
    }

    data class Powertrain(
        val accumulatedBrakingEnergy: Float = 0f,
        val combustionEngine: CombustionEngine = CombustionEngine(),
        val electricMotor: ElectricMotor = ElectricMotor(),
        val fuelSystem: FuelSystem = FuelSystem(),
        val isAutoPowerOptimize: Boolean = false,
        val powerOptimizeLevel: Int = 0,
        val range: Int = 0,
        val rangeExtender: RangeExtender = RangeExtender(),
        val timeRemaining: Int = 0,
        val tractionBattery: TractionBattery = TractionBattery(),
        val transmission: Transmission = Transmission(),
        val type: String = "",
    ) {
        data class CombustionEngine(
            val aspirationType: String = "UNKNOWN",
            val bore: Float = 0f,
            val compressionRatio: String = "",
            val configuration: String = "UNKNOWN",
            val dieselExhaustFluid: DieselExhaustFluid = DieselExhaustFluid(),
            val dieselParticulateFilter: DieselParticulateFilter = DieselParticulateFilter(),
            val displacement: Int = 0,
            val eop: Int = 0,
            val engineCode: String = "",
            val engineCoolant: EngineCoolant = EngineCoolant(),
            val engineHours: Float = 0f,
            val engineOil: EngineOil = EngineOil(),
            val idleHours: Float = 0f,
            val isRunning: Boolean = false,
            val maf: Int = 0,
            val map: Int = 0,
            val maxPower: Int = 0,
            val maxTorque: Int = 0,
            val numberOfCylinders: Int = 0,
            val numberOfValvesPerCylinder: Int = 0,
            val power: Int = 0,
            val speed: Float = 0f,
            val strokeLength: Float = 0f,
            val tps: Int = 0,
            val torque: Int = 0,
        ) {
            data class DieselExhaustFluid(
                val capacity: Float = 0f,
                val isLevelLow: Boolean = false,
                val level: Int = 0,
                val range: Int = 0,
            )

            data class DieselParticulateFilter(
                val deltaPressure: Float = 0f,
                val inletTemperature: Float = 0f,
                val outletTemperature: Float = 0f,
            )

            data class EngineCoolant(
                val capacity: Float = 0f,
                val level: String = "",
                val lifeRemaining: Int = 0,
                val temperature: Float = 0f,
            )

            data class EngineOil(
                val capacity: Float = 0f,
                val level: String = "",
                val lifeRemaining: Int = 0,
                val temperature: Float = 0f,
            )
        }

        data class ElectricMotor(
            val engineCode: String = "",
            val engineCoolant: EngineCoolant = EngineCoolant(),
            val maxPower: Int = 0,
            val maxRegenPower: Int = 0,
            val maxRegenTorque: Int = 0,
            val maxTorque: Int = 0,
            val power: Int = 0,
            val speed: Float = 0f,
            val temperature: Float = 0f,
            val timeInUse: Float = 0f,
            val torque: Int = 0,
        ) {
            data class EngineCoolant(
                val capacity: Float = 0f,
                val level: String = "",
                val lifeRemaining: Int = 0,
                val temperature: Float = 0f,
            )
        }

        data class FuelSystem(
            val absoluteLevel: Float = 0f,
            val afterRefuelingFuelEconomy: Float = 0f,
            val averageConsumption: Float = 0f,
            val consumptionSinceLastRefuel: Float = 0f,
            val consumptionSinceStart: Float = 0f,
            val cumulativeFuelEconomy: Float = 0f,
            val driveFuelEconomy: Float = 0f,
            val hybridType: String = "UNKNOWN",
            val instantConsumption: Float = 0f,
            val instantantFuelEconomy: Float = 0f,
            val isEngineStopStartEnabled: Boolean = false,
            val isFuelLevelEmpty: Boolean = false,
            val isFuelLevelLow: Boolean = false,
            val isFuelPortFlapOpen: Boolean = false,
            val range: Int = 0,
            val refuelPortPosition: List<String> = emptyList(),
            val relativeLevel: Int = 0,
            val supportedFuel: List<String> = emptyList(),
            val supportedFuelTypes: List<String> = emptyList(),
            val tankCapacity: Float = 0f,
            val timeRemaining: Int = 0,
        )

        data class RangeExtender(
            val chargeDepleting: ChargeDepleting = ChargeDepleting(),
            val chargeSustaining: ChargeSustaining = ChargeSustaining(),
            val combinedFuelEconomy: Float = 0f,
            val operatingMode: String = "",
        ) {
            data class ChargeDepleting(
                val energyConsumption: Float = 0f,
                val range: Int = 0,
            )

            data class ChargeSustaining(
                val fuelEconomy: Float = 0f,
                val range: Int = 0,
            )
        }

        data class TractionBattery(
            val accumulatedChargedEnergy: Float = 0f,
            val accumulatedChargedThroughput: Float = 0f,
            val accumulatedConsumedEnergy: Float = 0f,
            val accumulatedConsumedThroughput: Float = 0f,
            val batteryConditioning: BatteryConditioning = BatteryConditioning(),
            val cellVoltage: CellVoltage = CellVoltage(),
            val charging: Charging = Charging(),
            val currentCurrent: Float = 0f,
            val currentPower: Float = 0f,
            val currentVoltage: Float = 0f,
            val dcdc: DCDC = DCDC(),
            val errorCodes: List<String> = emptyList(),
            val grossCapacity: Int = 0,
            val id: String = "",
            val isGroundConnected: Boolean = false,
            val isPowerConnected: Boolean = false,
            val maxVoltage: Int = 0,
            val netCapacity: Int = 0,
            val nominalVoltage: Int = 0,
            val powerLoss: Float = 0f,
            val productionDate: String = "",
            val range: Int = 0,
            val stateOfCharge: StateOfCharge = StateOfCharge(),
            val stateOfHealth: Float = 0f,
            val temperature: Temperature = Temperature(),
            val timeRemaining: Int = 0,
        ) {
            data class BatteryConditioning(
                val isActive: Boolean = false,
                val isOngoing: Boolean = false,
                val requestedMode: String = "",
                val startTime: String = "",
                val targetTemperature: Float = 0f,
                val targetTime: String = "",
            )

            data class CellVoltage(
                val cellVoltages: List<Float> = emptyList(),
                val idMax: Int = 0,
                val idMin: Int = 0,
                val max: Float = 0f,
                val min: Float = 0f,
            )

            data class Charging(
                val averagePower: Float = 0f,
                val chargeCurrent: ChargeCurrent = ChargeCurrent(),
                val chargeLimit: Int = 100,
                val chargeRate: Float = 0f,
                val chargeVoltage: ChargeVoltage = ChargeVoltage(),
                val chargingPort: ChargingPort = ChargingPort(),
                val evseId: String = "",
                val isCharging: Boolean = false,
                val isDischarging: Boolean = false,
                val location: Location = Location(),
                val maxPower: Float = 0f,
                val maximumChargingCurrent: MaximumChargingCurrent = MaximumChargingCurrent(),
                val powerLoss: Float = 0f,
                val startStopCharging: String = "",
                val temperature: Float = 0f,
                val timeToComplete: Int = 0,
                val timer: Timer = Timer(),
            ) {
                data class ChargeCurrent(
                    val dc: Float = 0f,
                    val phase1: Float = 0f,
                    val phase2: Float = 0f,
                    val phase3: Float = 0f,
                )

                data class ChargeVoltage(
                    val dc: Float = 0f,
                    val phase1: Float = 0f,
                    val phase2: Float = 0f,
                    val phase3: Float = 0f,
                )

                data class ChargingPort(
                    val anyPosition: AnyPosition = AnyPosition(),
                    val frontLeft: FrontLeft = FrontLeft(),
                    val frontMiddle: FrontMiddle = FrontMiddle(),
                    val frontRight: FrontRight = FrontRight(),
                    val rearLeft: RearLeft = RearLeft(),
                    val rearMiddle: RearMiddle = RearMiddle(),
                    val rearRight: RearRight = RearRight(),
                ) {
                    data class AnyPosition(
                        val isChargingCableConnected: Boolean = false,
                        val isChargingCableLocked: Boolean = false,
                        val isFlapOpen: Boolean = false,
                        val supportedInletTypes: List<String> = emptyList(),
                    )

                    data class FrontLeft(
                        val isChargingCableConnected: Boolean = false,
                        val isChargingCableLocked: Boolean = false,
                        val isFlapOpen: Boolean = false,
                        val supportedInletTypes: List<String> = emptyList(),
                    )

                    data class FrontMiddle(
                        val isChargingCableConnected: Boolean = false,
                        val isChargingCableLocked: Boolean = false,
                        val isFlapOpen: Boolean = false,
                        val supportedInletTypes: List<String> = emptyList(),
                    )

                    data class FrontRight(
                        val isChargingCableConnected: Boolean = false,
                        val isChargingCableLocked: Boolean = false,
                        val isFlapOpen: Boolean = false,
                        val supportedInletTypes: List<String> = emptyList(),
                    )

                    data class RearLeft(
                        val isChargingCableConnected: Boolean = false,
                        val isChargingCableLocked: Boolean = false,
                        val isFlapOpen: Boolean = false,
                        val supportedInletTypes: List<String> = emptyList(),
                    )

                    data class RearMiddle(
                        val isChargingCableConnected: Boolean = false,
                        val isChargingCableLocked: Boolean = false,
                        val isFlapOpen: Boolean = false,
                        val supportedInletTypes: List<String> = emptyList(),
                    )

                    data class RearRight(
                        val isChargingCableConnected: Boolean = false,
                        val isChargingCableLocked: Boolean = false,
                        val isFlapOpen: Boolean = false,
                        val supportedInletTypes: List<String> = emptyList(),
                    )
                }

                data class Location(
                    val altitude: Float = 0f,
                    val latitude: Float = 0f,
                    val longitude: Float = 0f,
                )

                data class MaximumChargingCurrent(
                    val dc: Float = 0f,
                    val phase1: Float = 0f,
                    val phase2: Float = 0f,
                    val phase3: Float = 0f,
                )

                data class Timer(
                    val mode: String = "",
                    val time: String = "",
                )
            }

            data class DCDC(
                val powerLoss: Float = 0f,
                val temperature: Float = 0f,
            )

            data class StateOfCharge(
                val current: Float = 0f,
                val currentEnergy: Float = 0f,
                val displayed: Float = 0f,
            )

            data class Temperature(
                val average: Float = 0f,
                val cellTemperature: List<Float> = emptyList(),
                val max: Float = 0f,
                val min: Float = 0f,
            )
        }

        data class Transmission(
            val clutchEngagement: Float = 0f,
            val clutchWear: Int = 0,
            val currentGear: Int = 0,
            val diffLockFrontEngagement: Float = 0f,
            val diffLockRearEngagement: Float = 0f,
            val driveType: String = "UNKNOWN",
            val gearChangeMode: String = "",
            val gearCount: Int = 0,
            val isElectricalPowertrainEngaged: Boolean = false,
            val isLowRangeEngaged: Boolean = false,
            val isParkLockEngaged: Boolean = false,
            val performanceMode: String = "",
            val selectedGear: Int = 0,
            val temperature: Float = 0f,
            val torqueDistribution: Float = 0f,
            val travelledDistance: Float = 0f,
            val type: String = "UNKNOWN",
        )
    }

    data class Service(
        val distanceToService: Float = 0f,
        val isServiceDue: Boolean = false,
        val timeToService: Int = 0,
    )

    data class Trailer(
        val isConnected: Boolean = false,
    )

    data class VehicleIdentification(
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

    data class VersionVSS(
        val label: String = "",
        val major: Int = 6,
        val minor: Int = 0,
        val patch: Int = 0,
    )
}
