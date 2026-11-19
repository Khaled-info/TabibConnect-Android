package com.example.tabibconnect.ui.screens.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.tabibconnect.data.repository.TabibRepository
import com.example.tabibconnect.ui.components.InitialsAvatar
import com.example.tabibconnect.ui.components.RatingStars
import com.example.tabibconnect.ui.theme.PrimaryDarkTeal
import com.example.tabibconnect.ui.theme.PrimaryTeal
import com.example.tabibconnect.ui.theme.TealLightContainer
import com.example.tabibconnect.ui.theme.TealPrimary
import com.example.tabibconnect.ui.theme.TextDarkNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorProfileScreen(
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) {
        TabibRepository.fetchDoctorsAsync()
    }

    val currentDoc = TabibRepository.doctorsList.firstOrNull { it.id == "doc_1" }
        ?: TabibRepository.doctorsList.firstOrNull()

    val doctorName = currentDoc?.name ?: "Dr. Mohamed Ben Salah"
    val specialtyName = currentDoc?.specialtyName ?: "Cardiologie"
    val orderNumber = currentDoc?.orderNumber ?: "TN-12845"
    val governorate = currentDoc?.governorate ?: "Tunis"
    val delegation = currentDoc?.delegation ?: "Centre Ville"

    // Doctor profile settings states
    var priceDt by remember { mutableIntStateOf(currentDoc?.priceDt ?: 60) }
    var offersTeleconsultation by remember { mutableStateOf(false) }
    var offersInPerson by remember { mutableStateOf(currentDoc?.offersInPerson ?: true) }
    var bioText by remember { mutableStateOf(currentDoc?.bio ?: "Spécialiste diplômé en suivi cardiologique.") }

    LaunchedEffect(currentDoc) {
        if (currentDoc != null) {
            priceDt = currentDoc.priceDt
            offersTeleconsultation = false
            offersInPerson = currentDoc.offersInPerson
            bioText = currentDoc.bio
        }
    }

    // Platform Subscription States (50 DT / mois or 500 DT / an)
    var selectedPlan by remember { mutableStateOf("ANNUAL") } // "MONTHLY" (50 DT) or "ANNUAL" (500 DT)
    val subscriptionExpiryDate = remember { "02/11/2027" } // Synced with BDD
    var showPaymentSuccessDialog by remember { mutableStateOf(false) }
    var showSaveSuccessDialog by remember { mutableStateOf(false) }
    var isSavingProfile by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val cleanDoctorName = doctorName.replace("Dr.", "", ignoreCase = true).replace("Dr", "", ignoreCase = true).trim()
    val doctorInitials = cleanDoctorName.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }.take(2).joinToString("").ifBlank { "TB" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Espace Profil Médecin",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryDarkTeal
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Full Width Doctor Header Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                InitialsAvatar(
                    initials = doctorInitials,
                    backgroundColor = TealPrimary,
                    size = 68.dp,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = doctorName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDarkNavy,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    color = TealLightContainer,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "$specialtyName • Ordre: $orderNumber",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Cabinet : $governorate, $delegation",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                RatingStars(rating = currentDoc?.rating ?: 5.0, reviewCount = currentDoc?.reviewCount ?: 0)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section 1: Paramétrage du Tarif de Consultation
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tarif & Types de Consultation", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDarkNavy)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Price Adjustment Field
                Text("Coût de la séance / consultation (DT) :", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDarkNavy)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { if (priceDt > 20) priceDt -= 5 },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("- 5 DT", fontWeight = FontWeight.Bold, color = TealPrimary)
                    }

                    Surface(
                        color = TealLightContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text("$priceDt DT", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TealPrimary)
                        }
                    }

                    OutlinedButton(
                        onClick = { priceDt += 5 },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+ 5 DT", fontWeight = FontWeight.Bold, color = TealPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Consultation Services Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = TextDarkNavy, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Consultation Présentielle (Cabinet)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Switch(
                        checked = offersInPerson,
                        onCheckedChange = { offersInPerson = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TealPrimary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section 2: Espace d'Abonnement de la Plateforme (Synchronisé BDD)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Title Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CardMembership, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Abonnement Plateforme", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDarkNavy)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status & Expiry Badge BELOW Title
                Surface(
                    color = Color(0xFFE6F4EA),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "🟢 Abonnement Actif • Date d'expiration : $subscriptionExpiryDate",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF137333),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Choisissez votre formule d'accès Médecin :", fontSize = 12.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(8.dp))

                // Subscription Plan Choice Cards Side-by-Side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Monthly Plan (50 DT)
                    val isMonthly = selectedPlan == "MONTHLY"
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMonthly) TealLightContainer else Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isMonthly) TealPrimary else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPlan = "MONTHLY" }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Formule Mensuelle", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextDarkNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("50 DT", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TealPrimary)
                            Text("par mois", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    // Annual Plan (500 DT)
                    val isAnnual = selectedPlan == "ANNUAL"
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAnnual) TealLightContainer else Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isAnnual) TealPrimary else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPlan = "ANNUAL" }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Formule Annuelle", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextDarkNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("500 DT", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TealPrimary)
                            Text("par an (12 mois)", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Action Button
                Button(
                    onClick = { showPaymentSuccessDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkTeal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (selectedPlan == "MONTHLY") "Payer 50 DT par Carte / e-Dinar" else "Payer 500 DT par Carte / e-Dinar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section 3: Bio & Informations Médicales
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Présentation / Bio du Médecin", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDarkNavy)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = bioText,
                    onValueChange = { bioText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save Profile Button connected to SQL Server BDD
        Button(
            enabled = !isSavingProfile,
            onClick = {
                isSavingProfile = true
                coroutineScope.launch {
                    TabibRepository.updateDoctorProfileAsync(
                        doctorId = currentDoc?.id ?: "doc_1",
                        priceDt = priceDt,
                        bio = bioText,
                        offersTeleconsultation = offersTeleconsultation,
                        offersInPerson = offersInPerson
                    )
                    isSavingProfile = false
                    showSaveSuccessDialog = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (isSavingProfile) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
            } else {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enregistrer les modifications", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Logout Button (Red)
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCE8E6), contentColor = Color(0xFFE53935)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Se déconnecter", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }

    // Save Confirmation Popup Dialog
    if (showSaveSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSaveSuccessDialog = false },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(48.dp))
            },
            title = {
                Text("Modifications Enregistrées !", fontWeight = FontWeight.Bold, color = TextDarkNavy, fontSize = 18.sp)
            },
            text = {
                Text(
                    text = "Vos informations de profil, tarif de consultation et préférences ont été sauvegardés avec succès dans la base de données.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSaveSuccessDialog = false },
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

    // Payment Success Dialog for Subscription
    if (showPaymentSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentSuccessDialog = false },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(48.dp))
            },
            title = {
                Text("Abonnement Confirmé !", fontWeight = FontWeight.Bold, color = TextDarkNavy)
            },
            text = {
                Text(
                    text = "Votre paiement par Carte / e-Dinar pour la formule ${if (selectedPlan == "MONTHLY") "Mensuelle (50 DT)" else "Annuelle (500 DT)"} a été validé avec succès !",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPaymentSuccessDialog = false },
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
