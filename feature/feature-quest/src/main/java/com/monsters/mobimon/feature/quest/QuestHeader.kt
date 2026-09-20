package com.monsters.mobimon.feature.quest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.ui.MobiMonButton
import com.monsters.mobimon.core.ui.MobiMonButtonStyle
import com.monsters.mobimon.core.ui.MobiMonDimensions
import com.monsters.mobimon.core.ui.MobiMonColors as Colors

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
                            com.monsters.mobimon.core.ui.R.string.mobimon_back
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
                    text = stringResource(com.monsters.mobimon.core.ui.R.string.mobimon_home),
                    style = questTextStyle(28f, scale, bold = false, color = Colors.text),
                )
            }
            Spacer(Modifier.width(16.dp * scale))
        }

        Row(
            modifier =
                Modifier
                    .widthIn(min = 344.dp * scale)
                    .height(76.dp * scale)
                    .clip(RoundedCornerShape(38.dp * scale))
                    .background(Colors.panel)
                    .border(2.dp * scale, Colors.border, RoundedCornerShape(38.dp * scale))
                    .padding(horizontal = 24.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp * scale),
        ) {
            Image(
                painter = painterResource(R.drawable.quest_parking),
                contentDescription = null,
                modifier = Modifier.size(36.dp * scale),
                colorFilter = ColorFilter.tint(if (isParked) Colors.accent else Colors.warning),
            )
            Text(
                text =
                    stringResource(
                        if (isParked) R.string.quest_parking_confirmed else R.string.quest_parking_unconfirmed,
                    ),
                style =
                    questTextStyle(
                        30f,
                        scale,
                        bold = false,
                        color = if (isParked) Colors.accent else Colors.warning,
                    ),
            )
        }
    }
}
