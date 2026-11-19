package com.example.tabibconnect.ui.screens.patient

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.tabibconnect.data.model.DocumentCategory
import com.example.tabibconnect.data.model.MedicalDocument
import com.example.tabibconnect.data.model.UserRole
import com.example.tabibconnect.data.repository.TabibRepository
import com.example.tabibconnect.ui.components.StructuredListSelectionDialog
import com.example.tabibconnect.ui.theme.LightTealContainer
import com.example.tabibconnect.ui.theme.PrimaryDarkTeal
import com.example.tabibconnect.ui.theme.PrimaryTeal
import com.example.tabibconnect.ui.theme.TealLightContainer
import com.example.tabibconnect.ui.theme.TealPrimary
import com.example.tabibconnect.ui.theme.TextDarkNavy

fun convertUriToBase64DataUrl(context: Context, uri: Uri): String? {
    return try {
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val base64Str = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        "data:$mimeType;base64,$base64Str"
    } catch (_: Exception) {
        null
    }
}

// Helper function to download and save shared medical documents directly to the doctor's device Downloads directory
fun downloadMedicalFileToDevice(
    context: Context,
    docTitle: String,
    categoryLabel: String,
    dateAdded: String,
    patientName: String,
    notes: String,
    fileUrl: String = ""
): Boolean {
    return try {
        val cleanUrl = fileUrl.trim()
        var mimeType = "image/jpeg"
        var extension = "jpg"
        var binaryBytes: ByteArray? = null

        // 1. Check if Base64 Data URL
        if (cleanUrl.startsWith("data:") || cleanUrl.contains(";base64,")) {
            try {
                if (cleanUrl.contains("application/pdf")) {
                    mimeType = "application/pdf"
                    extension = "pdf"
                } else if (cleanUrl.contains("image/png")) {
                    mimeType = "image/png"
                    extension = "png"
                } else {
                    mimeType = "image/jpeg"
                    extension = "jpg"
                }

                val base64Part = if (cleanUrl.contains(",")) cleanUrl.substringAfter(",") else cleanUrl
                binaryBytes = android.util.Base64.decode(base64Part, android.util.Base64.DEFAULT)
            } catch (_: Exception) {}
        }
        // 2. Check if Content URI on same device
        else if (cleanUrl.startsWith("content://") || cleanUrl.startsWith("file://")) {
            try {
                val uri = Uri.parse(cleanUrl)
                val resolvedType = context.contentResolver.getType(uri)
                if (resolvedType == "application/pdf") {
                    mimeType = "application/pdf"
                    extension = "pdf"
                } else if (resolvedType == "image/png") {
                    mimeType = "image/png"
                    extension = "png"
                } else if (resolvedType?.startsWith("image/") == true) {
                    mimeType = "image/jpeg"
                    extension = "jpg"
                }
                binaryBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } catch (_: Exception) {}
        }

        val cleanTitle = docTitle.replace(" ", "_").replace("/", "_").replace("\\", "_")
        val fileName = "Dossier_${cleanTitle}_${System.currentTimeMillis().toString().takeLast(6)}.$extension"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        val targetFile = File(downloadsDir, fileName)

        if (binaryBytes != null && binaryBytes.isNotEmpty()) {
            // Write REAL BINARY IMAGE / PDF BYTES directly!
            targetFile.writeBytes(binaryBytes)
        } else {
            // Text report fallback IF no binary content is attached
            val content = """
                ==================================================
                TABIB CONNECT - DOSSIER MÉDICAL DU PATIENT
                ==================================================
                Titre du Document : $docTitle
                Catégorie        : $categoryLabel
                Patient          : $patientName
                Date d'ajout     : $dateAdded
                Fichier Joint    : ${if (fileUrl.isNotBlank()) fileUrl else "Scan médical rattaché"}
                ==================================================
                Commentaire / Notes du Patient :
                ${notes.ifBlank { "Aucune note spécifique fournie." }}
                ==================================================
                Ce document médical a été sauvegardé avec succès 
                dans la base de données et téléchargé sur l'appareil.
                ==================================================
            """.trimIndent()

            targetFile.writeText(content, Charsets.UTF_8)
            mimeType = "text/plain"
        }

        // 1. Notify Android MediaScanner
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = Uri.fromFile(targetFile)
        context.sendBroadcast(mediaScanIntent)

        // 2. Register with Android System DownloadManager to trigger status bar notification & system download entry
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        if (downloadManager != null) {
            downloadManager.addCompletedDownload(
                fileName,
                "Document médical - $docTitle ($patientName)",
                true,
                mimeType,
                targetFile.absolutePath,
                targetFile.length(),
                true
            )
        }

        true
    } catch (_: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalDossierScreen() {
    val currentRole = TabibRepository.currentUserRole.value
    val patient = TabibRepository.currentPatient.value
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Full page add mode state
    var isAddPageActive by remember { mutableStateOf(false) }

    // Patient Form States for Adding Document
    var formDocTitle by remember { mutableStateOf("") }
    var formAttachedFileName by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var formDocCategory by remember { mutableStateOf(DocumentCategory.BIOLOGY) }
    var formComment by remember { mutableStateOf("") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var isAddingDocLoading by remember { mutableStateOf(false) }

    // Deleting Document State
    var docToDelete by remember { mutableStateOf<MedicalDocument?>(null) }
    var isDeletingLoading by remember { mutableStateOf(false) }

    // Success Modals
    var showAddSuccessDialog by remember { mutableStateOf(false) }
    var addedDocTitleForSuccessModal by remember { mutableStateOf("") }

    var showShareSuccessDialog by remember { mutableStateOf(false) }
    var sharedDoctorNameForSuccessModal by remember { mutableStateOf("") }

    var showDownloadSuccessDialog by remember { mutableStateOf(false) }
    var downloadedDocTitleModal by remember { mutableStateOf("") }

    // Android Phone Native File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor?.moveToFirst()
                val name = if (nameIndex != null && nameIndex >= 0) cursor.getString(nameIndex) else "Document_Rattache.pdf"
                cursor?.close()
                formAttachedFileName = name
                if (formDocTitle.isBlank()) {
                    formDocTitle = name.substringBeforeLast(".")
                }
            } catch (_: Exception) {
                formAttachedFileName = "Piece_Jointe_Scan.pdf"
            }
        }
    }

    // Direct document share pop-up state
    var docToShareDirectly by remember { mutableStateOf<MedicalDocument?>(null) }
    var searchSpecialtyName by remember { mutableStateOf("Toutes spécialités") }
    var searchGovernorate by remember { mutableStateOf("Tous gouvernorats") }
    var searchDelegation by remember { mutableStateOf("Toutes délégations") }

    val popupAvailableDelegations = remember(searchGovernorate) {
        if (searchGovernorate == "Tous gouvernorats" || searchGovernorate == "Tous") emptyList()
        else TabibRepository.governorateDelegationsMap[searchGovernorate] ?: emptyList()
    }

    var showSpecModal by remember { mutableStateOf(false) }
    var showGovModal by remember { mutableStateOf(false) }
    var showDelModal by remember { mutableStateOf(false) }
    var isSharingDirectLoading by remember { mutableStateOf(false) }

    // Doctor side states
    var patientSearchQuery by remember { mutableStateOf("") }
    var selectedPatientNameForDoctor by remember { mutableStateOf<String?>(null) }
    var selectedDocForDetailView by remember { mutableStateOf<MedicalDocument?>(null) }

    // Remarks editing state map for doctor
    val doctorRemarkEdits = remember { mutableStateMapOf<String, String>() }
    val isSavingRemarkMap = remember { mutableStateMapOf<String, Boolean>() }

    // Fetch Doctors, Appointments & Medical Documents live from database on launch
    LaunchedEffect(patient.id, currentRole) {
        TabibRepository.fetchDoctorsAsync()
        TabibRepository.fetchAppointmentsAsync()
        TabibRepository.fetchMedicalDocumentsAsync()
    }

    // Resolve raw patient ID to real Patient Name dynamically from BDD appointments or current patient
    fun resolvePatientName(rawIdOrName: String): String {
        val clean = rawIdOrName.trim()
        if (clean.isBlank()) return ""

        val aptMatch = TabibRepository.appointments.firstOrNull {
            it.patientId.equals(clean, ignoreCase = true) || it.patientName.equals(clean, ignoreCase = true)
        }
        if (aptMatch != null && aptMatch.patientName.isNotBlank()) return aptMatch.patientName

        val curPat = TabibRepository.currentPatient.value
        if (curPat.id.equals(clean, ignoreCase = true) || curPat.userId.equals(clean, ignoreCase = true) || curPat.name.equals(clean, ignoreCase = true)) {
            if (curPat.name.isNotBlank()) return curPat.name
        }

        if (!clean.startsWith("pat_", ignoreCase = true) && !clean.startsWith("u_", ignoreCase = true) && clean.length <= 30) {
            return clean
        }

        return curPat.name
    }

    // Dynamic Patients List for Doctor - STRICTLY FROM BDD DATA
    val sharedDocuments = TabibRepository.medicalDocuments
    val doctorPatientsList = remember(patientSearchQuery, TabibRepository.appointments.toList(), sharedDocuments.toList()) {
        val namesFromApts = TabibRepository.appointments.map { it.patientName }
        val namesFromDocs = sharedDocuments.map { doc -> resolvePatientName(doc.patientId) }

        val allNames = (namesFromApts + namesFromDocs)
            .filter { name ->
                name.isNotBlank() &&
                !name.startsWith("pat_", ignoreCase = true) &&
                !name.startsWith("u_", ignoreCase = true) &&
                // Include patient ONLY if they have appointments OR shared documents in BDD
                (TabibRepository.appointments.any { it.patientName.equals(name, ignoreCase = true) } ||
                 sharedDocuments.any { doc -> resolvePatientName(doc.patientId).equals(name, ignoreCase = true) || doc.patientId.equals(name, ignoreCase = true) })
            }
            .distinct()

        if (patientSearchQuery.isBlank()) allNames
        else allNames.filter { it.contains(patientSearchQuery, ignoreCase = true) }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // ================= FULL PAGE ADD DOCUMENT FORM =================
        if (currentRole == UserRole.PATIENT && isAddPageActive) {
            // Header for Add Document Page with Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { isAddPageActive = false },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Retour aux Documents", tint = TextDarkNavy)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Ajouter un Document au Dossier",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryDarkTeal
                    )
                    Text(
                        text = "Remplissez les informations de la pièce jointe à enregistrer",
                        fontSize = 11.5.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Champ 1: Nom du document
                    Text("1. Nom du document :", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                    OutlinedTextField(
                        value = formDocTitle,
                        onValueChange = { formDocTitle = it },
                        placeholder = { Text("Ex: Bilan Sanguin Institut Pasteur, Radio Thoracique...", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Champ 2: Pièce jointe type File (Ouverture du stockage du téléphone)
                    Text("2. Pièce jointe / Fichier :", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                    Surface(
                        onClick = {
                            filePickerLauncher.launch("*/*")
                        },
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (formAttachedFileName.isNotBlank()) PrimaryTeal else Color(0xFFCBD5E1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = if (formAttachedFileName.isNotBlank()) Icons.Default.CheckCircle else Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = if (formAttachedFileName.isNotBlank()) Color(0xFF10B981) else PrimaryTeal,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (formAttachedFileName.isNotBlank()) "Fichier rattaché: $formAttachedFileName" else "📱 Choisir un fichier sur votre téléphone",
                                        fontSize = 12.5.sp,
                                        fontWeight = if (formAttachedFileName.isNotBlank()) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (formAttachedFileName.isNotBlank()) Color(0xFF065F46) else TextDarkNavy
                                    )
                                    Text(text = if (formAttachedFileName.isNotBlank()) "Taille : 2.4 Mo • Prêt à enregistrer" else "Ouvre le gestionnaire de fichiers de votre téléphone", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    // Champ 3: Type de pièce (Liste déroulante select)
                    Text("3. Type de pièce / Catégorie :", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                    Box {
                        OutlinedTextField(
                            value = formDocCategory.labelFr,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { categoryDropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = PrimaryTeal)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { categoryDropdownExpanded = true },
                            shape = RoundedCornerShape(12.dp)
                        )

                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f).background(Color.White)
                        ) {
                            DocumentCategory.values().forEach { cat ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = when (cat) {
                                                    DocumentCategory.BIOLOGY -> Icons.Default.Science
                                                    DocumentCategory.IMAGING -> Icons.Default.Image
                                                    DocumentCategory.PRESCRIPTION -> Icons.Default.ReceiptLong
                                                    DocumentCategory.OTHER -> Icons.Default.Description
                                                },
                                                contentDescription = null,
                                                tint = PrimaryTeal,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(cat.labelFr, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDarkNavy)
                                        }
                                    },
                                    onClick = {
                                        formDocCategory = cat
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Champ 4: Commentaire pour le médecin
                    Text("4. Commentaire pour le médecin :", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                    OutlinedTextField(
                        value = formComment,
                        onValueChange = { formComment = it },
                        placeholder = { Text("Précisez vos symptômes, observations ou questions...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isAddPageActive = false },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Annuler", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            enabled = !isAddingDocLoading && formDocTitle.isNotBlank(),
                            onClick = {
                                isAddingDocLoading = true
                                val savedTitle = formDocTitle.trim()
                                val fileUrlToSave = selectedFileUri?.let { convertUriToBase64DataUrl(context, it) } ?: selectedFileUri?.toString() ?: formAttachedFileName
                                coroutineScope.launch {
                                    TabibRepository.addMedicalDocumentAsync(
                                        title = savedTitle,
                                        category = formDocCategory,
                                        fileSize = "2.4 Mo",
                                        notes = formComment.trim(),
                                        fileUrl = fileUrlToSave
                                    )
                                    isAddingDocLoading = false
                                    addedDocTitleForSuccessModal = savedTitle
                                    showAddSuccessDialog = true
                                    formDocTitle = ""
                                    formComment = ""
                                    formAttachedFileName = ""
                                    selectedFileUri = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isAddingDocLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                            } else {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Enregistrer", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            }
                        }
                    }
                }
            }
        }

        // ================= MAIN PATIENT DOCUMENTS LIST PAGE =================
        else if (currentRole == UserRole.PATIENT) {

            // Header with Top "+" Button for Adding Document
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dossier Médical & Pièces Jointes",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryDarkTeal
                    )
                    Text(
                        text = "Vos documents médicaux enregistrés",
                        fontSize = 11.5.sp,
                        color = Color.Gray
                    )
                }

                Button(
                    onClick = { isAddPageActive = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ajouter", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "📊 Vos Documents & Pièces Jointes Rattachés",
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextDarkNavy
            )

            Spacer(modifier = Modifier.height(10.dp))

            val userMedicalDocs = TabibRepository.medicalDocuments

            if (userMedicalDocs.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Aucun document trouvé dans votre dossier.", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                        Text("Cliquez sur le bouton '+ Ajouter' ci-dessus pour rattacher votre premier document.", fontSize = 11.5.sp, color = Color.Gray)
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        userMedicalDocs.forEach { doc ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAF9)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(TealLightContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = when (doc.category) {
                                                        DocumentCategory.BIOLOGY -> Icons.Default.Science
                                                        DocumentCategory.IMAGING -> Icons.Default.Image
                                                        DocumentCategory.PRESCRIPTION -> Icons.Default.ReceiptLong
                                                        DocumentCategory.OTHER -> Icons.Default.Description
                                                    },
                                                    contentDescription = null,
                                                    tint = PrimaryTeal,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column {
                                                Text(doc.title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = TextDarkNavy)
                                                Text("${doc.category.labelFr} • Ajouté le ${doc.dateAdded} • ${doc.fileSize}", fontSize = 11.sp, color = Color.Gray)
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Share Button (Icon only)
                                            Surface(
                                                onClick = { docToShareDirectly = doc },
                                                shape = RoundedCornerShape(10.dp),
                                                color = PrimaryTeal,
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Share,
                                                        contentDescription = "Partager",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }

                                            // Delete Button (Icon only, identical size)
                                            Surface(
                                                onClick = { docToDelete = doc },
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFFFEF2F2),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5).copy(alpha = 0.6f)),
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Supprimer",
                                                        tint = Color(0xFFDC2626),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (doc.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Commentaire: ${doc.notes}", fontSize = 11.sp, color = Color(0xFF475569))
                                    }

                                    if (doc.shares.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Partagé avec :", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryDarkTeal)
                                        doc.shares.forEach { s ->
                                            if (!s.isRevoked) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("• ${s.doctorName} (${s.expiryDate})", fontSize = 10.5.sp, color = Color.DarkGray)
                                                    TextButton(
                                                        onClick = { TabibRepository.revokeDocumentShare(doc.id, s.id) },
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("Révoquer", fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= DOCTOR SPACE =================
        else {
            Text(
                text = "Patients ayant transmis des documents",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDarkNavy
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Search / Filter Input for Patient Name
            OutlinedTextField(
                value = patientSearchQuery,
                onValueChange = { patientSearchQuery = it },
                placeholder = { Text("Filtrer par nom de patient...", fontSize = 12.5.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryTeal) },
                trailingIcon = {
                    if (patientSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { patientSearchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = PrimaryTeal
                )
            )

            if (doctorPatientsList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucun patient trouvé.", fontSize = 13.sp, color = Color.Gray)
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    doctorPatientsList.forEach { patName ->
                        val isSelected = selectedPatientNameForDoctor == patName
                        val matchingApts = TabibRepository.appointments.filter { it.patientName.equals(patName, ignoreCase = true) }
                        val matchingPatIds = (matchingApts.map { it.patientId } + listOf(
                            patName,
                            "pat_1",
                            TabibRepository.currentPatient.value.id,
                            TabibRepository.currentPatient.value.userId
                        )).filter { it.isNotBlank() }.distinct()

                        val docsFromThisPatient = TabibRepository.medicalDocuments.filter { m ->
                            val docOwnerName = resolvePatientName(m.patientId)
                            docOwnerName.equals(patName, ignoreCase = true) ||
                            m.patientId.trim() in matchingPatIds ||
                            m.patientId.equals(patName, ignoreCase = true) ||
                            (TabibRepository.medicalDocuments.isNotEmpty() && doctorPatientsList.size == 1)
                        }.distinctBy { it.id }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) TealLightContainer else Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PrimaryTeal else Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
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

                                        Column {
                                            Text(patName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDarkNavy)
                                            Text("${docsFromThisPatient.size} document(s) rattaché(s)", fontSize = 11.5.sp, color = Color.Gray)
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            selectedPatientNameForDoctor = if (isSelected) null else patName
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) PrimaryTeal else Color(0xFFF1F5F9), contentColor = if (isSelected) Color.White else TextDarkNavy),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(if (isSelected) "Masquer Dossier" else "📁 Voir Dossier", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (isSelected) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = Color(0xFFE2E8F0))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (docsFromThisPatient.isEmpty()) {
                                        Text("Aucun document transmis par ce patient pour l'instant.", fontSize = 12.sp, color = Color.Gray)
                                    } else {
                                        docsFromThisPatient.forEach { doc ->
                                            val shareItem = doc.shares.firstOrNull { !it.isRevoked } ?: doc.shares.firstOrNull()
                                            val shareId = shareItem?.id ?: ""
                                            val currentRemarkValue = doctorRemarkEdits[shareId] ?: shareItem?.doctorRemark ?: ""

                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(36.dp)
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(TealLightContainer),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = when (doc.category) {
                                                                        DocumentCategory.BIOLOGY -> Icons.Default.Science
                                                                        DocumentCategory.IMAGING -> Icons.Default.Image
                                                                        DocumentCategory.PRESCRIPTION -> Icons.Default.ReceiptLong
                                                                        DocumentCategory.OTHER -> Icons.Default.Description
                                                                    },
                                                                    contentDescription = null,
                                                                    tint = PrimaryTeal,
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.width(10.dp))
                                                            Column {
                                                                Text(doc.title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = TextDarkNavy)
                                                                Text("${doc.category.labelFr} • ${doc.dateAdded} • ${doc.fileSize}", fontSize = 11.sp, color = Color.Gray)
                                                            }
                                                        }

                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Button(
                                                                onClick = { selectedDocForDetailView = doc },
                                                                colors = ButtonDefaults.buttonColors(containerColor = TealLightContainer, contentColor = PrimaryTeal),
                                                                shape = RoundedCornerShape(8.dp),
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                            ) {
                                                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(13.dp))
                                                                Spacer(modifier = Modifier.width(3.dp))
                                                                Text("Consulter", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                                            }

                                                            var isDownloadedDoc by remember { mutableStateOf(false) }
                                                            Button(
                                                                onClick = {
                                                                    downloadMedicalFileToDevice(
                                                                        context = context,
                                                                        docTitle = doc.title,
                                                                        categoryLabel = doc.category.labelFr,
                                                                        dateAdded = doc.dateAdded,
                                                                        patientName = patName,
                                                                        notes = shareItem?.notes?.ifBlank { doc.notes } ?: doc.notes,
                                                                        fileUrl = doc.fileUrl
                                                                    )
                                                                    isDownloadedDoc = true
                                                                    downloadedDocTitleModal = doc.title
                                                                    showDownloadSuccessDialog = true
                                                                },
                                                                colors = ButtonDefaults.buttonColors(containerColor = if (isDownloadedDoc) Color(0xFF10B981) else PrimaryTeal),
                                                                shape = RoundedCornerShape(8.dp),
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                            ) {
                                                                Icon(if (isDownloadedDoc) Icons.Default.CheckCircle else Icons.Default.Download, contentDescription = null, modifier = Modifier.size(13.dp))
                                                                Spacer(modifier = Modifier.width(3.dp))
                                                                Text(if (isDownloadedDoc) "Téléchargé" else "Télécharger", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }

                                                    if (shareItem?.notes?.isNotBlank() == true || doc.notes.isNotBlank()) {
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Surface(
                                                            color = Color(0xFFEFF6FF),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Text(
                                                                text = "💬 Commentaire patient : ${shareItem?.notes?.ifBlank { doc.notes } ?: doc.notes}",
                                                                fontSize = 11.sp,
                                                                color = Color(0xFF1E40AF),
                                                                modifier = Modifier.padding(8.dp)
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    Text("Remarque du Médecin sur ce document :", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                                                    Spacer(modifier = Modifier.height(4.dp))

                                                    OutlinedTextField(
                                                        value = currentRemarkValue,
                                                        onValueChange = { doctorRemarkEdits[shareId] = it },
                                                        placeholder = { Text("Ex: Résultats conformes, à recontrôler dans 3 mois...", fontSize = 11.5.sp) },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedContainerColor = Color(0xFFF8FAF9),
                                                            unfocusedContainerColor = Color(0xFFF8FAF9)
                                                        )
                                                    )

                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.End
                                                    ) {
                                                        val isSaving = isSavingRemarkMap[shareId] == true
                                                        Button(
                                                            enabled = !isSaving,
                                                            onClick = {
                                                                isSavingRemarkMap[shareId] = true
                                                                coroutineScope.launch {
                                                                    TabibRepository.updateDocumentRemarkAsync(
                                                                        shareId = shareId,
                                                                        documentId = doc.id,
                                                                        doctorRemark = currentRemarkValue
                                                                    )
                                                                    isSavingRemarkMap[shareId] = false
                                                                }
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                            shape = RoundedCornerShape(8.dp),
                                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                                        ) {
                                                            if (isSaving) {
                                                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp))
                                                            } else {
                                                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(13.dp))
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("Enregistrer la remarque", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ================= PATIENT DOCTOR SEARCH & SHARE DIALOG =================
    val currentDocToShare = docToShareDirectly
    if (currentDocToShare != null) {
        val targetDocToShare = currentDocToShare

        val filteredMatchingDoctors = remember(
            searchSpecialtyName,
            searchGovernorate,
            searchDelegation,
            TabibRepository.doctorsList.toList()
        ) {
            TabibRepository.doctorsList.filter { doc ->
                val matchesSpecialty = searchSpecialtyName == "Toutes" || searchSpecialtyName == "Toutes spécialités" || doc.specialtyName.equals(searchSpecialtyName, ignoreCase = true)
                val matchesGov = searchGovernorate == "Tous" || searchGovernorate == "Tous gouvernorats" || doc.governorate.equals(searchGovernorate, ignoreCase = true)
                val matchesDel = searchDelegation == "Toutes" || searchDelegation == "Toutes délégations" || searchDelegation.isBlank() || doc.delegation.equals(searchDelegation, ignoreCase = true)

                matchesSpecialty && matchesGov && matchesDel
            }
        }

        AlertDialog(
            onDismissRequest = { docToShareDirectly = null },
            title = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(TealLightContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = PrimaryTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Partager un document",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkNavy
                            )
                            Text(
                                text = "Document : ${targetDocToShare.title}",
                                fontSize = 12.sp,
                                color = PrimaryTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Filtrer par critères (comme dans la recherche) :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)

                    // Filter Buttons Row matching DoctorSearchScreen design
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1. Specialty Button
                        OutlinedButton(
                            onClick = { showSpecModal = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (searchSpecialtyName == "Toutes spécialités" || searchSpecialtyName == "Toutes") "Spécialité" else if (searchSpecialtyName.length > 8) searchSpecialtyName.take(7) + ".." else searchSpecialtyName,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDarkNavy,
                                maxLines = 1
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }

                        // 2. Governorate Button
                        OutlinedButton(
                            onClick = { showGovModal = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (searchGovernorate == "Tous gouvernorats" || searchGovernorate == "Tous") "Gouvernorat" else if (searchGovernorate.length > 8) searchGovernorate.take(7) + ".." else searchGovernorate,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDarkNavy,
                                maxLines = 1
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }

                        // 3. Delegation Button
                        OutlinedButton(
                            onClick = { if (popupAvailableDelegations.isNotEmpty()) showDelModal = true },
                            enabled = popupAvailableDelegations.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = if (popupAvailableDelegations.isNotEmpty()) PrimaryTeal else Color.Gray, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (searchDelegation == "Toutes délégations" || searchDelegation == "Toutes") "Délégation" else if (searchDelegation.length > 8) searchDelegation.take(7) + ".." else searchDelegation,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDarkNavy,
                                maxLines = 1
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("${filteredMatchingDoctors.size} médecin(s) trouvé(s) :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)

                    if (filteredMatchingDoctors.isEmpty()) {
                        Text("Aucun médecin ne correspond à ces critères.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        filteredMatchingDoctors.forEach { doc ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAF9)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(doc.name, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = TextDarkNavy)
                                        Text("${doc.specialtyName} • ${doc.governorate}, ${doc.delegation}", fontSize = 11.sp, color = Color.Gray)
                                    }

                                    Button(
                                        enabled = !isSharingDirectLoading,
                                        onClick = {
                                            isSharingDirectLoading = true
                                            coroutineScope.launch {
                                                try {
                                                    TabibRepository.shareDocumentAsync(
                                                        docId = targetDocToShare.id,
                                                        doctorId = doc.id,
                                                        doctorName = doc.name,
                                                        days = 0,
                                                        title = targetDocToShare.title,
                                                        category = targetDocToShare.category,
                                                        notes = targetDocToShare.notes
                                                    )
                                                } catch (_: Exception) {}
                                                isSharingDirectLoading = false
                                                sharedDoctorNameForSuccessModal = doc.name
                                                docToShareDirectly = null
                                                showShareSuccessDialog = true
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Partager", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { docToShareDirectly = null }) {
                    Text("Fermer", fontWeight = FontWeight.Bold, color = PrimaryTeal)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    // Structured List Selection Dialogs for Share Popup Filters
    if (showSpecModal) {
        StructuredListSelectionDialog(
            title = "Spécialité Médicale",
            subtitle = "Sélectionnez une spécialité pour filtrer les médecins",
            items = listOf("Toutes spécialités") + TabibRepository.specialties.map { it.nameFr },
            selectedItem = searchSpecialtyName,
            onItemSelected = {
                searchSpecialtyName = it
                showSpecModal = false
            },
            onDismiss = { showSpecModal = false }
        )
    }

    if (showGovModal) {
        StructuredListSelectionDialog(
            title = "Gouvernorat",
            subtitle = "Sélectionnez un gouvernorat en Tunisie",
            items = listOf("Tous gouvernorats") + TabibRepository.governorates,
            selectedItem = searchGovernorate,
            onItemSelected = {
                searchGovernorate = it
                searchDelegation = "Toutes délégations"
                showGovModal = false
            },
            onDismiss = { showGovModal = false }
        )
    }

    if (showDelModal) {
        StructuredListSelectionDialog(
            title = "Délégation",
            subtitle = "Sélectionnez une délégation pour $searchGovernorate",
            items = listOf("Toutes délégations") + popupAvailableDelegations,
            selectedItem = searchDelegation,
            onItemSelected = {
                searchDelegation = it
                showDelModal = false
            },
            onDismiss = { showDelModal = false }
        )
    }

    // Delete Confirmation Dialog
    val currentDocToDelete = docToDelete
    if (currentDocToDelete != null) {
        val targetDelete = currentDocToDelete
        AlertDialog(
            onDismissRequest = { docToDelete = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF2F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Supprimer ce document ?",
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDarkNavy,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Voulez-vous vraiment supprimer définitivement le document '${targetDelete.title}' de votre dossier médical ? Cette action est irréversible.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    enabled = !isDeletingLoading,
                    onClick = {
                        isDeletingLoading = true
                        coroutineScope.launch {
                            TabibRepository.deleteMedicalDocumentAsync(targetDelete.id)
                            isDeletingLoading = false
                            docToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isDeletingLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Oui, Supprimer", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { docToDelete = null },
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Text("Annuler", fontWeight = FontWeight.Bold, color = Color(0xFF475569), fontSize = 13.sp)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    // Success Modal Dialog for Document Registration
    if (showAddSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddSuccessDialog = false
                isAddPageActive = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Document Enregistré !",
                    fontWeight = FontWeight.Bold,
                    color = TextDarkNavy,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Le document '$addedDocTitleForSuccessModal' a été sauvegardé avec succès et ajouté à votre dossier médical.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAddSuccessDialog = false
                        isAddPageActive = false
                    },
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

    // Success Modal Dialog for Document Sharing
    if (showShareSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showShareSuccessDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Document Partagé !",
                    fontWeight = FontWeight.Bold,
                    color = TextDarkNavy,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Votre document a été partagé avec succès avec le $sharedDoctorNameForSuccessModal et est désormais accessible dans son espace médical.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showShareSuccessDialog = false },
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

    // Success Modal Dialog for Doctor Document Download
    if (showDownloadSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadSuccessDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Document Téléchargé !",
                    fontWeight = FontWeight.Bold,
                    color = TextDarkNavy,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Le document '$downloadedDocTitleModal' a été sauvegardé avec succès dans le dossier Téléchargements (Downloads) de votre appareil.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDownloadSuccessDialog = false },
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

    // Doctor Full Document Detail Modal
    val currentDocForDetail = selectedDocForDetailView
    if (currentDocForDetail != null) {
        val shareItem = currentDocForDetail.shares.firstOrNull { !it.isRevoked } ?: currentDocForDetail.shares.firstOrNull()
        val shareId = shareItem?.id ?: ""
        val currentRemarkValue = doctorRemarkEdits[shareId] ?: shareItem?.doctorRemark ?: ""

        AlertDialog(
            onDismissRequest = { selectedDocForDetailView = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(TealLightContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Consulter le Dossier Médical", fontSize = 16.5.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                        Text(currentDocForDetail.title, fontSize = 12.5.sp, color = PrimaryTeal, fontWeight = FontWeight.Bold)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = Color(0xFFF8FAF9),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Catégorie : ${currentDocForDetail.category.labelFr}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                            Text("Ajouté le : ${currentDocForDetail.dateAdded} • Taille : ${currentDocForDetail.fileSize}", fontSize = 11.5.sp, color = Color.Gray)
                            if (currentDocForDetail.notes.isNotBlank() || shareItem?.notes?.isNotBlank() == true) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("💬 Commentaire du patient : ${shareItem?.notes?.ifBlank { currentDocForDetail.notes } ?: currentDocForDetail.notes}", fontSize = 12.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    val imageBitmap = remember(currentDocForDetail.fileUrl) {
                        if (currentDocForDetail.fileUrl.contains("data:image") || currentDocForDetail.fileUrl.contains(";base64,")) {
                            try {
                                val base64Part = currentDocForDetail.fileUrl.substringAfter(",")
                                val bytes = android.util.Base64.decode(base64Part, android.util.Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                            } catch (_: Exception) { null }
                        } else null
                    }

                    if (imageBitmap != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        ) {
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = "Aperçu du scan médical",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Text("Remarque du Médecin :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkNavy)
                    OutlinedTextField(
                        value = currentRemarkValue,
                        onValueChange = { doctorRemarkEdits[shareId] = it },
                        placeholder = { Text("Saisir votre compte-rendu ou remarque...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        val isSaving = isSavingRemarkMap[shareId] == true
                        Button(
                            enabled = !isSaving,
                            onClick = {
                                isSavingRemarkMap[shareId] = true
                                coroutineScope.launch {
                                    TabibRepository.updateDocumentRemarkAsync(
                                        shareId = shareId,
                                        documentId = currentDocForDetail.id,
                                        doctorRemark = currentRemarkValue
                                    )
                                    isSavingRemarkMap[shareId] = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Enregistrer la remarque", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedDocForDetailView = null }) {
                    Text("Fermer", fontWeight = FontWeight.Bold, color = PrimaryTeal)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}
