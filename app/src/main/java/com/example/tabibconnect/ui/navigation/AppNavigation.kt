package com.example.tabibconnect.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tabibconnect.data.model.UserRole
import com.example.tabibconnect.data.repository.TabibRepository
import com.example.tabibconnect.ui.components.RoleHeaderSwitcher
import com.example.tabibconnect.ui.screens.auth.AuthScreen
import com.example.tabibconnect.ui.screens.doctor.DoctorHomeScreen
import com.example.tabibconnect.ui.screens.patient.*
import com.example.tabibconnect.ui.theme.TealLightContainer
import com.example.tabibconnect.ui.theme.TealPrimary

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object PatientHome : Screen("patient_home")
    object DoctorSearch : Screen("doctor_search")
    object PatientAppointments : Screen("patient_appointments")
    object PatientNotifications : Screen("patient_notifications")
    object MedicalDossier : Screen("medical_dossier")
    object PatientProfile : Screen("patient_profile")
    object ConsultationHistory : Screen("consultation_history")
    object DoctorHome : Screen("doctor_home")
    object DoctorAgenda : Screen("doctor_agenda")
    object DoctorSchedule : Screen("doctor_schedule")
    object DoctorPatients : Screen("doctor_patients")
    object DoctorNotifications : Screen("doctor_notifications")
    object DoctorProfileBooking : Screen("doctor_booking")
    object VideoCall : Screen("video_call")
    object Chat : Screen("chat")
}

@Composable
fun TabibConnectApp() {
    var isAuthenticated by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.PatientHome) }
    var selectedDoctorForBookingId by remember { mutableStateOf("doc_1") }
    var searchInitialSpecialty by remember { mutableStateOf<String?>(null) }

    val currentRole = TabibRepository.currentUserRole.value

    if (!isAuthenticated) {
        AuthScreen(
            onLoginSuccess = {
                isAuthenticated = true
                currentScreen = if (TabibRepository.currentUserRole.value == UserRole.DOCTOR) {
                    Screen.DoctorHome
                } else {
                    Screen.PatientHome
                }
            }
        )
        return
    }



    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            RoleHeaderSwitcher(
                onNavigateToProfile = { currentScreen = Screen.PatientProfile },
                onLogout = { isAuthenticated = false }
            )
        },
        bottomBar = {
            if (currentScreen !is Screen.DoctorProfileBooking && currentScreen !is Screen.VideoCall) {
                if (currentRole == UserRole.PATIENT) {
                    val patientItems = listOf(
                        com.example.tabibconnect.ui.components.NavigationTabItem("Accueil", Icons.Default.Home, Icons.Default.Home),
                        com.example.tabibconnect.ui.components.NavigationTabItem("Recherche", Icons.Default.Search, Icons.Default.Search),
                        com.example.tabibconnect.ui.components.NavigationTabItem("RDV", Icons.Default.CalendarToday, Icons.Default.CalendarToday),
                        com.example.tabibconnect.ui.components.NavigationTabItem("Docs", Icons.Default.FolderCopy, Icons.Default.FolderCopy),
                        com.example.tabibconnect.ui.components.NavigationTabItem("Chat", Icons.Default.ChatBubble, Icons.Default.ChatBubble),
                        com.example.tabibconnect.ui.components.NavigationTabItem("Notifs", Icons.Default.Notifications, Icons.Default.Notifications),
                        com.example.tabibconnect.ui.components.NavigationTabItem("Profil", Icons.Default.PersonOutline, Icons.Default.Person)
                    )
                    val selectedIndex = when (currentScreen) {
                        is Screen.PatientHome -> 0
                        is Screen.DoctorSearch -> 1
                        is Screen.PatientAppointments, is Screen.ConsultationHistory -> 2
                        is Screen.MedicalDossier -> 3
                        is Screen.Chat -> 4
                        is Screen.PatientNotifications -> 5
                        is Screen.PatientProfile -> 6
                        else -> 0
                    }
                    com.example.tabibconnect.ui.components.InnovativeNavigationBar(
                        items = patientItems,
                        selectedIndex = selectedIndex,
                        onItemSelected = { index ->
                            currentScreen = when (index) {
                                0 -> Screen.PatientHome
                                1 -> Screen.DoctorSearch
                                2 -> Screen.PatientAppointments
                                3 -> Screen.MedicalDossier
                                4 -> Screen.Chat
                                5 -> Screen.PatientNotifications
                                6 -> Screen.PatientProfile
                                else -> Screen.PatientHome
                            }
                        }
                    )
                } else {
                    val doctorItems = listOf(
                        com.example.tabibconnect.ui.components.NavigationTabItem("Tableau", Icons.Default.Home, Icons.Default.Home),
                        com.example.tabibconnect.ui.components.NavigationTabItem("Agenda", Icons.Default.CalendarToday, Icons.Default.CalendarToday),
                        com.example.tabibconnect.ui.components.NavigationTabItem("Horaire", Icons.Default.AccessTime, Icons.Default.AccessTime),
                        com.example.tabibconnect.ui.components.NavigationTabItem("Docs", Icons.Default.FolderCopy, Icons.Default.FolderCopy),
                        com.example.tabibconnect.ui.components.NavigationTabItem("Chat", Icons.Default.ChatBubble, Icons.Default.ChatBubble),
                        com.example.tabibconnect.ui.components.NavigationTabItem("Notifs", Icons.Default.Notifications, Icons.Default.Notifications),
                        com.example.tabibconnect.ui.components.NavigationTabItem("Profil", Icons.Default.PersonOutline, Icons.Default.Person)
                    )
                    val selectedIndex = when (currentScreen) {
                        is Screen.DoctorHome -> 0
                        is Screen.DoctorAgenda -> 1
                        is Screen.DoctorSchedule -> 2
                        is Screen.MedicalDossier -> 3
                        is Screen.Chat -> 4
                        is Screen.DoctorNotifications -> 5
                        is Screen.PatientProfile -> 6
                        else -> 0
                    }
                    com.example.tabibconnect.ui.components.InnovativeNavigationBar(
                        items = doctorItems,
                        selectedIndex = selectedIndex,
                        onItemSelected = { index ->
                            currentScreen = when (index) {
                                0 -> Screen.DoctorHome
                                1 -> Screen.DoctorAgenda
                                2 -> Screen.DoctorSchedule
                                3 -> Screen.MedicalDossier
                                4 -> Screen.Chat
                                5 -> Screen.DoctorNotifications
                                6 -> Screen.PatientProfile
                                else -> Screen.DoctorHome
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                is Screen.PatientHome -> PatientHomeScreen(
                    onNavigateToSearch = { spec ->
                        searchInitialSpecialty = spec
                        currentScreen = Screen.DoctorSearch
                    },
                    onNavigateToDoctorProfile = { docId ->
                        selectedDoctorForBookingId = docId
                        currentScreen = Screen.DoctorProfileBooking
                    },
                    onNavigateToCall = { _ ->
                        currentScreen = Screen.PatientAppointments
                    },
                    onNavigateToDocuments = { currentScreen = Screen.MedicalDossier }
                )

                is Screen.DoctorSearch -> DoctorSearchScreen(
                    initialSpecialty = searchInitialSpecialty,
                    onDoctorSelected = { docId ->
                        selectedDoctorForBookingId = docId
                        currentScreen = Screen.DoctorProfileBooking
                    }
                )

                is Screen.DoctorProfileBooking -> DoctorProfileBookingScreen(
                    doctorId = selectedDoctorForBookingId,
                    onBookingSuccess = { currentScreen = Screen.PatientAppointments },
                    onBackClick = { currentScreen = Screen.DoctorSearch }
                )

                is Screen.PatientAppointments -> PatientAppointmentsScreen(
                    onNavigateToSearch = { currentScreen = Screen.DoctorSearch }
                )

                is Screen.MedicalDossier -> MedicalDossierScreen()

                is Screen.ConsultationHistory -> ConsultationHistoryScreen()

                is Screen.PatientProfile -> {
                    if (currentRole == UserRole.DOCTOR) {
                        com.example.tabibconnect.ui.screens.doctor.DoctorProfileScreen(
                            onLogout = { isAuthenticated = false }
                        )
                    } else {
                        PatientProfileScreen(
                            onLogout = { isAuthenticated = false }
                        )
                    }
                }

                is Screen.DoctorHome -> DoctorHomeScreen()

                is Screen.DoctorAgenda -> com.example.tabibconnect.ui.screens.doctor.DoctorAgendaScreen(
                    onStartCall = { _ ->
                        currentScreen = Screen.DoctorHome
                    }
                )

                is Screen.DoctorSchedule -> com.example.tabibconnect.ui.screens.doctor.DoctorScheduleScreen()

                is Screen.DoctorPatients -> com.example.tabibconnect.ui.screens.doctor.DoctorPatientsScreen()

                is Screen.DoctorNotifications, is Screen.PatientNotifications -> com.example.tabibconnect.ui.screens.doctor.DoctorNotificationsScreen()

                is Screen.Chat -> com.example.tabibconnect.ui.screens.chat.ChatScreen()

                else -> PatientHomeScreen(
                    onNavigateToSearch = { currentScreen = Screen.DoctorSearch },
                    onNavigateToDoctorProfile = { docId ->
                        selectedDoctorForBookingId = docId
                        currentScreen = Screen.DoctorProfileBooking
                    },
                    onNavigateToCall = { _ ->
                        currentScreen = Screen.PatientAppointments
                    },
                    onNavigateToDocuments = { currentScreen = Screen.MedicalDossier }
                )
            }
        }
    }
}
