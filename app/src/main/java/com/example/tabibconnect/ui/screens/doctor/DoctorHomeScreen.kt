package com.example.tabibconnect.ui.screens.doctor

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.tabibconnect.data.model.AppointmentStatus
import com.example.tabibconnect.data.model.DoctorScheduleDay
import com.example.tabibconnect.data.repository.TabibRepository
import com.example.tabibconnect.ui.components.StatusBadge
import com.example.tabibconnect.ui.theme.LightTealContainer
import com.example.tabibconnect.ui.theme.PrimaryDarkTeal
import com.example.tabibconnect.ui.theme.PrimaryTeal
import com.example.tabibconnect.ui.theme.TealLightContainer
import com.example.tabibconnect.ui.theme.TealPrimary
import com.example.tabibconnect.ui.theme.TextDarkNavy
import java.util.Calendar

fun isSameMonthAndYear(dateStr: String): Boolean {
    if (dateStr.isBlank() || dateStr.equals("Aujourd'hui", ignoreCase = true)) return true
    return try {
        val currentCal = Calendar.getInstance()
        val curMonth = currentCal.get(Calendar.MONTH) + 1
        val curYear = currentCal.get(Calendar.YEAR)

        val parts = if (dateStr.contains("/")) dateStr.split("/")
        else if (dateStr.contains("-")) dateStr.split("-")
        else emptyList()

        if (parts.size >= 3) {
            val month = parts[1].toInt()
            val year = parts[2].toInt()
            month == curMonth && year == curYear
        } else true
    } catch (_: Exception) {
        true
    }
}

// 1. MAIN DOCTOR DASHBOARD SCREEN ("Tableau")
@Composable
fun DoctorHomeScreen() {
    val currentDoc = TabibRepository.doctorsList.firstOrNull { it.id == "doc_1" }
        ?: TabibRepository.doctorsList.firstOrNull()

    // Fetch dynamic doctor profile, appointments & revenue stats from BDD on launch
    LaunchedEffect(currentDoc?.id) {
        val docId = currentDoc?.id ?: "doc_1"
        TabibRepository.fetchDoctorsAsync()
        TabibRepository.fetchAppointmentsAsync()
        TabibRepository.fetchDoctorRevenueStatsAsync(docId)
    }

    val doctorName = currentDoc?.name ?: "Dr. Mohamed Ben Salah"
    val specialtyName = currentDoc?.specialtyName ?: "Cardiologie"
    val orderNumber = currentDoc?.orderNumber ?: "TN-12845"
    val governorate = currentDoc?.governorate ?: "Tunis"
    val delegation = currentDoc?.delegation ?: "Centre Ville"
    val priceDt = currentDoc?.priceDt ?: 60

    val doctorAppointments = TabibRepository.appointments.filter {
        it.doctorId == (currentDoc?.id ?: "doc_1") || it.doctorName.equals(doctorName, ignoreCase = true)
    }

    // Filter valid appointments (excluding CANCELLED and REJECTED) for stats & counts
    val validAppointments = doctorAppointments.filter {
        it.status != AppointmentStatus.CANCELLED && it.status != AppointmentStatus.REJECTED
    }

    // Revenue calculated ONLY for COMPLETED appointments in the current month based on appointment date
    val completedAppointmentsThisMonth = doctorAppointments.filter { apt ->
        apt.status == AppointmentStatus.COMPLETED && isSameMonthAndYear(apt.date)
    }

    val monthlyEarnings = completedAppointmentsThisMonth.sumOf { it.priceDt }
    val revenueStats = TabibRepository.doctorRevenueStats.value
    val displayMonthlyRevenue = if (revenueStats.totalRevenue > 0) revenueStats.totalRevenue else monthlyEarnings
    val todayRdvCount = if (revenueStats.todayCount > 0) revenueStats.todayCount else validAppointments.size

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Doctor Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Espace Médecin 👨‍⚕️",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryTeal
                )
                Text(
                    text = "Bonjour, $doctorName 👋",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDarkTeal
                )
                Text(
                    text = "$specialtyName • $governorate, $delegation • Ordre: $orderNumber",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LightTealContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = PrimaryTeal,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Revenue Dashboard Card (Revenus du Mois)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryDarkTeal),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Revenus du Mois", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    Text("$displayMonthlyRevenue DT", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("Tarif par consultation : $priceDt DT", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                }

                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("RDV aujourd'hui", fontSize = 11.sp, color = Color.White)
                        Text("$todayRdvCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Revenue Bar Chart (Only COMPLETED appointments)
        val weeklyMap = revenueStats.weeklyRevenues
        val weeklyRevenueData = remember(revenueStats, doctorAppointments) {
            val days = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Auj.")
            days.map { dayKey ->
                val bddVal = weeklyMap[dayKey] ?: 0f
                val displayVal = if (bddVal > 0f) {
                    bddVal
                } else if (dayKey == "Auj.") {
                    doctorAppointments.filter { it.status == AppointmentStatus.COMPLETED }.sumOf { it.priceDt }.toFloat()
                } else {
                    0f
                }
                com.example.tabibconnect.ui.components.BarChartData(dayKey, displayVal)
            }
        }

        com.example.tabibconnect.ui.components.PrettyBarChart(data = weeklyRevenueData)

        Spacer(modifier = Modifier.height(18.dp))

        // Executive Statistics KPI Grid Overview
        Text(
            text = "📊 Vue d'Ensemble des Activités",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryDarkTeal
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(Icons.Default.Videocam, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Téléconsultations", fontSize = 11.sp, color = Color.Gray)
                    Text("${doctorAppointments.count { it.consultationType == "TELECONSULTATION" && it.status == AppointmentStatus.COMPLETED }} effectuées", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(Icons.Default.LocalHospital, contentDescription = null, tint = TextDarkNavy, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Présentielles", fontSize = 11.sp, color = Color.Gray)
                    Text("${doctorAppointments.count { it.consultationType == "IN_PERSON" && it.status == AppointmentStatus.COMPLETED }} au cabinet", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

fun isConsultationTimePassed(aptDateStr: String, aptTimeSlotStr: String, aptStatus: AppointmentStatus): Boolean {
    if (aptStatus == AppointmentStatus.COMPLETED) return true

    return try {
        val now = Calendar.getInstance()
        val curYear = now.get(Calendar.YEAR)
        val curHour = now.get(Calendar.HOUR_OF_DAY)
        val curMinute = now.get(Calendar.MINUTE)

        val cleanDate = aptDateStr.trim()
        if (cleanDate.equals("Demain", ignoreCase = true)) return false

        if (cleanDate.equals("Aujourd'hui", ignoreCase = true)) {
            val startTime = aptTimeSlotStr.split("-").firstOrNull()?.trim() ?: aptTimeSlotStr.trim()
            val timeParts = startTime.split(":")
            if (timeParts.size >= 2) {
                val startHour = timeParts[0].toIntOrNull() ?: 0
                val startMin = timeParts[1].toIntOrNull() ?: 0

                if (curHour > startHour) return true
                if (curHour == startHour && curMinute >= startMin) return true
                return false
            }
            return true
        }

        val parts = if (cleanDate.contains("/")) cleanDate.split("/")
        else if (cleanDate.contains("-")) cleanDate.split("-")
        else emptyList()

        if (parts.size >= 3) {
            val (day, month, year) = if (cleanDate.contains("-") && parts[0].length == 4) {
                Triple(parts[2].toIntOrNull() ?: 1, parts[1].toIntOrNull() ?: 1, parts[0].toIntOrNull() ?: curYear)
            } else {
                Triple(parts[0].toIntOrNull() ?: 1, parts[1].toIntOrNull() ?: 1, parts[2].toIntOrNull() ?: curYear)
            }

            val aptCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)

                val startTime = aptTimeSlotStr.split("-").firstOrNull()?.trim() ?: aptTimeSlotStr.trim()
                val timeParts = startTime.split(":")
                if (timeParts.size >= 2) {
                    set(Calendar.HOUR_OF_DAY, timeParts[0].toIntOrNull() ?: 0)
                    set(Calendar.MINUTE, timeParts[1].toIntOrNull() ?: 0)
                } else {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                }
            }

            return now.after(aptCal) || now.equals(aptCal)
        }

        true
    } catch (_: Exception) {
        true
    }
}

// 2. DEDICATED DOCTOR AGENDA & CONSULTATIONS SCREEN ("Agenda")
@Composable
fun DoctorAgendaScreen(
    onStartCall: (String) -> Unit
) {
    val currentDoc = TabibRepository.doctorsList.firstOrNull { it.id == "doc_1" }
        ?: TabibRepository.doctorsList.firstOrNull()

    val doctorName = currentDoc?.name ?: "Dr. Mohamed Ben Salah"
    val doctorAppointments = TabibRepository.appointments.filter {
        it.doctorId == (currentDoc?.id ?: "doc_1") || it.doctorName.equals(doctorName, ignoreCase = true)
    }

    var selectedFilter by remember { mutableStateOf("TOUS") }
    var appointmentForDocView by remember { mutableStateOf<com.example.tabibconnect.data.model.Appointment?>(null) }
    var appointmentForResult by remember { mutableStateOf<com.example.tabibconnect.data.model.Appointment?>(null) }

    // State for error modal when trying to enter result before date/time
    var showNotYetEffectuatedErrorModalFor by remember { mutableStateOf<com.example.tabibconnect.data.model.Appointment?>(null) }
    // State for PDF result preview modal
    var selectedConsultationRecordForPdf by remember { mutableStateOf<com.example.tabibconnect.data.model.ConsultationRecord?>(null) }

    // State for initial OUI / NON action confirmation prompt modals
    var appointmentToConfirmPrompt by remember { mutableStateOf<com.example.tabibconnect.data.model.Appointment?>(null) }
    var appointmentToRefusePrompt by remember { mutableStateOf<com.example.tabibconnect.data.model.Appointment?>(null) }

    // State for confirmation success notification modals after OUI is clicked
    var showConfirmSuccessDialogFor by remember { mutableStateOf<com.example.tabibconnect.data.model.Appointment?>(null) }
    var showRefuseSuccessDialogFor by remember { mutableStateOf<com.example.tabibconnect.data.model.Appointment?>(null) }
    var showResultSuccessDialogFor by remember { mutableStateOf<com.example.tabibconnect.data.model.Appointment?>(null) }

    val filteredAppointments = remember(selectedFilter, doctorAppointments) {
        when (selectedFilter) {
            "CONFIRMED" -> doctorAppointments.filter { it.status == AppointmentStatus.CONFIRMED }
            "PENDING" -> doctorAppointments.filter { it.status == AppointmentStatus.PENDING }
            "COMPLETED" -> doctorAppointments.filter { it.status == AppointmentStatus.COMPLETED }
            else -> doctorAppointments
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "📅 Agenda & Consultations",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryDarkTeal
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Pills Row (Horizontally Scrollable)
        val pFilterScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(pFilterScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("TOUS", "Tous", doctorAppointments.size),
                Triple("CONFIRMED", "Confirmés", doctorAppointments.count { it.status == AppointmentStatus.CONFIRMED }),
                Triple("PENDING", "En attente", doctorAppointments.count { it.status == AppointmentStatus.PENDING }),
                Triple("COMPLETED", "Terminés", doctorAppointments.count { it.status == AppointmentStatus.COMPLETED })
            ).forEach { (filterKey, label, count) ->
                val isSelected = selectedFilter == filterKey
                Surface(
                    onClick = { selectedFilter = filterKey },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) TealPrimary else Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) TealPrimary else Color(0xFFE2E8F0))
                ) {
                    Text(
                        text = "$label ($count)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else TextDarkNavy,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredAppointments.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Aucune consultation enregistrée dans cette catégorie.",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7C85),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                filteredAppointments.forEach { apt ->
                    val isVideo = apt.consultationType == "TELECONSULTATION"
                    val existingRecord = TabibRepository.consultationRecords.firstOrNull { it.appointmentId == apt.id }

                    Card(
                        shape = RoundedCornerShape(16.dp),
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
                                Column {
                                    Text(
                                        text = apt.patientName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = PrimaryDarkTeal
                                    )
                                    Text(
                                        text = "Bénéficiaire : ${apt.beneficiary}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                StatusBadge(status = apt.status)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    color = if (isVideo) TealLightContainer else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.LocalHospital,
                                            contentDescription = null,
                                            tint = if (isVideo) TealPrimary else TextDarkNavy,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isVideo) "Téléconsultation Vidéo" else "Présentiel en Cabinet",
                                            fontSize = 10.sp,
                                            color = if (isVideo) TealPrimary else TextDarkNavy,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Date : ${apt.date} à ${apt.timeSlot} • Motif: ${apt.motif}",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )

                            Text(
                                text = "Montant : ${apt.priceDt} DT (${apt.paymentMethod})",
                                fontSize = 12.sp,
                                color = PrimaryTeal,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { appointmentForDocView = apt },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("📄 Documents", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }

                                if (apt.status != AppointmentStatus.CANCELLED && apt.status != AppointmentStatus.REJECTED) {
                                    Button(
                                        onClick = {
                                            if (!isConsultationTimePassed(apt.date, apt.timeSlot, apt.status)) {
                                                showNotYetEffectuatedErrorModalFor = apt
                                            } else {
                                                if (existingRecord != null) {
                                                    selectedConsultationRecordForPdf = existingRecord
                                                } else {
                                                    appointmentForResult = apt
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (existingRecord != null) PrimaryTeal else Color(0xFF10B981)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (existingRecord != null) Icons.Default.PictureAsPdf else Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (existingRecord != null) "📄 Résultat (PDF)" else "✍️ Résultat",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (apt.status == AppointmentStatus.PENDING) {
                                    Button(
                                        onClick = { appointmentToConfirmPrompt = apt },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Confirmer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { appointmentToRefusePrompt = apt },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Refuser", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else if (apt.status == AppointmentStatus.CONFIRMED) {
                                    if (isVideo) {
                                        Button(
                                            onClick = { onStartCall(apt.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Videocam,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Démarrer Téléconsultation", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (appointmentForDocView != null) {
        com.example.tabibconnect.ui.components.DoctorViewPatientDocumentsDialog(
            appointment = appointmentForDocView!!,
            onDismiss = { appointmentForDocView = null }
        )
    }

    if (appointmentForResult != null) {
        com.example.tabibconnect.ui.components.DoctorConsultationResultDialog(
            appointment = appointmentForResult!!,
            onDismiss = { appointmentForResult = null },
            onSuccess = {
                val completedApt = appointmentForResult
                appointmentForResult = null
                showResultSuccessDialogFor = completedApt
            }
        )
    }

    // ERROR MODAL: Consultation date & time has not occurred yet
    if (showNotYetEffectuatedErrorModalFor != null) {
        val apt = showNotYetEffectuatedErrorModalFor!!
        AlertDialog(
            onDismissRequest = { showNotYetEffectuatedErrorModalFor = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text("Consultation Non Effectuée", fontWeight = FontWeight.Bold, color = TextDarkNavy, fontSize = 18.sp)
            },
            text = {
                Text(
                    text = "Impossible de saisir le résultat. La consultation de ${apt.patientName} prévue le ${apt.date} à ${apt.timeSlot} n'a pas encore été effectuée.",
                    fontSize = 13.5.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showNotYetEffectuatedErrorModalFor = null },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
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

    // PDF Consultation Result Preview & Print Modal
    if (selectedConsultationRecordForPdf != null) {
        val rec = selectedConsultationRecordForPdf!!
        com.example.tabibconnect.ui.components.ConsultationResultPdfDialog(
            record = rec,
            onDismiss = { selectedConsultationRecordForPdf = null },
            onEdit = {
                val matchingApt = TabibRepository.appointments.firstOrNull { it.id == rec.appointmentId }
                    ?: TabibRepository.appointments.firstOrNull { it.patientName.equals(rec.patientName, ignoreCase = true) }
                    ?: com.example.tabibconnect.data.model.Appointment(
                        id = rec.appointmentId,
                        patientId = "pat_1",
                        patientName = rec.patientName,
                        doctorId = "doc_1",
                        doctorName = rec.doctorName,
                        specialtyName = rec.specialtyName,
                        date = rec.date,
                        timeSlot = "10:00",
                        motif = rec.diagnosis,
                        beneficiary = "Moi-même",
                        priceDt = 60,
                        status = com.example.tabibconnect.data.model.AppointmentStatus.COMPLETED,
                        isPaid = true
                    )
                appointmentForResult = matchingApt
            }
        )
    }

    // PROMPT DIALOG 1: Confirmation Question for "Confirmer" (OUI / NON)
    if (appointmentToConfirmPrompt != null) {
        val apt = appointmentToConfirmPrompt!!
        AlertDialog(
            onDismissRequest = { appointmentToConfirmPrompt = null },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(44.dp))
            },
            title = {
                Text("Confirmer le Rendez-vous ?", fontWeight = FontWeight.Bold, color = TextDarkNavy, fontSize = 18.sp)
            },
            text = {
                Text(
                    text = "Voulez-vous vraiment confirmer le rendez-vous de ${apt.patientName} prévu le ${apt.date} à ${apt.timeSlot} ?",
                    fontSize = 13.5.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetApt = appointmentToConfirmPrompt
                        appointmentToConfirmPrompt = null
                        if (targetApt != null) {
                            TabibRepository.updateAppointmentStatus(targetApt.id, AppointmentStatus.CONFIRMED)
                            showConfirmSuccessDialogFor = targetApt
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OUI", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 14.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { appointmentToConfirmPrompt = null },
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Text("NON", fontWeight = FontWeight.Bold, color = Color(0xFF475569), fontSize = 14.sp)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    // PROMPT DIALOG 2: Rejection Question for "Refuser" (OUI / NON)
    if (appointmentToRefusePrompt != null) {
        val apt = appointmentToRefusePrompt!!
        AlertDialog(
            onDismissRequest = { appointmentToRefusePrompt = null },
            icon = {
                Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(44.dp))
            },
            title = {
                Text("Refuser le Rendez-vous ?", fontWeight = FontWeight.Bold, color = TextDarkNavy, fontSize = 18.sp)
            },
            text = {
                Text(
                    text = "Voulez-vous vraiment refuser le rendez-vous de ${apt.patientName} prévu le ${apt.date} à ${apt.timeSlot} ?",
                    fontSize = 13.5.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetApt = appointmentToRefusePrompt
                        appointmentToRefusePrompt = null
                        if (targetApt != null) {
                            TabibRepository.updateAppointmentStatus(targetApt.id, AppointmentStatus.REJECTED)
                            showRefuseSuccessDialogFor = targetApt
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OUI", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 14.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { appointmentToRefusePrompt = null },
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Text("NON", fontWeight = FontWeight.Bold, color = Color(0xFF475569), fontSize = 14.sp)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    // SUCCESS MODAL 1: Confirmation success dialog after OUI
    if (showConfirmSuccessDialogFor != null) {
        val apt = showConfirmSuccessDialogFor!!
        AlertDialog(
            onDismissRequest = { showConfirmSuccessDialogFor = null },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp))
            },
            title = {
                Text("Rendez-vous Confirmé !", fontWeight = FontWeight.Bold, color = TextDarkNavy, fontSize = 18.sp)
            },
            text = {
                Text(
                    text = "Le rendez-vous avec ${apt.patientName} prévu le ${apt.date} à ${apt.timeSlot} a été confirmé avec succès dans la base de données.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showConfirmSuccessDialogFor = null },
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

    // SUCCESS MODAL 2: Rejection success dialog after OUI
    if (showRefuseSuccessDialogFor != null) {
        val apt = showRefuseSuccessDialogFor!!
        AlertDialog(
            onDismissRequest = { showRefuseSuccessDialogFor = null },
            icon = {
                Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(48.dp))
            },
            title = {
                Text("Rendez-vous Refusé !", fontWeight = FontWeight.Bold, color = TextDarkNavy, fontSize = 18.sp)
            },
            text = {
                Text(
                    text = "Le rendez-vous de ${apt.patientName} pour le ${apt.date} à ${apt.timeSlot} a été refusé.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showRefuseSuccessDialogFor = null },
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

    // SUCCESS MODAL 3: Result success dialog after saving consultation result
    if (showResultSuccessDialogFor != null) {
        val apt = showResultSuccessDialogFor!!
        AlertDialog(
            onDismissRequest = { showResultSuccessDialogFor = null },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp))
            },
            title = {
                Text("Résultat Enregistré !", fontWeight = FontWeight.Bold, color = TextDarkNavy, fontSize = 18.sp)
            },
            text = {
                Text(
                    text = "Le compte-rendu de consultation et l'ordonnance pour ${apt.patientName} ont été sauvegardés et rattachés avec succès.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showResultSuccessDialogFor = null },
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

// 3. DEDICATED DOCTOR SCHEDULE & AVAILABILITY SCREEN ("Horaire")
@Composable
fun DoctorScheduleScreen() {
    val currentDoc = TabibRepository.doctorsList.firstOrNull { it.id == "doc_1" }
        ?: TabibRepository.doctorsList.firstOrNull()

    val context = LocalContext.current
    var isSavingSchedule by remember { mutableStateOf(false) }
    var showScheduleSuccessModal by remember { mutableStateOf(false) }

    val editableSchedule = remember { mutableStateListOf<DoctorScheduleDay>() }

    LaunchedEffect(Unit) {
        TabibRepository.fetchDoctorScheduleAsync(currentDoc?.id ?: "doc_1")
        TabibRepository.fetchDoctorVacationsAsync(currentDoc?.id ?: "doc_1")
    }

    LaunchedEffect(TabibRepository.doctorSchedule.toList()) {
        editableSchedule.clear()
        editableSchedule.addAll(TabibRepository.doctorSchedule)
    }

    // 7 Upcoming Chronological Dates starting from Today
    val upcoming7DaysList = remember {
        val list = mutableListOf<Pair<String, String>>()
        val cal = Calendar.getInstance()
        val dateFormat = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
        for (i in 0..6) {
            val dayNameFr = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Lundi"
                Calendar.TUESDAY -> "Mardi"
                Calendar.WEDNESDAY -> "Mercredi"
                Calendar.THURSDAY -> "Jeudi"
                Calendar.FRIDAY -> "Vendredi"
                Calendar.SATURDAY -> "Samedi"
                Calendar.SUNDAY -> "Dimanche"
                else -> "Lundi"
            }
            val dateFormatted = dateFormat.format(cal.time)
            val label = when (i) {
                0 -> "Aujourd'hui"
                1 -> "Demain"
                else -> "$dayNameFr $dateFormatted"
            }
            list.add(Pair(dayNameFr, label))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        list
    }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Vacation Date Picker Dialog
    val nowCal = Calendar.getInstance()
    var vYear by remember { mutableIntStateOf(nowCal.get(Calendar.YEAR)) }
    var vMonth by remember { mutableIntStateOf(nowCal.get(Calendar.MONTH)) }
    var vDay by remember { mutableIntStateOf(nowCal.get(Calendar.DAY_OF_MONTH)) }

    val vacationDatePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedVDate = String.format(java.util.Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)
            coroutineScope.launch {
                TabibRepository.addDoctorVacationAsync(currentDoc?.id ?: "doc_1", formattedVDate, "Congé / Repos")
            }
        },
        vYear, vMonth, vDay
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Paramétrage des Horaires & Disponibilités",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryDarkTeal
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Section 1: 7 Prochains Jours (Triés Chronologiquement)
        Card(
            shape = RoundedCornerShape(18.dp),
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
                    Column {
                        Text(
                            text = "Horaires des 7 Prochains Jours",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDarkTeal,
                            fontSize = 15.sp
                        )
                        Text("Définissez vos heures d'ouverture et de fermeture", fontSize = 11.sp, color = Color.Gray)
                    }

                    Surface(color = TealLightContainer, shape = RoundedCornerShape(8.dp)) {
                        Text("30 min/créneau", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TealPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                upcoming7DaysList.forEach { (dayNameFr, fullLabel) ->
                    val matchingSchedIdx = editableSchedule.indexOfFirst { it.dayNameFr.equals(dayNameFr, ignoreCase = true) }
                    val sched = if (matchingSchedIdx != -1) {
                        editableSchedule[matchingSchedIdx]
                    } else {
                        val isSun = dayNameFr.equals("Dimanche", ignoreCase = true)
                        DoctorScheduleDay(dayNameFr = dayNameFr, isOpen = !isSun, startTime = "08:30", endTime = "17:30")
                    }

                    var startDropdownOpen by remember { mutableStateOf(false) }
                    var endDropdownOpen by remember { mutableStateOf(false) }

                    val startOptions = listOf("08:00", "08:30", "09:00", "09:30", "10:00")
                    val endOptions = listOf("12:00", "13:00", "16:00", "17:00", "17:30", "18:00", "19:00")

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (sched.isOpen) Color(0xFFF8FAF9) else Color(0xFFFEF2F2)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (sched.isOpen) Color(0xFFE2E8F0) else Color(0xFFFCA5A5)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (sched.isOpen) Color(0xFFE6F4EA) else Color(0xFFFCE8E6),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (sched.isOpen) "🟢 Ouvert" else "🔴 Fermé",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sched.isOpen) Color(0xFF137333) else Color(0xFFC5221F),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = fullLabel,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextDarkNavy
                                    )
                                }

                                Switch(
                                    checked = sched.isOpen,
                                    onCheckedChange = { openState ->
                                        if (matchingSchedIdx != -1) {
                                            editableSchedule[matchingSchedIdx] = sched.copy(isOpen = openState)
                                        } else {
                                            editableSchedule.add(sched.copy(isOpen = openState))
                                        }
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TealPrimary)
                                )
                            }

                            if (sched.isOpen) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Start Time Selector
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Début : ", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                                        Box {
                                            Surface(
                                                color = Color.White,
                                                shape = RoundedCornerShape(8.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                modifier = Modifier.clickable { startDropdownOpen = true }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(sched.startTime, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                                                }
                                            }

                                            DropdownMenu(
                                                expanded = startDropdownOpen,
                                                onDismissRequest = { startDropdownOpen = false }
                                            ) {
                                                startOptions.forEach { t ->
                                                    DropdownMenuItem(
                                                        text = { Text(t, fontSize = 12.sp) },
                                                        onClick = {
                                                            if (matchingSchedIdx != -1) {
                                                                editableSchedule[matchingSchedIdx] = sched.copy(startTime = t)
                                                            }
                                                            startDropdownOpen = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // End Time Selector
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Fin : ", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                                        Box {
                                            Surface(
                                                color = Color.White,
                                                shape = RoundedCornerShape(8.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                modifier = Modifier.clickable { endDropdownOpen = true }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(sched.endTime, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                                                }
                                            }

                                            DropdownMenu(
                                                expanded = endDropdownOpen,
                                                onDismissRequest = { endDropdownOpen = false }
                                            ) {
                                                endOptions.forEach { t ->
                                                    DropdownMenuItem(
                                                        text = { Text(t, fontSize = 12.sp) },
                                                        onClick = {
                                                            if (matchingSchedIdx != -1) {
                                                                editableSchedule[matchingSchedIdx] = sched.copy(endTime = t)
                                                            }
                                                            endDropdownOpen = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save Schedule Button
                Button(
                    enabled = !isSavingSchedule,
                    onClick = {
                        isSavingSchedule = true
                        coroutineScope.launch {
                            TabibRepository.saveDoctorScheduleAsync(
                                doctorId = currentDoc?.id ?: "doc_1",
                                updatedSchedule = editableSchedule.toList()
                            )
                            isSavingSchedule = false
                            showScheduleSuccessModal = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSavingSchedule) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enregistrer", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section 2: Calendrier des Congés Exceptionnels
        Card(
            shape = RoundedCornerShape(18.dp),
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
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color(0xFFC5221F), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Congés Exceptionnels", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDarkNavy)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { vacationDatePicker.show() },
                        colors = ButtonDefaults.buttonColors(containerColor = TealLightContainer, contentColor = TealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ajouter", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Sélectionnez une date future via le calendrier pour déclarer une fermeture exceptionelle :", fontSize = 11.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(10.dp))

                val vacations = TabibRepository.doctorVacations
                if (vacations.isEmpty()) {
                    Surface(
                        color = Color(0xFFF8FAF9),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Aucune date de congé exceptionnel enregistrée.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        vacations.forEach { vac ->
                            Surface(
                                color = Color(0xFFFEF2F2),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🔴 Congé le : ${vac.vacationDate}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFC5221F))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("(${vac.reason})", fontSize = 11.sp, color = Color.Gray)
                                    }

                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                TabibRepository.deleteDoctorVacationAsync(vac.id, currentDoc?.id ?: "doc_1")
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color(0xFFC5221F), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Schedule Save Confirmation Dialog
    if (showScheduleSuccessModal) {
        AlertDialog(
            onDismissRequest = { showScheduleSuccessModal = false },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(48.dp))
            },
            title = {
                Text("Horaires Enregistrés !", fontWeight = FontWeight.Bold, color = TextDarkNavy, fontSize = 18.sp)
            },
            text = {
                Text(
                    text = "Vos heures de travail et jours de fermeture ont été sauvegardés avec succès dans la base de données.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showScheduleSuccessModal = false },
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

// 4. DEDICATED DOCTOR PATIENTS SCREEN ("Patients")
@Composable
fun DoctorPatientsScreen() {
    val currentDoc = TabibRepository.doctorsList.firstOrNull { it.id == "doc_1" }
        ?: TabibRepository.doctorsList.firstOrNull()

    val doctorName = currentDoc?.name ?: "Dr. Mohamed Ben Salah"
    val doctorAppointments = TabibRepository.appointments.filter {
        it.doctorId == (currentDoc?.id ?: "doc_1") || it.doctorName.equals(doctorName, ignoreCase = true)
    }

    val uniquePatients = doctorAppointments.map { it.patientName }.distinct()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Gestion de vos Patients",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryDarkTeal
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (uniquePatients.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucun patient enregistré pour le moment.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                uniquePatients.forEach { patientName ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(LightTealContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(22.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(patientName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDarkNavy)
                                Text("Patient suivi • Suivi régulier", fontSize = 11.sp, color = Color.Gray)
                            }

                            OutlinedButton(
                                onClick = { /* View dossier */ },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Dossier", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryTeal)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// 5. DEDICATED DOCTOR NOTIFICATIONS SCREEN ("Notifs")
@Composable
fun DoctorNotificationsScreen() {
    val currentDoc = TabibRepository.doctorsList.firstOrNull { it.id == "doc_1" }
        ?: TabibRepository.doctorsList.firstOrNull()
    val docId = currentDoc?.id ?: "doc_1"

    var selectedFilter by remember { mutableStateOf("ALL") }
    val notificationsList = TabibRepository.notifications

    val nonPaymentNotifications = remember(notificationsList.toList()) {
        notificationsList.filter {
            !it.title.contains("Paiement", ignoreCase = true) &&
            !it.message.contains("Paiement", ignoreCase = true) &&
            !it.title.contains("e-Dinar", ignoreCase = true) &&
            !it.title.contains("Abonnement", ignoreCase = true) &&
            !it.message.contains("Abonnement", ignoreCase = true) &&
            !it.title.contains("Plateforme", ignoreCase = true)
        }
    }

    val filteredNotifications = remember(selectedFilter, nonPaymentNotifications) {
        when (selectedFilter) {
            "CONFIRMED" -> nonPaymentNotifications.filter { it.type == com.example.tabibconnect.data.model.NotificationType.CONFIRMED || it.type == com.example.tabibconnect.data.model.NotificationType.COMPLETED }
            "MODIFIED" -> nonPaymentNotifications.filter { it.type == com.example.tabibconnect.data.model.NotificationType.MODIFIED }
            "CANCELLED" -> nonPaymentNotifications.filter { it.type == com.example.tabibconnect.data.model.NotificationType.CANCELLED || it.type == com.example.tabibconnect.data.model.NotificationType.REJECTED }
            "ADDED" -> nonPaymentNotifications.filter { it.type == com.example.tabibconnect.data.model.NotificationType.ADDED }
            else -> nonPaymentNotifications
        }
    }

    LaunchedEffect(docId) {
        TabibRepository.fetchNotificationsAsync(docId)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(TealLightContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Centre de Notifications",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkTeal
                    )
                    Text(
                        text = "Événements et alertes sur vos rendez-vous",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

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
                Text("Tout marquer comme lu", fontSize = 12.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
            }

            TextButton(
                onClick = { TabibRepository.clearNotifications() },
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Effacer tout", fontSize = 12.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips Row
        val filterOptions = listOf(
            Triple("ALL", "Tous", notificationsList.size),
            Triple("CONFIRMED", "Validés 🟢", notificationsList.count { it.type == com.example.tabibconnect.data.model.NotificationType.CONFIRMED || it.type == com.example.tabibconnect.data.model.NotificationType.COMPLETED }),
            Triple("MODIFIED", "Modifiés 🔵", notificationsList.count { it.type == com.example.tabibconnect.data.model.NotificationType.MODIFIED }),
            Triple("CANCELLED", "Annulés 🔴", notificationsList.count { it.type == com.example.tabibconnect.data.model.NotificationType.CANCELLED || it.type == com.example.tabibconnect.data.model.NotificationType.REJECTED }),
            Triple("ADDED", "Réservés 🟡", notificationsList.count { it.type == com.example.tabibconnect.data.model.NotificationType.ADDED })
        )

        val notifFilterScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(notifFilterScrollState),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filterOptions.forEach { (key, label, count) ->
                val isSel = selectedFilter == key
                Surface(
                    onClick = { selectedFilter = key },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) TealPrimary else Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) TealPrimary else Color(0xFFE2E8F0))
                ) {
                    Text(
                        text = "$label ($count)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else TextDarkNavy,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notifications List
        if (filteredNotifications.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Aucune notification dans cette catégorie.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filteredNotifications.forEach { notif ->
                    val (badgeBg, badgeTextColor, iconVec) = when (notif.type) {
                        com.example.tabibconnect.data.model.NotificationType.CONFIRMED,
                        com.example.tabibconnect.data.model.NotificationType.COMPLETED -> Triple(Color(0xFFE6F4EA), Color(0xFF137333), Icons.Default.CheckCircle)
                        com.example.tabibconnect.data.model.NotificationType.MODIFIED -> Triple(Color(0xFFE8F0FE), Color(0xFF1A73E8), Icons.Default.EditCalendar)
                        com.example.tabibconnect.data.model.NotificationType.CANCELLED,
                        com.example.tabibconnect.data.model.NotificationType.REJECTED -> Triple(Color(0xFFFCE8E6), Color(0xFFC5221F), Icons.Default.Cancel)
                        com.example.tabibconnect.data.model.NotificationType.ADDED -> Triple(Color(0xFFFEF7E0), Color(0xFFB06000), Icons.Default.NotificationsActive)
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
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
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(badgeBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconVec,
                                    contentDescription = null,
                                    tint = badgeTextColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = notif.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = TextDarkNavy,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = notif.timestamp,
                                            fontSize = 10.5.sp,
                                            color = Color.Gray
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

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Supprimer",
                                            tint = Color.Gray,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { TabibRepository.deleteNotification(notif.id) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = notif.message,
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
