package com.example.tabibconnect.ui.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tabibconnect.data.model.AppointmentStatus
import com.example.tabibconnect.data.repository.TabibRepository
import com.example.tabibconnect.ui.components.EmergencyBanner
import com.example.tabibconnect.ui.components.RatingStars
import com.example.tabibconnect.ui.theme.LightTealContainer
import com.example.tabibconnect.ui.theme.PrimaryDarkTeal
import com.example.tabibconnect.ui.theme.PrimaryTeal
import com.example.tabibconnect.ui.theme.TealLightContainer
import com.example.tabibconnect.ui.theme.TealPrimary
import com.example.tabibconnect.ui.theme.TextDarkNavy

fun calculatePatientAge(birthDateStr: String): String {
    if (birthDateStr.isBlank()) return "29 ans"
    return try {
        val parts = if (birthDateStr.contains("/")) {
            birthDateStr.split("/")
        } else if (birthDateStr.contains("-")) {
            birthDateStr.split("-")
        } else emptyList()

        if (parts.size >= 3) {
            val birthYear = if (parts[0].length == 4) parts[0].toInt() else parts[2].toInt()
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val age = (currentYear - birthYear).coerceAtLeast(1)
            "$age ans"
        } else "29 ans"
    } catch (_: Exception) {
        "29 ans"
    }
}

@Composable
fun PatientHomeScreen(
    onNavigateToSearch: (String?) -> Unit,
    onNavigateToDoctorProfile: (String) -> Unit,
    onNavigateToCall: (String) -> Unit,
    onNavigateToDocuments: () -> Unit
) {
    val patient = TabibRepository.currentPatient.value
    val scrollState = rememberScrollState()

    var isAddMetricOpen by remember { mutableStateOf(false) }
    var selectedChartMetric by remember { mutableStateOf("HEART_RATE") }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    // Fetch doctors, appointments, health metrics & notifications from SQL Server Database on launch
    LaunchedEffect(patient.id, patient.userId) {
        TabibRepository.fetchDoctorsAsync()
        TabibRepository.fetchAppointmentsAsync(patient.id, patient.userId)
        TabibRepository.fetchHealthMetricsAsync(patient.id)
        TabibRepository.fetchNotificationsAsync(patient.id)
    }

    // Find next upcoming active appointment
    val upcomingApt = TabibRepository.appointments.firstOrNull {
        it.status == AppointmentStatus.CONFIRMED || it.status == AppointmentStatus.PENDING
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Welcome Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Bonjour, ${patient.name.ifBlank { "Patient" }} 👋",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDarkTeal
                )
                Text(
                    text = "Adresse: ${patient.governorate}, ${patient.delegation} | Âge: ${calculatePatientAge(patient.birthDate)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            IconButton(
                onClick = { showNotificationsDialog = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(LightTealContainer)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = PrimaryDarkTeal
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SAMU Emergency Banner
        EmergencyBanner()

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Actions Grid (Recherche Médecin & Consultation en Cabinet)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Rechercher un Médecin Action
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToSearch(null) }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(TealLightContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Trouver un Médecin", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDarkNavy)
                    Text("Recherche & Filtres", fontSize = 11.sp, color = Color.Gray)
                }
            }

            // Consultation en Cabinet Action
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToSearch(null) }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2F4F3)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("En Cabinet", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDarkNavy)
                    Text("RDV en présentiel", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar Trigger
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToSearch(null) }
        ) {
            Row(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = PrimaryTeal
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Rechercher par nom, spécialité, ville ou délégation...",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Specialties Horizontal List (Full 15 Specialties)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Spécialités Médicales (15)",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryDarkTeal
            )
            TextButton(onClick = { onNavigateToSearch(null) }) {
                Text("Voir tout", color = PrimaryTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(TabibRepository.specialties) { specialty ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = LightTealContainer),
                    modifier = Modifier
                        .width(115.dp)
                        .clickable { onNavigateToSearch(specialty.nameFr) }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(PrimaryTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = specialty.nameFr,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = PrimaryDarkTeal,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Next Appointment Section
        Text(
            text = "Prochain rendez-vous",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryDarkTeal
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (upcomingApt != null) {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(Color(0xFF1E1B4B), Color(0xFF2563EB), Color(0xFF0D9488))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalHospital,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = upcomingApt.doctorName,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "${upcomingApt.specialtyName} • Présentiel en Cabinet 🩺",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${upcomingApt.date} à ${upcomingApt.timeSlot}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = { onNavigateToSearch(null) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = PrimaryTeal
                                ),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Voir Cabinet", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        } else {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.EventBusy,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pas de rendez-vous prévu pour le moment.",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7C85),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { onNavigateToSearch(null) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Prendre un rendez-vous", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Health Tracking Header with + Saisir Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Suivi de Santé",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDarkTeal
                )
                Text("Bilan et évolution de vos constantes", fontSize = 11.sp, color = Color.Gray)
            }

            Button(
                onClick = { isAddMetricOpen = true },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Saisir Constante", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dynamic Health Graph Card with Metric Selector Dropdown inside Header
        var showMetricDropdown by remember { mutableStateOf(false) }

        val filteredMetricRecords = TabibRepository.healthRecordsHistory.filter { it.metricType == selectedChartMetric }
        val chartValues = if (filteredMetricRecords.isNotEmpty()) {
            filteredMetricRecords.map { it.metricValue }.reversed()
        } else if (selectedChartMetric == "HEART_RATE") {
            TabibRepository.healthPulseData.toList()
        } else emptyList()

        val chartLabels = if (filteredMetricRecords.isNotEmpty()) {
            filteredMetricRecords.map { it.recordedDate.take(8) }.reversed()
        } else if (selectedChartMetric == "HEART_RATE") {
            TabibRepository.healthDaysLabels.toList()
        } else emptyList()

        val metricTitle = when (selectedChartMetric) {
            "TENSION" -> "Tension Artérielle"
            "GLYCEMIE" -> "Glycémie (Sucre)"
            "TEMPERATURE" -> "Température"
            "WEIGHT" -> "Suivi du Poids"
            else -> "Fréquence Cardiaque (Pouls)"
        }

        val metricUnit = when (selectedChartMetric) {
            "TENSION" -> "mmHg"
            "GLYCEMIE" -> "g/L"
            "TEMPERATURE" -> "°C"
            "WEIGHT" -> "kg"
            else -> "BPM"
        }

        val selectedMetricLabel = when (selectedChartMetric) {
            "TENSION" -> "🩸 Tension"
            "GLYCEMIE" -> "🍬 Sucre"
            "TEMPERATURE" -> "🌡️ Temp."
            "WEIGHT" -> "⚖️ Poids"
            else -> "❤️ Pouls"
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Graphique - $metricTitle",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PrimaryDarkTeal
                        )
                        Text(
                            text = if (chartValues.isNotEmpty()) "Données issues de la BDD (${chartValues.size} mesures)" else "Aucune donnée enregistrée",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    // Metric Switcher Dropdown Button directly inside Header
                    Box {
                        OutlinedButton(
                            onClick = { showMetricDropdown = true },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(selectedMetricLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryDarkTeal)
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(16.dp))
                        }

                        DropdownMenu(
                            expanded = showMetricDropdown,
                            onDismissRequest = { showMetricDropdown = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("❤️ Fréquence Cardiaque (Pouls)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedChartMetric = "HEART_RATE"
                                    showMetricDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🩸 Tension Artérielle", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedChartMetric = "TENSION"
                                    showMetricDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🍬 Glycémie (Sucre)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedChartMetric = "GLYCEMIE"
                                    showMetricDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🌡️ Température Corporelle", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedChartMetric = "TEMPERATURE"
                                    showMetricDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("⚖️ Suivi du Poids", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedChartMetric = "WEIGHT"
                                    showMetricDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (chartValues.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pas de données enregistrées",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7C85),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { isAddMetricOpen = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Saisir cette constante", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    val maxVal = (chartValues.maxOrNull() ?: 100f).coerceAtLeast(1f)
                    val minVal = (chartValues.minOrNull() ?: 0f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            color = LightTealContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "${chartValues.last().toInt()} $metricUnit",
                                color = PrimaryTeal,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        val width = size.width
                        val height = size.height
                        val spacing = if (chartValues.size > 1) width / (chartValues.size - 1) else width

                        val points = chartValues.mapIndexed { index, value ->
                            val x = index * spacing
                            val normalizedY = if (maxVal != minVal) (value - minVal) / (maxVal - minVal) else 0.5f
                            val y = height - (normalizedY * (height * 0.7f) + height * 0.15f)
                            androidx.compose.ui.geometry.Offset(x, y)
                        }

                        val fillPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, height)
                            points.forEach { point -> lineTo(point.x, point.y) }
                            lineTo(width, height)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(PrimaryTeal.copy(alpha = 0.35f), Color.Transparent)
                            )
                        )

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
                        chartLabels.forEach { label ->
                            Text(text = label, fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recommended Doctors Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Médecins recommandés",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryDarkTeal
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (TabibRepository.doctorsList.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(22.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalInformation,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pas de données pour le moment",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7C85),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            TabibRepository.doctorsList.take(3).forEach { doc ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onNavigateToDoctorProfile(doc.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(LightTealContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = PrimaryTeal,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = doc.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextDarkNavy
                            )
                            Text(
                                text = "${doc.specialtyName} • ${doc.governorate}, ${doc.delegation}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            RatingStars(rating = doc.rating, reviewCount = doc.reviewCount)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${doc.priceDt} DT",
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "En Cabinet",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }

    // Dynamic Modal to Add Health Metrics (Tension, Glycémie, Pouls, Température, Poids, Maladies)
    if (isAddMetricOpen) {
        com.example.tabibconnect.ui.components.AddHealthMetricDialog(
            onDismiss = { isAddMetricOpen = false },
            onSuccess = { isAddMetricOpen = false }
        )
    }

    if (showNotificationsDialog) {
        com.example.tabibconnect.ui.components.NotificationSpaceDialog(
            onDismiss = { showNotificationsDialog = false }
        )
    }
}
