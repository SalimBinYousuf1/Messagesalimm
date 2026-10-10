package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Caption
import com.example.ui.theme.FrostAccentBlue
import com.example.ui.theme.FrostDarkBubbleReceived
import com.example.ui.theme.FrostDarkCanvas
import com.example.ui.theme.FrostDarkGlassBorder
import com.example.ui.theme.FrostDarkGlassFill
import com.example.ui.theme.FrostLightBubbleReceived
import com.example.ui.theme.FrostLightGlassBorder
import com.example.ui.theme.FrostLightGlassFill
import com.example.ui.theme.FrostWhiteCanvas

/**
 * Apple Liquid Glass Frost Surface Modifier:
 * 72% translucent acrylic fill with a subtle 1dp specular rim gradient and soft shadow.
 */
@Composable
fun Modifier.liquidGlassSurface(
    shape: Shape = RoundedCornerShape(22.dp),
    elevation: Dp = 8.dp
): Modifier {
    val isDark = isSystemInDarkTheme()
    val glassFill = if (isDark) FrostDarkGlassFill else FrostLightGlassFill

    val rimGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x66FFFFFF), // Top specular rim
                Color(0x1AFFFFFF),
                Color(0x0DFFFFFF)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x99FFFFFF), // Top specular rim
                Color(0x33000000),
                Color(0x1A000000)
            )
        )
    }

    return this
        .shadow(elevation, shape, clip = false)
        .clip(shape)
        .background(glassFill)
        .border(1.dp, rimGradient, shape)
}

/**
 * Apple touch response: subtle 0.96 scale down on press with spring physics,
 * strictly replacing default platform ripple.
 */
@Composable
fun Modifier.springPressScale(
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "press_scale"
    )

    return this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

/**
 * Apple touch & hold response:
 * Smooth scale feedback on press, long-press detection (holding contact for 1.2s or standard hold)
 * with tactile haptic feedback, opening the theme-dependent Apple Action Sheet.
 */
@Composable
fun Modifier.appleHoldOrClick(
    onHold: () -> Unit,
    onClick: () -> Unit
): Modifier {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "hold_press_scale"
    )

    return this
        .scale(scale)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                },
                onLongPress = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onHold()
                },
                onTap = {
                    onClick()
                }
            )
        }
}

/**
 * Squircle bubble shape with consecutive sender flattening:
 * If consecutive from same sender, the inner shared corner is flattened (4dp),
 * while outer corners maintain full 18dp squircle curvature.
 */
fun getBubbleShape(
    isOutgoing: Boolean,
    isConsecutive: Boolean
): Shape {
    val fullRadius = 18.dp
    val flatRadius = 4.dp

    return if (isOutgoing) {
        RoundedCornerShape(
            topStart = fullRadius,
            topEnd = if (isConsecutive) flatRadius else fullRadius,
            bottomStart = fullRadius,
            bottomEnd = flatRadius
        )
    } else {
        RoundedCornerShape(
            topStart = if (isConsecutive) flatRadius else fullRadius,
            topEnd = fullRadius,
            bottomStart = flatRadius,
            bottomEnd = fullRadius
        )
    }
}

/**
 * Live Segment Counter Pill:
 * Displays characters remaining, total segments, and character set (GSM-7 vs UCS-2).
 */
@Composable
fun SegmentCounterPill(
    totalChars: Int,
    remainingInSegment: Int,
    segmentCount: Int,
    isUcs2: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val pillBg = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000)
    val textLabel = if (isUcs2) {
        "$remainingInSegment left • $segmentCount UCS-2"
    } else {
        if (segmentCount > 1) "$remainingInSegment/$segmentCount GSM" else "$remainingInSegment left"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(pillBg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = textLabel,
            style = Caption.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xCCFFFFFF) else Color(0x99000000)
            )
        )
    }
}

/**
 * Real Waveform Visualizer for decoded voice audio messages.
 * Highlights played bars in accent blue, unplayed bars in muted gray.
 */
@Composable
fun WaveformVisualizer(
    amplitudes: List<Int>,
    playbackProgress: Float,
    isOutgoing: Boolean,
    modifier: Modifier = Modifier
) {
    val bars = if (amplitudes.isNotEmpty()) amplitudes else listOf(20, 35, 55, 75, 90, 60, 45, 80, 70, 40, 25, 50, 65, 30)
    val playedCount = (bars.size * playbackProgress).toInt().coerceIn(0, bars.size)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        bars.forEachIndexed { index, amp ->
            val isPlayed = index <= playedCount
            val barHeightFraction = (amp / 100f).coerceIn(0.15f, 1f)
            val barColor = if (isOutgoing) {
                if (isPlayed) Color.White else Color(0x80FFFFFF)
            } else {
                if (isPlayed) FrostAccentBlue else Color(0x4D000000)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(barHeightFraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
    }
}

/**
 * Reaction Pill Bar on message bubbles.
 */
@Composable
fun ReactionBar(
    reactions: List<com.example.data.local.entity.ReactionEntity>,
    modifier: Modifier = Modifier
) {
    if (reactions.isEmpty()) return

    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) FrostDarkGlassFill else FrostWhiteCanvas
    val border = if (isDark) FrostDarkGlassBorder else FrostLightGlassBorder

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val emojis = reactions.map { it.emoji }.distinct()
        emojis.forEach { emoji ->
            Text(text = emoji, fontSize = 13.sp)
        }
        if (reactions.size > 1) {
            Text(
                text = "${reactions.size}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color.Black
            )
        }
    }
}
