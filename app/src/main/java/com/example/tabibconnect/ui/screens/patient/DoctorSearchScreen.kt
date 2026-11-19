package com.example.tabibconnect.ui.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.tabibconnect.data.repository.TabibRepository
import com.example.tabibconnect.ui.components.RatingStars
import com.example.tabibconnect.ui.components.StructuredListSelectionDialog
import com.example.tabibconnect.ui.theme.LightTealContainer
import com.example.tabibconnect.ui.theme.PrimaryDarkTeal
import com.example.tabibconnect.ui.theme.PrimaryTeal
import com.example.tabibconnect.ui.theme.TealLightContainer
import com.example.tabibconnect.ui.theme.TealPrimary
import com.example.tabibconnect.ui.theme.TextDarkNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorSearchScreen(
    initialSpecialty: String? = null,
    onDoctorSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSpecialty by remember { mutableStateOf(initialSpecialty ?: "Toutes") }
    var selectedGovernorate by remember { mutableStateOf("Tous") }
    var selectedDelegation by remember { mutableStateOf("Toutes") }
    var selectedType by remember { mutableStateOf("Tous") } // "Tous", "Vidéo", "En Cabinet"

    var showSpecModal by remember { mutableStateOf(false) }
    var showGovModal by remember { mutableStateOf(false) }
    var showDelModal by remember { mutableStateOf(false) }

    var filterFavoritesOnly by remember { mutableStateOf(false) }

    // Fetch doctors from SQL Server Database on launch or specialty selection
    LaunchedEffect(initialSpecialty) {
        TabibRepository.fetchDoctorsAsync()
        if (!initialSpecialty.isNullOrBlank()) {
            selectedSpecialty = initialSpecialty
        }
    }

    val availableDelegations = remember(selectedGovernorate) {
        if (selectedGovernorate == "Tous") emptyList()
        else TabibRepository.governorateDelegationsMap[selectedGovernorate] ?: emptyList()
    }

    val filteredDoctors = remember(
        searchQuery,
        selectedSpecialty,
        selectedGovernorate,
        selectedDelegation,
        selectedType,
        filterFavoritesOnly,
        TabibRepository.doctorsList.toList()
    ) {
        TabibRepository.doctorsList.filter { doc ->
            val matchesQuery = searchQuery.isEmpty() ||
                    doc.name.contains(searchQuery, ignoreCase = true) ||
                    doc.specialtyName.contains(searchQuery, ignoreCase = true)
            val matchesSpecialty = selectedSpecialty == "Toutes" || doc.specialtyName.equals(selectedSpecialty, ignoreCase = true)
            val matchesGov = selectedGovernorate == "Tous" || doc.governorate.equals(selectedGovernorate, ignoreCase = true)
            val matchesDel = selectedDelegation == "Toutes" || doc.delegation.equals(selectedDelegation, ignoreCase = true)
            val matchesType = when (selectedType) {
                "En Cabinet" -> doc.offersInPerson
                else -> true
            }
            val matchesFav = !filterFavoritesOnly || doc.isFavorite

            matchesQuery && matchesSpecialty && matchesGov && matchesDel && matchesType && matchesFav
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9))
            .padding(16.dp)
    ) {
        Text(
            text = "Rechercher un Médecin",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryDarkTeal
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Nom de médecin ou spécialité...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryTeal) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Effacer")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = PrimaryTeal,
                unfocusedBorderColor = Color(0xFFE2E8F0)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Structured Filter Buttons Row (Opening Unified Selection Dialogs)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Specialty Modal Button
            OutlinedButton(
                onClick = { showSpecModal = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (selectedSpecialty == "Toutes") "Spécialité" else if (selectedSpecialty.length > 9) selectedSpecialty.take(8) + ".." else selectedSpecialty,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDarkNavy,
                    maxLines = 1
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }

            // Governorate Modal Button
            OutlinedButton(
                onClick = { showGovModal = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (selectedGovernorate == "Tous") "Gouvernorat" else selectedGovernorate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDarkNavy,
                    maxLines = 1
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }

            // Delegation Modal Button (Enabled when Governorate is selected)
            OutlinedButton(
                onClick = { if (availableDelegations.isNotEmpty()) showDelModal = true },
                enabled = availableDelegations.isNotEmpty(),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Map, contentDescription = null, tint = if (availableDelegations.isNotEmpty()) PrimaryTeal else Color.Gray, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (selectedDelegation == "Toutes") "Délégation" else if (selectedDelegation.length > 8) selectedDelegation.take(7) + ".." else selectedDelegation,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDarkNavy,
                    maxLines = 1
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Consultation Type Filter Pills Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val typeOptions = listOf("Tous", "En Cabinet 🩺")
            typeOptions.forEach { option ->
                val typeKey = when {
                    option.startsWith("En Cabinet") -> "En Cabinet"
                    else -> "Tous"
                }
                val isSelected = selectedType == typeKey
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedType = typeKey },
                    label = { Text(option, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TealLightContainer,
                        selectedLabelColor = TealPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Favorites Filter Button
            IconButton(
                onClick = { filterFavoritesOnly = !filterFavoritesOnly },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (filterFavoritesOnly) Color(0xFFFFEAEA) else Color.White)
            ) {
                Icon(
                    imageVector = if (filterFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favoris",
                    tint = if (filterFavoritesOnly) Color.Red else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Results count
        Text(
            text = "${filteredDoctors.size} médecin(s) trouvé(s)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryDarkTeal
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredDoctors.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aucun médecin ne correspond à vos filtres.",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredDoctors) { doc ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDoctorSelected(doc.id) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(LightTealContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = PrimaryTeal,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = doc.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
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

                                IconButton(onClick = { TabibRepository.toggleFavorite(doc.id) }) {
                                    Icon(
                                        imageVector = if (doc.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favori",
                                        tint = if (doc.isFavorite) Color.Red else Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Badges for Consultation Types
                            Surface(
                                color = TealLightContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocalHospital, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Consultation en Cabinet", fontSize = 10.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Disponible : Auj. ${doc.availableSlots.firstOrNull() ?: "10:00"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = PrimaryDarkTeal
                                    )
                                    Text(
                                        text = "Tarif : ${doc.priceDt} DT",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PrimaryTeal
                                    )
                                }

                                Button(
                                    onClick = { onDoctorSelected(doc.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text("Prendre RDV", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Unified Selection Modal for Specialty
    if (showSpecModal) {
        StructuredListSelectionDialog(
            title = "Sélectionner une Spécialité",
            subtitle = "15 spécialités médicales",
            items = listOf("Toutes") + TabibRepository.specialties.map { it.nameFr },
            selectedItem = selectedSpecialty,
            onItemSelected = { spec ->
                selectedSpecialty = spec
                showSpecModal = false
            },
            onDismiss = { showSpecModal = false }
        )
    }

    // Unified Selection Modal for Governorate
    if (showGovModal) {
        StructuredListSelectionDialog(
            title = "Sélectionner un Gouvernorat",
            subtitle = "Gouvernorats tunisiens",
            items = listOf("Tous") + TabibRepository.governorates,
            selectedItem = selectedGovernorate,
            onItemSelected = { gov ->
                selectedGovernorate = gov
                selectedDelegation = "Toutes"
                showGovModal = false
            },
            onDismiss = { showGovModal = false }
        )
    }

    // Unified Selection Modal for Delegation
    if (showDelModal) {
        StructuredListSelectionDialog(
            title = "Sélectionner une Délégation",
            subtitle = "Délégations de $selectedGovernorate",
            items = listOf("Toutes") + availableDelegations,
            selectedItem = selectedDelegation,
            onItemSelected = { del ->
                selectedDelegation = del
                showDelModal = false
            },
            onDismiss = { showDelModal = false }
        )
    }
}
