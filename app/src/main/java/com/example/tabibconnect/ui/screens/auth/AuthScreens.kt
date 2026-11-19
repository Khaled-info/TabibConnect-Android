package com.example.tabibconnect.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tabibconnect.data.model.UserRole
import com.example.tabibconnect.data.repository.TabibRepository
import com.example.tabibconnect.ui.components.TabibConnectLogo
import com.example.tabibconnect.ui.theme.TealLightContainer
import com.example.tabibconnect.ui.theme.TealPrimary
import com.example.tabibconnect.ui.theme.TextDarkNavy
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {
    var selectedRole by remember { mutableStateOf(UserRole.PATIENT) }
    var isRegisterMode by remember { mutableStateOf(false) }

    // Form fields (Empty by default with "Saisir votre..." placeholders)
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Governorate & Delegation
    var governorate by remember { mutableStateOf("Tunis") }
    var delegation by remember { mutableStateOf("Centre Ville") }

    // Gender & Birth Date
    var gender by remember { mutableStateOf("Homme") }
    var birthDate by remember { mutableStateOf("") }

    var specialtyName by remember { mutableStateOf("Cardiologie") }
    var doctorOrderNo by remember { mutableStateOf("") }

    // Selection Dialog States
    var showGovModal by remember { mutableStateOf(false) }
    var showDelModal by remember { mutableStateOf(false) }
    var showSpecModal by remember { mutableStateOf(false) }

    // Forgot Password Modal State
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Dialog states for Account Registration Popup
    var showRegisterSuccessDialog by remember { mutableStateOf(false) }
    var registerSuccessMessage by remember { mutableStateOf("") }
    var showRegisterErrorDialog by remember { mutableStateOf(false) }
    var registerErrorMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Dynamic delegations for selected governorate
    val availableDelegations = remember(governorate) {
        TabibRepository.governorateDelegationsMap[governorate] ?: listOf("Centre Ville", "Nord", "Sud")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F8FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding() // Auto-scroll fields into view above virtual keyboard
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(54.dp))

            // Centered Official TabibConnect Logo
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TabibConnectLogo(
                    iconSize = 58.dp,
                    showSubtitle = true
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Centered Header Title
            Text(
                text = if (isRegisterMode) "CRÉATION DE COMPTE" else "AUTHENTIFICATION",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDarkNavy,
                textAlign = TextAlign.Center,
                letterSpacing = 1.2.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Role Selection Cards shown ONLY during Account Registration Mode
            AnimatedVisibility(visible = isRegisterMode) {
                Column {
                    Text(
                        text = "Choisissez votre espace",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Patient Space Card
                        val isPatient = selectedRole == UserRole.PATIENT
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPatient) TealLightContainer else Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isPatient) TealPrimary else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedRole = UserRole.PATIENT
                                    errorMessage = null
                                }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Icon(
                                    imageVector = Icons.Default.PersonOutline,
                                    contentDescription = null,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Espace patient",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextDarkNavy
                                )
                                Text(
                                    text = "Consulter, réserver",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6B7C85)
                                )
                            }
                        }

                        // Doctor Space Card
                        val isDoctor = selectedRole == UserRole.DOCTOR
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDoctor) TealLightContainer else Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isDoctor) TealPrimary else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedRole = UserRole.DOCTOR
                                    errorMessage = null
                                }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = null,
                                    tint = if (isDoctor) TealPrimary else Color(0xFF6B7C85),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Espace médecin",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextDarkNavy
                                )
                                Text(
                                    text = "Agenda, patients",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6B7C85)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Explicit Red Error Banner if registration fails
            if (errorMessage != null) {
                Surface(
                    color = Color(0xFFFEF2F2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage!!,
                            fontSize = 12.sp,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Explicit Green Success Banner when saved into SQL Server table
            if (successMessage != null) {
                Surface(
                    color = TealLightContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, TealPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = successMessage!!,
                            fontSize = 12.sp,
                            color = TextDarkNavy,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Registration Mode Fields
            AnimatedVisibility(visible = isRegisterMode) {
                Column {
                    Text(
                        text = "Nom et Prénom",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            errorMessage = null
                        },
                        placeholder = { Text("Saisir votre nom et prénom", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Governorate & Associated Delegation Structured Pickers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Governorate Button Box
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gouvernorat",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkNavy
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showGovModal = true }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 14.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(governorate, color = TextDarkNavy, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF6B7C85))
                                }
                            }
                        }

                        // Delegation Button Box
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Délégation",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkNavy
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDelModal = true }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 14.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(delegation, color = TextDarkNavy, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF6B7C85))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Genre & Date de Naissance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Genre Selector Box
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Genre",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkNavy
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Homme Option
                                Surface(
                                    onClick = { gender = "Homme" },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (gender == "Homme") TealLightContainer else Color.White,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (gender == "Homme") TealPrimary else Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Homme",
                                            fontSize = 12.sp,
                                            fontWeight = if (gender == "Homme") FontWeight.Bold else FontWeight.Normal,
                                            color = if (gender == "Homme") TealPrimary else TextDarkNavy
                                        )
                                    }
                                }

                                // Femme Option
                                Surface(
                                    onClick = { gender = "Femme" },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (gender == "Femme") TealLightContainer else Color.White,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (gender == "Femme") TealPrimary else Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Femme",
                                            fontSize = 12.sp,
                                            fontWeight = if (gender == "Femme") FontWeight.Bold else FontWeight.Normal,
                                            color = if (gender == "Femme") TealPrimary else TextDarkNavy
                                        )
                                    }
                                }
                            }
                        }

                        // Date de Naissance Field
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Date de naissance",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkNavy
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = birthDate,
                                onValueChange = { input ->
                                    birthDate = formatDateInput(input)
                                    errorMessage = null
                                },
                                placeholder = { Text("JJ/MM/AAAA", color = Color(0xFF94A3B8), fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = TealPrimary,
                                    unfocusedBorderColor = Color(0xFFE2E8F0)
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // Phone Input
            Text(
                text = "Téléphone",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextDarkNavy
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    errorMessage = null
                },
                placeholder = { Text("Saisir votre numéro (+216)", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password Input
            Text(
                text = "Mot de passe",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextDarkNavy
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                placeholder = { Text("Saisir votre mot de passe", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                singleLine = true
            )

            // Password Confirmation Input (Only in Registration Mode)
            AnimatedVisibility(visible = isRegisterMode) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Text(
                        text = "Confirmation du mot de passe",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        placeholder = { Text("Confirmer votre mot de passe", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        singleLine = true
                    )
                }
            }

            // Doctor Specific Registration Fields
            AnimatedVisibility(visible = isRegisterMode && selectedRole == UserRole.DOCTOR) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Text(
                        text = "Spécialité Médicale",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSpecModal = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(specialtyName, color = TextDarkNavy, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF6B7C85))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "N° Ordre des Médecins",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = doctorOrderNo,
                        onValueChange = { doctorOrderNo = it },
                        placeholder = { Text("Saisir votre N° d'Ordre (Ex: TN-12845)", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Submit Button (Connected to SQL Server)
            Button(
                enabled = !isLoading,
                onClick = {
                    errorMessage = null
                    successMessage = null

                    if (isRegisterMode) {
                        if (fullName.isBlank()) {
                            val msg = "Veuillez saisir votre nom et prénom."
                            errorMessage = msg
                            registerErrorMessage = msg
                            showRegisterErrorDialog = true
                            return@Button
                        }
                        if (phone.isBlank()) {
                            val msg = "Veuillez saisir votre numéro de téléphone."
                            errorMessage = msg
                            registerErrorMessage = msg
                            showRegisterErrorDialog = true
                            return@Button
                        }
                        if (password.isBlank()) {
                            val msg = "Veuillez saisir votre mot de passe."
                            errorMessage = msg
                            registerErrorMessage = msg
                            showRegisterErrorDialog = true
                            return@Button
                        }
                        if (password != confirmPassword) {
                            val msg = "La confirmation du mot de passe ne correspond pas."
                            errorMessage = msg
                            registerErrorMessage = msg
                            showRegisterErrorDialog = true
                            return@Button
                        }
                        if (birthDate.isNotBlank() && !isValidDateOfBirth(birthDate)) {
                            val msg = "La date de naissance doit être au format JJ/MM/AAAA valide (ex: 01/02/1997)."
                            errorMessage = msg
                            registerErrorMessage = msg
                            showRegisterErrorDialog = true
                            return@Button
                        }
                        if (selectedRole == UserRole.DOCTOR && doctorOrderNo.isBlank()) {
                            val msg = "Veuillez saisir votre N° d'Ordre des Médecins."
                            errorMessage = msg
                            registerErrorMessage = msg
                            showRegisterErrorDialog = true
                            return@Button
                        }
                    } else {
                        if (phone.isBlank() || password.isBlank()) {
                            errorMessage = "Veuillez saisir votre téléphone et mot de passe."
                            return@Button
                        }
                    }

                    isLoading = true
                    coroutineScope.launch {
                        if (isRegisterMode) {
                            val regRes = TabibRepository.registerUserAsync(
                                fullName = fullName,
                                phone = phone,
                                pass = password,
                                governorate = governorate,
                                delegation = delegation,
                                gender = gender,
                                birthDate = birthDate,
                                role = selectedRole,
                                specialtyName = specialtyName,
                                orderNumber = doctorOrderNo
                            )
                            isLoading = false
                            if (regRes.isSuccess) {
                                registerSuccessMessage = "Votre compte a été créé avec succès ! Cliquez sur OK pour vous connecter."
                                showRegisterSuccessDialog = true
                            } else {
                                val errorMsg = regRes.exceptionOrNull()?.message ?: "Erreur d'inscription"
                                errorMessage = "❌ $errorMsg"
                                registerErrorMessage = errorMsg
                                showRegisterErrorDialog = true
                            }
                        } else {
                            val loginRes = TabibRepository.loginUserAsync(phone, password)
                            isLoading = false
                            if (loginRes.isSuccess) {
                                onLoginSuccess()
                            } else {
                                errorMessage = "❌ ${loginRes.exceptionOrNull()?.message}"
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isRegisterMode) Icons.Default.PersonAdd else Icons.AutoMirrored.Filled.Login,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRegisterMode) "Créer mon compte" else "Se connecter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle link between Login and Account Creation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Mot de passe oublié ?",
                    color = TealPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showForgotPasswordDialog = true }
                )

                Text(
                    text = if (isRegisterMode) "Déjà un compte ? Connexion" else "Créer un compte",
                    color = TealPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        isRegisterMode = !isRegisterMode
                        errorMessage = null
                        successMessage = null
                    }
                )
            }
        }
    }

    // Modern Structured Selection Modal for Governorate
    if (showGovModal) {
        StructuredListSelectionDialog(
            title = "Sélectionner un Gouvernorat",
            subtitle = "24 gouvernorats tunisiens",
            items = TabibRepository.governorates,
            selectedItem = governorate,
            onItemSelected = { selectedGov ->
                governorate = selectedGov
                val newDels = TabibRepository.governorateDelegationsMap[selectedGov] ?: emptyList()
                if (newDels.isNotEmpty()) {
                    delegation = newDels.first()
                }
                showGovModal = false
            },
            onDismiss = { showGovModal = false }
        )
    }

    // Modern Structured Selection Modal for Delegation
    if (showDelModal) {
        StructuredListSelectionDialog(
            title = "Sélectionner une Délégation",
            subtitle = "Délégations de $governorate",
            items = availableDelegations,
            selectedItem = delegation,
            onItemSelected = { selectedDel ->
                delegation = selectedDel
                showDelModal = false
            },
            onDismiss = { showDelModal = false }
        )
    }

    // Modern Structured Selection Modal for Specialty
    if (showSpecModal) {
        StructuredListSelectionDialog(
            title = "Sélectionner une Spécialité",
            subtitle = "Spécialités médicales",
            items = TabibRepository.specialties.map { it.nameFr },
            selectedItem = specialtyName,
            onItemSelected = { selectedSpec ->
                specialtyName = selectedSpec
                showSpecModal = false
            },
            onDismiss = { showSpecModal = false }
        )
    }

    // Dedicated "Mot de passe oublié ?" Interface Modal
    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            onDismiss = { showForgotPasswordDialog = false },
            onPasswordResetSuccess = {
                showForgotPasswordDialog = false
                successMessage = "Mot de passe réinitialisé avec succès !"
            }
        )
    }

    // Success Dialog after Registration with OK button redirecting to Login mode
    if (showRegisterSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showRegisterSuccessDialog = false
                isRegisterMode = false
                password = ""
                confirmPassword = ""
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Inscription Réussie !",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextDarkNavy,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = registerSuccessMessage,
                    fontSize = 14.sp,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRegisterSuccessDialog = false
                        isRegisterMode = false // Redirection vers la page d'authentification (Se connecter)
                        password = ""
                        confirmPassword = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "OK",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Error Dialog after Registration failure (stay on registration page)
    if (showRegisterErrorDialog) {
        AlertDialog(
            onDismissRequest = { showRegisterErrorDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Erreur d'inscription",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextDarkNavy,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = registerErrorMessage,
                    fontSize = 14.sp,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { showRegisterErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "OK",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// Dedicated Modal Interface for "Mot de passe oublié ?"
@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onPasswordResetSuccess: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: Enter Phone, 2: Enter OTP & New Password
    var resetPhone by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    var modalError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LockReset, contentDescription = null, tint = TealPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Réinitialisation du mot de passe",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkNavy
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (step == 1) "Saisissez votre numéro pour recevoir un code OTP" else "Saisissez le code reçu et votre nouveau mot de passe",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7C85)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (modalError != null) {
                    Text(
                        text = modalError!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (step == 1) {
                    Text("Numéro de téléphone (+216)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = resetPhone,
                        onValueChange = {
                            resetPhone = it
                            modalError = null
                        },
                        placeholder = { Text("Saisir votre numéro de téléphone", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                } else {
                    Text("Code OTP reçu par SMS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = {
                            otpInput = it
                            modalError = null
                        },
                        placeholder = { Text("Saisir le code OTP (Ex: 1234)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Nouveau mot de passe", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            modalError = null
                        },
                        placeholder = { Text("Saisir votre nouveau mot de passe", fontSize = 12.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Confirmer le nouveau mot de passe", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = confirmNewPassword,
                        onValueChange = {
                            confirmNewPassword = it
                            modalError = null
                        },
                        placeholder = { Text("Confirmer le nouveau mot de passe", fontSize = 12.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting,
                onClick = {
                    if (step == 1) {
                        if (resetPhone.isBlank()) {
                            modalError = "Veuillez saisir votre numéro."
                            return@Button
                        }
                        step = 2
                    } else {
                        if (otpInput.isBlank()) {
                            modalError = "Veuillez saisir le code OTP."
                            return@Button
                        }
                        if (newPassword.isBlank()) {
                            modalError = "Veuillez saisir un nouveau mot de passe."
                            return@Button
                        }
                        if (newPassword != confirmNewPassword) {
                            modalError = "Les mots de passe ne correspondent pas."
                            return@Button
                        }

                        isSubmitting = true
                        coroutineScope.launch {
                            TabibRepository.resetPasswordAsync(resetPhone, newPassword)
                            isSubmitting = false
                            onPasswordResetSuccess()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text(if (step == 1) "Envoyer le code OTP" else "Réinitialiser", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = Color(0xFF6B7C85))
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}

// Reusable Modern Structured Selection Modal Component
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
                                containerColor = if (isSelected) TealLightContainer else Color.White
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

// Helper functions for Date formatting and validation
private fun formatDateInput(input: String): String {
    val digits = input.filter { it.isDigit() }.take(8)
    val sb = StringBuilder()
    for (i in digits.indices) {
        if (i == 2 || i == 4) {
            sb.append('/')
        }
        sb.append(digits[i])
    }
    return sb.toString()
}

private fun isValidDateOfBirth(dateStr: String): Boolean {
    if (dateStr.isBlank()) return true
    val regex = Regex("""^(\d{2})/(\d{2})/(\d{4})$""")
    val match = regex.matchEntire(dateStr) ?: return false
    val (dayStr, monthStr, yearStr) = match.destructured
    val day = dayStr.toIntOrNull() ?: return false
    val month = monthStr.toIntOrNull() ?: return false
    val year = yearStr.toIntOrNull() ?: return false

    if (month !in 1..12) return false
    if (day !in 1..31) return false
    if (year !in 1900..2026) return false

    val daysInMonth = when (month) {
        2 -> if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    return day <= daysInMonth
}
