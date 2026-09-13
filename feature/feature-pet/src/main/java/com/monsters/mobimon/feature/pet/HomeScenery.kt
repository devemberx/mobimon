package com.monsters.mobimon.feature.pet

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser

/** Static paths exported from Figma P01 (70:30131), scaled to the available window. */
@Composable
internal fun HomeScenery(
    modifier: Modifier = Modifier,
    insetReference: Boolean = false,
) {
    val paths = remember { sceneryPaths.map { PathParser().parsePathString(it).toPath() } }
    Canvas(modifier) {
        scale(size.width / 2560f, size.height / if (insetReference) 1268f else 1440f, pivot = Offset.Zero) {
            translate(top = if (insetReference) -76f else 0f) {
                drawPath(
                    paths[0],
                    Brush.verticalGradient(
                        0f to Color(0xFF142A43),
                        0.62f to Color(0xFF2A4657),
                        1f to Color(0xFF496563),
                        endY = 1440f,
                    ),
                )
                scale(1f, 0.72f, pivot = Offset(1280f, 760f)) {
                    drawCircle(
                        Brush.radialGradient(
                            listOf(Color(0x459BBBA7), Color(0x00577574)),
                            center = Offset(1280f, 760f),
                            radius = 1000f,
                        ),
                        1000f,
                        Offset(1280f, 760f),
                    )
                }
                drawPath(paths[2], Color(0xFFDAD8BC), alpha = 0.65f)
                drawPath(paths[3], Color(0xFF415F5E))
                drawPath(
                    paths[4],
                    Brush.verticalGradient(
                        listOf(Color(0xFF4F6D64), Color(0xFF2E494A)),
                        startY = 848.179f,
                        endY = 1440f,
                    ),
                )
                drawPath(paths[5], Color(0xFF719084), alpha = 0.44f)
                drawPath(paths[6], Color(0xFF89A492), alpha = 0.28f)
                drawPath(paths[7], Color(0xFF759281))
                drawPath(
                    paths[8],
                    Color(0xFF759281),
                    style = Stroke(7f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
                drawPath(paths[9], Color(0xFF769384))
                drawPath(
                    paths[10],
                    Color(0xFF769384),
                    style = Stroke(6f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
        }
    }
}

private val sceneryPaths =
    listOf(
        "M2560 0H0V1440H2560V0Z",
        "M1280 1480C1832.28 1480 2280 1157.65 2280 760C2280 362.355 1832.28 40 1280 40C727.715 40 280 362.355 280 760C280 1157.65 727.715 1480 1280 1480Z",
        "M2156 516C2214.54 516 2262 468.542 2262 410C2262 351.458 2214.54 304 2156 304C2097.46 304 2050 351.458 2050 410C2050 468.542 2097.46 516 2156 516Z",
        "M0 769C254.667 649.667 490.333 654.333 707 783C923.667 911.667 1133.67 919.667 1337 807C1540.33 694.333 1758.33 643.667 1991 655C2223.67 666.333 2413.33 697.333 2560 748V1440H0V769Z",
        "M0 1004C188 891.333 396.333 883.667 625 981C853.667 1078.33 1058.67 1075.67 1240 973C1421.33 870.333 1644.67 831 1910 855C2175.33 879 2392 918.333 2560 973V1440H0V1004Z",
        "M1280 1127C1515.27 1127 1706 1089.84 1706 1044C1706 998.16 1515.27 961 1280 961C1044.73 961 854 998.16 854 1044C854 1089.84 1044.73 1127 1280 1127Z",
        "M1280 1077C1475.51 1077 1634 1055.06 1634 1028C1634 1000.94 1475.51 979 1280 979C1084.49 979 926 1000.94 926 1028C926 1055.06 1084.49 1077 1280 1077Z",
        "M245 1114C251.667 1056.67 237 1014 201 986L245 1114ZM249 1062C199 1070.67 179 1056.33 189 1019C211.667 1009 231.667 1023.33 249 1062ZM250 1081C289.333 1024.33 316.667 1008 332 1032C332.667 1070 305.333 1086.33 250 1081Z",
        "M245 1114C251.667 1056.67 237 1014 201 986M249 1062C199 1070.67 179 1056.33 189 1019C211.667 1009 231.667 1023.33 249 1062ZM250 1081C289.333 1024.33 316.667 1008 332 1032C332.667 1070 305.333 1086.33 250 1081Z",
        "M2300 1091C2289.33 1061 2291.33 1029.67 2306 997L2300 1091ZM2297 1065C2335.67 1035.67 2360.33 1032.33 2371 1055C2353 1081.67 2328.33 1085 2297 1065Z",
        "M2300 1091C2289.33 1061 2291.33 1029.67 2306 997M2297 1065C2335.67 1035.67 2360.33 1032.33 2371 1055C2353 1081.67 2328.33 1085 2297 1065Z",
    )
