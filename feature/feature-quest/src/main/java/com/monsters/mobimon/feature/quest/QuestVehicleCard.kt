package com.monsters.mobimon.feature.quest

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.presentation.parkedVerified
import com.monsters.mobimon.core.ui.MobiMonButton
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonSection

@Composable
fun QuestVehicleCard(
    snapshot: VehicleSnapshot,
    questActive: Boolean,
    questCompleted: Boolean,
    canAcknowledge: Boolean,
    onAcknowledge: (String) -> Unit,
    modifier: Modifier = Modifier,
    isBusy: Boolean = false,
    errorMessage: String? = null,
    legacyQuestVisible: Boolean = true,
) {
    val parked = snapshot.parkedVerified
    val enabled = questActive && !questCompleted && canAcknowledge && parked && !isBusy
    Column(modifier) {
        errorMessage?.let { MobiMonMessage(it, isError = true) }
        if (legacyQuestVisible) {
            MobiMonSection(title = stringResource(R.string.quest_vehicle_q01_title)) {
                Text(
                    stringResource(
                        when {
                            questCompleted -> R.string.quest_vehicle_q01_completed
                            !questActive -> R.string.quest_vehicle_q01_start_required
                            isBusy -> R.string.quest_vehicle_q01_saving
                            !canAcknowledge && parked -> R.string.quest_vehicle_q01_waiting
                            else -> R.string.quest_vehicle_q01_instruction
                        },
                    ),
                )
                MobiMonButton(
                    onClick = { onAcknowledge(snapshot.id) },
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.quest_vehicle_acknowledge))
                }
            }
        }
    }
}
