package com.example.tabibconnect.data.repository

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.tabibconnect.data.database.optStringAny
import com.example.tabibconnect.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object TabibRepository {

    var currentUserRole = mutableStateOf(UserRole.PATIENT)
    var currentLanguage = mutableStateOf("FR") // "FR" or "AR"

    val governorates = listOf(
        "Tunis", "Ariana", "Ben Arous", "Manouba",
        "Nabeul", "Zaghouan", "Bizerte", "Béja",
        "Jendouba", "Siliana", "Le Kef", "Sousse",
        "Monastir", "Mahdia", "Sfax", "Kairouan",
        "Kasserine", "Sidi Bouzid", "Gabès", "Medenine",
        "Tataouine", "Gafsa", "Tozeur", "Kébili"
    )

    val governorateDelegationsMap = mapOf(
        "Tunis" to listOf("Centre Ville", "La Marsa", "El Menzah", "Carthage", "Le Bardo", "Sidi Bou Saïd"),
        "Ariana" to listOf("Ariana Ville", "Soukra", "Mnihla", "Raoued"),
        "Ben Arous" to listOf("Ben Arous", "Mégrine", "Ezzahra", "Hammam Lif", "Rades"),
        "Manouba" to listOf("Manouba", "Denden", "Oued Ellil", "Tebourba"),
        "Nabeul" to listOf("Nabeul", "Hammamet", "Grombalia", "Kelibia", "Korba"),
        "Zaghouan" to listOf("Zaghouan", "El Fahs", "Zriba"),
        "Bizerte" to listOf("Bizerte Nord", "Menzel Bourguiba", "Mateur", "Ras Jebel"),
        "Béja" to listOf("Béja Nord", "Medjez El Bab", "Téboursouk"),
        "Jendouba" to listOf("Jendouba Ville", "Tabarka", "Aïn Draham"),
        "Siliana" to listOf("Siliana Ville", "Makthar", "Bou Arada"),
        "Le Kef" to listOf("Le Kef Ville", "Dahmani", "Tajerouine"),
        "Sousse" to listOf("Sousse Ville", "Kantaoui", "Hammam Sousse", "Sousse Riadh"),
        "Monastir" to listOf("Monastir Ville", "Ksar Hellal", "Moknine", "Jemmal"),
        "Mahdia" to listOf("Mahdia Ville", "Ksour Essef", "El Jem"),
        "Sfax" to listOf("Sfax Ville", "Sakiet Ezzit", "Sakiet Eddaïer", "Sfax Sud"),
        "Kairouan" to listOf("Kairouan Ville", "Bou Hajla", "Oueslatia"),
        "Kasserine" to listOf("Kasserine Nord", "Thala", "Sbeitla", "Feriana"),
        "Sidi Bouzid" to listOf("Sidi Bouzid Ville", "Regueb", "Meknassy"),
        "Gabès" to listOf("Gabès Ville", "El Hamma", "Mareth"),
        "Medenine" to listOf("Médenine Ville", "Djerba Houmt Souk", "Djerba Midoun", "Zarzis"),
        "Tataouine" to listOf("Tataouine Nord", "Tataouine Sud", "Ghomrassen"),
        "Gafsa" to listOf("Gafsa Ville", "Metlaoui", "El Ksar"),
        "Tozeur" to listOf("Tozeur Ville", "Nefta", "Degache"),
        "Kébili" to listOf("Kébili Ville", "Douz", "Souk Lahad")
    )

    val specialties = listOf(
        Specialty("1", "Cardiologie", "أمراض القلب", "Favorite"),
        Specialty("2", "Pédiatrie", "طب الأطفال", "ChildCare"),
        Specialty("3", "Dermatologie", "أمراض الجلد", "Face"),
        Specialty("4", "Gynécologie-Obstétrique", "أمراض النساء والتوليد", "PregnantWoman"),
        Specialty("5", "Médecine Générale", "الطب العام", "Healing"),
        Specialty("6", "Orthopédie", "جراحة العظام", "Accessibility"),
        Specialty("7", "Ophtalmologie", "طب العيون", "Visibility"),
        Specialty("8", "Neurologie", "أمراض الأعصاب", "Psychology"),
        Specialty("9", "Psychiatrie", "الطب النفسي", "Mood"),
        Specialty("10", "ORL", "أنف وأذن وحنجرة", "Hearing"),
        Specialty("11", "Gastro-entérologie", "أمراض الجهاز الهضمي", "Restaurant"),
        Specialty("12", "Dentiste", "طب الأسنان", "CleanHands"),
        Specialty("13", "Pneumologie", "أمراض الرئة والجهاز التنفسي", "Air"),
        Specialty("14", "Urologie", "أمراض المسالك البولية", "WaterDrop"),
        Specialty("15", "Endocrinologie", "أمراض الغدد والصماء", "MonitorHeart")
    )

    val doctorsList = mutableStateListOf<Doctor>()

    val currentPatient = mutableStateOf(
        Patient(
            id = "pat_1",
            userId = "u_pat1",
            birthDate = "14/05/1990",
            gender = "Homme",
            governorate = "Kasserine",
            bloodType = "O+",
            allergies = "Pénicilline",
            antecedents = "Hypertension artérielle sous traitement",
            relatives = emptyList()
        )
    )

    val appointments = mutableStateListOf<Appointment>()

    val notifications = mutableStateListOf<AppNotification>()

    fun addNotification(
        title: String,
        message: String,
        type: NotificationType,
        appointmentId: String? = null,
        doctorName: String = "",
        patientName: String = ""
    ) {
        val newNotif = AppNotification(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            timestamp = "Aujourd'hui",
            type = type,
            appointmentId = appointmentId,
            doctorName = doctorName,
            patientName = patientName,
            isRead = false
        )
        notifications.add(0, newNotif)
    }

    suspend fun fetchNotificationsAsync(userId: String = "") {
        val currentRole = currentUserRole.value
        val uid = if (userId.isNotBlank()) userId else if (currentRole == UserRole.DOCTOR) "doc_1" else currentPatient.value.id.ifBlank { "pat_1" }
        val dbNotifs = com.example.tabibconnect.data.database.SqlServerDatabase.fetchNotifications(uid)
        withContext(Dispatchers.Main) {
            notifications.clear()
            notifications.addAll(dbNotifs)
        }
    }

    fun markNotificationAsRead(notifId: String) {
        val idx = notifications.indexOfFirst { it.id == notifId }
        if (idx != -1) {
            notifications[idx] = notifications[idx].copy(isRead = true)
        }
        val currentRole = currentUserRole.value
        val uid = if (currentRole == UserRole.DOCTOR) "doc_1" else currentPatient.value.id.ifBlank { "pat_1" }
        CoroutineScope(Dispatchers.IO).launch {
            com.example.tabibconnect.data.database.SqlServerDatabase.markNotificationRead(notificationId = notifId, userId = uid)
        }
    }

    fun markAllNotificationsAsRead() {
        for (i in 0 until notifications.size) {
            notifications[i] = notifications[i].copy(isRead = true)
        }
        val currentRole = currentUserRole.value
        val uid = if (currentRole == UserRole.DOCTOR) "doc_1" else currentPatient.value.id.ifBlank { "pat_1" }
        CoroutineScope(Dispatchers.IO).launch {
            com.example.tabibconnect.data.database.SqlServerDatabase.markNotificationRead(userId = uid, markAll = true)
        }
    }

    fun deleteNotification(notifId: String) {
        notifications.removeAll { it.id == notifId }
        val currentRole = currentUserRole.value
        val uid = if (currentRole == UserRole.DOCTOR) "doc_1" else currentPatient.value.id.ifBlank { "pat_1" }
        CoroutineScope(Dispatchers.IO).launch {
            com.example.tabibconnect.data.database.SqlServerDatabase.deleteNotifications(notificationId = notifId, userId = uid)
        }
    }

    fun clearNotifications() {
        notifications.clear()
        val currentRole = currentUserRole.value
        val uid = if (currentRole == UserRole.DOCTOR) "doc_1" else currentPatient.value.id.ifBlank { "pat_1" }
        CoroutineScope(Dispatchers.IO).launch {
            com.example.tabibconnect.data.database.SqlServerDatabase.deleteNotifications(userId = uid, clearAll = true)
        }
    }

    val medicalDocuments = mutableStateListOf<MedicalDocument>()

    val consultationRecords = mutableStateListOf(
        ConsultationRecord(
            id = "rec_1",
            appointmentId = "apt_1",
            doctorName = "Dr. Mohamed Ben Salah",
            patientName = "Amine Ben Ammor",
            specialtyName = "Cardiologie",
            date = "12/10/2026",
            diagnosis = "Consultation de contrôle cardiologique.",
            recommendations = "Poursuivre le traitement habituel.",
            prescription = Prescription(
                id = "presc_1",
                doctorName = "Dr. Mohamed Ben Salah",
                doctorOrderNumber = "TN-12845",
                date = "12/10/2026",
                medications = listOf("CORDARONE 200mg - 1 comprimé par jour")
            )
        )
    )

    val chatMessages = mutableStateListOf<ChatMessage>()

    suspend fun fetchChatMessagesAsync(userId: String = "", partnerId: String = "") {
        val uId = if (userId.isNotBlank()) userId else if (currentUserRole.value == UserRole.DOCTOR) "doc_1" else currentPatient.value.id.ifBlank { "pat_1" }
        val dbMsgs = com.example.tabibconnect.data.database.SqlServerDatabase.getChatMessages(uId, partnerId)
        withContext(Dispatchers.Main) {
            if (dbMsgs.isNotEmpty()) {
                val merged = (chatMessages + dbMsgs).distinctBy { msg ->
                    "${msg.senderId.trim().lowercase()}_${msg.text.trim()}_${msg.timestamp.trim()}"
                }
                if (chatMessages.size != merged.size || chatMessages != merged) {
                    chatMessages.clear()
                    chatMessages.addAll(merged)
                }
            }
        }
    }

    suspend fun markChatReadAsync(userId: String, partnerId: String) {
        val uId = userId.ifBlank { if (currentUserRole.value == UserRole.DOCTOR) "doc_1" else currentPatient.value.id.ifBlank { "pat_1" } }
        val pId = partnerId.trim()
        withContext(Dispatchers.Main) {
            val updatedList = chatMessages.map { msg ->
                if (msg.senderId.trim() == pId && (msg.receiverId.trim() == uId || msg.receiverId.isBlank())) {
                    msg.copy(isRead = true)
                } else {
                    msg
                }
            }
            chatMessages.clear()
            chatMessages.addAll(updatedList)
        }
        withContext(Dispatchers.IO) {
            com.example.tabibconnect.data.database.SqlServerDatabase.markChatRead(uId, pId)
        }
    }

    suspend fun sendChatMessageAsync(
        senderId: String,
        senderName: String,
        receiverId: String,
        receiverName: String,
        isDoctor: Boolean,
        text: String
    ): Result<ChatMessage> {
        val timeNow = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val tempId = "temp_" + java.util.UUID.randomUUID().toString()
        val localMsg = ChatMessage(
            id = tempId,
            senderId = senderId,
            senderName = senderName,
            receiverId = receiverId,
            receiverName = receiverName,
            isDoctor = isDoctor,
            text = text,
            timestamp = timeNow,
            isRead = false
        )

        withContext(Dispatchers.Main) {
            val exists = chatMessages.any {
                it.senderId.trim().equals(senderId.trim(), ignoreCase = true) &&
                it.text.trim() == text.trim() &&
                it.timestamp == timeNow
            }
            if (!exists) {
                chatMessages.add(localMsg)
            }
        }

        val res = com.example.tabibconnect.data.database.SqlServerDatabase.sendChatMessage(
            senderId = senderId,
            senderName = senderName,
            receiverId = receiverId,
            receiverName = receiverName,
            isDoctor = isDoctor,
            text = text,
            timestamp = timeNow
        )

        if (res.isSuccess) {
            val serverMsg = res.getOrNull()
            if (serverMsg != null) {
                withContext(Dispatchers.Main) {
                    val idx = chatMessages.indexOfFirst { it.id == tempId }
                    if (idx != -1) {
                        chatMessages[idx] = serverMsg
                    }
                }
            }
        }
        return res
    }

    suspend fun loginUserAsync(phone: String, pass: String): Result<User> {
        if (phone.isBlank() || pass.isBlank()) {
            return Result.failure(Exception("Veuillez saisir votre numéro de téléphone et votre mot de passe."))
        }

        val result = com.example.tabibconnect.data.database.SqlServerDatabase.loginUser(phone, pass)
        if (result.isSuccess) {
            val json = result.getOrNull()!!
            val role = if (json.optString("role").equals("DOCTOR", true)) UserRole.DOCTOR else UserRole.PATIENT
            val user = User(
                id = json.optStringAny("id", "Id", fallback = "u_1"),
                name = json.optStringAny("full_name", "fullName", "Name", fallback = "Utilisateur"),
                phone = json.optStringAny("phone", "Phone", fallback = phone),
                email = json.optStringAny("email", "Email", fallback = ""),
                role = role
            )

            withContext(Dispatchers.Main) {
                currentUserRole.value = role
                if (role == UserRole.PATIENT) {
                    val pId = json.optStringAny("patient_id", "patientId", "PatientId", "id", fallback = "pat_1")
                    val gGov = json.optStringAny("governorate", "Governorate", fallback = "Tunis")
                    val dDel = json.optStringAny("delegation", "Delegation", fallback = "Centre Ville")
                    val bDate = json.optStringAny("birth_date", "birthDate", "BirthDate", fallback = "14/05/1990")
                    val gSex = json.optStringAny("gender", "Gender", fallback = "Homme")

                    currentPatient.value = currentPatient.value.copy(
                        id = pId,
                        userId = user.id,
                        name = user.name,
                        phone = user.phone,
                        birthDate = bDate,
                        gender = gSex,
                        governorate = gGov,
                        delegation = dDel
                    )
                    fetchRelativesAsync(pId, user.id)
                }
            }
            return Result.success(user)
        }

        val serverErr = result.exceptionOrNull()?.message
        val finalErrMsg = if (serverErr != null && serverErr.contains("injoignable", ignoreCase = true)) {
            serverErr
        } else {
            "Numéro de téléphone ou mot de passe incorrect. Veuillez vérifier vos identifiants."
        }
        return Result.failure(Exception(finalErrMsg))
    }

    suspend fun addRelativeAsync(name: String, relation: String): Result<Boolean> {
        val pid = currentPatient.value.id
        val uid = currentPatient.value.userId
        val res = com.example.tabibconnect.data.database.SqlServerDatabase.addRelative(pid, name, relation)
        withContext(Dispatchers.Main) {
            addRelative(name, relation)
        }
        fetchRelativesAsync(pid, uid)
        return if (res.isSuccess) Result.success(true) else res
    }

    suspend fun fetchRelativesAsync(patientId: String, userId: String = "") {
        val uId = userId.ifBlank { currentPatient.value.userId }
        val dbRelatives = com.example.tabibconnect.data.database.SqlServerDatabase.getRelatives(patientId, uId)
        withContext(Dispatchers.Main) {
            currentPatient.value = currentPatient.value.copy(
                relatives = dbRelatives
            )
        }
    }

    suspend fun registerUserAsync(
        fullName: String,
        phone: String,
        pass: String,
        governorate: String,
        delegation: String = "Centre",
        gender: String = "Homme",
        birthDate: String = "",
        role: UserRole,
        specialtyName: String = "Cardiologie",
        orderNumber: String = "TN-99999"
    ): Result<Boolean> {
        val regResult = com.example.tabibconnect.data.database.SqlServerDatabase.registerUser(
            fullName, phone, pass, governorate, delegation, gender, birthDate, role, specialtyName, orderNumber
        )
        if (regResult.isSuccess) {
            val json = regResult.getOrNull()!!
            val userId = json.optString("userId", "")
            val patientId = json.optString("patientId", "")
            withContext(Dispatchers.Main) {
                currentUserRole.value = role
                registerUser(
                    fullName = fullName,
                    phone = phone,
                    password = pass,
                    governorate = governorate,
                    delegation = delegation,
                    role = role,
                    specialtyName = specialtyName,
                    orderNumber = orderNumber,
                    userId = userId,
                    patientId = patientId
                )
            }
            return Result.success(true)
        } else {
            return Result.failure(regResult.exceptionOrNull() ?: Exception("Erreur d'inscription MS SQL"))
        }
    }

    suspend fun resetPasswordAsync(phone: String, newPass: String): Result<Boolean> {
        return com.example.tabibconnect.data.database.SqlServerDatabase.resetPassword(phone, newPass)
    }

    suspend fun fetchDoctorsAsync() {
        val dbDoctors = com.example.tabibconnect.data.database.SqlServerDatabase.getDoctors()
        withContext(Dispatchers.Main) {
            doctorsList.clear()
            doctorsList.addAll(dbDoctors)
        }
    }

    suspend fun fetchAppointmentsAsync(patientId: String = "", userId: String = "") {
        val pId = patientId.ifBlank { currentPatient.value.id }
        val uId = userId.ifBlank { currentPatient.value.userId }
        val dbApts = com.example.tabibconnect.data.database.SqlServerDatabase.getAppointments(pId, uId)
        withContext(Dispatchers.Main) {
            appointments.clear()
            appointments.addAll(dbApts)
        }
    }

    val healthPulseData = mutableStateListOf<Float>()
    val healthDaysLabels = mutableStateListOf<String>()

    suspend fun fetchHealthMetricsAsync(patientId: String = "") {
        val pId = patientId.ifBlank { currentPatient.value.id }
        val dbMetrics = com.example.tabibconnect.data.database.SqlServerDatabase.getHealthMetrics(pId)
        withContext(Dispatchers.Main) {
            healthPulseData.clear()
            healthDaysLabels.clear()
            dbMetrics.forEach { (label, value) ->
                healthDaysLabels.add(label)
                healthPulseData.add(value)
            }
        }
    }

    val healthRecordsHistory = mutableStateListOf<HealthMetricRecord>()

    suspend fun addHealthMetricAsync(
        metricType: String,
        metricValue: Float,
        secondaryValue: Float? = null,
        notes: String = "",
        diseaseName: String = ""
    ): Result<Boolean> {
        val pid = currentPatient.value.id
        val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        com.example.tabibconnect.data.database.SqlServerDatabase.addHealthMetric(
            patientId = pid,
            metricType = metricType,
            metricValue = metricValue,
            secondaryValue = secondaryValue,
            recordedDate = dateStr,
            notes = notes,
            diseaseName = diseaseName
        )

        withContext(Dispatchers.Main) {
            val record = HealthMetricRecord(
                patientId = pid,
                metricType = metricType,
                metricValue = metricValue,
                secondaryValue = secondaryValue,
                recordedDate = "Aujourd'hui ${dateStr.takeLast(5)}",
                notes = notes,
                diseaseName = diseaseName
            )
            healthRecordsHistory.add(0, record)
            if (metricType == "HEART_RATE") {
                healthPulseData.add(metricValue)
                healthDaysLabels.add("Auj.")
            }
        }
        return Result.success(true)
    }

    suspend fun fetchHealthMetricsHistoryAsync(patientId: String = "") {
        val pId = patientId.ifBlank { currentPatient.value.id }
        val dbRecords = com.example.tabibconnect.data.database.SqlServerDatabase.getHealthMetricsRecords(pId)
        withContext(Dispatchers.Main) {
            val dbIds = dbRecords.map { it.id }.toSet()
            val localUnsaved = healthRecordsHistory.filter { it.id !in dbIds }
            healthRecordsHistory.clear()
            healthRecordsHistory.addAll(dbRecords + localUnsaved)
        }
    }

    suspend fun updateDoctorProfileAsync(
        doctorId: String,
        priceDt: Int,
        bio: String,
        offersTeleconsultation: Boolean = false,
        offersInPerson: Boolean = true
    ): Result<Boolean> {
        val did = doctorId.ifBlank { "doc_1" }
        com.example.tabibconnect.data.database.SqlServerDatabase.updateDoctorProfile(
            doctorId = did,
            priceDt = priceDt,
            bio = bio,
            offersTeleconsultation = offersTeleconsultation,
            offersInPerson = offersInPerson
        )

        withContext(Dispatchers.Main) {
            val index = doctorsList.indexOfFirst { it.id == did || it.id == "doc_1" }
            if (index != -1) {
                val old = doctorsList[index]
                doctorsList[index] = old.copy(
                    priceDt = priceDt,
                    bio = bio,
                    offersTeleconsultation = offersTeleconsultation,
                    offersInPerson = offersInPerson
                )
            }
        }
        return Result.success(true)
    }

    val defaultScheduleList = listOf(
        DoctorScheduleDay(dayNameFr = "Lundi", isOpen = true, startTime = "08:30", endTime = "17:30"),
        DoctorScheduleDay(dayNameFr = "Mardi", isOpen = true, startTime = "08:30", endTime = "17:30"),
        DoctorScheduleDay(dayNameFr = "Mercredi", isOpen = true, startTime = "08:30", endTime = "17:30"),
        DoctorScheduleDay(dayNameFr = "Jeudi", isOpen = true, startTime = "08:30", endTime = "17:30"),
        DoctorScheduleDay(dayNameFr = "Vendredi", isOpen = true, startTime = "08:30", endTime = "17:30"),
        DoctorScheduleDay(dayNameFr = "Samedi", isOpen = true, startTime = "08:30", endTime = "13:00"),
        DoctorScheduleDay(dayNameFr = "Dimanche", isOpen = false, startTime = "08:30", endTime = "12:00")
    )

    val doctorSchedule = mutableStateListOf<DoctorScheduleDay>().apply {
        addAll(defaultScheduleList)
    }

    suspend fun fetchDoctorScheduleAsync(doctorId: String = "doc_1") {
        val dbSchedules = com.example.tabibconnect.data.database.SqlServerDatabase.getDoctorSchedule(doctorId)
        withContext(Dispatchers.Main) {
            if (dbSchedules.isNotEmpty()) {
                doctorSchedule.clear()
                doctorSchedule.addAll(dbSchedules)
            }
        }
    }

    suspend fun saveDoctorScheduleAsync(doctorId: String = "doc_1", updatedSchedule: List<DoctorScheduleDay>): Result<Boolean> {
        com.example.tabibconnect.data.database.SqlServerDatabase.saveDoctorSchedule(doctorId, updatedSchedule)
        withContext(Dispatchers.Main) {
            doctorSchedule.clear()
            doctorSchedule.addAll(updatedSchedule)
        }
        return Result.success(true)
    }

    val doctorVacations = mutableStateListOf<DoctorVacationDay>()

    suspend fun fetchDoctorVacationsAsync(doctorId: String = "doc_1") {
        val dbVacations = com.example.tabibconnect.data.database.SqlServerDatabase.getDoctorVacations(doctorId)
        withContext(Dispatchers.Main) {
            doctorVacations.clear()
            doctorVacations.addAll(dbVacations)
        }
    }

    suspend fun addDoctorVacationAsync(doctorId: String = "doc_1", vacationDate: String, reason: String = "Congé exceptionnel"): Result<Boolean> {
        val res = com.example.tabibconnect.data.database.SqlServerDatabase.addDoctorVacation(doctorId, vacationDate, reason)
        fetchDoctorVacationsAsync(doctorId)
        return res
    }

    suspend fun deleteDoctorVacationAsync(vacationId: String, doctorId: String = "doc_1"): Result<Boolean> {
        val res = com.example.tabibconnect.data.database.SqlServerDatabase.deleteDoctorVacation(vacationId)
        fetchDoctorVacationsAsync(doctorId)
        return res
    }

    var doctorRevenueStats = mutableStateOf(DoctorRevenueStats())

    suspend fun fetchDoctorRevenueStatsAsync(doctorId: String = "doc_1") {
        val stats = com.example.tabibconnect.data.database.SqlServerDatabase.getDoctorRevenueStats(doctorId)
        withContext(Dispatchers.Main) {
            doctorRevenueStats.value = stats
        }
    }

    fun toggleFavorite(doctorId: String) {
        val index = doctorsList.indexOfFirst { it.id == doctorId }
        if (index != -1) {
            val old = doctorsList[index]
            doctorsList[index] = old.copy(isFavorite = !old.isFavorite)
        }
    }

    fun bookAppointment(
        doctorId: String,
        date: String,
        timeSlot: String,
        motif: String,
        beneficiary: String,
        paymentMethod: String
    ): Appointment {
        val doc = doctorsList.firstOrNull { it.id == doctorId } ?: doctorsList.first()
        val pat = currentPatient.value
        val pName = pat.name.ifBlank { "Patient" }
        val pId = pat.id.ifBlank { "pat_1" }

        val newApt = Appointment(
            patientId = pId,
            patientName = pName,
            doctorId = doc.id,
            doctorName = doc.name,
            specialtyName = doc.specialtyName,
            date = date,
            timeSlot = timeSlot,
            motif = motif,
            beneficiary = beneficiary,
            priceDt = doc.priceDt,
            status = AppointmentStatus.PENDING,
            isPaid = false,
            paymentMethod = paymentMethod.ifBlank { "Paiement au cabinet" }
        )
        appointments.add(0, newApt)

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            com.example.tabibconnect.data.database.SqlServerDatabase.bookAppointment(
                patientId = pId,
                patientName = pName,
                doctorId = doc.id,
                doctorName = doc.name,
                specialtyName = doc.specialtyName,
                date = date,
                timeSlot = timeSlot,
                motif = motif,
                beneficiary = beneficiary,
                priceDt = doc.priceDt,
                paymentMethod = paymentMethod
            )
            fetchNotificationsAsync(pId)
        }

        return newApt
    }

    fun cancelAppointment(appointmentId: String) {
        val index = appointments.indexOfFirst { it.id == appointmentId }
        if (index != -1) {
            val old = appointments[index]
            val updated = old.copy(status = AppointmentStatus.CANCELLED)
            appointments[index] = updated

            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                com.example.tabibconnect.data.database.SqlServerDatabase.updateAppointmentStatus(
                    appointmentId = appointmentId,
                    status = "CANCELLED"
                )
                fetchNotificationsAsync()
            }
        }
    }

    fun updateAppointmentStatus(appointmentId: String, newStatus: AppointmentStatus) {
        val index = appointments.indexOfFirst { it.id == appointmentId }
        if (index != -1) {
            val old = appointments[index]
            appointments[index] = old.copy(status = newStatus)

            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                com.example.tabibconnect.data.database.SqlServerDatabase.updateAppointmentStatus(
                    appointmentId = appointmentId,
                    status = newStatus.name
                )
                fetchNotificationsAsync()
            }
        }
    }

    fun updateAppointmentDateTime(appointmentId: String, newDate: String, newTimeSlot: String) {
        val index = appointments.indexOfFirst { it.id == appointmentId }
        if (index != -1) {
            val old = appointments[index]
            appointments[index] = old.copy(date = newDate, timeSlot = newTimeSlot, status = AppointmentStatus.CONFIRMED)

            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                com.example.tabibconnect.data.database.SqlServerDatabase.updateAppointmentStatus(
                    appointmentId = appointmentId,
                    status = "CONFIRMED",
                    date = newDate,
                    timeSlot = newTimeSlot
                )
                fetchNotificationsAsync()
            }
        }
    }

    fun rejectAppointmentWithReason(appointmentId: String, reason: String) {
        val index = appointments.indexOfFirst { it.id == appointmentId }
        if (index != -1) {
            val old = appointments[index]
            val updatedMotif = if (reason.isNotBlank()) "${old.motif} • Refus: $reason" else old.motif
            appointments[index] = old.copy(motif = updatedMotif, status = AppointmentStatus.REJECTED)

            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                com.example.tabibconnect.data.database.SqlServerDatabase.updateAppointmentStatus(
                    appointmentId = appointmentId,
                    status = "REJECTED",
                    reason = reason
                )
                fetchNotificationsAsync()
            }
        }
    }

    suspend fun fetchMedicalDocumentsAsync(patientId: String = "", doctorId: String = "", appointmentId: String = "") {
        val currentRole = currentUserRole.value
        val pid = if (patientId.isNotBlank()) patientId else if (currentRole == UserRole.PATIENT) currentPatient.value.id.ifBlank { "pat_1" } else ""
        val did = if (doctorId.isNotBlank()) doctorId else if (currentRole == UserRole.DOCTOR) "doc_1" else ""

        val dbDocs = com.example.tabibconnect.data.database.SqlServerDatabase.getMedicalDocuments(patientId = pid, doctorId = did, appointmentId = appointmentId)
        withContext(Dispatchers.Main) {
            val uniqueDocs = if (dbDocs.isNotEmpty()) (medicalDocuments + dbDocs).distinctBy { it.id } else medicalDocuments.distinctBy { it.id }
            medicalDocuments.clear()
            medicalDocuments.addAll(uniqueDocs)
        }
    }

    fun addMedicalDocument(title: String, category: DocumentCategory, fileSize: String = "2.4 Mo", notes: String = "", fileUrl: String = "") {
        val pid = currentPatient.value.id.ifBlank { "pat_1" }
        val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        val tempId = "doc_med_" + java.util.UUID.randomUUID().toString().take(8)
        val newDoc = MedicalDocument(
            id = tempId,
            patientId = pid,
            title = title,
            category = category,
            dateAdded = dateStr,
            fileSize = fileSize,
            notes = notes,
            fileUrl = fileUrl
        )
        medicalDocuments.add(0, newDoc)

        CoroutineScope(Dispatchers.IO).launch {
            com.example.tabibconnect.data.database.SqlServerDatabase.addMedicalDocument(
                patientId = pid,
                title = title,
                category = category.name,
                fileSize = fileSize,
                notes = notes,
                fileUrl = fileUrl
            )
            fetchMedicalDocumentsAsync(patientId = pid)
        }
    }

    suspend fun addMedicalDocumentAsync(
        title: String,
        category: DocumentCategory,
        fileSize: String = "2.4 Mo",
        notes: String = "",
        fileUrl: String = ""
    ): Result<Boolean> {
        val pid = currentPatient.value.id.ifBlank { "pat_1" }
        val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())

        val res = com.example.tabibconnect.data.database.SqlServerDatabase.addMedicalDocument(
            patientId = pid,
            title = title,
            category = category.name,
            fileSize = fileSize,
            notes = notes,
            fileUrl = fileUrl
        )

        val newDoc = MedicalDocument(
            id = res.getOrNull() ?: ("doc_med_" + java.util.UUID.randomUUID().toString().take(8)),
            patientId = pid,
            title = title,
            category = category,
            dateAdded = dateStr,
            fileSize = fileSize,
            notes = notes,
            fileUrl = fileUrl
        )
        withContext(Dispatchers.Main) {
            medicalDocuments.add(0, newDoc)
        }

        fetchMedicalDocumentsAsync(patientId = pid)
        return Result.success(res.isSuccess)
    }

    suspend fun deleteMedicalDocumentAsync(documentId: String): Result<Boolean> {
        val pid = currentPatient.value.id.ifBlank { "pat_1" }
        withContext(Dispatchers.Main) {
            medicalDocuments.removeAll { it.id == documentId }
        }
        val res = com.example.tabibconnect.data.database.SqlServerDatabase.deleteMedicalDocument(documentId)
        fetchMedicalDocumentsAsync(patientId = pid)
        return res
    }

    suspend fun shareDocumentAsync(
        docId: String = "",
        doctorId: String,
        doctorName: String,
        days: Int,
        title: String = "",
        category: DocumentCategory = DocumentCategory.BIOLOGY,
        notes: String = ""
    ): Result<Boolean> {
        val pid = currentPatient.value.id.ifBlank { "pat_1" }
        val expStr = if (days <= 0 || days >= 999) "Sans limite de durée" else "$days jours"

        val res = try {
            com.example.tabibconnect.data.database.SqlServerDatabase.shareDocument(
                documentId = docId,
                doctorId = doctorId,
                doctorName = doctorName,
                days = days,
                title = title,
                category = category.name,
                notes = notes,
                patientId = pid
            )
        } catch (e: Exception) {
            Result.failure(e)
        }

        withContext(Dispatchers.Main) {
            val index = medicalDocuments.indexOfFirst { it.id == docId }
            if (index != -1) {
                val doc = medicalDocuments[index]
                val newShares = doc.shares.toMutableList()
                newShares.add(
                    DocumentShare(
                        doctorId = doctorId,
                        doctorName = doctorName,
                        startDate = "Aujourd'hui",
                        expiryDate = expStr,
                        notes = notes
                    )
                )
                medicalDocuments[index] = doc.copy(shares = newShares)
            } else if (title.isNotBlank()) {
                val newDoc = MedicalDocument(
                    patientId = pid,
                    title = title,
                    category = category,
                    dateAdded = "Aujourd'hui",
                    fileSize = "2.4 Mo",
                    notes = notes,
                    shares = listOf(
                        DocumentShare(
                            doctorId = doctorId,
                            doctorName = doctorName,
                            startDate = "Aujourd'hui",
                            expiryDate = expStr,
                            notes = notes
                        )
                    )
                )
                medicalDocuments.add(0, newDoc)
            }
        }

        fetchMedicalDocumentsAsync(patientId = pid)
        return res
    }

    fun shareDocument(
        docId: String = "",
        doctorId: String,
        doctorName: String,
        days: Int,
        title: String = "",
        category: DocumentCategory = DocumentCategory.BIOLOGY,
        notes: String = ""
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                shareDocumentAsync(docId, doctorId, doctorName, days, title, category, notes)
            } catch (_: Exception) {}
        }
    }

    suspend fun updateDocumentRemarkAsync(shareId: String = "", documentId: String = "", doctorRemark: String): Result<Boolean> {
        withContext(Dispatchers.Main) {
            val docIdx = medicalDocuments.indexOfFirst { doc -> doc.id == documentId || doc.shares.any { it.id == shareId } }
            if (docIdx != -1) {
                val doc = medicalDocuments[docIdx]
                val updatedShares = doc.shares.map { s ->
                    if (s.id == shareId || doc.id == documentId) s.copy(doctorRemark = doctorRemark) else s
                }
                medicalDocuments[docIdx] = doc.copy(shares = updatedShares)
            }
        }

        val res = com.example.tabibconnect.data.database.SqlServerDatabase.updateDocumentRemark(
            shareId = shareId,
            documentId = documentId,
            doctorRemark = doctorRemark
        )
        return res
    }

    fun revokeDocumentShare(docId: String, shareId: String) {
        val index = medicalDocuments.indexOfFirst { it.id == docId }
        if (index != -1) {
            val doc = medicalDocuments[index]
            val updatedShares = doc.shares.map {
                if (it.id == shareId) it.copy(isRevoked = true) else it
            }
            medicalDocuments[index] = doc.copy(shares = updatedShares)

            CoroutineScope(Dispatchers.IO).launch {
                com.example.tabibconnect.data.database.SqlServerDatabase.revokeDocumentShare(
                    documentId = docId,
                    shareId = shareId
                )
            }
        }
    }

    fun addChatMessage(senderName: String, isDoctor: Boolean, text: String) {
        val sId = if (isDoctor) "doc_1" else currentPatient.value.id.ifBlank { "pat_1" }
        val rId = if (isDoctor) currentPatient.value.id.ifBlank { "pat_1" } else "doc_1"
        val timeNow = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val msg = ChatMessage(
            senderId = sId,
            senderName = senderName,
            receiverId = rId,
            receiverName = if (isDoctor) currentPatient.value.name else "Dr. Mohamed Ben Salah",
            isDoctor = isDoctor,
            text = text,
            timestamp = timeNow,
            isRead = false
        )
        chatMessages.add(msg)
        CoroutineScope(Dispatchers.IO).launch {
            sendChatMessageAsync(
                senderId = sId,
                senderName = senderName,
                receiverId = rId,
                receiverName = if (isDoctor) currentPatient.value.name else "Dr. Mohamed Ben Salah",
                isDoctor = isDoctor,
                text = text
            )
        }
    }

    fun addConsultationRecord(
        appointmentId: String,
        doctorName: String,
        patientName: String,
        specialtyName: String,
        diagnosis: String,
        recommendations: String,
        restDays: Int = 0,
        medications: List<String>
    ) {
        val presc = if (medications.isNotEmpty()) {
            Prescription(
                doctorName = doctorName,
                doctorOrderNumber = "TN-12845",
                date = "Aujourd'hui",
                medications = medications
            )
        } else null

        val record = ConsultationRecord(
            appointmentId = appointmentId,
            doctorName = doctorName,
            patientName = patientName,
            specialtyName = specialtyName,
            date = "Aujourd'hui",
            diagnosis = diagnosis,
            recommendations = recommendations,
            restDays = restDays,
            prescription = presc
        )
        consultationRecords.add(0, record)
        updateAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED)
    }

    suspend fun fetchConsultationRecordsAsync(patientId: String = "", doctorId: String = "") {
        val currentRole = currentUserRole.value
        val pId = if (patientId.isNotBlank()) patientId else if (currentRole == UserRole.PATIENT) currentPatient.value.id.ifBlank { "pat_1" } else ""
        val dId = if (doctorId.isNotBlank()) doctorId else if (currentRole == UserRole.DOCTOR) "doc_1" else ""

        val dbRecords = com.example.tabibconnect.data.database.SqlServerDatabase.getConsultationRecords(patientId = pId, doctorId = dId)
        withContext(Dispatchers.Main) {
            if (dbRecords.isNotEmpty()) {
                val dbIds = dbRecords.map { it.id }.toSet()
                val localUnsaved = consultationRecords.filter { it.id !in dbIds }
                consultationRecords.clear()
                consultationRecords.addAll(dbRecords + localUnsaved)
            }
        }
    }

    suspend fun saveConsultationResultAsync(
        appointmentId: String,
        doctorId: String,
        doctorName: String,
        patientId: String,
        patientName: String,
        specialtyName: String,
        diagnosis: String,
        recommendations: String,
        restDays: Int = 0,
        medications: List<String>
    ): Result<Boolean> {
        val dateToday = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        val presc = if (medications.isNotEmpty()) {
            Prescription(
                doctorName = doctorName,
                doctorOrderNumber = "TN-12845",
                date = dateToday,
                medications = medications
            )
        } else null

        val localRec = ConsultationRecord(
            appointmentId = appointmentId,
            doctorName = doctorName,
            patientName = patientName,
            specialtyName = specialtyName,
            date = dateToday,
            diagnosis = diagnosis,
            recommendations = recommendations,
            restDays = restDays,
            prescription = presc
        )

        withContext(Dispatchers.Main) {
            consultationRecords.add(0, localRec)
            updateAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED)
            addNotification(
                title = "Compte-Rendu Médical Disponible",
                message = "Le Dr. $doctorName a ajouté le compte-rendu et l'ordonnance de votre consultation ($specialtyName).",
                type = NotificationType.COMPLETED,
                appointmentId = appointmentId,
                doctorName = doctorName,
                patientName = patientName
            )
        }

        val res = com.example.tabibconnect.data.database.SqlServerDatabase.saveConsultationResult(
            appointmentId = appointmentId,
            doctorId = doctorId,
            doctorName = doctorName,
            patientId = patientId,
            patientName = patientName,
            specialtyName = specialtyName,
            diagnosis = diagnosis,
            recommendations = recommendations,
            restDays = restDays,
            medications = medications
        )
        fetchConsultationRecordsAsync(patientId = patientId, doctorId = doctorId)
        fetchAppointmentsAsync(patientId = patientId)
        return res
    }

    suspend fun attachDocumentToAppointmentAsync(
        documentId: String,
        appointmentId: String,
        doctorId: String,
        doctorName: String,
        days: Int = 30
    ): Result<Boolean> {
        val docIndex = medicalDocuments.indexOfFirst { it.id == documentId }
        val expStr = if (days <= 0 || days >= 999) "Sans limite de durée" else "$days jours"
        if (docIndex != -1) {
            val doc = medicalDocuments[docIndex]
            val newShares = doc.shares.toMutableList()
            newShares.add(
                DocumentShare(
                    doctorId = doctorId,
                    doctorName = doctorName,
                    startDate = "Aujourd'hui",
                    expiryDate = expStr
                )
            )
            withContext(Dispatchers.Main) {
                medicalDocuments[docIndex] = doc.copy(shares = newShares)
            }
        }

        val res = com.example.tabibconnect.data.database.SqlServerDatabase.attachDocumentToAppointment(
            documentId = documentId,
            appointmentId = appointmentId,
            doctorId = doctorId,
            doctorName = doctorName,
            days = days
        )
        return res
    }

    fun addRelative(name: String, relation: String) {
        val currentP = currentPatient.value
        val updatedRelatives = currentP.relatives.toMutableList()
        updatedRelatives.add(Relative(UUID.randomUUID().toString(), name, relation))
        currentPatient.value = currentP.copy(relatives = updatedRelatives)
    }

    fun registerUser(
        fullName: String,
        phone: String,
        password: String,
        governorate: String,
        delegation: String = "Centre",
        role: UserRole,
        specialtyName: String = "Cardiologie",
        orderNumber: String = "TN-99999",
        userId: String = "",
        patientId: String = ""
    ) {
        currentUserRole.value = role
        if (role == UserRole.PATIENT) {
            val pId = patientId.ifBlank { "pat_${UUID.randomUUID().toString().take(6)}" }
            val uId = userId.ifBlank { "u_pat_new" }
            currentPatient.value = Patient(
                id = pId,
                userId = uId,
                name = fullName,
                phone = phone,
                birthDate = "14/05/1990",
                gender = "Homme",
                governorate = if (governorate.isBlank()) "Tunis" else governorate,
                delegation = delegation,
                bloodType = "O+",
                allergies = "Aucune",
                antecedents = "Aucun"
            )
        } else {
            val newDoctor = Doctor(
                id = "doc_${UUID.randomUUID().toString().take(6)}",
                userId = "u_doc_new",
                name = if (fullName.startsWith("Dr.")) fullName else "Dr. $fullName",
                orderNumber = if (orderNumber.isBlank()) "TN-77777" else orderNumber,
                specialtyId = "1",
                specialtyName = specialtyName,
                governorate = if (governorate.isBlank()) "Tunis" else governorate,
                delegation = delegation,
                bio = "Spécialiste nouvellement inscrit sur TabibConnect.",
                languages = listOf("Français", "Arabe"),
                priceDt = 60,
                rating = 5.0,
                reviewCount = 1,
                isFavorite = false
            )
            doctorsList.add(0, newDoctor)
        }
    }
}
