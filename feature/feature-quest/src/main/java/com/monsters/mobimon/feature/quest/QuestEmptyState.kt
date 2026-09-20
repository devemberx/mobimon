package com.monsters.mobimon.feature.quest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.ui.MobiMonColors as Colors

@Composable
internal fun QuestEmptyStateCard(
    selectedTab: QuestFilterTab,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    val isCompletedTab = selectedTab == QuestFilterTab.COMPLETED
    val title =
        if (isCompletedTab) {
            stringResource(R.string.quest_empty_completed_title)
        } else {
            stringResource(R.string.quest_empty_ongoing_title)
        }
    val subtitle =
        if (isCompletedTab) {
            stringResource(R.string.quest_empty_completed_subtitle)
        } else {
            stringResource(R.string.quest_empty_ongoing_subtitle)
        }

    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(32.dp * scale))
                .background(Colors.panel)
                .padding(48.dp * scale)
                .testTag("quest-empty-state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.quest_empty_teong),
            contentDescription = null,
            modifier = Modifier.size(360.dp * scale),
        )
        Spacer(Modifier.height(32.dp * scale))
        Text(
            text = title,
            style = questTextStyle(48f, scale, bold = true, color = Colors.text),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(14.dp * scale))
        Text(
            text = subtitle,
            style = questTextStyle(34f, scale, bold = false, color = Color(0xFF8496AC)),
            textAlign = TextAlign.Center,
        )
    }
}
