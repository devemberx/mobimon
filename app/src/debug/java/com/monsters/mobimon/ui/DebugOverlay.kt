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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monsters.mobimon.core.database.AppDatabase
import com.monsters.mobimon.core.domain.ProgressionIdentity
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.debug.DebugVssState
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DebugOverlayEntryPoint {
    fun settingsRepository(): SettingsRepository

    fun debugStore(): com.monsters.mobimon.debug.DebugStore

    fun appDatabase(): AppDatabase

    fun progressionIdentity(): ProgressionIdentity
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

    val isDebugEnabledFlow =
        remember(settingsRepository) { settingsRepository.settings.map { it.launcherCharacterEnabled } }
    val isDebugEnabled by isDebugEnabledFlow.collectAsStateWithLifecycle(initialValue = false)
    val state by debugStore.state.collectAsStateWithLifecycle()

    fun updateState(reducer: (DebugVssState) -> DebugVssState) {
        debugStore.updateState(reducer)
    }

    if (isDebugEnabled) {
        val scope = rememberCoroutineScope()
        var offsetX by remember { mutableStateOf(50f) }
        var offsetY by remember { mutableStateOf(100f) }
        var pointUnit by remember { mutableIntStateOf(100) }

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
                                    BasicTextField(
                                        value = pointUnit.toString(),
                                        onValueChange = {
                                            val newVal = it.toIntOrNull() ?: 0
                                            if (newVal in 1..1000) {
                                                pointUnit = newVal
                                            } else if (it.isEmpty()) {
                                                pointUnit = 0
                                            }
                                        },
                                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                                        cursorBrush = SolidColor(Color(0xFF71E5C5)),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (pointUnit == 0) return@Button
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            val dao = entryPoint.appDatabase().economyDao()
                                            val pid = entryPoint.progressionIdentity().profileId
                                            if (dao.account(pid) == null) {
                                                dao.insertAccount(
                                                    com.monsters.mobimon.core.database
                                                        .PointAccountEntity(pid, 0),
                                                )
                                            }
                                            dao.credit(pid, pointUnit.toLong(), Long.MAX_VALUE)
                                        } catch (e: Exception) {
                                            // ignore in debug
                                        }
                                    }
                                },
                                modifier = Modifier.weight(0.8f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF203C58)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            ) { Text("+", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }

                            Button(
                                onClick = {
                                    if (pointUnit == 0) return@Button
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            entryPoint.appDatabase().economyDao().debit(
                                                entryPoint.progressionIdentity().profileId,
                                                pointUnit.toLong(),
                                            )
                                        } catch (e: Exception) {
                                            // ignore in debug
                                        }
                                    }
                                },
                                modifier = Modifier.weight(0.8f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF203C58)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            ) { Text("-", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }

                            Button(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            val dao = entryPoint.appDatabase().economyDao()
                                            val pid = entryPoint.progressionIdentity().profileId
                                            val acc = dao.account(pid)
                                            if (acc != null && acc.balance > 0) {
                                                dao.debit(pid, acc.balance)
                                            }
                                        } catch (e: Exception) {
                                            // ignore in debug
                                        }
                                    }
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
                                    attentionLevel =
                                        v.toIntOrNull() ?: it.attentionLevel,
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
                                    distanceToFrontVehicle =
                                        v.toIntOrNull() ?: it.distanceToFrontVehicle,
                                )
                            }
                        }
                    }

                    DebugSection("C. Energy") {
                        DebugToggleRow("isCharging", state.isCharging) { v -> updateState { it.copy(isCharging = v) } }
                        DebugInputRow("batteryPercent", state.batteryPercent.toString()) { v ->
                            v.toIntOrNull()?.let { num -> updateState { it.copy(batteryPercent = num) } }
                        }
                    }

                    DebugSection("D. Environment") {
                        DebugToggleRow("isRaining", state.isRaining) { v -> updateState { it.copy(isRaining = v) } }
                        DebugInputRow("outsideTemperature", state.outsideTemperature.toString()) { v ->
                            updateState {
                                it.copy(
                                    outsideTemperature =
                                        v.toIntOrNull() ?: it.outsideTemperature,
                                )
                            }
                        }
                    }

                    DebugSection("E. Consumables") {
                        DebugInputRow("washerFluidLevel", state.washerFluidLevel.toString()) { v ->
                            updateState {
                                it.copy(
                                    washerFluidLevel =
                                        v.toIntOrNull() ?: it.washerFluidLevel,
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
                                    speed =
                                        v.toIntOrNull() ?: it.speed,
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
                                    distanceToDestination =
                                        v.toIntOrNull() ?: it.distanceToDestination,
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
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
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
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
