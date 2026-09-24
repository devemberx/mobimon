package com.monsters.mobimon.feature.quest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
    onShowAll: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
) {
    val isCompletedTab = selectedTab == QuestFilterTab.COMPLETED
    val title =
        stringResource(if (isCompletedTab) R.string.quest_empty_completed_title else R.string.quest_empty_ongoing_title)
    val subtitle =
        stringResource(
            if (isCompletedTab) R.string.quest_empty_completed_subtitle else R.string.quest_empty_ongoing_subtitle,
        )
    val panelModifier =
        modifier
            .clip(RoundedCornerShape(24.dp * scale))
            .background(Colors.panel)
            .testTag("quest-empty-state")

    if (isCompact) {
        Column(
            modifier = panelModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(painterResource(R.drawable.quest_empty_clipboard), null, Modifier.size(180.dp * scale))
            Spacer(Modifier.height(20.dp * scale))
            Text(
                title,
                style = questTextStyle(42f, scale, bold = true, color = Colors.text),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp * scale))
            Text(subtitle, style = questTextStyle(28f, scale, color = Colors.muted), textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp * scale))
            QuestEmptyShowAllButton(scale, onShowAll, Modifier.fillMaxWidth())
        }
    } else {
        Box(panelModifier) {
            Image(
                painter = painterResource(R.drawable.quest_empty_clipboard),
                contentDescription = null,
                modifier = Modifier.align(Alignment.TopCenter).offset(y = 70.dp * scale).size(360.dp * scale),
            )
            Text(
                text = title,
                style = questTextStyle(48f, scale, bold = true, color = Colors.text),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.TopCenter).offset(y = 458.dp * scale),
            )
            Text(
                text = subtitle,
                style = questTextStyle(34f, scale, color = Color(0xFF8496AC)),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.TopCenter).offset(y = 523.dp * scale),
            )
            QuestEmptyShowAllButton(
                scale = scale,
                onShowAll = onShowAll,
                modifier = Modifier.align(Alignment.BottomCenter).offset(y = -(40.dp * scale)).width(600.dp * scale),
            )
        }
    }
}

@Composable
private fun QuestEmptyShowAllButton(
    scale: Float,
    onShowAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .height(100.dp * scale)
                .clip(RoundedCornerShape(50.dp * scale))
                .background(Colors.button)
                .clickable(onClick = onShowAll)
                .testTag("quest-empty-show-all"),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.quest_empty_show_all),
            style = questTextStyle(38f, scale, bold = true, color = Colors.onButton),
        )
    }
}
