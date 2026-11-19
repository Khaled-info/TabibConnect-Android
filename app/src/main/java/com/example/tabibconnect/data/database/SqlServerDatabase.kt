package com.example.tabibconnect.data.database

import com.example.tabibconnect.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

fun JSONObject.optStringAny(vararg keys: String, fallback: String = ""): String {
    for (key in keys) {
        if (has(key) && !isNull(key)) {
            val v = optString(key, "")
            if (v.isNotBlank()) return v
        }
    }
    return fallback
}

object SqlServerDatabase {

    // IP du PC (ipconfig -> IPv4). Téléphone et PC sur le même Wi-Fi.
    // Émulateur Android : mettre "10.0.2.2"
    var customServerIp: String = "172.20.10.3"
    var PORT: Int = 5190


    private fun callRaw(method: String, path: String, body: JSONObject? = null): String {
        val conn = URL("http://$customServerIp:$PORT$path").openConnection() as HttpURLConnection
        try {
            conn.requestMethod = method
            conn.connectTimeout = 5000
            conn.readTimeout = 8000
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            if (body != null) {
                conn.doOutput = true
                conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
            return stream?.bufferedReader()?.use { it.readText() } ?: "{}"
        } finally {
            conn.disconnect()
        }
    }

    private fun call(method: String, path: String, body: JSONObject? = null): JSONObject {
        val text = callRaw(method, path, body)
        return try { JSONObject(text) } catch (e: Exception) { JSONObject() }
    }

    private fun unreachable() = Exception("Serveur injoignable ($customServerIp:$PORT). Vérifiez l'IP et que l'API ASP.NET est lancée.")

    suspend fun loginUser(phone: String, pass: String): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val r = call("POST", "/api/login",
                JSONObject().put("phone", phone.trim()).put("password", pass.trim()))
            if (r.optString("status") == "success") {
                Result.success(r.getJSONObject("user"))
            } else Result.failure(Exception(r.optString("message", "Identifiants incorrects")))
        } catch (e: Exception) { Result.failure(unreachable()) }
    }

    suspend fun registerUser(
        fullName: String, phone: String, pass: String, governorate: String,
        delegation: String = "Centre", gender: String = "Homme", birthDate: String = "",
        role: UserRole, specialtyName: String = "Cardiologie", orderNumber: String = "TN-99999"
    ): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val r = call("POST", "/api/register", JSONObject()
                .put("fullName", fullName.trim())
                .put("phone", phone.trim())
                .put("password", pass.trim())
                .put("role", role.name)
                .put("governorate", governorate)
                .put("delegation", delegation)
                .put("gender", gender)
                .put("birthDate", birthDate)
                .put("specialtyName", specialtyName)
                .put("orderNumber", orderNumber))
            if (r.optString("status") == "success") {
                Result.success(r)
            } else Result.failure(Exception(r.optString("message", "Erreur d'inscription")))
        } catch (e: Exception) { Result.failure(unreachable()) }
    }

    suspend fun resetPassword(phone: String, newPass: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val r = call("POST", "/api/reset_password",
                JSONObject().put("phone", phone.trim()).put("password", newPass.trim()))
            Result.success(r.optString("status") == "success")
        } catch (e: Exception) { Result.failure(unreachable()) }
    }

    suspend fun getDoctors(): List<Doctor> = withContext(Dispatchers.IO) {
        try {
            val text = callRaw("GET", "/api/doctors")
            var arr: org.json.JSONArray? = null
            val trimmed = text.trim()
            if (trimmed.startsWith("[")) {
                arr = org.json.JSONArray(trimmed)
            } else if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                arr = obj.optJSONArray("doctors")
                    ?: obj.optJSONArray("Doctors")
                    ?: obj.optJSONArray("data")
                    ?: obj.optJSONArray("Data")
            }

            if (arr == null) return@withContext emptyList()

            (0 until arr.length()).mapNotNull { i ->
                val d = arr.optJSONObject(i) ?: return@mapNotNull null
                val id = d.optStringAny("id", "Id", "ID", fallback = "")
                val name = d.optStringAny("name", "Name", "fullName", "FullName", fallback = "")
                val specialtyName = d.optStringAny("specialty_name", "specialtyName", "SpecialtyName", "specialty", "Specialty", fallback = "Médecine Générale")
                val governorate = d.optStringAny("governorate", "Governorate", fallback = "Tunis")
                val delegation = d.optStringAny("delegation", "Delegation", fallback = "Centre")
                val orderNumber = d.optStringAny("order_number", "orderNumber", "OrderNumber", fallback = "TN-1000")
                val bio = d.optStringAny("bio", "Bio", fallback = "Médecin spécialiste diplômé.")
                val priceDt = d.optInt("price_dt", d.optInt("priceDt", d.optInt("price", 50)))
                val rating = d.optDouble("rating", d.optDouble("Rating", 4.8))
                val reviewCount = d.optInt("review_count", d.optInt("reviewCount", 30))

                if (name.isNotBlank()) {
                    Doctor(
                        id = id.ifBlank { java.util.UUID.randomUUID().toString() },
                        userId = d.optStringAny("user_id", "userId", fallback = "u_doc"),
                        name = name,
                        orderNumber = orderNumber,
                        specialtyId = d.optStringAny("specialty_id", "specialtyId", fallback = "1"),
                        specialtyName = specialtyName,
                        governorate = governorate,
                        delegation = delegation,
                        bio = bio,
                        priceDt = priceDt,
                        rating = rating,
                        reviewCount = reviewCount,
                        isFavorite = d.optBoolean("is_favorite", d.optBoolean("isFavorite", false))
                    )
                } else null
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun bookAppointment(
        patientId: String, patientName: String, doctorId: String, doctorName: String,
        specialtyName: String, date: String, timeSlot: String, motif: String,
        beneficiary: String, priceDt: Int, paymentMethod: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val r = call("POST", "/api/book_appointment", JSONObject()
                .put("patientId", patientId).put("patientName", patientName)
                .put("doctorId", doctorId).put("doctorName", doctorName)
                .put("specialtyName", specialtyName).put("date", date)
                .put("timeSlot", timeSlot).put("motif", motif)
                .put("beneficiary", beneficiary).put("priceDt", priceDt)
                .put("paymentMethod", paymentMethod))
            r.optString("status") == "success"
        } catch (e: Exception) { false }
    }

    suspend fun updateAppointmentStatus(
        appointmentId: String,
        status: String,
        date: String = "",
        timeSlot: String = "",
        reason: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject()
                .put("appointmentId", appointmentId)
                .put("status", status)
            if (date.isNotBlank()) body.put("date", date)
            if (timeSlot.isNotBlank()) body.put("timeSlot", timeSlot)
            if (reason.isNotBlank()) body.put("reason", reason)

            val r = call("POST", "/api/update_appointment_status", body)
            r.optString("status") == "success"
        } catch (e: Exception) { false }
    }

    suspend fun getAppointments(patientId: String = "", userId: String = ""): List<Appointment> = withContext(Dispatchers.IO) {
        try {
            val pidClean = patientId.trim()
            val uidClean = userId.trim()
            var text = ""

            if (pidClean.isNotBlank()) {
                val encodedPid = java.net.URLEncoder.encode(pidClean, "UTF-8")
                text = callRaw("GET", "/api/appointments?patientId=$encodedPid")
            }
            if ((text.isBlank() || text == "{}" || text == "[]") && uidClean.isNotBlank()) {
                val encodedUid = java.net.URLEncoder.encode(uidClean, "UTF-8")
                text = callRaw("GET", "/api/appointments?patientId=$encodedUid")
            }
            if (text.isBlank() || text == "{}" || text == "[]") {
                text = callRaw("GET", "/api/appointments")
            }

            var arr: org.json.JSONArray? = null
            val trimmed = text.trim()
            if (trimmed.startsWith("[")) {
                arr = org.json.JSONArray(trimmed)
            } else if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                arr = obj.optJSONArray("appointments")
                    ?: obj.optJSONArray("Appointments")
                    ?: obj.optJSONArray("data")
                    ?: obj.optJSONArray("Data")
            }

            if (arr == null) return@withContext emptyList()

            (0 until arr.length()).mapNotNull { i ->
                val a = arr.optJSONObject(i) ?: return@mapNotNull null
                val id = a.optStringAny("id", "Id", fallback = "")
                val doctorName = a.optStringAny("doctor_name", "doctorName", "DoctorName", fallback = "Dr. Médecin")
                val date = a.optStringAny("date", "Date", fallback = "")
                val timeSlot = a.optStringAny("time_slot", "timeSlot", "TimeSlot", fallback = "10:00")
                val statusStr = a.optStringAny("status", "Status", fallback = "CONFIRMED").uppercase()

                if (id.isNotBlank() || doctorName.isNotBlank()) {
                    Appointment(
                        id = id.ifBlank { java.util.UUID.randomUUID().toString() },
                        patientId = a.optStringAny("patient_id", "patientId", fallback = "pat_1"),
                        patientName = a.optStringAny("patient_name", "patientName", fallback = "Patient"),
                        doctorId = a.optStringAny("doctor_id", "doctorId", fallback = "doc_1"),
                        doctorName = doctorName,
                        specialtyName = a.optStringAny("specialty_name", "specialtyName", fallback = "Médecine Générale"),
                        date = date.ifBlank { "Aujourd'hui" },
                        timeSlot = timeSlot,
                        motif = a.optStringAny("motif", "Motif", fallback = "Consultation"),
                        beneficiary = a.optStringAny("beneficiary", "Beneficiary", fallback = "Moi-même"),
                        priceDt = a.optInt("price_dt", a.optInt("priceDt", a.optInt("price", 50))),
                        status = when (statusStr) {
                            "COMPLETED" -> AppointmentStatus.COMPLETED
                            "CANCELLED" -> AppointmentStatus.CANCELLED
                            "PENDING" -> AppointmentStatus.PENDING
                            "REJECTED" -> AppointmentStatus.REJECTED
                            else -> AppointmentStatus.CONFIRMED
                        },
                        isPaid = a.optBoolean("is_paid", a.optBoolean("isPaid", false)),
                        paymentMethod = a.optStringAny("payment_method", "paymentMethod", fallback = "Paiement au cabinet"),
                        consultationType = "IN_PERSON"
                    )
                } else null
            }
        } catch (e: Exception) { emptyList() }
    }
    suspend fun addRelative(patientId: String, name: String, relation: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val r = call("POST", "/api/add_relative", JSONObject()
                .put("patientId", patientId)
                .put("name", name.trim())
                .put("relation", relation.trim()))
            if (r.optString("status") == "success") {
                Result.success(true)
            } else {
                Result.failure(Exception(r.optString("message", "Erreur lors de l'ajout du proche")))
            }
        } catch (e: Exception) { Result.failure(unreachable()) }
    }

    suspend fun getRelatives(patientId: String, userId: String = ""): List<Relative> = withContext(Dispatchers.IO) {
        try {
            val pidClean = patientId.trim()
            val uidClean = userId.trim()
            var text = ""

            if (pidClean.isNotBlank()) {
                val encodedPid = java.net.URLEncoder.encode(pidClean, "UTF-8")
                text = callRaw("GET", "/api/relatives?patientId=$encodedPid")
            }

            if ((text.isBlank() || text == "{}" || text == "[]") && uidClean.isNotBlank()) {
                val encodedUid = java.net.URLEncoder.encode(uidClean, "UTF-8")
                text = callRaw("GET", "/api/relatives?patientId=$encodedUid")
            }

            if (text.isBlank() || text == "{}" || text == "[]") {
                text = callRaw("GET", "/api/relatives")
            }

            var arr: org.json.JSONArray? = null
            val trimmed = text.trim()
            if (trimmed.startsWith("[")) {
                arr = org.json.JSONArray(trimmed)
            } else if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                arr = obj.optJSONArray("relatives")
                    ?: obj.optJSONArray("Relatives")
                    ?: obj.optJSONArray("data")
                    ?: obj.optJSONArray("Data")
            }

            if (arr == null) return@withContext emptyList()

            (0 until arr.length()).mapNotNull { i ->
                val rel = arr.optJSONObject(i) ?: return@mapNotNull null
                val id = rel.optStringAny("id", "Id", "ID", fallback = "")
                val name = rel.optStringAny("name", "Name", "fullName", "FullName", fallback = "")
                val relation = rel.optStringAny("relation", "Relation", "relationship", "Relationship", fallback = "")
                if (name.isNotBlank()) {
                    Relative(
                        id = id.ifBlank { java.util.UUID.randomUUID().toString() },
                        name = name,
                        relation = relation.ifBlank { "Proche" }
                    )
                } else null
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun addHealthMetric(
        patientId: String,
        metricType: String,
        metricValue: Float,
        secondaryValue: Float? = null,
        recordedDate: String,
        notes: String = "",
        diseaseName: String = ""
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val r = call("POST", "/api/add_health_metric", JSONObject()
                .put("patientId", patientId)
                .put("metricType", metricType)
                .put("metricValue", metricValue)
                .put("secondaryValue", secondaryValue ?: 0f)
                .put("recordedDate", recordedDate)
                .put("notes", notes)
                .put("diseaseName", diseaseName))
            Result.success(r.optString("status") == "success")
        } catch (e: Exception) { Result.failure(unreachable()) }
    }

    suspend fun getHealthMetricsRecords(patientId: String): List<HealthMetricRecord> = withContext(Dispatchers.IO) {
        try {
            val pidClean = patientId.trim()
            if (pidClean.isBlank()) return@withContext emptyList()
            val encodedPid = java.net.URLEncoder.encode(pidClean, "UTF-8")
            val text = callRaw("GET", "/api/health_metrics?patientId=$encodedPid")
            val trimmed = text.trim()
            var arr: org.json.JSONArray? = null
            if (trimmed.startsWith("[")) {
                arr = org.json.JSONArray(trimmed)
            } else if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                arr = obj.optJSONArray("metrics") ?: obj.optJSONArray("data")
            }
            if (arr == null) return@withContext emptyList()

            (0 until arr.length()).mapNotNull { i ->
                val m = arr.optJSONObject(i) ?: return@mapNotNull null
                val id = m.optStringAny("id", "Id", fallback = "")
                val type = m.optStringAny("metric_type", "metricType", fallback = "HEART_RATE")
                val val1 = m.optDouble("metric_value", m.optDouble("value", 72.0)).toFloat()
                val val2 = if (m.has("secondary_value")) m.optDouble("secondary_value", 0.0).toFloat() else null
                val dateStr = m.optStringAny("recorded_date", "recordedDate", "date", fallback = "Aujourd'hui")
                val notesStr = m.optStringAny("notes", "Notes", fallback = "")
                val diseaseStr = m.optStringAny("disease_name", "diseaseName", fallback = "")

                HealthMetricRecord(
                    id = id.ifBlank { java.util.UUID.randomUUID().toString() },
                    patientId = patientId,
                    metricType = type,
                    metricValue = val1,
                    secondaryValue = val2,
                    recordedDate = dateStr,
                    notes = notesStr,
                    diseaseName = diseaseStr
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getHealthMetrics(patientId: String): List<Pair<String, Float>> = withContext(Dispatchers.IO) {
        try {
            val pidClean = patientId.trim()
            if (pidClean.isBlank()) return@withContext emptyList()
            val encodedPid = java.net.URLEncoder.encode(pidClean, "UTF-8")
            val text = callRaw("GET", "/api/health_metrics?patientId=$encodedPid")
            val trimmed = text.trim()
            var arr: org.json.JSONArray? = null
            if (trimmed.startsWith("[")) {
                arr = org.json.JSONArray(trimmed)
            } else if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                arr = obj.optJSONArray("metrics") ?: obj.optJSONArray("data")
            }
            if (arr == null) return@withContext emptyList()

            (0 until arr.length()).mapNotNull { i ->
                val m = arr.optJSONObject(i) ?: return@mapNotNull null
                val label = m.optStringAny("recorded_date", "recordedDate", "date", fallback = "")
                val value = m.optDouble("metric_value", m.optDouble("value", 72.0)).toFloat()
                if (label.isNotBlank()) Pair(label, value) else null
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun updateDoctorProfile(
        doctorId: String,
        priceDt: Int,
        bio: String,
        offersTeleconsultation: Boolean = false,
        offersInPerson: Boolean = true
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val r = call("POST", "/api/update_doctor", JSONObject()
                .put("doctorId", doctorId)
                .put("priceDt", priceDt)
                .put("bio", bio)
                .put("offersTeleconsultation", offersTeleconsultation)
                .put("offersInPerson", offersInPerson))
            Result.success(r.optString("status") == "success")
        } catch (e: Exception) { Result.failure(unreachable()) }
    }

    suspend fun getDoctorSchedule(doctorId: String): List<DoctorScheduleDay> = withContext(Dispatchers.IO) {
        try {
            val didClean = doctorId.trim()
            val encodedDid = java.net.URLEncoder.encode(if (didClean.isBlank()) "doc_1" else didClean, "UTF-8")
            val text = callRaw("GET", "/api/doctor_schedule?doctorId=$encodedDid")
            val trimmed = text.trim()
            var arr: org.json.JSONArray? = null
            if (trimmed.startsWith("[")) {
                arr = org.json.JSONArray(trimmed)
            } else if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                arr = obj.optJSONArray("schedules") ?: obj.optJSONArray("data")
            }
            if (arr == null) return@withContext emptyList()
            (0 until arr.length()).mapNotNull { i ->
                val s = arr.optJSONObject(i) ?: return@mapNotNull null
                val id = s.optStringAny("id", "Id", fallback = "")
                val day = s.optStringAny("day_name_fr", "dayNameFr", "day", fallback = "Lundi")
                val isOpen = s.optBoolean("is_open", s.optBoolean("isOpen", true))
                val start = s.optStringAny("start_time", "startTime", fallback = "08:30")
                val end = s.optStringAny("end_time", "endTime", fallback = "17:30")
                val dur = s.optInt("slot_duration_minutes", s.optInt("slotDurationMinutes", 30))

                DoctorScheduleDay(
                    id = id.ifBlank { java.util.UUID.randomUUID().toString() },
                    dayNameFr = day,
                    isOpen = isOpen,
                    startTime = start,
                    endTime = end,
                    slotDurationMinutes = dur
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun saveDoctorSchedule(doctorId: String, scheduleList: List<DoctorScheduleDay>): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val arr = org.json.JSONArray()
            scheduleList.forEach { s ->
                arr.put(
                    JSONObject()
                        .put("dayNameFr", s.dayNameFr)
                        .put("isOpen", s.isOpen)
                        .put("startTime", s.startTime)
                        .put("endTime", s.endTime)
                        .put("slotDurationMinutes", s.slotDurationMinutes)
                )
            }
            val body = JSONObject()
                .put("doctorId", doctorId.ifBlank { "doc_1" })
                .put("schedules", arr)

            val r = call("POST", "/api/save_doctor_schedule", body)
            Result.success(r.optString("status") == "success")
        } catch (e: Exception) { Result.failure(unreachable()) }
    }

    suspend fun getDoctorVacations(doctorId: String): List<DoctorVacationDay> = withContext(Dispatchers.IO) {
        try {
            val didClean = doctorId.trim()
            val encodedDid = java.net.URLEncoder.encode(if (didClean.isBlank()) "doc_1" else didClean, "UTF-8")
            val text = callRaw("GET", "/api/doctor_vacations?doctorId=$encodedDid")
            val trimmed = text.trim()
            var arr: org.json.JSONArray? = null
            if (trimmed.startsWith("[")) {
                arr = org.json.JSONArray(trimmed)
            } else if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                arr = obj.optJSONArray("vacations") ?: obj.optJSONArray("data")
            }
            if (arr == null) return@withContext emptyList()

            (0 until arr.length()).mapNotNull { i ->
                val v = arr.optJSONObject(i) ?: return@mapNotNull null
                val id = v.optStringAny("id", "Id", fallback = "")
                val vdate = v.optStringAny("vacation_date", "vacationDate", "date", fallback = "")
                val reason = v.optStringAny("reason", "Reason", fallback = "Congé exceptionnel")

                if (vdate.isNotBlank()) {
                    DoctorVacationDay(
                        id = id.ifBlank { java.util.UUID.randomUUID().toString() },
                        vacationDate = vdate,
                        reason = reason
                    )
                } else null
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun addDoctorVacation(doctorId: String, vacationDate: String, reason: String = "Congé exceptionnel"): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val r = call("POST", "/api/add_doctor_vacation", JSONObject()
                .put("doctorId", doctorId.ifBlank { "doc_1" })
                .put("vacationDate", vacationDate)
                .put("reason", reason))
            Result.success(r.optString("status") == "success")
        } catch (e: Exception) { Result.failure(unreachable()) }
    }

    suspend fun deleteDoctorVacation(vacationId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val r = call("POST", "/api/delete_doctor_vacation", JSONObject().put("vacationId", vacationId))
            Result.success(r.optString("status") == "success")
        } catch (e: Exception) { Result.failure(unreachable()) }
    }

    suspend fun getDoctorRevenueStats(doctorId: String): DoctorRevenueStats = withContext(Dispatchers.IO) {
        try {
            val didClean = doctorId.trim()
            val encodedDid = java.net.URLEncoder.encode(if (didClean.isBlank()) "doc_1" else didClean, "UTF-8")
            val obj = call("GET", "/api/doctor_revenue_stats?doctorId=$encodedDid")
            if (obj.optString("status") != "success") return@withContext DoctorRevenueStats()
            val totalRevenue = obj.optInt("totalRevenue", 0)
            val todayCount = obj.optInt("todayCount", 0)
            val videoCount = obj.optInt("videoCount", 0)
            val inPersonCount = obj.optInt("inPersonCount", 0)
            val weeklyRevenues = mutableMapOf<String, Float>()
            val dailyObj = obj.optJSONObject("weeklyRevenues")
            if (dailyObj != null) {
                val keys = dailyObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    weeklyRevenues[k] = dailyObj.optDouble(k, 0.0).toFloat()
                }
            }

            DoctorRevenueStats(
                totalRevenue = totalRevenue,
                todayCount = todayCount,
                videoCount = videoCount,
                inPersonCount = inPersonCount,
                weeklyRevenues = weeklyRevenues
            )
        } catch (e: Exception) { DoctorRevenueStats() }
    }

    suspend fun fetchNotifications(userId: String): List<com.example.tabibconnect.data.model.AppNotification> = withContext(Dispatchers.IO) {
        try {
            val uidClean = userId.trim()
            val encodedUid = java.net.URLEncoder.encode(if (uidClean.isBlank()) "pat_1" else uidClean, "UTF-8")
            val text = callRaw("GET", "/api/notifications?userId=$encodedUid")
            val trimmed = text.trim()
            var arr: org.json.JSONArray? = null
            if (trimmed.startsWith("[")) {
                arr = org.json.JSONArray(trimmed)
            } else if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                arr = obj.optJSONArray("notifications") ?: obj.optJSONArray("data")
            }
            if (arr == null) return@withContext emptyList()

            (0 until arr.length()).mapNotNull { i ->
                val n = arr.optJSONObject(i) ?: return@mapNotNull null
                val id = n.optStringAny("id", "Id", fallback = "")
                val title = n.optStringAny("title", "Title", fallback = "Notification")
                val message = n.optStringAny("message", "Message", fallback = "")
                val typeStr = n.optStringAny("type", "Type", fallback = "CONFIRMED").uppercase()
                val isRead = n.optBoolean("is_read", n.optBoolean("isRead", false))
                val timestamp = n.optStringAny("timestamp", "Timestamp", "created_at", fallback = "Aujourd'hui")

                val nType = when {
                    typeStr.contains("MODIFIED") -> com.example.tabibconnect.data.model.NotificationType.MODIFIED
                    typeStr.contains("CANCELLED") || typeStr.contains("REJECTED") -> com.example.tabibconnect.data.model.NotificationType.CANCELLED
                    typeStr.contains("ADDED") -> com.example.tabibconnect.data.model.NotificationType.ADDED
                    typeStr.contains("COMPLETED") -> com.example.tabibconnect.data.model.NotificationType.COMPLETED
                    else -> com.example.tabibconnect.data.model.NotificationType.CONFIRMED
                }

                com.example.tabibconnect.data.model.AppNotification(
                    id = id.ifBlank { java.util.UUID.randomUUID().toString() },
                    title = title,
                    message = message,
                    timestamp = timestamp,
                    type = nType,
                    isRead = isRead
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun markNotificationRead(notificationId: String = "", userId: String = "", markAll: Boolean = false): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val r = call("POST", "/api/mark_notification_read", JSONObject()
                .put("notificationId", notificationId)
                .put("userId", userId)
                .put("markAll", markAll))
            Result.success(r.optString("status") == "success")
        } catch (e: Exception) { Result.failure(unreachable()) }
    }

    suspend fun deleteNotifications(notificationId: String = "", userId: String = "", clearAll: Boolean = false): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val r = call("POST", "/api/delete_notifications", JSONObject()
                .put("notificationId", notificationId)
                .put("userId", userId)
                .put("clearAll", clearAll))
            Result.success(r.optString("status") == "success")
        } catch (e: Exception) { Result.failure(unreachable()) }
    }

    suspend fun getChatMessages(userId: String = "", partnerId: String = ""): List<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val u1 = java.net.URLEncoder.encode(userId.trim(), "UTF-8")
            val u2 = java.net.URLEncoder.encode(partnerId.trim(), "UTF-8")
            val text = callRaw("GET", "/api/chat_messages?userId=$u1&partnerId=$u2")
            val trimmed = text.trim()
            var arr: org.json.JSONArray? = null
            if (trimmed.startsWith("[")) {
                arr = org.json.JSONArray(trimmed)
            } else if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                arr = obj.optJSONArray("messages") ?: obj.optJSONArray("data")
            }
            if (arr == null) return@withContext emptyList()

            (0 until arr.length()).mapNotNull { i ->
                val m = arr.optJSONObject(i) ?: return@mapNotNull null
                val id = m.optStringAny("id", "Id", fallback = "")
                val senderId = m.optStringAny("sender_id", "senderId", fallback = "")
                val senderName = m.optStringAny("sender_name", "senderName", fallback = "Utilisateur")
                val receiverId = m.optStringAny("receiver_id", "receiverId", fallback = "")
                val receiverName = m.optStringAny("receiver_name", "receiverName", fallback = "")
                val msgText = m.optStringAny("text", "Text", fallback = "")
                val isDoc = m.optBoolean("is_doctor", m.optBoolean("isDoctor", false))
                val ts = m.optStringAny("timestamp", "Timestamp", fallback = "10:00")
                val isRead = m.optBoolean("is_read", m.optBoolean("isRead", true))

                if (msgText.isNotBlank()) {
                    ChatMessage(
                        id = id.ifBlank { java.util.UUID.randomUUID().toString() },
                        senderId = senderId,
                        senderName = senderName,
                        receiverId = receiverId,
                        receiverName = receiverName,
                        isDoctor = isDoc,
                        text = msgText,
                        timestamp = ts,
                        isRead = isRead
                    )
                } else null
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun sendChatMessage(
        senderId: String,
        senderName: String,
        receiverId: String,
        receiverName: String,
        isDoctor: Boolean,
        text: String,
        timestamp: String
    ): Result<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject()
                .put("senderId", senderId)
                .put("senderName", senderName)
                .put("receiverId", receiverId)
                .put("receiverName", receiverName)
                .put("isDoctor", isDoctor)
                .put("text", text)
                .put("timestamp", timestamp)

            val r = call("POST", "/api/send_chat_message", body)
            if (r.optString("status") == "success") {
                val mObj = r.optJSONObject("message")
                val msg = ChatMessage(
                    id = mObj?.optString("id") ?: java.util.UUID.randomUUID().toString(),
                    senderId = senderId,
                    senderName = senderName,
                    receiverId = receiverId,
                    receiverName = receiverName,
                    isDoctor = isDoctor,
                    text = text,
                    timestamp = timestamp,
                    isRead = false
                )
                Result.success(msg)
            } else {
                Result.failure(Exception(r.optString("message", "Erreur lors de l'envoi du message")))
            }
        } catch (e: Exception) { Result.failure(unreachable()) }
    }

    suspend fun markChatRead(userId: String, partnerId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val r = call("POST", "/api/mark_chat_read", JSONObject()
                .put("userId", userId)
                .put("partnerId", partnerId))
            Result.success(r.optString("status") == "success")
        } catch (e: Exception) { Result.failure(unreachable()) }
    }

    suspend fun getMedicalDocuments(patientId: String = "", doctorId: String = "", appointmentId: String = ""): List<MedicalDocument> = withContext(Dispatchers.IO) {
        try {
            val pidClean = java.net.URLEncoder.encode(patientId.trim(), "UTF-8")
            val didClean = java.net.URLEncoder.encode(doctorId.trim(), "UTF-8")
            val aptClean = java.net.URLEncoder.encode(appointmentId.trim(), "UTF-8")
            val text = callRaw("GET", "/api/medical_documents?patientId=$pidClean&doctorId=$didClean&appointmentId=$aptClean")
            val trimmed = text.trim()
            var arr: org.json.JSONArray? = null
            if (trimmed.startsWith("[")) {
                arr = org.json.JSONArray(trimmed)
            } else if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                arr = obj.optJSONArray("documents") ?: obj.optJSONArray("data")
            }
            if (arr == null) return@withContext emptyList()

            (0 until arr.length()).mapNotNull { i ->
                val d = arr.optJSONObject(i) ?: return@mapNotNull null
                val id = d.optStringAny("id", "Id", fallback = "")
                val title = d.optStringAny("title", "Title", fallback = "Document Médical")
                val catStr = d.optStringAny("category", "Category", fallback = "BIOLOGY").uppercase()
                val dateAdded = d.optStringAny("date_added", "dateAdded", fallback = "Aujourd'hui")
                val fileSize = d.optStringAny("file_size", "fileSize", fallback = "2.4 Mo")

                val cat = when {
                    catStr.contains("IMAG") -> DocumentCategory.IMAGING
                    catStr.contains("PRES") || catStr.contains("ORD") -> DocumentCategory.PRESCRIPTION
                    catStr.contains("OTH") || catStr.contains("AUT") -> DocumentCategory.OTHER
                    else -> DocumentCategory.BIOLOGY
                }

                val sharesArr = d.optJSONArray("shares")
                val sharesList = mutableListOf<DocumentShare>()
                if (sharesArr != null) {
                    for (j in 0 until sharesArr.length()) {
                        val s = sharesArr.optJSONObject(j) ?: continue
                        val shId = s.optStringAny("id", "Id", fallback = "")
                        val docName = s.optStringAny("doctor_name", "doctorName", fallback = "Dr. Médecin")
                        val docId = s.optStringAny("doctor_id", "doctorId", fallback = "doc_1")
                        val start = s.optStringAny("start_date", "startDate", fallback = "Aujourd'hui")
                        val exp = s.optStringAny("expiry_date", "expiryDate", fallback = "7 jours")
                        val revoked = s.optBoolean("is_revoked") || s.optInt("is_revoked", 0) == 1 || s.optString("is_revoked") == "1" || s.optBoolean("isRevoked") || s.optInt("isRevoked", 0) == 1
                        val sNotes = s.optStringAny("notes", "Notes", fallback = "")
                        val sRemark = s.optStringAny("doctor_remark", "doctorRemark", "remark", fallback = "")

                        sharesList.add(
                            DocumentShare(
                                id = shId.ifBlank { java.util.UUID.randomUUID().toString() },
                                doctorId = docId,
                                doctorName = docName,
                                startDate = start,
                                expiryDate = exp,
                                isRevoked = revoked,
                                notes = sNotes,
                                doctorRemark = sRemark
                            )
                        )
                    }
                }

                MedicalDocument(
                    id = id.ifBlank { java.util.UUID.randomUUID().toString() },
                    patientId = d.optStringAny("patient_id", "patientId", "PatientId", fallback = "pat_1"),
                    title = title,
                    category = cat,
                    dateAdded = dateAdded,
                    fileSize = fileSize,
                    notes = d.optStringAny("notes", "Notes", fallback = ""),
                    fileUrl = d.optStringAny("file_url", "fileUrl", fallback = ""),
                    shares = sharesList
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun addMedicalDocument(
        patientId: String,
        title: String,
        category: String,
        fileSize: String = "2.4 Mo",
        notes: String = "",
        fileUrl: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val r = call("POST", "/api/add_medical_document", JSONObject()
                .put("patientId", patientId.ifBlank { "pat_1" })
                .put("title", title.trim())
                .put("category", category)
                .put("fileSize", fileSize)
                .put("notes", notes.trim())
                .put("fileUrl", fileUrl.trim()))
            if (r.optString("status") == "success") {
                Result.success(r.optString("documentId", ""))
            } else {
                Result.failure(Exception(r.optString("message", "Erreur lors de l'ajout du document")))
            }
        } catch (e: Exception) { Result.failure(unreachable()) }
    }

    suspend fun deleteMedicalDocument(documentId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val r = call("POST", "/api/delete_medical_document", JSONObject().put("documentId", documentId))
            Result.success(r.optString("status") == "success")
        } catch (e: Exception) { Result.failure(unreachable()) }
    }

    suspend fun shareDocument(
        documentId: String = "",
        doctorId: String,
        doctorName: String,
        days: Int,
        title: String = "",
        category: String = "",
        notes: String = "",
        patientId: String = ""
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val r = call("POST", "/api/share_document", JSONObject()
                .put("documentId", documentId)
                .put("doctorId", doctorId.ifBlank { "doc_1" })
                .put("doctorName", doctorName)
                .put("days", days)
                .put("title", title)
                .put("category", category)
                .put("notes", notes)
                .put("patientId", patientId))
            Result.success(r.optString("status") == "success")
        } catch (e: Exception) { Result.failure(unreachable()) }
    }

    suspend fun updateDocumentRemark(shareId: String = "", documentId: String = "", doctorRemark: String = ""): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val r = call("POST", "/api/update_document_remark", JSONObject()
                .put("shareId", shareId)
                .put("documentId", documentId)
                .put("doctorRemark", doctorRemark))
            Result.success(r.optString("status") == "success")
        } catch (e: Exception) { Result.failure(unreachable()) }
    }

    suspend fun revokeDocumentShare(documentId: String, shareId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val r = call("POST", "/api/revoke_document_share", JSONObject()
                .put("documentId", documentId)
                .put("shareId", shareId))
            Result.success(r.optString("status") == "success")
        } catch (e: Exception) { Result.failure(unreachable()) }
    }

    suspend fun saveConsultationResult(
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
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val medsArr = org.json.JSONArray()
            medications.forEach { medsArr.put(it) }
            val r = call("POST", "/api/save_consultation_result", JSONObject()
                .put("appointmentId", appointmentId)
                .put("doctorId", doctorId)
                .put("doctorName", doctorName)
                .put("patientId", patientId)
                .put("patientName", patientName)
                .put("specialtyName", specialtyName)
                .put("diagnosis", diagnosis)
                .put("recommendations", recommendations)
                .put("restDays", restDays)
                .put("medications", medsArr))
            Result.success(r.optString("status") == "success")
        } catch (e: Exception) { Result.failure(unreachable()) }
    }

    suspend fun getConsultationRecords(patientId: String = "", doctorId: String = ""): List<ConsultationRecord> = withContext(Dispatchers.IO) {
        try {
            val pidClean = java.net.URLEncoder.encode(patientId.trim(), "UTF-8")
            val didClean = java.net.URLEncoder.encode(doctorId.trim(), "UTF-8")
            val text = callRaw("GET", "/api/consultation_records?patientId=$pidClean&doctorId=$didClean")
            val trimmed = text.trim()
            var arr: org.json.JSONArray? = null
            if (trimmed.startsWith("[")) {
                arr = org.json.JSONArray(trimmed)
            } else if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                arr = obj.optJSONArray("records") ?: obj.optJSONArray("data")
            }
            if (arr == null) return@withContext emptyList()

            (0 until arr.length()).mapNotNull { i ->
                val r = arr.optJSONObject(i) ?: return@mapNotNull null
                val id = r.optStringAny("id", "Id", fallback = "")
                val aptId = r.optStringAny("appointment_id", "appointmentId", "AppointmentId", fallback = "")
                val docName = r.optStringAny("doctor_name", "doctorName", "DoctorName", fallback = "Dr. Médecin")
                val patName = r.optStringAny("patient_name", "patientName", "PatientName", fallback = "Patient")
                val specName = r.optStringAny("specialty_name", "specialtyName", "SpecialtyName", fallback = "Médecine Générale")
                val dateStr = r.optStringAny("date", "Date", "created_at", "createdAt", fallback = "Aujourd'hui")
                val diag = r.optStringAny("diagnosis", "Diagnosis", "description", fallback = "")
                val recom = r.optStringAny("recommendations", "Recommendations", fallback = "")
                val restDaysVal = r.optInt("rest_days", r.optInt("restDays", r.optInt("RestDays", 0)))
                val medsRaw = r.optStringAny("medications", "Medications", "meds", fallback = "")

                val medsArray = r.optJSONArray("medications") ?: r.optJSONArray("Medications")
                val medsList = if (medsArray != null && medsArray.length() > 0) {
                    (0 until medsArray.length()).map { medsArray.optString(it) }.filter { it.isNotBlank() }
                } else if (medsRaw.isNotBlank()) {
                    medsRaw.split(";", ",").map { it.trim() }.filter { it.isNotBlank() }
                } else emptyList()

                val presc = if (medsList.isNotEmpty()) {
                    Prescription(
                        id = "presc_$id",
                        doctorName = docName,
                        doctorOrderNumber = "TN-12845",
                        date = dateStr,
                        medications = medsList
                    )
                } else null

                ConsultationRecord(
                    id = id.ifBlank { java.util.UUID.randomUUID().toString() },
                    appointmentId = aptId,
                    doctorName = docName,
                    patientName = patName,
                    specialtyName = specName,
                    date = dateStr,
                    diagnosis = diag,
                    recommendations = recom,
                    restDays = restDaysVal,
                    prescription = presc
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun attachDocumentToAppointment(documentId: String, appointmentId: String, doctorId: String, doctorName: String, days: Int = 30): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val r = call("POST", "/api/attach_document", JSONObject()
                .put("documentId", documentId)
                .put("appointmentId", appointmentId)
                .put("doctorId", doctorId)
                .put("doctorName", doctorName)
                .put("days", days))
            Result.success(r.optString("status") == "success")
        } catch (e: Exception) { Result.failure(unreachable()) }
    }
}