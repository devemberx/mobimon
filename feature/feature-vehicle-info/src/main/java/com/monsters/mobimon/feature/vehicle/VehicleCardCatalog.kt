package com.monsters.mobimon.feature.vehicle

import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.VehicleSnapshot

internal data class VehicleCardSpec(
    val id: String,
    val title: String,
    val supporting: String,
    val vssPaths: List<String>,
)

internal data class VehicleCardReading(
    val value: String?,
    val supporting: String,
)

/** The catalog names VSS candidates. Only values carried by a valid snapshot are displayed. */
internal object VehicleCardCatalog {
    private const val BATTERY = "Vehicle.Powertrain.TractionBattery."
    private const val CHASSIS = "Vehicle.Chassis."
    private val wheels = listOf("Row1.Wheel.Left", "Row1.Wheel.Right", "Row2.Wheel.Left", "Row2.Wheel.Right")

    private fun card(
        id: String,
        title: String,
        supporting: String,
        path: String,
    ) = VehicleCardSpec(id, title, supporting, listOf(path))

    private fun fourWheels(
        id: String,
        title: String,
        supporting: String,
        suffix: String,
    ) = VehicleCardSpec(id, title, supporting, wheels.map { "$CHASSIS" + "Axle.$it.$suffix" })

    val cards: List<VehicleCardSpec> =
        listOf(
            card("battery", "배터리 잔량", "구동 배터리 표시 잔량", "${BATTERY}StateOfCharge.Displayed"),
            card("battery-health", "배터리 건강도", "표준 조건에서 계산한 건강도", "${BATTERY}StateOfHealth"),
            card("battery-range", "주행 가능 거리", "배터리 기반 남은 거리", "${BATTERY}Range"),
            card("battery-time", "배터리 남은 시간", "방전까지 남은 예상 시간", "${BATTERY}TimeRemaining"),
            card("battery-error", "배터리 오류", "배터리 진단 코드", "${BATTERY}ErrorCodes"),
            card("driver-door", "운전석 문 잠금", "문 잠금 상태", "Vehicle.Cabin.Door.Row1.DriverSide.IsLocked"),
            card("service-due", "정비 필요", "차량 정비 알림", "Vehicle.Service.IsServiceDue"),
            card("charging-time", "충전 완료 예상", "설정한 충전 목표까지", "${BATTERY}Charging.TimeToComplete"),
            card("service-distance", "정비까지 거리", "다음 정비까지 남은 거리", "Vehicle.Service.DistanceToService"),
            card("service-time", "정비까지 시간", "다음 정비까지 남은 시간", "Vehicle.Service.TimeToService"),
            fourWheels("brake-fluid", "브레이크액 부족", "네 바퀴 부족 신호 종합", "Brake.IsFluidLevelLow"),
            card("low-beam", "하향등", "램프 고장 여부", "Vehicle.Body.Lights.Beam.Low.IsDefect"),
            card("brake-light", "브레이크등", "램프 고장 여부", "Vehicle.Body.Lights.Brake.IsDefect"),
            card("parking-brake", "주차 브레이크", "주차 브레이크 상태", "${CHASSIS}ParkingBrake.IsEngaged"),
            fourWheels("tire-low", "타이어 저압 경고", "네 바퀴 저압 신호 종합", "Tire.IsPressureLow"),
            card("driver-belt", "운전석 안전벨트", "안전벨트 착용 상태", "Vehicle.Cabin.Seat.Row1.DriverSide.IsBelted"),
            fourWheels("pad-wear", "브레이크 패드 마모", "네 바퀴 중 최대 마모율", "Brake.PadWear"),
            fourWheels("pad-warning", "패드 마모 경고", "네 바퀴 마모 신호 종합", "Brake.IsBrakesWorn"),
            card("abs", "ABS 점검", "제동 보조 시스템 오류 여부", "Vehicle.ADAS.ABS.IsError"),
            card("hood", "보닛", "보닛 개폐 상태", "Vehicle.Body.Hood.IsOpen"),
            card("trunk", "트렁크", "뒤 트렁크 개폐 상태", "Vehicle.Body.Trunk.Rear.IsOpen"),
            card("washer-low", "워셔액 부족", "앞유리 워셔액 부족 신호", "Vehicle.Body.Windshield.Front.WasherFluid.IsLevelLow"),
            card("air-temperature", "외기 온도", "차량 외부 공기 온도", "Vehicle.Exterior.AirTemperature"),
            card("rain-intensity", "비 감지 강도", "빗물 감지 센서", "Vehicle.Body.Raindetection.Intensity"),
            card("cabin-temperature", "실내 온도", "현재 실내 공기 온도", "Vehicle.Cabin.HVAC.AmbientAirTemperature"),
            card("distance", "누적 주행거리", "차량 누적 이동 거리", "Vehicle.TraveledDistance"),
            card("dtc-count", "진단 코드 수", "차량 진단 코드 개수", "Vehicle.Diagnostics.DTCCount"),
            card("fatigue", "운전자 피로도", "운전자 피로 추정 수준", "Vehicle.Driver.FatigueLevel"),
            card("distraction", "운전자 주의분산", "운전자 주의 분산 추정 수준", "Vehicle.Driver.DistractionLevel"),
            card("breakdown", "차량 고장 감지", "도로 위 고장 감지 여부", "Vehicle.IsBrokenDown"),
        )

    private val initialOnly =
        listOf(
            card("charging", "충전 상태", "현재 충전 상태", "${BATTERY}Charging.IsCharging"),
            fourWheels("tire", "타이어 공기압", "확인된 공기압 상태", "Tire.IsPressureLow"),
            card("environment", "외부 환경", "외기 온도 및 비 감지", "Vehicle.Exterior.AirTemperature"),
            card("assist", "운전자 보조", "운전자 주의 상태", "Vehicle.Driver.Attentive"),
            card("washer", "워셔액", "앞유리 워셔액 잔량", "Vehicle.Body.Windshield.Front.WasherFluid.Level"),
        )

    val defaultSlots =
        listOf("battery", "charging", "tire", "washer", "environment", "assist")
            .map { id -> requireNotNull(find(id)) }

    fun find(id: String): VehicleCardSpec? = (cards + initialOnly).firstOrNull { it.id == id }

    fun reading(
        id: String,
        snapshot: VehicleSnapshot,
    ): VehicleCardReading? {
        val spec = find(id) ?: return null
        val current = snapshot.quality == SignalQuality.VALID
        val batteryCurrent = (snapshot.batteryQuality ?: snapshot.quality) == SignalQuality.VALID
        val value =
            when (id) {
                "battery" -> snapshot.batteryPercent?.takeIf { batteryCurrent && it in 0..100 }?.let { "$it%" }
                "charging" -> snapshot.isCharging?.takeIf { current }?.let { if (it) "충전 중" else "충전 안 함" }
                "tire" -> snapshot.tirePressureStatus?.takeIf { current && it.isNotBlank() }
                "tire-low" ->
                    snapshot.tirePressureStatus?.takeIf { current }?.let {
                        when (it) {
                            "NG" -> "경고 있음"
                            "OK" -> "경고 없음"
                            else -> null
                        }
                    }
                "environment", "air-temperature" -> snapshot.outsideTemperature?.takeIf { current }?.let { "$it°" }
                "assist" -> snapshot.attentionLevel?.takeIf { current && it in 0..100 }?.toString()
                "washer" -> snapshot.washerFluidLevel?.takeIf { current && it in 0..100 }?.let { "$it%" }
                else -> null
            }
        return VehicleCardReading(value, spec.supporting)
    }
}
