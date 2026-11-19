package com.example.tabibconnect.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import kotlinx.coroutines.launch
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
import com.example.tabibconnect.data.model.HealthMetricRecord
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tabibconnect.data.model.AppointmentStatus
import com.example.tabibconnect.data.model.UserRole
import com.example.tabibconnect.data.repository.TabibRepository
import com.example.tabibconnect.ui.theme.EmergencyRed
import com.example.tabibconnect.ui.theme.HeaderGradient
import com.example.tabibconnect.ui.theme.PrimaryDarkTeal
import com.example.tabibconnect.ui.theme.PrimaryTeal
import com.example.tabibconnect.ui.theme.TealPrimary
import com.example.tabibconnect.ui.theme.TextDarkNavy

@Composable
fun TabibConnectLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 60.dp,
    showSubtitle: Boolean = true,
    isDarkBackground: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo Emblem
        Box(
            modifier = Modifier
                .size(iconSize)
                .clip(RoundedCornerShape(iconSize * 0.28f))
                .background(if (isDarkBackground) Color.White else TealPrimary),
            contentAlignment = Alignment.Center
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                // Speech bubble with plus inside
                Surface(
                    color = if (isDarkBackground) TealPrimary else Color.White,
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomEnd = 10.dp, bottomStart = 2.dp),
                    modifier = Modifier
                        .size(iconSize * 0.58f)
                        .padding(2.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = if (isDarkBackground) Color.White else TealPrimary,
                            modifier = Modifier.size(iconSize * 0.36f)
                        )
                    }
                }

                // Small orange dot notification badge at top-right
                Box(
                    modifier = Modifier
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(iconSize * 0.16f)
                        .clip(CircleShape)
                        .background(Color(0xFFFA7052))
                )
            }
        }

        Spacer(modifier = Modifier.width(if (showSubtitle) 16.dp else 10.dp))

        // Text Part: "Tabib" + "Connect"
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Tabib",
                    fontSize = (iconSize.value * 0.45f).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDarkBackground) Color.White else TextDarkNavy,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Connect",
                    fontSize = (iconSize.value * 0.45f).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDarkBackground) Color(0xFF80CBC4) else TealPrimary,
                    letterSpacing = (-0.5).sp
                )
            }

            if (showSubtitle) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "Votre médecin, partout en Tunisie",
                    fontSize = (iconSize.value * 0.26f).coerceAtLeast(10f).sp,
                    color = if (isDarkBackground) Color.White.copy(alpha = 0.85f) else Color(0xFF6B7C85),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun InitialsAvatar(
    initials: String,
    backgroundColor: Color,
    textColor: Color = Color.White,
    size: Dp = 48.dp,
    fontSize: TextUnit = 16.sp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            color = textColor
        )
    }
}

@Composable
fun EmergencyBanner(
    modifier: Modifier = Modifier,
    onEmergencyClick: () -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEmergencyClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(EmergencyRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Urgence",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Avis important - Urgences Vitales",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = EmergencyRed
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "L'application ne remplace pas le SAMU. En cas d'urgence, composez immédiatement le 190.",
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun RoleHeaderSwitcher(
    onNavigateToProfile: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var showProfileMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderGradient)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Official TabibConnect Logo with subtitle (matching Authentication Page)
            TabibConnectLogo(
                iconSize = 34.dp,
                showSubtitle = true,
                isDarkBackground = true
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Profile Icon Button with Dropdown Menu (Profile & Logout)
                Box {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { showProfileMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showProfileMenu,
                        onDismissRequest = { showProfileMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Mon profil",
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkNavy,
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.PersonOutline,
                                    contentDescription = null,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                showProfileMenu = false
                                onNavigateToProfile()
                            }
                        )

                        HorizontalDivider(color = Color(0xFFE2E8F0))

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Déconnexion",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE53935),
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null,
                                    tint = Color(0xFFE53935),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                showProfileMenu = false
                                onLogout()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: AppointmentStatus) {
    val (bgColor, textColor, text) = when (status) {
        AppointmentStatus.CONFIRMED -> Triple(Color(0xFFE6F4EA), Color(0xFF137333), "Confirmé")
        AppointmentStatus.PENDING -> Triple(Color(0xFFFEF7E0), Color(0xFFB06000), "En attente")
        AppointmentStatus.COMPLETED -> Triple(Color(0xFFE8F0FE), Color(0xFF1A73E8), "Terminé")
        AppointmentStatus.CANCELLED -> Triple(Color(0xFFFCE8E6), Color(0xFFC5221F), "Annulé")
        AppointmentStatus.REJECTED -> Triple(Color(0xFFFCE8E6), Color(0xFFC5221F), "Refusé")
        AppointmentStatus.ABSENT -> Triple(Color(0xFFE8EAED), Color(0xFF5F6368), "Absent")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun RatingStars(rating: Double, reviewCount: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFFFB800),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$rating",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Text(
            text = " ($reviewCount avis)",
            color = Color.Gray,
            fontSize = 11.sp
        )
    }
}

data class BarChartData(val label: String, val value: Float)

@Composable
fun PrettyBarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier,
    barColor: Color = PrimaryTeal,
    accentColor: Color = com.example.tabibconnect.ui.theme.SecondaryTeal
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Revenus & Consultations Hebdomadaires",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PrimaryDarkTeal
                    )
                    Text("Vue globale des 7 derniers jours", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val maxValue = (data.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { item ->
                    val ratio = (item.value / maxValue).coerceIn(0.08f, 1f)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${item.value.toInt()} DT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDarkTeal,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .height(80.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .height((80 * ratio).dp)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        if (item.value > 0f)
                                            androidx.compose.ui.graphics.Brush.verticalGradient(listOf(barColor, accentColor))
                                        else
                                            androidx.compose.ui.graphics.SolidColor(PrimaryTeal.copy(alpha = 0.3f))
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrettyLineChart(
    dataPoints: List<Float>,
    labels: List<String>,
    title: String,
    subtitle: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryDarkTeal)
                    Text(subtitle, fontSize = 11.sp, color = Color.Gray)
                }
                val latest = dataPoints.lastOrNull()?.toInt() ?: 0
                Surface(
                    color = com.example.tabibconnect.ui.theme.LightTealContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$latest $unit",
                        color = PrimaryTeal,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val maxVal = (dataPoints.maxOrNull() ?: 100f).coerceAtLeast(1f)
            val minVal = (dataPoints.minOrNull() ?: 0f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                val width = size.width
                val height = size.height
                val spacing = if (dataPoints.size > 1) width / (dataPoints.size - 1) else width

                val points = dataPoints.mapIndexed { index, value ->
                    val x = index * spacing
                    val normalizedY = if (maxVal != minVal) (value - minVal) / (maxVal - minVal) else 0.5f
                    val y = height - (normalizedY * (height * 0.7f) + height * 0.15f)
                    androidx.compose.ui.geometry.Offset(x, y)
                }

                // Draw Area Gradient Fill
                val fillPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, height)
                    points.forEach { point ->
                        lineTo(point.x, point.y)
                    }
                    lineTo(width, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            PrimaryTeal.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )

                // Draw Smooth Curve
                val linePath = androidx.compose.ui.graphics.Path().apply {
                    points.forEachIndexed { i, pt ->
                        if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                    }
                }

                drawPath(
                    path = linePath,
                    color = PrimaryTeal,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                )

                // Draw Point Markers
                points.forEach { pt ->
                    drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
                    drawCircle(color = PrimaryTeal, radius = 3.5.dp.toPx(), center = pt)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { label ->
                    Text(text = label, fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

val ShieldShape = androidx.compose.foundation.shape.GenericShape { size, _ ->
    val width = size.width
    val height = size.height
    val cornerRadius = width * 0.32f

    moveTo(0f, cornerRadius)
    quadraticTo(0f, 0f, cornerRadius, 0f)
    lineTo(width - cornerRadius, 0f)
    quadraticTo(width, 0f, width, cornerRadius)
    lineTo(width, height * 0.60f)
    quadraticTo(width, height * 0.88f, width * 0.5f, height)
    quadraticTo(0f, height * 0.88f, 0f, height * 0.60f)
    close()
}

data class NavigationTabItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector = icon
)

@Composable
fun InnovativeNavigationBar(
    items: List<NavigationTabItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val navBgColor = com.example.tabibconnect.ui.theme.NavyDark // Elegant dark navy background (#0F2537)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(navBgColor)
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Bottom bar container surface (Ultra-slim height)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            color = navBgColor,
            shadowElevation = 12.dp
        ) {}

        // Tab items row positioned over the bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val scale by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (isSelected) 1.06f else 1.0f,
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                    ),
                    label = "badge_scale"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) { onItemSelected(index) },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (isSelected) {
                        // SELECTED TAB: Shield badge icon + text label below (Always visible)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(bottom = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = if (items.size >= 7) 26.dp else 28.dp, height = if (items.size >= 7) 28.dp else 30.dp)
                                    .graphicsLayer(scaleX = scale, scaleY = scale)
                                    .clip(ShieldShape)
                                    .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            listOf(TealPrimary, com.example.tabibconnect.ui.theme.TealDark)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.selectedIcon,
                                    contentDescription = item.title,
                                    tint = Color.White,
                                    modifier = Modifier.size(if (items.size >= 7) 13.5.dp else 15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = item.title,
                                fontSize = if (items.size >= 7) 7.5.sp else 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    } else {
                        // UNSELECTED TAB: Icon + text label below
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(top = 6.dp, bottom = 2.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = Color(0xFF8E9DAE),
                                modifier = Modifier.size(if (items.size >= 7) 14.5.dp else 16.dp)
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = item.title,
                                fontSize = if (items.size >= 7) 7.5.sp else 8.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF8E9DAE),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

// Reusable Modern Structured Selection Modal Component (Unified Design for Governorates, Delegations & Specialties)
@Composable
fun StructuredListSelectionDialog(
    title: String,
    subtitle: String,
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredItems = remember(searchQuery, items) {
        if (searchQuery.isBlank()) items
        else items.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                Text(text = subtitle, fontSize = 12.sp, color = Color(0xFF6B7C85))
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Search Input inside Modal
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Rechercher...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TealPrimary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredItems) { item ->
                        val isSelected = item == selectedItem
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) com.example.tabibconnect.ui.theme.TealLightContainer else Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) TealPrimary else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemSelected(item) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) TealPrimary else TextDarkNavy,
                                    fontSize = 14.sp
                                )

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = TealPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", color = TealPrimary, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

// Dynamic Health Data Entry Modal
@Composable
fun AddHealthMetricDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var selectedType by remember { mutableStateOf("TENSION") }

    var val1 by remember { mutableStateOf("") }
    var val2 by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var diseaseName by remember { mutableStateOf("") }
    var mealState by remember { mutableStateOf("A jeun") }
    var restState by remember { mutableStateOf("Au repos") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Enregistrer une Constante de Santé", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                Text("Sauvegarde sécurisée dans votre BDD", fontSize = 11.sp, color = Color(0xFF6B7C85))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Type de constante / Maladie :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)

                // Category Selector Chips Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val categories = listOf(
                        Triple("TENSION", "🩸 Tension", "Tension"),
                        Triple("GLYCEMIE", "🍬 Sucre", "Glycémie"),
                        Triple("HEART_RATE", "❤️ Pouls", "Pouls")
                    )
                    categories.forEach { (typeKey, label, _) ->
                        val isSelected = selectedType == typeKey
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedType = typeKey
                                errorMessage = null
                            },
                            label = { Text(label, fontSize = 10.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = com.example.tabibconnect.ui.theme.TealLightContainer,
                                selectedLabelColor = TealPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Category Selector Chips Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val categories2 = listOf(
                        Triple("TEMPERATURE", "🌡️ Temp.", "Température"),
                        Triple("WEIGHT", "⚖️ Poids", "Poids"),
                        Triple("DISEASE", "🩺 Maladie", "Pathologie")
                    )
                    categories2.forEach { (typeKey, label, _) ->
                        val isSelected = selectedType == typeKey
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedType = typeKey
                                errorMessage = null
                            },
                            label = { Text(label, fontSize = 10.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = com.example.tabibconnect.ui.theme.TealLightContainer,
                                selectedLabelColor = TealPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Error Message if invalid
                if (errorMessage != null) {
                    Text(errorMessage!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // DYNAMIC FORM FIELDS
                when (selectedType) {
                    "TENSION" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Systolique (mmHg)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = val1,
                                    onValueChange = { val1 = it },
                                    placeholder = { Text("Ex: 120", fontSize = 12.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Diastolique (mmHg)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = val2,
                                    onValueChange = { val2 = it },
                                    placeholder = { Text("Ex: 80", fontSize = 12.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    "GLYCEMIE" -> {
                        Column {
                            Text("Taux de sucre (g/L)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = val1,
                                onValueChange = { val1 = it },
                                placeholder = { Text("Ex: 1.10", fontSize = 12.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = mealState == "A jeun",
                                    onClick = { mealState = "A jeun" },
                                    label = { Text("A jeun ☀️", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = mealState == "Après repas",
                                    onClick = { mealState = "Après repas" },
                                    label = { Text("Après repas 🍽️", fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    "HEART_RATE" -> {
                        Column {
                            Text("Fréquence Cardiaque (BPM)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = val1,
                                onValueChange = { val1 = it },
                                placeholder = { Text("Ex: 72", fontSize = 12.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = restState == "Au repos",
                                    onClick = { restState = "Au repos" },
                                    label = { Text("Au repos 🧘", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = restState == "Après effort",
                                    onClick = { restState = "Après effort" },
                                    label = { Text("Après effort 🏃", fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    "TEMPERATURE" -> {
                        Column {
                            Text("Température (°C)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = val1,
                                onValueChange = { val1 = it },
                                placeholder = { Text("Ex: 37.2", fontSize = 12.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    "WEIGHT" -> {
                        Column {
                            Text("Poids (kg)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = val1,
                                onValueChange = { val1 = it },
                                placeholder = { Text("Ex: 75.5", fontSize = 12.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    "DISEASE" -> {
                        Column {
                            Text("Nom de la Maladie / Pathologie", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = diseaseName,
                                onValueChange = { diseaseName = it },
                                placeholder = { Text("Ex: Diabète Type 2, Hypertension, Asthme...", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Valeur / Niveau d'observation", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = val1,
                                onValueChange = { val1 = it },
                                placeholder = { Text("Ex: 130 ou Stable", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Notes Field
                Text("Observations complémentaires :", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Symptômes, traitement pris...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Red Cancel Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Annuler", fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Green Save Button
                Button(
                    enabled = !isLoading,
                    onClick = {
                        val floatVal1 = val1.toFloatOrNull() ?: 0f
                        val floatVal2 = val2.toFloatOrNull()
                        val noteStr = if (selectedType == "GLYCEMIE") "$mealState - $notes".trim('-',' ')
                        else if (selectedType == "HEART_RATE") "$restState - $notes".trim('-',' ')
                        else notes

                        if (selectedType != "DISEASE" && floatVal1 <= 0f) {
                            errorMessage = "Veuillez entrer une valeur valide."
                            return@Button
                        }
                        if (selectedType == "DISEASE" && diseaseName.isBlank()) {
                            errorMessage = "Veuillez préciser le nom de la maladie."
                            return@Button
                        }

                        isLoading = true
                        coroutineScope.launch {
                            TabibRepository.addHealthMetricAsync(
                                metricType = selectedType,
                                metricValue = floatVal1,
                                secondaryValue = floatVal2,
                                notes = noteStr,
                                diseaseName = diseaseName
                            )
                            isLoading = false
                            onSuccess()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Enregistrer", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        },
        dismissButton = {},
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}

// Dedicated Espace de Notifications Component
@Composable
fun NotificationSpaceDialog(
    onDismiss: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    val notificationsList = TabibRepository.notifications

    val filteredNotifications = remember(selectedFilter, notificationsList.toList()) {
        when (selectedFilter) {
            "CONFIRMED" -> notificationsList.filter { it.type == com.example.tabibconnect.data.model.NotificationType.CONFIRMED || it.type == com.example.tabibconnect.data.model.NotificationType.COMPLETED }
            "MODIFIED" -> notificationsList.filter { it.type == com.example.tabibconnect.data.model.NotificationType.MODIFIED }
            "CANCELLED" -> notificationsList.filter { it.type == com.example.tabibconnect.data.model.NotificationType.CANCELLED || it.type == com.example.tabibconnect.data.model.NotificationType.REJECTED }
            "ADDED" -> notificationsList.filter { it.type == com.example.tabibconnect.data.model.NotificationType.ADDED }
            else -> notificationsList
        }
    }

    val unreadCount = notificationsList.count { !it.isRead }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(com.example.tabibconnect.ui.theme.TealLightContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Centre de Notifications",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkNavy,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Événements sur vos rendez-vous",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Actions Bar: Tout marquer comme lu & Effacer tout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { TabibRepository.markAllNotificationsAsRead() },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tout marquer comme lu", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = { TabibRepository.clearNotifications() },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Effacer tout", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Filter Chips Row
                val filterOptions = listOf(
                    Triple("ALL", "Tous", notificationsList.size),
                    Triple("CONFIRMED", "Validés 🟢", notificationsList.count { it.type == com.example.tabibconnect.data.model.NotificationType.CONFIRMED || it.type == com.example.tabibconnect.data.model.NotificationType.COMPLETED }),
                    Triple("MODIFIED", "Modifiés 🔵", notificationsList.count { it.type == com.example.tabibconnect.data.model.NotificationType.MODIFIED }),
                    Triple("CANCELLED", "Annulés 🔴", notificationsList.count { it.type == com.example.tabibconnect.data.model.NotificationType.CANCELLED || it.type == com.example.tabibconnect.data.model.NotificationType.REJECTED }),
                    Triple("ADDED", "Réservés 🟡", notificationsList.count { it.type == com.example.tabibconnect.data.model.NotificationType.ADDED })
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filterOptions.forEach { (key, label, count) ->
                        val isSel = selectedFilter == key
                        Surface(
                            onClick = { selectedFilter = key },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) TealPrimary else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) TealPrimary else Color(0xFFE2E8F0))
                        ) {
                            Text(
                                text = "$label ($count)",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else TextDarkNavy,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (filteredNotifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aucune notification dans cette catégorie.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredNotifications) { notif ->
                            val (badgeBg, badgeTextColor, iconVec) = when (notif.type) {
                                com.example.tabibconnect.data.model.NotificationType.CONFIRMED,
                                com.example.tabibconnect.data.model.NotificationType.COMPLETED -> Triple(Color(0xFFE6F4EA), Color(0xFF137333), Icons.Default.CheckCircle)
                                com.example.tabibconnect.data.model.NotificationType.MODIFIED -> Triple(Color(0xFFE8F0FE), Color(0xFF1A73E8), Icons.Default.EditCalendar)
                                com.example.tabibconnect.data.model.NotificationType.CANCELLED,
                                com.example.tabibconnect.data.model.NotificationType.REJECTED -> Triple(Color(0xFFFCE8E6), Color(0xFFC5221F), Icons.Default.Cancel)
                                com.example.tabibconnect.data.model.NotificationType.ADDED -> Triple(Color(0xFFFEF7E0), Color(0xFFB06000), Icons.Default.NotificationsActive)
                            }

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (!notif.isRead) Color(0xFFF0FDF4) else Color.White
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (!notif.isRead) TealPrimary.copy(alpha = 0.5f) else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { TabibRepository.markNotificationAsRead(notif.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(badgeBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = iconVec,
                                            contentDescription = null,
                                            tint = badgeTextColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = notif.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                color = TextDarkNavy,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )

                                            Spacer(modifier = Modifier.width(6.dp))

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.wrapContentWidth()
                                            ) {
                                                Text(
                                                    text = notif.timestamp,
                                                    fontSize = 10.sp,
                                                    color = Color.Gray,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )

                                                if (!notif.isRead) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF2563EB))
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = notif.message,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF475569),
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fermer", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

// Attach Document Dialog for Patient
@Composable
fun AttachDocumentToAppointmentDialog(
    appointment: com.example.tabibconnect.data.model.Appointment,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var selectedDocId by remember { mutableStateOf("") }
    var newDocTitle by remember { mutableStateOf("") }
    var newDocCategory by remember { mutableStateOf(com.example.tabibconnect.data.model.DocumentCategory.BIOLOGY) }
    var isNewDocument by remember { mutableStateOf(false) }
    var shareDurationDays by remember { mutableIntStateOf(30) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val userDocs = TabibRepository.medicalDocuments

    LaunchedEffect(Unit) {
        TabibRepository.fetchMedicalDocumentsAsync()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(com.example.tabibconnect.ui.theme.TealLightContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Rattacher un Document Médical",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkNavy
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Transmettre au ${appointment.doctorName} pour la séance",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7C85)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMessage != null) {
                    Text(errorMessage!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isNewDocument,
                        onClick = { isNewDocument = false; errorMessage = null },
                        label = { Text("📋 Vos Documents", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = com.example.tabibconnect.ui.theme.TealLightContainer,
                            selectedLabelColor = TealPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isNewDocument,
                        onClick = { isNewDocument = true; errorMessage = null },
                        label = { Text("➕ Nouveau Document", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = com.example.tabibconnect.ui.theme.TealLightContainer,
                            selectedLabelColor = TealPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (!isNewDocument) {
                    if (userDocs.isEmpty()) {
                        Text(
                            text = "Aucun document à sélectionner",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    } else {
                        Text("Sélectionnez un document à transmettre :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                        userDocs.forEach { doc ->
                            val isSelected = doc.id == selectedDocId
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) com.example.tabibconnect.ui.theme.TealLightContainer else Color.White
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) TealPrimary else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedDocId = doc.id }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(doc.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDarkNavy)
                                        Text("${doc.category.labelFr} • ${doc.dateAdded} • ${doc.fileSize}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Intitulé du document :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                        OutlinedTextField(
                            value = newDocTitle,
                            onValueChange = { newDocTitle = it },
                            placeholder = { Text("Ex: Analyse de sang, Radio Thoracique...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text("Catégorie :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            com.example.tabibconnect.data.model.DocumentCategory.values().forEach { cat ->
                                val isSel = newDocCategory == cat
                                FilterChip(
                                    selected = isSel,
                                    onClick = { newDocCategory = cat },
                                    label = { Text(cat.labelFr, fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text("Durée du partage pour le médecin :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        Pair(7, "7 jours"),
                        Pair(15, "15 jours"),
                        Pair(30, "30 jours"),
                        Pair(0, "Sans durée ♾️")
                    ).forEach { (d, label) ->
                        val isSel = shareDurationDays == d
                        FilterChip(
                            selected = isSel,
                            onClick = { shareDurationDays = d },
                            label = { Text(label, fontSize = 10.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = com.example.tabibconnect.ui.theme.TealLightContainer,
                                selectedLabelColor = TealPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Annuler", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    enabled = !isLoading,
                    onClick = {
                        if (!isNewDocument && selectedDocId.isBlank()) {
                            errorMessage = "Veuillez sélectionner un document."
                            return@Button
                        }
                        if (isNewDocument && newDocTitle.isBlank()) {
                            errorMessage = "Veuillez saisir le nom du document."
                            return@Button
                        }

                        isLoading = true
                        coroutineScope.launch {
                            var docIdToAttach = selectedDocId
                            if (isNewDocument) {
                                TabibRepository.addMedicalDocumentAsync(
                                    title = newDocTitle,
                                    category = newDocCategory,
                                    fileSize = "2.4 Mo"
                                )
                                docIdToAttach = TabibRepository.medicalDocuments.firstOrNull()?.id ?: "doc_med_1"
                            }
                            TabibRepository.attachDocumentToAppointmentAsync(
                                documentId = docIdToAttach,
                                appointmentId = appointment.id,
                                doctorId = appointment.doctorId,
                                doctorName = appointment.doctorName,
                                days = shareDurationDays
                            )
                            TabibRepository.fetchMedicalDocumentsAsync()
                            isLoading = false
                            onSuccess()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Transmettre", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        },
        dismissButton = {},
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}

// Doctor Consultation Result Dialog
@Composable
fun DoctorConsultationResultDialog(
    appointment: com.example.tabibconnect.data.model.Appointment,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var diagnosis by remember { mutableStateOf("") }
    var recommendations by remember { mutableStateOf("") }
    var restDaysInput by remember { mutableStateOf("") }
    var medNameInput by remember { mutableStateOf("") }
    var medUsageInput by remember { mutableStateOf("") }
    val medicationsList = remember { mutableStateListOf<String>() }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(appointment.id) {
        val existing = TabibRepository.consultationRecords.firstOrNull { it.appointmentId == appointment.id }
            ?: TabibRepository.consultationRecords.firstOrNull { it.patientName.equals(appointment.patientName, ignoreCase = true) }
        if (existing != null) {
            diagnosis = existing.diagnosis
            recommendations = existing.recommendations
            if (existing.restDays > 0) restDaysInput = existing.restDays.toString()
            if (existing.prescription != null && existing.prescription.medications.isNotEmpty()) {
                medicationsList.clear()
                medicationsList.addAll(existing.prescription.medications)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(com.example.tabibconnect.ui.theme.TealLightContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Saisir le Résultat de Consultation",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkNavy
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Patient : ${appointment.patientName} (${appointment.specialtyName})",
                    fontSize = 11.5.sp,
                    color = Color(0xFF6B7C85)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMessage != null) {
                    Text(errorMessage!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Diagnosis / Description
                Text("Diagnostic Médical & Description :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                OutlinedTextField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    placeholder = { Text("Ex: Examen clinique normal, syndrome grippal...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Rest Days / Arrêt de travail
                Text("Nombre de jours de repos / Arrêt de travail :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                OutlinedTextField(
                    value = restDaysInput,
                    onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) restDaysInput = it },
                    placeholder = { Text("Ex: 3 (Saisir 0 si aucun arrêt)", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Recommendations
                Text("Recommandations & Consignes :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                OutlinedTextField(
                    value = recommendations,
                    onValueChange = { recommendations = it },
                    placeholder = { Text("Ex: Repos strict au lit, réévaluation dans 5 jours...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Prescription / Medications + Usage Manner
                Text("Prescription Médicamenteuse & Manière d'utilisation :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = medNameInput,
                        onValueChange = { medNameInput = it },
                        placeholder = { Text("Nom du médicament (ex: PARACETAMOL 1000mg)", fontSize = 11.5.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = medUsageInput,
                        onValueChange = { medUsageInput = it },
                        placeholder = { Text("Manière d'utilisation (ex: 1 comprimé 3x/j après repas)", fontSize = 11.5.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            if (medNameInput.isNotBlank()) {
                                val itemText = if (medUsageInput.isNotBlank()) {
                                    "${medNameInput.trim()} — Posologie : ${medUsageInput.trim()}"
                                } else {
                                    medNameInput.trim()
                                }
                                medicationsList.add(itemText)
                                medNameInput = ""
                                medUsageInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ajouter ce médicament à l'ordonnance", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (medicationsList.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        medicationsList.forEachIndexed { idx, med ->
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("• $med", fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = TextDarkNavy, modifier = Modifier.weight(1f))
                                    IconButton(
                                        onClick = { medicationsList.removeAt(idx) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Annuler", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    enabled = !isLoading,
                    onClick = {
                        if (diagnosis.isBlank()) {
                            errorMessage = "Veuillez saisir un diagnostic ou description."
                            return@Button
                        }

                        isLoading = true
                        coroutineScope.launch {
                            TabibRepository.saveConsultationResultAsync(
                                appointmentId = appointment.id,
                                doctorId = appointment.doctorId,
                                doctorName = appointment.doctorName,
                                patientId = appointment.patientId,
                                patientName = appointment.patientName,
                                specialtyName = appointment.specialtyName,
                                diagnosis = diagnosis,
                                recommendations = recommendations,
                                restDays = restDaysInput.toIntOrNull() ?: 0,
                                medications = medicationsList.toList()
                            )
                            isLoading = false
                            onSuccess()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Enregistrer", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        },
        dismissButton = {},
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}

// PDF Exporter helper function for Consultation Results & Prescriptions
fun exportConsultationResultToPdf(
    context: android.content.Context,
    record: com.example.tabibconnect.data.model.ConsultationRecord
): Boolean {
    val medsList = record.prescription?.medications?.joinToString("\n") { "  • $it" } ?: "  Aucun médicament prescrit."
    val restDaysStr = if (record.restDays > 0) "${record.restDays} jour(s) de repos médical prescrit(s)" else "Aucun arrêt de travail prescrit"

    val pdfContent = """
        ================================================================
        TABIB CONNECT - COMPTE-RENDU MÉDICAL ET ORDONNANCE (PDF)
        ================================================================
        Médecin Traitant : ${record.doctorName}
        Spécialité       : ${record.specialtyName}
        N° Ordre         : TN-12845
        Date Consultation: ${record.date}
        ----------------------------------------------------------------
        PATIENT          : ${record.patientName}
        ----------------------------------------------------------------
        
        1. DESCRIPTION & DIAGNOSTIC MÉDICAL :
        ${record.diagnosis.ifBlank { "Consultation de contrôle." }}

        2. NOMBRE DE JOURS DE REPOS / ARRÊT DE TRAVAIL :
        $restDaysStr

        3. RECOMMANDATIONS & CONSIGNES :
        ${record.recommendations.ifBlank { "Suivi régulier recommandé." }}

        4. PRESCRIPTION MÉDICAMENTEUSE & MANIÈRE D'UTILISATION :
      $medsList

        ================================================================
        Authentification : Signé et certifié numériquement via TabibConnect
        ================================================================
    """.trimIndent()

    return com.example.tabibconnect.ui.screens.patient.downloadMedicalFileToDevice(
        context = context,
        docTitle = "Resultat_Consultation_${record.patientName.replace(" ", "_")}",
        categoryLabel = "Ordonnance & Compte-Rendu",
        dateAdded = record.date,
        patientName = record.patientName,
        notes = pdfContent,
        fileUrl = ""
    )
}

// Helper function to invoke Android Native PrintManager Service & Save PDF
fun printConsultationResult(
    context: android.content.Context,
    record: com.example.tabibconnect.data.model.ConsultationRecord
): Boolean {
    // 1. Save copy to Downloads
    exportConsultationResultToPdf(context, record)

    // 2. Launch Android System Native Print Service
    try {
        val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as? android.print.PrintManager
        if (printManager != null) {
            val jobName = "Consultation_${record.patientName.replace(" ", "_")}"
            val printAdapter = object : android.print.PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: android.print.PrintAttributes?,
                    newAttributes: android.print.PrintAttributes?,
                    cancellationSignal: android.os.CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: android.os.Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val builder = android.print.PrintDocumentInfo.Builder(jobName)
                        .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                    val info = builder.build()
                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out android.print.PageRange>?,
                    destination: android.os.ParcelFileDescriptor?,
                    cancellationSignal: android.os.CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    try {
                        val pdfDocument = android.graphics.pdf.PdfDocument()
                        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Format
                        val page = pdfDocument.startPage(pageInfo)
                        val canvas = page.canvas

                        val titlePaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.rgb(11, 75, 92)
                            textSize = 18f
                            isFakeBoldText = true
                        }
                        canvas.drawText("TABIB CONNECT - ORDONNANCE & COMPTE-RENDU", 40f, 50f, titlePaint)

                        val textPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 12f
                        }
                        textPaint.isFakeBoldText = true
                        canvas.drawText("Médecin : ${record.doctorName} (${record.specialtyName})", 40f, 90f, textPaint)
                        canvas.drawText("Date : ${record.date}", 40f, 110f, textPaint)
                        canvas.drawText("Patient : ${record.patientName}", 40f, 130f, textPaint)

                        textPaint.isFakeBoldText = false
                        var y = 170f
                        canvas.drawText("---------------------------------------------------------------------------------", 40f, y, textPaint)
                        y += 25f

                        val sectionPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.rgb(11, 75, 92)
                            textSize = 13f
                            isFakeBoldText = true
                        }
                        canvas.drawText("1. DIAGNOSTIC & DESCRIPTION MÉDICALE :", 40f, y, sectionPaint)
                        y += 20f
                        canvas.drawText(record.diagnosis.ifBlank { "Consultation de contrôle." }, 50f, y, textPaint)
                        y += 30f

                        if (record.restDays > 0) {
                            canvas.drawText("2. REPOS MÉDICAL PRESCRIT : ${record.restDays} jour(s)", 40f, y, sectionPaint)
                            y += 30f
                        }

                        if (record.recommendations.isNotBlank()) {
                            canvas.drawText("3. RECOMMANDATIONS :", 40f, y, sectionPaint)
                            y += 20f
                            canvas.drawText(record.recommendations, 50f, y, textPaint)
                            y += 30f
                        }

                        if (record.prescription != null && record.prescription.medications.isNotEmpty()) {
                            canvas.drawText("4. PRESCRIPTION MÉDICALE & POSOLOGIE :", 40f, y, sectionPaint)
                            y += 20f
                            record.prescription.medications.forEach { med ->
                                canvas.drawText("  • $med", 50f, y, textPaint)
                                y += 18f
                            }
                            y += 10f
                        }

                        y += 20f
                        canvas.drawText("---------------------------------------------------------------------------------", 40f, y, textPaint)
                        y += 25f
                        val certPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.rgb(19, 115, 51)
                            textSize = 11f
                            isFakeBoldText = true
                        }
                        canvas.drawText("Certifié et délivré numériquement via l'application TabibConnect", 40f, y, certPaint)

                        pdfDocument.finishPage(page)
                        val outStream = java.io.FileOutputStream(destination?.fileDescriptor)
                        pdfDocument.writeTo(outStream)
                        pdfDocument.close()

                        callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    }
                }
            }
            printManager.print(jobName, printAdapter, null)
        }
    } catch (_: Exception) {}
    return true
}

// Consultation Result PDF Preview & Export Modal Dialog
@Composable
fun ConsultationResultPdfDialog(
    record: com.example.tabibconnect.data.model.ConsultationRecord,
    onDismiss: () -> Unit,
    onEdit: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showExportSuccessModal by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(com.example.tabibconnect.ui.theme.TealLightContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Résultat & Ordonnance (PDF)",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkNavy
                    )
                    Text(
                        text = "Consultation du ${record.date} • ${record.doctorName}",
                        fontSize = 11.5.sp,
                        color = Color.Gray
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Official Document Container
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, TealPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(record.doctorName, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = PrimaryDarkTeal)
                                Text("${record.specialtyName} • TN-12845", fontSize = 11.sp, color = Color.Gray)
                            }
                            Surface(color = com.example.tabibconnect.ui.theme.TealLightContainer, shape = RoundedCornerShape(6.dp)) {
                                Text("Certifié 🟢", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TealPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFE2E8F0))

                        Text("Patient : ${record.patientName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDarkNavy)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Description / Diagnostic
                        Text("1. Description & Diagnostic Médical :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryDarkTeal)
                        Text(record.diagnosis.ifBlank { "Examen médical normal." }, fontSize = 12.sp, color = Color(0xFF334155))

                        Spacer(modifier = Modifier.height(8.dp))

                        // Nombre de jours de repos / Arrêt de travail
                        Text("2. Repos & Arrêt de travail :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryDarkTeal)
                        if (record.restDays > 0) {
                            Surface(
                                color = Color(0xFFFEF2F2),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "🔴 Arrêt de travail : ${record.restDays} jour(s) de repos médical prescrit(s)",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC5221F),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Text("Aucun arrêt de travail prescrit.", fontSize = 12.sp, color = Color(0xFF334155))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Recommandations
                        if (record.recommendations.isNotBlank()) {
                            Text("3. Recommandations :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryDarkTeal)
                            Text(record.recommendations, fontSize = 12.sp, color = Color(0xFF334155))
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Prescription
                        if (record.prescription != null && record.prescription.medications.isNotEmpty()) {
                            Text("4. Prescription & Manière d'utilisation :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryDarkTeal)
                            Column(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                record.prescription.medications.forEach { med ->
                                    Surface(
                                        color = Color(0xFFF8FAF9),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("• $med", fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = TextDarkNavy, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stamp/Signature
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(color = Color(0xFFE6F4EA), shape = RoundedCornerShape(8.dp)) {
                                Text("✍️ Certifié & Signé par TabibConnect", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF137333), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }

                // Buttons inside modal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onEdit != null) {
                        OutlinedButton(
                            onClick = {
                                onEdit()
                                onDismiss()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Modifier", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            printConsultationResult(context, record)
                            showExportSuccessModal = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Imprimer", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fermer", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )

    if (showExportSuccessModal) {
        AlertDialog(
            onDismissRequest = { showExportSuccessModal = false },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp))
            },
            title = {
                Text("PDF Généré & Imprimé !", fontWeight = FontWeight.Bold, color = TextDarkNavy, fontSize = 18.sp)
            },
            text = {
                Text(
                    text = "Le compte-rendu de consultation et l'ordonnance de ${record.patientName} ont été exportés au format PDF et sauvegardés avec succès dans vos Téléchargements.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showExportSuccessModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

// Single Document Detailed Preview & Remark Modal for Doctor Consultation
@Composable
fun DoctorViewSingleDocumentDetailModal(
    doc: com.example.tabibconnect.data.model.MedicalDocument,
    patientName: String,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val shareItem = doc.shares.firstOrNull { !it.isRevoked } ?: doc.shares.firstOrNull()
    val shareId = shareItem?.id ?: ""

    var doctorRemarkInput by remember { mutableStateOf(shareItem?.doctorRemark ?: "") }
    var isSavingRemark by remember { mutableStateOf(false) }
    var showRemarkSuccessModal by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(com.example.tabibconnect.ui.theme.TealLightContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Consulter le Document Médical", fontSize = 16.5.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                    Text(
                        text = doc.title,
                        fontSize = 12.5.sp,
                        color = TealPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = Color(0xFFF8FAF9),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Patient : $patientName", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Catégorie : ${doc.category.labelFr}", fontSize = 12.sp, color = TextDarkNavy)
                        Text("Ajouté le : ${doc.dateAdded} • Taille : ${doc.fileSize}", fontSize = 11.5.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color(0xFFE6F4EA),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "🟢 Document Rattaché à la Consultation",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF137333),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        if (doc.notes.isNotBlank() || shareItem?.notes?.isNotBlank() == true) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "💬 Commentaire du patient :\n${shareItem?.notes?.ifBlank { doc.notes } ?: doc.notes}",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF1E40AF),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }

                // Base64 Image Preview if available
                val imageBitmap = remember(doc.fileUrl) {
                    if (doc.fileUrl.contains("data:image") || doc.fileUrl.contains(";base64,")) {
                        try {
                            val base64Part = doc.fileUrl.substringAfter(",")
                            val bytes = android.util.Base64.decode(base64Part, android.util.Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                        } catch (_: Exception) { null }
                    } else null
                }

                if (imageBitmap != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TealPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "Aperçu de la pièce jointe",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Remarque / Compte-rendu du Médecin :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                OutlinedTextField(
                    value = doctorRemarkInput,
                    onValueChange = { doctorRemarkInput = it },
                    placeholder = { Text("Saisir votre analyse ou remarque sur cette pièce jointe...", fontSize = 11.5.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    enabled = !isSavingRemark,
                    onClick = {
                        isSavingRemark = true
                        coroutineScope.launch {
                            TabibRepository.updateDocumentRemarkAsync(
                                shareId = shareId,
                                documentId = doc.id,
                                doctorRemark = doctorRemarkInput
                            )
                            isSavingRemark = false
                            showRemarkSuccessModal = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSavingRemark) {
                        androidx.compose.material3.CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enregistrer la remarque", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fermer", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )

    if (showRemarkSuccessModal) {
        AlertDialog(
            onDismissRequest = { showRemarkSuccessModal = false },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp))
            },
            title = {
                Text("Remarque Enregistrée !", fontWeight = FontWeight.Bold, color = TextDarkNavy, fontSize = 18.sp)
            },
            text = {
                Text(
                    text = "Votre remarque sur le document '${doc.title}' a été enregistrée avec succès.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showRemarkSuccessModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

// Doctor View Patient Documents Dialog
@Composable
fun DoctorViewPatientDocumentsDialog(
    appointment: com.example.tabibconnect.data.model.Appointment,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(appointment.id, appointment.patientId, appointment.doctorId) {
        TabibRepository.fetchMedicalDocumentsAsync(
            patientId = appointment.patientId,
            doctorId = appointment.doctorId,
            appointmentId = appointment.id
        )
    }

    val documents = TabibRepository.medicalDocuments
    var docForDetailView by remember { mutableStateOf<com.example.tabibconnect.data.model.MedicalDocument?>(null) }
    var downloadedDocTitleModal by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(com.example.tabibconnect.ui.theme.TealLightContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Documents Médicaux Rattachés",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkNavy
                        )
                        Text(
                            text = "Dossier transmis par ${appointment.patientName}",
                            fontSize = 11.5.sp,
                            color = Color(0xFF6B7C85)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (documents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FolderOff,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aucun document médical rattaché par ce patient.",
                                fontSize = 12.5.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(documents) { doc ->
                            val shareItem = doc.shares.firstOrNull { !it.isRevoked } ?: doc.shares.firstOrNull()

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAF9)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(com.example.tabibconnect.ui.theme.TealLightContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = when (doc.category) {
                                                        com.example.tabibconnect.data.model.DocumentCategory.BIOLOGY -> Icons.Default.Science
                                                        com.example.tabibconnect.data.model.DocumentCategory.IMAGING -> Icons.Default.Image
                                                        com.example.tabibconnect.data.model.DocumentCategory.PRESCRIPTION -> Icons.Default.ReceiptLong
                                                        com.example.tabibconnect.data.model.DocumentCategory.OTHER -> Icons.Default.Description
                                                    },
                                                    contentDescription = null,
                                                    tint = TealPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = doc.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp,
                                                    color = TextDarkNavy,
                                                    maxLines = 2,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${doc.category.labelFr} • ${doc.dateAdded} • ${doc.fileSize}",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Surface(
                                            color = Color(0xFFE6F4EA),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "Rattaché 🟢",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF137333),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (shareItem?.notes?.isNotBlank() == true || doc.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            color = Color(0xFFEFF6FF),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "💬 Commentaire patient : ${shareItem?.notes?.ifBlank { doc.notes } ?: doc.notes}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF1E40AF),
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { docForDetailView = doc },
                                            colors = ButtonDefaults.buttonColors(containerColor = com.example.tabibconnect.ui.theme.TealLightContainer, contentColor = TealPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Consulter", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                com.example.tabibconnect.ui.screens.patient.downloadMedicalFileToDevice(
                                                    context = context,
                                                    docTitle = doc.title,
                                                    categoryLabel = doc.category.labelFr,
                                                    dateAdded = doc.dateAdded,
                                                    patientName = appointment.patientName,
                                                    notes = shareItem?.notes?.ifBlank { doc.notes } ?: doc.notes,
                                                    fileUrl = doc.fileUrl
                                                )
                                                downloadedDocTitleModal = doc.title
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Télécharger", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fermer", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )

    if (docForDetailView != null) {
        DoctorViewSingleDocumentDetailModal(
            doc = docForDetailView!!,
            patientName = appointment.patientName,
            onDismiss = { docForDetailView = null }
        )
    }

    if (downloadedDocTitleModal != null) {
        AlertDialog(
            onDismissRequest = { downloadedDocTitleModal = null },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp))
            },
            title = {
                Text("Document Téléchargé !", fontWeight = FontWeight.Bold, color = TextDarkNavy, fontSize = 18.sp)
            },
            text = {
                Text(
                    text = "Le document '$downloadedDocTitleModal' rattaché par ${appointment.patientName} a été téléchargé avec succès et sauvegardé dans votre dossier Téléchargements (Downloads).",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = { downloadedDocTitleModal = null },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}
