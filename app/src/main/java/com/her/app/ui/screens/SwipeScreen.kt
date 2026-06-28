package com.her.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.her.app.data.MatchEngine
import com.her.app.data.Personality
import com.her.app.data.UserProfile
import com.her.app.ui.theme.DarkBg
import com.her.app.ui.theme.DarkBorder
import com.her.app.ui.theme.Saffron
import com.her.app.ui.theme.TextMuted
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeScreen(
    userProfile: UserProfile,
    onPersonalitySelected: (Personality) -> Unit
) {
    val matched = remember { MatchEngine.match(userProfile) }
    val deck = remember { mutableStateListOf(*matched.toTypedArray()) }
    var isEmpty by remember { mutableStateOf(false) }

    LaunchedEffect(deck.size) {
        isEmpty = deck.isEmpty()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SwipeTopBar(userName = userProfile.name, userMbti = userProfile.mbtiType)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Your matches today",
                color = TextMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isEmpty) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌙", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Come back tomorrow\nfor new matches",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 26.sp,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Saffron)
                                .padding(horizontal = 24.dp, vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("See all personalities", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Background stacked cards
                    deck.reversed().forEachIndexed { reversedIndex, personality ->
                        val index = deck.size - 1 - reversedIndex
                        if (index < deck.size - 1) {
                            val stackOffset = (deck.size - 1 - index).coerceAtMost(3)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(480.dp)
                                    .offset(y = (stackOffset * 10).dp)
                                    .scale(1f - stackOffset * 0.04f)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(personality.cardGradientStart),
                                                Color(personality.cardGradientEnd)
                                            )
                                        )
                                    )
                            )
                        }
                    }

                    // Top swipeable card
                    deck.lastOrNull()?.let { topCard ->
                        SwipeCard(
                            personality = topCard,
                            userMbti = userProfile.mbtiType,
                            onSwipeRight = {
                                deck.removeLastOrNull()
                                onPersonalitySelected(topCard)
                            },
                            onSwipeLeft = {
                                deck.removeLastOrNull()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                deck.lastOrNull()?.let { topCard ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 60.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ActionButton(onClick = { deck.removeLastOrNull() }, color = Color(0xFF2A2A35), size = 64.dp) {
                            Icon(Icons.Default.Close, contentDescription = "Pass", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        ActionButton(
                            onClick = {
                                deck.removeLastOrNull()
                                onPersonalitySelected(topCard)
                            },
                            color = Saffron,
                            size = 72.dp
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = "Chat", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SwipeCard(
    personality: Personality,
    userMbti: String,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val animX = remember { Animatable(0f) }
    val animY = remember { Animatable(0f) }

    val rotation = animX.value / 22f
    val likeAlpha = (animX.value / 150f).coerceIn(0f, 1f)
    val passAlpha = (-animX.value / 150f).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(480.dp)
            .graphicsLayer {
                translationX = animX.value
                translationY = animY.value
                rotationZ = rotation
            }
            .shadow(16.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(personality.cardGradientStart),
                        Color(personality.cardGradientEnd)
                    )
                )
            )
            .pointerInput(personality.id) {
                detectDragGestures(
                    onDragEnd = {
                        scope.launch {
                            when {
                                animX.value > 200f -> {
                                    launch { animX.animateTo(1200f, tween(300)) }
                                    onSwipeRight()
                                }
                                animX.value < -200f -> {
                                    launch { animX.animateTo(-1200f, tween(300)) }
                                    onSwipeLeft()
                                }
                                else -> {
                                    launch {
                                        animX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                                    }
                                    launch {
                                        animY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                                    }
                                }
                            }
                        }
                    }
                ) { change, dragAmount ->
                    change.consume()
                    scope.launch {
                        animX.snapTo(animX.value + dragAmount.x)
                        animY.snapTo(animY.value + dragAmount.y)
                    }
                }
            }
    ) {
        CardContent(personality = personality, userMbti = userMbti)

        if (likeAlpha > 0f) {
            Box(
                modifier = Modifier
                    .padding(24.dp)
                    .rotate(-20f)
                    .border(2.dp, Saffron, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .graphicsLayer { alpha = likeAlpha }
            ) {
                Text("LIKE", color = Saffron, fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = 2.sp)
            }
        }

        if (passAlpha > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .rotate(20f)
                    .border(2.dp, Color(0xFF9E9EA8), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .graphicsLayer { alpha = passAlpha }
            ) {
                Text("PASS", color = Color(0xFF9E9EA8), fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = 2.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardContent(personality: Personality, userMbti: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        // MBTI badge top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(personality.mbtiType, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
        }

        // Compatibility label top left
        val compatLabel = if (userMbti.length >= 4) MatchEngine.compatibilityLabel(userMbti, personality.mbtiType) else ""
        if (compatLabel.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (compatLabel.startsWith("✦"))
                            Saffron.copy(alpha = 0.85f)
                        else
                            Color.White.copy(alpha = 0.15f)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(compatLabel, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Center emoji avatar
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-40).dp)
                .size(96.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(personality.avatarEmoji, fontSize = 44.sp)
        }

        // Bottom info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                        startY = 0f
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(personality.name, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("${personality.age} · ${personality.role}", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "\"${personality.tagline}\"",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    personality.traits.forEach { trait ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(trait, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeTopBar(userName: String, userMbti: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("H.E.R", color = Saffron, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.weight(1f))

        // User MBTI badge
        if (userMbti.length >= 4) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Saffron.copy(alpha = 0.15f))
                    .border(1.dp, Saffron.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    userMbti.take(4),
                    color = Saffron,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        // Profile avatar
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Saffron.copy(alpha = 0.2f))
                .border(1.dp, Saffron.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.firstOrNull()?.uppercase() ?: "?",
                color = Saffron,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun ActionButton(
    onClick: () -> Unit,
    color: Color,
    size: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(size)) {
            content()
        }
    }
}
