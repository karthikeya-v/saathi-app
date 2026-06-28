package com.her.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.her.app.data.Personality
import com.her.app.ui.theme.DarkText
import com.her.app.ui.theme.Saffron
import com.her.app.ui.theme.TextMuted
import com.her.app.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonalityCard(
    personality: Personality,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gradient emoji circle
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(personality.cardGradientStart),
                                Color(personality.cardGradientEnd)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(personality.avatarEmoji, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = personality.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // MBTI badge: saffron outline, no fill
                    Box(
                        modifier = Modifier
                            .border(1.dp, Saffron, RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = personality.mbtiType,
                            color = Saffron,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Text(
                    text = "\"${personality.tagline}\"",
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    personality.traits.forEach { trait ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF1A1A2E))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(trait, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Arrow
            Text("→", color = TextMuted, fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
        }
    }
}
