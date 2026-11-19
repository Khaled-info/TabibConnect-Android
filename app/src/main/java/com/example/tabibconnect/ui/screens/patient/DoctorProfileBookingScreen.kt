package com.example.tabibconnect.ui.screens.patient

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.tabibconnect.data.repository.TabibRepository
import com.example.tabibconnect.ui.components.InitialsAvatar
import com.example.tabibconnect.ui.theme.PrimaryDarkTeal
import com.example.tabibconnect.ui.theme.PrimaryTeal
import com.example.tabibconnect.ui.theme.TealLightContainer
import com.example.tabibconnect.ui.theme.TealPrimary
import com.example.tabibconnect.ui.theme.TextDarkNavy
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorProfileBookingScreen(
    doctorId: String,
    onBookingSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val doctor = TabibRepository.doctorsList.firstOrNull { it.id == doctorId }
        ?: TabibRepository.doctorsList.firstOrNull()

    if (doctor == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAF9)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Médecin non disponible dans la base de données.", fontWeight = FontWeight.Bold, color = PrimaryDarkTeal)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBackClick) { Text("Retour à la recherche") }
            }
        }
        return
    }

    val patient = TabibRepository.currentPatient.value
    val context = LocalContext.current

    LaunchedEffect(doctor.id) {
        if (patient.id.isNotBlank()) {
            TabibRepository.fetchRelativesAsync(patient.id)
            TabibRepository.fetchMedicalDocumentsAsync(patientId = patient.id)
        } else {
            TabibRepository.fetchMedicalDocumentsAsync()
        }
        TabibRepository.fetchDoctorScheduleAsync(doctor.id)
        TabibRepository.fetchDoctorVacationsAsync(doctor.id)
        TabibRepository.fetchAppointmentsAsync(doctor.id)
    }

    // Dynamic Calendar Date State
    val calendar = Calendar.getInstance()
    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    var showRestDayDialog by remember { mutableStateOf(false) }
    var showPastDateTimeDialog by remember { mutableStateOf(false) }

    val formattedDateStr = remember(selectedYear, selectedMonth, selectedDay) {
        String.format(java.util.Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
    }

    val selectedDayOfWeekFr = remember(selectedYear, selectedMonth, selectedDay) {
        val cal = Calendar.getInstance().apply {
            set(selectedYear, selectedMonth, selectedDay)
        }
        when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Lundi"
            Calendar.TUESDAY -> "Mardi"
            Calendar.WEDNESDAY -> "Mercredi"
            Calendar.THURSDAY -> "Jeudi"
            Calendar.FRIDAY -> "Vendredi"
            Calendar.SATURDAY -> "Samedi"
            Calendar.SUNDAY -> "Dimanche"
            else -> "Lundi"
        }
    }

    val isSlotInPast = remember(selectedYear, selectedMonth, selectedDay) {
        { slot: String ->
            val now = Calendar.getInstance()
            val slotCal = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay)
                val parts = slot.split(":")
                val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
                val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            slotCal.before(now)
        }
    }

    val doctorScheduleForDay = remember(selectedDayOfWeekFr, TabibRepository.doctorSchedule.toList()) {
        TabibRepository.doctorSchedule.firstOrNull { it.dayNameFr.equals(selectedDayOfWeekFr, ignoreCase = true) }
    }

    val isVacationDate = remember(formattedDateStr, TabibRepository.doctorVacations.toList()) {
        TabibRepository.doctorVacations.any { it.vacationDate == formattedDateStr }
    }

    val isDayOpen = doctorScheduleForDay?.isOpen
        ?: (!selectedDayOfWeekFr.equals("Dimanche", ignoreCase = true))

    val isDoctorClosedOnSelectedDate = !isDayOpen || isVacationDate

    // Date Picker Dialog Launcher
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val tempCal = Calendar.getInstance().apply { set(year, month, dayOfMonth, 23, 59, 59) }
            val nowCal = Calendar.getInstance()
            if (tempCal.before(nowCal)) {
                showPastDateTimeDialog = true
                return@DatePickerDialog
            }

            val tempDateStr = String.format(java.util.Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)
            val tempDayOfWeek = when (tempCal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Lundi"
                Calendar.TUESDAY -> "Mardi"
                Calendar.WEDNESDAY -> "Mercredi"
                Calendar.THURSDAY -> "Jeudi"
                Calendar.FRIDAY -> "Vendredi"
                Calendar.SATURDAY -> "Samedi"
                Calendar.SUNDAY -> "Dimanche"
                else -> "Lundi"
            }

            val isVac = TabibRepository.doctorVacations.any { it.vacationDate == tempDateStr }
            val sched = TabibRepository.doctorSchedule.firstOrNull { it.dayNameFr.equals(tempDayOfWeek, ignoreCase = true) }
            val tempIsOpen = sched?.isOpen ?: (!tempDayOfWeek.equals("Dimanche", ignoreCase = true))
            val isClosed = !tempIsOpen || isVac

            selectedYear = year
            selectedMonth = month
            selectedDay = dayOfMonth

            if (isClosed) {
                showRestDayDialog = true
            }
        },
        selectedYear,
        selectedMonth,
        selectedDay
    ).apply {
        val minCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        datePicker.minDate = minCal.timeInMillis
    }

    // Time Slots every 30 minutes
    val timeSlotsEvery30Min = listOf(
        "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
        "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"
    )

    val availableTimeSlotsForDay = remember(doctorScheduleForDay, timeSlotsEvery30Min) {
        if (doctorScheduleForDay != null && doctorScheduleForDay.startTime.isNotBlank() && doctorScheduleForDay.endTime.isNotBlank()) {
            val start = doctorScheduleForDay.startTime
            val end = doctorScheduleForDay.endTime
            timeSlotsEvery30Min.filter { slot -> slot >= start && slot < end }
                .ifEmpty { timeSlotsEvery30Min }
        } else {
            timeSlotsEvery30Min
        }
    }

    // Occupied / Booked Slots on Selected Date
    val takenTimeSlotsOnSelectedDate = remember(formattedDateStr, doctor.id, TabibRepository.appointments.toList()) {
        TabibRepository.appointments
            .filter { (it.doctorId == doctor.id || it.doctorName.equals(doctor.name, ignoreCase = true)) &&
                      it.date == formattedDateStr &&
                      it.status != AppointmentStatus.CANCELLED &&
                      it.status != AppointmentStatus.REJECTED }
            .map { it.timeSlot }
            .toSet()
    }

    var selectedTimeSlot by remember { mutableStateOf("10:00") }

    LaunchedEffect(selectedYear, selectedMonth, selectedDay, formattedDateStr, takenTimeSlotsOnSelectedDate, availableTimeSlotsForDay) {
        val available = availableTimeSlotsForDay.firstOrNull { slot ->
            val notTaken = !takenTimeSlotsOnSelectedDate.contains(slot)
            val notPassed = !isSlotInPast(slot)
            notTaken && notPassed
        }
        if (available != null) {
            selectedTimeSlot = available
        }
    }

    var motif by remember { mutableStateOf("") }
    var selectedBeneficiary by remember { mutableStateOf("Moi-même (${patient.name})") }
    var isFavorite by remember { mutableStateOf(doctor.isFavorite) }
    var isBookingProgress by remember { mutableStateOf(false) }

    val cleanDocName = doctor.name.replace("Dr.", "", ignoreCase = true).replace("Dr", "", ignoreCase = true).trim()
    val doctorInitials = cleanDocName.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }.take(2).joinToString("").ifBlank { "DOC" }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F8FA))
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Retour",
                    tint = TextDarkNavy,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = "Prise de Rendez-vous",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = TextDarkNavy
            )

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { isFavorite = !isFavorite },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favori",
                    tint = if (isFavorite) Color.Red else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Doctor Info Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InitialsAvatar(
                    initials = doctorInitials,
                    backgroundColor = TealPrimary,
                    textColor = Color.White,
                    size = 60.dp,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = doctor.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextDarkNavy
                    )
                    Text(
                        text = "${doctor.specialtyName} • ${doctor.governorate}, ${doctor.delegation}",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7C85)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(color = TealLightContainer, shape = RoundedCornerShape(8.dp)) {
                            Text("30 min / séance", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TealPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                        Surface(color = Color(0xFFFDE8E4), shape = RoundedCornerShape(8.dp)) {
                            Text("${doctor.priceDt} DT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC85A48), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mode de Consultation (Présentiel en Cabinet)
        Text("Mode de Consultation", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = TealLightContainer),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, TealPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalHospital, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Consultation en Cabinet", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDarkNavy)
                    Text("Présentiel • Sur rendez-vous", fontSize = 11.sp, color = Color(0xFF6B7C85))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Choisir la Date avec Calendrier
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Choisir une date", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)

            OutlinedButton(
                onClick = { datePickerDialog.show() },
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TealPrimary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(formattedDateStr, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TealPrimary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected Date Banner Display (Layout fixed with weight)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }
        ) {
            Row(
                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.EventAvailable, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Date sélectionnée : $formattedDateStr ($selectedDayOfWeekFr)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextDarkNavy
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Modifier",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isDoctorClosedOnSelectedDate) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color(0xFFC5221F), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "🔴 Jour de repos le $selectedDayOfWeekFr",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC5221F)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Le médecin ne consulte pas le $selectedDayOfWeekFr ($formattedDateStr). Veuillez sélectionner une autre date.",
                            fontSize = 11.sp,
                            color = Color(0xFF7F1D1D)
                        )
                    }
                }
            }
        } else {
            // Créneaux disponibles (Chaque 30 minutes, hors créneaux réservés)
            Text("Créneaux disponibles (Durée : 30 min)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                availableTimeSlotsForDay.chunked(3).forEach { rowSlots ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowSlots.forEach { slot ->
                            val isTaken = takenTimeSlotsOnSelectedDate.contains(slot)
                            val isPassed = isSlotInPast(slot)
                            val isSelected = selectedTimeSlot == slot && !isTaken && !isPassed

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isTaken || isPassed -> Color(0xFFF1F5F9)
                                        isSelected -> TealPrimary
                                        else -> Color.White
                                    }
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    when {
                                        isTaken || isPassed -> Color(0xFFCBD5E1)
                                        isSelected -> TealPrimary
                                        else -> Color(0xFFE2E8F0)
                                    }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (isPassed) {
                                            showPastDateTimeDialog = true
                                        } else if (!isTaken) {
                                            selectedTimeSlot = slot
                                        }
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when {
                                            isTaken -> "$slot 🚫"
                                            isPassed -> "$slot ⌛"
                                            else -> slot
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = when {
                                            isTaken || isPassed -> Color(0xFF94A3B8)
                                            isSelected -> Color.White
                                            else -> TextDarkNavy
                                        }
                                    )
                                }
                            }
                        }
                        if (rowSlots.size < 3) {
                            Spacer(modifier = Modifier.weight((3 - rowSlots.size).toFloat()))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Pour qui est ce rendez-vous ?
        Text("Pour qui est ce rendez-vous ?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
        Spacer(modifier = Modifier.height(10.dp))

        val beneficiariesList = remember(patient.name, patient.relatives) {
            listOf(Triple("pat_me", "Moi-même (${patient.name.ifBlank { "Patient" }})", "Moi-même")) +
                    patient.relatives.map { Triple(it.id, "${it.name} (${it.relation})", it.relation) }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                beneficiariesList.forEach { (_, displayLabel, relType) ->
                    val isSelected = selectedBeneficiary == displayLabel
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) TealLightContainer else Color(0xFFF8FAF9),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) TealPrimary else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth().clickable { selectedBeneficiary = displayLabel }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (relType == "Moi-même") Icons.Default.Person else Icons.Default.FamilyRestroom,
                                    contentDescription = null,
                                    tint = if (isSelected) TealPrimary else TextDarkNavy,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = displayLabel,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) TealPrimary else TextDarkNavy
                                )
                            }

                            Button(
                                onClick = { selectedBeneficiary = displayLabel },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) TealPrimary else Color.White,
                                    contentColor = if (isSelected) Color.White else TealPrimary
                                ),
                                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, TealPrimary) else null,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(if (isSelected) "Sélectionné 🟢" else "Sélectionner", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Motif de la consultation
        Text("Motif de la consultation", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = motif,
            onValueChange = { motif = it },
            placeholder = { Text("Décrivez vos symptômes ou le motif...", color = Color(0xFF94A3B8), fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = Color(0xFFE2E8F0)
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Pièce jointe / Rattachement de document médical (Optionnel)
        Text("📎 Pièce jointe / Document médical (Optionnel)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
        Text("Rattachez un bilan, une radio ou une ordonnance pour le médecin", fontSize = 11.5.sp, color = Color(0xFF6B7C85))
        Spacer(modifier = Modifier.height(10.dp))

        var attachDocumentOption by remember { mutableStateOf("NONE") }
        var selectedDocIdForBooking by remember { mutableStateOf("") }

        val patientMedicalDocs = TabibRepository.medicalDocuments

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = attachDocumentOption == "NONE",
                        onClick = { attachDocumentOption = "NONE"; selectedDocIdForBooking = "" },
                        label = { Text("Aucune", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = attachDocumentOption == "EXISTING",
                        onClick = { attachDocumentOption = "EXISTING" },
                        label = { Text("📋 Vos Bilans / Docs", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TealLightContainer,
                            selectedLabelColor = TealPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (attachDocumentOption == "EXISTING") {
                    if (patientMedicalDocs.isEmpty()) {
                        Text(
                            text = "Aucun document à sélectionner",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    } else {
                        Text("Sélectionnez le document rattaché à votre espace docs :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                        patientMedicalDocs.forEach { doc ->
                            val isSel = selectedDocIdForBooking == doc.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) TealLightContainer else Color(0xFFF8FAF9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) TealPrimary else Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth().clickable { selectedDocIdForBooking = doc.id }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(doc.title, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                                        Text("${doc.category.labelFr} • ${doc.dateAdded} • ${doc.fileSize}", fontSize = 10.5.sp, color = Color.Gray)
                                    }
                                    if (isSel) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Confirmation Action Button
        Button(
            enabled = !isBookingProgress && !isDoctorClosedOnSelectedDate,
            onClick = {
                if (isSlotInPast(selectedTimeSlot)) {
                    showPastDateTimeDialog = true
                    return@Button
                }
                if (isDoctorClosedOnSelectedDate) {
                    showRestDayDialog = true
                    return@Button
                }
                isBookingProgress = true
                val newApt = TabibRepository.bookAppointment(
                    doctorId = doctor.id,
                    date = formattedDateStr,
                    timeSlot = selectedTimeSlot,
                    motif = motif.ifBlank { "Consultation générale" },
                    beneficiary = selectedBeneficiary,
                    paymentMethod = "Paiement au cabinet"
                )

                if (attachDocumentOption == "EXISTING" && selectedDocIdForBooking.isNotBlank()) {
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        TabibRepository.attachDocumentToAppointmentAsync(
                            documentId = selectedDocIdForBooking,
                            appointmentId = newApt.id,
                            doctorId = doctor.id,
                            doctorName = doctor.name,
                            days = 30
                        )
                    }
                }

                isBookingProgress = false
                onBookingSuccess()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDoctorClosedOnSelectedDate) Color(0xFF94A3B8) else TealPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isBookingProgress) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
            } else {
                Text(
                    text = if (isDoctorClosedOnSelectedDate) "Jour de repos (Indisponible)" else "Confirmer le RDV",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }

    // Popup Alert Dialog for Rest Days / Vacations
    if (showRestDayDialog) {
        AlertDialog(
            onDismissRequest = { showRestDayDialog = false },
            icon = {
                Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color(0xFFC5221F), modifier = Modifier.size(44.dp))
            },
            title = {
                Text(
                    text = "Jour de Repos du Médecin",
                    fontWeight = FontWeight.Bold,
                    color = TextDarkNavy,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "Le médecin ne consulte pas le $selectedDayOfWeekFr ($formattedDateStr).\n\nIl s'agit d'un jour de fermeture ou de congé exceptionnel. Veuillez sélectionner une autre date disponible.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestDayDialog = false
                        datePickerDialog.show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Choisir une autre date", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    // Popup Alert Dialog for Past Date / Time Selection Error
    if (showPastDateTimeDialog) {
        AlertDialog(
            onDismissRequest = { showPastDateTimeDialog = false },
            icon = {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFC5221F), modifier = Modifier.size(44.dp))
            },
            title = {
                Text(
                    text = "Date ou Heure Dépassée",
                    fontWeight = FontWeight.Bold,
                    color = TextDarkNavy,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "Impossible de réserver un rendez-vous pour une date ou un créneau horaire déjà passé.\n\nVeuillez sélectionner une date et un créneau horaire valide à venir.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPastDateTimeDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Compris", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}
