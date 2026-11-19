package com.example.tabibconnect.ui.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tabibconnect.data.model.ConsultationRecord
import com.example.tabibconnect.data.repository.TabibRepository
import com.example.tabibconnect.ui.components.ConsultationResultPdfDialog
import com.example.tabibconnect.ui.theme.PrimaryDarkTeal
import com.example.tabibconnect.ui.theme.PrimaryTeal
import com.example.tabibconnect.ui.theme.TealPrimary

@Composable
fun ConsultationHistoryScreen() {
    var selectedRecordForPdf by remember { mutableStateOf<ConsultationRecord?>(null) }

    LaunchedEffect(Unit) {
        TabibRepository.fetchConsultationRecordsAsync()
    }

    val records = TabibRepository.consultationRecords

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9))
            .padding(16.dp)
    ) {
        Text(
            text = "Historique des Consultations",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryDarkTeal
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.HistoryEdu,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Aucune consultation enregistrée.", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(records) { rec ->
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
                                Text(
                                    text = rec.date,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryTeal
                                )
                                Surface(
                                    color = Color(0xFFE6F4EA),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Consultation Finalisée 🟢",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF137333),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = rec.doctorName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = PrimaryDarkTeal
                            )

                            Text(
                                text = rec.specialtyName,
                                fontSize = 12.5.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Description / Diagnostic : ${rec.diagnosis}",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.DarkGray
                            )

                            if (rec.restDays > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "🔴 Repos prescrit : ${rec.restDays} jour(s)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC5221F)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { selectedRecordForPdf = rec },
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Consulter & Exporter PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // PDF Consultation Result Modal Dialog
    if (selectedRecordForPdf != null) {
        ConsultationResultPdfDialog(
            record = selectedRecordForPdf!!,
            onDismiss = { selectedRecordForPdf = null }
        )
    }
}
