package com.monsters.mobimon.feature.auth

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/** The exported panels retain their horizontal corners while their vertical corners follow the available height. */
internal class ConversationPanelShape(
    private val horizontalRadiusFraction: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline =
        Outline.Rounded(
            RoundRect(
                Rect(0f, 0f, size.width, size.height),
                CornerRadius(size.width * horizontalRadiusFraction, size.height * 0.0483f),
            ),
        )
}
