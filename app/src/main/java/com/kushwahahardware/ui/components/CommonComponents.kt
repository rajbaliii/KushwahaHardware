package com.kushwahahardware.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kushwahahardware.R

import com.kushwahahardware.ui.theme.DesignTokens

// ── Shop Header ─────────────────────────────────
@Composable
fun ShopHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = DesignTokens.PremiumGradient
                )
            )
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Logo container
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.ic_logo_premium),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "KUSHWAHA",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "HARDWARE",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 5.sp
                )
            }

            // Avatar / Profile
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

// ── Summary Card ────────────────────────────────
@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color = Color.White,
    iconTint: Color = DesignTokens.MidTeal,
    badge: String? = null,
    badgeColor: Color = DesignTokens.ErrorRed,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0).copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconTint.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (badge != null) {
                    Surface(
                        color = badgeColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = badge,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = DesignTokens.SubtitleGray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// ── Quick Action Button ──────────────────────────
@Composable
fun QuickActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: Color = DesignTokens.MidTeal,
    contentColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
