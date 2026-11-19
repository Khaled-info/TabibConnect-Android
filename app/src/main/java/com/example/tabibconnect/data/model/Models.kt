package com.example.tabibconnect.data.model

import java.util.UUID

enum class UserRole {
    PATIENT,
    DOCTOR,
    ADMIN
}

enum class ValidationStatus {
    PENDING,
    APPROVED,
    REJECTED
}

enum class AppointmentStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    REJECTED,
    ABSENT
}

enum class DocumentCategory(val labelFr: String, val labelAr: String) {
    BIOLOGY("Biologie", "تحاليل طبية"),
    IMAGING("Imagerie", "أشعة ورنين"),
    PRESCRIPTION("Ordonnance", "وصفات طبية"),
    OTHER("Autre", "وثائق أخرى")
}

data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val email: String,
    val role: UserRole,
    val language: String = "FR" // "FR" or "AR"
)

data class Patient(
    val id: String = "pat_1",
    val userId: String,
    val name: String = "Amine Ben Ammor",
    val phone: String = "+216 98 123 456",
    val birthDate: String = "14/05/1990",
    val gender: String = "Homme",
    val governorate: String = "Kasserine",
    val delegation: String = "Centre",
    val bloodType: String = "O+",
    val allergies: String = "Pénicilline",
    val antecedents: String = "Hypertension artérielle légère",
    val relatives: List<Relative> = emptyList()
)

data class Relative(
    val id: String,
    val name: String,
    val relation: String
)

data class Doctor(
    val id: String,
    val userId: String,
    val name: String,
    val orderNumber: String,
    val specialtyId: String,
    val specialtyName: String,
    val governorate: String,
    val delegation: String = "Centre",
    val bio: String,
    val languages: List<String> = listOf("Français", "Arabe"),
    val priceDt: Int,
    val validationStatus: ValidationStatus = ValidationStatus.APPROVED,
    val rating: Double,
    val reviewCount: Int,
    val isFavorite: Boolean = false,
    val photoUrl: String = "",
    val availableSlots: List<String> = listOf("09:00", "09:30", "10:00", "11:00", "14:00", "15:30", "16:00"),
    val offersInPerson: Boolean = true,
    val offersTeleconsultation: Boolean = false,
    val cabinetAddress: String = "Cabinet Médical - Centre Ville"
)

data class Specialty(
    val id: String,
    val nameFr: String,
    val nameAr: String,
    val iconName: String = "MedicalServices"
)

data class DoctorScheduleDay(
    val id: String = UUID.randomUUID().toString(),
    val dayNameFr: String,
    val isOpen: Boolean = true,
    val startTime: String = "08:30",
    val endTime: String = "17:30",
    val slotDurationMinutes: Int = 30
)

data class DoctorVacationDay(
    val id: String = UUID.randomUUID().toString(),
    val vacationDate: String,
    val reason: String = "Congé exceptionnel"
)

data class Appointment(
    val id: String = UUID.randomUUID().toString(),
    val patientId: String,
    val patientName: String,
    val doctorId: String,
    val doctorName: String,
    val specialtyName: String,
    val date: String,
    val timeSlot: String,
    val motif: String,
    val beneficiary: String = "Moi-même",
    val priceDt: Int,
    val status: AppointmentStatus = AppointmentStatus.CONFIRMED,
    val isPaid: Boolean = false,
    val paymentMethod: String = "Paiement au cabinet",
    val consultationType: String = "IN_PERSON" // "IN_PERSON"
)

data class MedicalDocument(
    val id: String = UUID.randomUUID().toString(),
    val patientId: String = "pat_1",
    val title: String,
    val category: DocumentCategory,
    val dateAdded: String,
    val fileSize: String,
    val notes: String = "",
    val fileUrl: String = "",
    val shares: List<DocumentShare> = emptyList()
)

data class HealthMetricRecord(
    val id: String = UUID.randomUUID().toString(),
    val patientId: String = "pat_1",
    val metricType: String, // "TENSION", "GLYCEMIE", "HEART_RATE", "TEMPERATURE", "WEIGHT", "DISEASE"
    val metricValue: Float,
    val secondaryValue: Float? = null, // Ex: Diastolique pour la tension
    val recordedDate: String = "Aujourd'hui",
    val notes: String = "",
    val diseaseName: String = ""
)

data class DocumentShare(
    val id: String = UUID.randomUUID().toString(),
    val doctorId: String,
    val doctorName: String,
    val startDate: String,
    val expiryDate: String,
    val isRevoked: Boolean = false,
    val notes: String = "",
    val doctorRemark: String = ""
)

data class ConsultationRecord(
    val id: String = UUID.randomUUID().toString(),
    val appointmentId: String,
    val doctorName: String,
    val patientName: String,
    val specialtyName: String,
    val date: String,
    val diagnosis: String,
    val recommendations: String,
    val restDays: Int = 0,
    val prescription: Prescription? = null
)

data class Prescription(
    val id: String = UUID.randomUUID().toString(),
    val doctorName: String,
    val doctorOrderNumber: String,
    val date: String,
    val medications: List<String>
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String = "",
    val senderName: String,
    val receiverId: String = "",
    val receiverName: String = "",
    val isDoctor: Boolean,
    val text: String,
    val timestamp: String,
    val isRead: Boolean = true
)

data class ChatConversation(
    val partnerId: String,
    val partnerName: String,
    val partnerRole: String = "DOCTOR",
    val partnerSpecialty: String = "",
    val lastMessage: String = "",
    val lastTimestamp: String = "",
    val unreadCount: Int = 0,
    val isOnline: Boolean = true
)

data class DoctorReview(
    val id: String = UUID.randomUUID().toString(),
    val doctorId: String,
    val patientName: String,
    val rating: Int,
    val comment: String,
    val date: String
)

data class DoctorRevenueStats(
    val totalRevenue: Int = 0,
    val todayCount: Int = 0,
    val videoCount: Int = 0,
    val inPersonCount: Int = 0,
    val weeklyRevenues: Map<String, Float> = emptyMap()
)

enum class NotificationType {
    ADDED,      // Nouveau rendez-vous réservé
    CONFIRMED,  // Rendez-vous validé / confirmé
    MODIFIED,   // Rendez-vous modifié (date / heure)
    CANCELLED,  // Rendez-vous annulé
    REJECTED,   // Rendez-vous refusé
    COMPLETED   // Consultation terminée
}

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: String = "Aujourd'hui",
    val type: NotificationType,
    val appointmentId: String? = null,
    val patientName: String = "",
    val doctorName: String = "",
    val isRead: Boolean = false
)

