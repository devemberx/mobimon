package com.monsters.mobimon.feature.quest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.ui.MobiMonButton
import com.monsters.mobimon.core.ui.MobiMonButtonStyle
import com.monsters.mobimon.core.ui.MobiMonDimensions
import com.monsters.mobimon.core.ui.MobiMonParkingBadge
import com.monsters.mobimon.core.ui.MobiMonColors as Colors
import com.monsters.mobimon.core.ui.R as CoreUiR

@Composable
internal fun QuestHeader(
    isParked: Boolean,
    friendId: String,
    scale: Float,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onHome: (() -> Unit)? = null,
    isDetail: Boolean = false,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val buttonSize = if (scale >= 0.7f) 104.dp * scale else MobiMonDimensions.touchTarget
        val iconSize = if (scale >= 0.7f) 40.dp * scale else 24.dp
        IconButton(
            onClick = onBack,
            modifier =
                Modifier
                    .size(buttonSize)
                    .background(Colors.panel, CircleShape)
                    .border(1.dp, Colors.border, CircleShape)
                    .testTag("quest-header-back-button"),
        ) {
            Icon(
                painter = painterResource(R.drawable.quest_icon_back),
                contentDescription =
                    stringResource(
                        if (isDetail) {
                            R.string.quest_back_to_list
                        } else {
                            CoreUiR.string.mobimon_back
                        },
                    ),
                tint = Colors.text,
                modifier = Modifier.size(iconSize),
            )
        }
        Spacer(Modifier.width(if (scale >= 0.7f) 32.dp * scale else 16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.quest_header_title),
                style = questTextStyle(46f, scale, bold = true, color = Colors.text),
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text =
                    stringResource(R.string.quest_header_subtitle).replace(
                        "모비",
                        if (friendId ==
                            "friend:luna"
                        ) {
                            "루나"
                        } else {
                            "모비"
                        },
                    ),
                style = questTextStyle(28f, scale, bold = false, color = Colors.muted),
            )
        }

        if (onHome != null) {
            val homeHeight = if (scale >= 0.7f) 76.dp * scale else MobiMonDimensions.touchTarget
            MobiMonButton(
                style = MobiMonButtonStyle.SECONDARY,
                onClick = onHome,
                modifier = Modifier.height(homeHeight).testTag("quest-header-home-button"),
            ) {
                Text(
                    text = stringResource(CoreUiR.string.mobimon_home),
                    style = questTextStyle(28f, scale, bold = false, color = Colors.text),
                )
            }
            Spacer(Modifier.width(16.dp * scale))
        }

        MobiMonParkingBadge(
            status =
                stringResource(
                    if (isParked) {
                        CoreUiR.string.mobimon_parking_confirmed
                    } else {
                        CoreUiR.string.mobimon_parking_unconfirmed
                    },
                ),
            scale = scale,
        )
    }
}
