package com.monsters.mobimon.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monsters.mobimon.core.database.DebugPointRepository
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.debug.DebugVssState
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DebugOverlayEntryPoint {
    fun settingsRepository(): SettingsRepository

    fun debugStore(): com.monsters.mobimon.debug.DebugStore

    fun debugPoints(): DebugPointRepository
}

@Composable
fun DebugOverlay() {
    val context = LocalContext.current
    val entryPoint =
        remember(context) {
            try {
                EntryPointAccessors.fromApplication(context, DebugOverlayEntryPoint::class.java)
            } catch (
                e: Exception,
            ) {
                null
            }
        }
    if (entryPoint == null) return
    val settingsRepository = entryPoint.settingsRepository()
    val debugStore = entryPoint.debugStore()
    val debugPoints = entryPoint.debugPoints()

    val isDebugEnabledFlow =
        remember(settingsRepository) { settingsRepository.settings.map { it.debugModeEnabled } }
    val isDebugEnabled by isDebugEnabledFlow.collectAsStateWithLifecycle(initialValue = false)
    val state by debugStore.state.collectAsStateWithLifecycle()

    fun updateState(reducer: (DebugVssState) -> DebugVssState) {
        debugStore.updateState(reducer)
    }

    if (isDebugEnabled) {
        val scope = rememberCoroutineScope()
        var offsetX by remember { mutableStateOf(50f) }
        var offsetY by remember { mutableStateOf(100f) }
        var pointUnitText by remember { mutableStateOf("100") }
        val pointUnit = pointUnitText.toIntOrNull() ?: 0

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier =
                    Modifier
                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                        .width(440.dp)
                        .height(600.dp)
                        .background(Color(0xFF091525), RoundedCornerShape(12.dp))
                        .border(2.dp, Color(0xFF142A42), RoundedCornerShape(12.dp))
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            }
                        },
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        "디버깅 모드 (테스트 신호)",
                        color = Color(0xFFF4F7FC),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    // App State / Points
                    DebugSection("Point") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1.2f),
                            ) {
                                Text("+/-", color = Color.White, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .background(Color(0xFF203C58), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (pointUnitText.isEmpty()) {
                                        Text(
                                            text = "0",
                                            color = Color(0xFF627D98),
                                            fontSize = 12.sp,
                                        )
                                    }
                                    BasicTextField(
                                        value = pointUnitText,
                                        onValueChange = { newText ->
                                            if (newText.isEmpty()) {
                                                pointUnitText = ""
                                            } else {
                                                val newVal = newText.toIntOrNull()
                                                if (newVal != null && newVal in 0..1000) {
                                                    pointUnitText =
                                                        if (pointUnitText == "0" &&
                                                            newText.length == 2 &&
                                                            newText.startsWith("0")
                                                        ) {
                                                            newText.drop(1)
                                                        } else {
                                                            newText
                                                        }
                                                }
                                            }
                                        },
                                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                                        cursorBrush = SolidColor(Color(0xFF71E5C5)),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.testTag("debug-point-unit"),
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (pointUnit == 0) return@Button
                                    scope.launch { debugPoints.add(pointUnit.toLong()) }
                                },
                                modifier = Modifier.weight(0.8f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF203C58)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            ) { Text("+", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }

                            Button(
                                onClick = {
                                    if (pointUnit == 0) return@Button
                                    scope.launch { debugPoints.subtract(pointUnit.toLong()) }
                                },
                                modifier = Modifier.weight(0.8f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF203C58)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            ) { Text("-", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }

                            Button(
                                onClick = {
                                    scope.launch { debugPoints.reset() }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF802020)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            ) { Text("Reset", color = Color.White, fontSize = 12.sp) }
                        }
                    }

                    Text(
                        "차량 신호 (VSS)",
                        color = Color(0xFFF4F7FC),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    DebugSection("A. Driver State") {
                        DebugToggleRow(
                            "isDistracted",
                            state.isDistracted,
                        ) { v -> updateState { it.copy(isDistracted = v) } }
                        DebugToggleRow("isDrowsy", state.isDrowsy) { v -> updateState { it.copy(isDrowsy = v) } }
                        DebugInputRow("attentionLevel", state.attentionLevel.toString()) { v ->
                            updateState {
                                it.copy(
                                    attentionLevel = v.toIntOrNull() ?: 0,
                                )
                            }
                        }
                    }

                    DebugSection("B. Safety/ADAS") {
                        DebugToggleRow(
                            "isEmergencyBraking",
                            state.isEmergencyBraking,
                        ) { v -> updateState { it.copy(isEmergencyBraking = v) } }
                        DebugInputRow("distanceToFrontVehicle", state.distanceToFrontVehicle.toString()) { v ->
                            updateState {
                                it.copy(
                                    distanceToFrontVehicle = v.toIntOrNull() ?: 0,
                                )
                            }
                        }
                    }

                    DebugSection("C. Energy") {
                        DebugToggleRow("isCharging", state.isCharging) { v -> updateState { it.copy(isCharging = v) } }
                        DebugInputRow("batteryPercent", state.batteryPercent.toString()) { v ->
                            updateState {
                                it.copy(
                                    batteryPercent = v.toIntOrNull() ?: 0,
                                )
                            }
                        }
                    }

                    DebugSection("D. Environment") {
                        DebugToggleRow("isRaining", state.isRaining) { v -> updateState { it.copy(isRaining = v) } }
                        DebugInputRow("outsideTemperature", state.outsideTemperature.toString()) { v ->
                            updateState {
                                it.copy(
                                    outsideTemperature = v.toIntOrNull() ?: 0,
                                )
                            }
                        }
                    }

                    DebugSection("E. Consumables") {
                        DebugInputRow("washerFluidLevel", state.washerFluidLevel.toString()) { v ->
                            updateState {
                                it.copy(
                                    washerFluidLevel = v.toIntOrNull() ?: 0,
                                )
                            }
                        }
                    }

                    DebugSection("F. Vehicle Health") {
                        DebugToggleRow(
                            "isEngineWarning",
                            state.isEngineWarning,
                        ) { v -> updateState { it.copy(isEngineWarning = v) } }
                        DebugSegmentedRow(
                            "tirePressureStatus",
                            listOf("OK", "NG"),
                            state.tirePressureStatus,
                        ) { v ->
                            updateState {
                                it.copy(tirePressureStatus = v)
                            }
                        }
                    }

                    DebugSection("G. Driving / Activity") {
                        DebugToggleRow("isMoving", state.isMoving) { v -> updateState { it.copy(isMoving = v) } }
                        DebugInputRow("speed (km/h)", state.speed.toString()) { v ->
                            updateState {
                                it.copy(
                                    speed = v.toIntOrNull() ?: 0,
                                )
                            }
                        }
                        DebugSegmentedRow(
                            "gear (P/R/N/D)",
                            listOf("P", "R", "N", "D"),
                            state.gear,
                        ) { v -> updateState { it.copy(gear = v) } }
                    }

                    DebugSection("H. Direction") {
                        DebugToggleRow(
                            "isNavigating",
                            state.isNavigating,
                        ) { v -> updateState { it.copy(isNavigating = v) } }
                        DebugInputRow("distanceToDestination", state.distanceToDestination.toString()) { v ->
                            updateState {
                                it.copy(
                                    distanceToDestination = v.toIntOrNull() ?: 0,
                                )
                            }
                        }
                    }

                    DebugSection("I. Trip / Start") {
                        DebugToggleRow("isEngineOn", state.isEngineOn) { v -> updateState { it.copy(isEngineOn = v) } }
                    }
                }
            }
        }
    }
}

@Composable
fun DebugSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF142A42), RoundedCornerShape(8.dp))
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, color = Color(0xFF87DAF5), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        content()
    }
}

@Composable
fun DebugToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Color(0xFFFFFFFF), fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF71E5C5),
                ),
        )
    }
}

@Composable
fun DebugSegmentedRow(
    label: String,
    options: List<String>,
    selectedValue: String,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Color(0xFFFFFFFF), fontSize = 16.sp)
        Row(
            modifier = Modifier.background(Color(0xFF203C58), RoundedCornerShape(8.dp)),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            options.forEach { option ->
                val isSelected = option == selectedValue
                Box(
                    modifier =
                        Modifier
                            .clickable { onValueChange(option) }
                            .background(
                                if (isSelected) Color(0xFF71E5C5) else Color.Transparent,
                                RoundedCornerShape(8.dp),
                            ).padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = option,
                        color = if (isSelected) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun DebugInputRow(
    label: String,
    value: String,
    isNumber: Boolean = true,
    onValueChange: (String) -> Unit,
) {
    var text by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        if (isNumber) {
            val currentParsed = text.toIntOrNull() ?: 0
            val externalParsed = value.toIntOrNull() ?: 0
            if (currentParsed != externalParsed) {
                text = value
            }
        } else {
            if (text != value) {
                text = value
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Color(0xFFFFFFFF), fontSize = 16.sp)
        Box(
            modifier =
                Modifier
                    .width(100.dp)
                    .background(Color(0xFF203C58), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF42658A), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (isNumber && text.isEmpty()) {
                Text(
                    text = "0",
                    color = Color(0xFF627D98),
                    fontSize = 14.sp,
                )
            }
            BasicTextField(
                value = text,
                onValueChange = { newText ->
                    if (isNumber) {
                        if (newText.isNotEmpty() && newText != "-" && newText.toIntOrNull() == null) {
                            return@BasicTextField
                        }
                    }
                    val sanitized =
                        if (isNumber &&
                            text == "0" &&
                            newText.length == 2 &&
                            newText.startsWith("0") &&
                            newText[1].isDigit()
                        ) {
                            newText.drop(1)
                        } else {
                            newText
                        }
                    text = sanitized
                    onValueChange(sanitized)
                },
                textStyle =
                    TextStyle(
                        color = Color.White,
                        fontSize = 14.sp,
                    ),
                cursorBrush = SolidColor(Color(0xFF71E5C5)),
                keyboardOptions =
                    if (isNumber) {
                        KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                        )
                    } else {
                        KeyboardOptions.Default
                    },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("debug-input-$label"),
            )
        }
    }
}
