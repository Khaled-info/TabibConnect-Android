package com.example.tabibconnect.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tabibconnect.data.model.ChatMessage
import com.example.tabibconnect.data.model.UserRole
import com.example.tabibconnect.data.repository.TabibRepository
import com.example.tabibconnect.ui.components.InitialsAvatar
import com.example.tabibconnect.ui.theme.TealDark
import com.example.tabibconnect.ui.theme.TealPrimary
import com.example.tabibconnect.ui.theme.TextDarkNavy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ContactConversationItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val initials: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int,
    val isOnline: Boolean = true,
    val isDoctor: Boolean = true
)

// Robust matching helper to associate messages with partner
fun isMessageForPartner(
    msg: ChatMessage,
    partnerId: String,
    partnerName: String,
    partnerUserId: String = ""
): Boolean {
    val pId = partnerId.trim().lowercase()
    val pUserId = partnerUserId.trim().lowercase()
    val pNameClean = partnerName.lowercase().replace("dr.", "").replace("dr", "").trim()

    val sId = msg.senderId.trim().lowercase()
    val rId = msg.receiverId.trim().lowercase()

    val sNameClean = msg.senderName.lowercase().replace("dr.", "").replace("dr", "").trim()
    val rNameClean = msg.receiverName.lowercase().replace("dr.", "").replace("dr", "").trim()

    // 1. Direct ID match (sender or receiver)
    if (pId.isNotBlank() && (sId == pId || rId == pId)) return true
    if (pUserId.isNotBlank() && (sId == pUserId || rId == pUserId)) return true

    // 2. Name match (sender or receiver)
    if (pNameClean.length >= 3) {
        if (sNameClean.isNotBlank() && (sNameClean.contains(pNameClean) || pNameClean.contains(sNameClean))) return true
        if (rNameClean.isNotBlank() && (rNameClean.contains(pNameClean) || pNameClean.contains(rNameClean))) return true
    }

    return (pId.isBlank() && pNameClean.isBlank()) || msg.isDoctor || sId.isNotBlank()
}

@Composable
fun ChatScreen(
    initialPartnerId: String? = null,
    onBackClick: (() -> Unit)? = null
) {
    val currentRole = TabibRepository.currentUserRole.value
    val currentPatient = TabibRepository.currentPatient.value
    val doctorsList = TabibRepository.doctorsList
    val appointments = TabibRepository.appointments
    val allMessages = TabibRepository.chatMessages

    var selectedPartnerId by remember { mutableStateOf(initialPartnerId) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch initial data strictly from BDD on launch
    LaunchedEffect(Unit) {
        TabibRepository.fetchAppointmentsAsync()
        TabibRepository.fetchDoctorsAsync()
        TabibRepository.fetchChatMessagesAsync()
    }

    // Current User Identity
    val currentUserId = if (currentRole == UserRole.DOCTOR) "doc_1" else currentPatient.id.ifBlank { "pat_1" }
    val currentUserName = if (currentRole == UserRole.DOCTOR) {
        "Dr. Mohamed Ben Salah"
    } else {
        currentPatient.name.ifBlank { "Amine Ben Ammor" }
    }

    // Real-time BDD Synchronization Loop
    LaunchedEffect(selectedPartnerId, currentUserId) {
        while (true) {
            val partnerToFetch = selectedPartnerId ?: ""
            TabibRepository.fetchChatMessagesAsync(userId = currentUserId, partnerId = partnerToFetch)
            delay(3000)
        }
    }

    // Build contacts conversation list strictly from REAL database items
    val conversationItems = remember(
        doctorsList.toList(),
        appointments.toList(),
        allMessages.toList(),
        currentRole,
        currentPatient
    ) {
        val contacts = mutableListOf<ContactConversationItem>()

        if (currentRole == UserRole.PATIENT) {
            // Patient view: Real doctors loaded from SQL Server BDD
            doctorsList.forEach { doc ->
                val msgs = allMessages.filter { msg ->
                    isMessageForPartner(msg, partnerId = doc.id, partnerName = doc.name, partnerUserId = doc.userId)
                }

                val lastMsg = msgs.lastOrNull()
                val unread = msgs.count { msg ->
                    !msg.isRead && (
                        msg.senderId.trim().equals(doc.id.trim(), ignoreCase = true) ||
                        msg.senderId.trim().equals(doc.userId.trim(), ignoreCase = true) ||
                        (doc.name.isNotBlank() && msg.senderName.lowercase().contains(doc.name.lowercase().replace("dr.", "").trim()))
                    )
                }

                contacts.add(
                    ContactConversationItem(
                        id = doc.id,
                        name = doc.name,
                        subtitle = doc.specialtyName,
                        initials = "DR",
                        lastMessage = lastMsg?.text ?: "Commencer la discussion",
                        timestamp = lastMsg?.timestamp ?: "",
                        unreadCount = unread,
                        isOnline = true,
                        isDoctor = true
                    )
                )
            }
        } else {
            // Doctor view: Real patients loaded from SQL Server BDD appointments & chat messages
            val patientList = mutableListOf<Pair<String, String>>()
            if (currentPatient.name.isNotBlank()) {
                patientList.add(Pair(currentPatient.id.ifBlank { "pat_1" }, currentPatient.name))
            }

            appointments.forEach { apt ->
                val pid = apt.patientId.ifBlank { "pat_1" }
                val pname = apt.patientName.ifBlank { "Patient" }
                if (patientList.none { it.first == pid }) {
                    patientList.add(Pair(pid, pname))
                }
            }

            allMessages.forEach { msg ->
                if (!msg.isDoctor && msg.senderId.isNotBlank() && msg.senderName.isNotBlank()) {
                    if (patientList.none { it.first == msg.senderId }) {
                        patientList.add(Pair(msg.senderId, msg.senderName))
                    }
                }
            }

            patientList.distinctBy { it.first }.forEach { (pid, pname) ->
                val msgs = allMessages.filter { msg ->
                    isMessageForPartner(msg, partnerId = pid, partnerName = pname)
                }

                val lastMsg = msgs.lastOrNull()
                val unread = msgs.count { msg ->
                    !msg.isRead && (
                        msg.senderId.trim().equals(pid.trim(), ignoreCase = true) ||
                        (pname.isNotBlank() && msg.senderName.lowercase().contains(pname.lowercase().trim()))
                    )
                }

                contacts.add(
                    ContactConversationItem(
                        id = pid,
                        name = pname,
                        subtitle = "Patient • Suivi médical",
                        initials = pname.take(2).uppercase(),
                        lastMessage = lastMsg?.text ?: "Commencer la discussion",
                        timestamp = lastMsg?.timestamp ?: "",
                        unreadCount = unread,
                        isOnline = true,
                        isDoctor = false
                    )
                )
            }
        }

        // Sort conversations: unread first, then by timestamp/id
        contacts.sortedWith(
            compareByDescending<ContactConversationItem> { it.unreadCount }
                .thenByDescending { it.timestamp }
        )
    }

    if (selectedPartnerId == null) {
        // 1. MESSENGER CONVERSATIONS LIST VIEW
        MessengerConversationsView(
            conversations = conversationItems,
            onConversationClick = { partnerId ->
                selectedPartnerId = partnerId
                coroutineScope.launch {
                    TabibRepository.markChatReadAsync(currentUserId, partnerId)
                }
            },
            onBackClick = onBackClick
        )
    } else {
        // 2. INDIVIDUAL DISCUSSION DETAIL VIEW
        val partnerId = selectedPartnerId!!
        val partnerItem = conversationItems.firstOrNull { it.id == partnerId }

        val partnerName = partnerItem?.name ?: if (currentRole == UserRole.PATIENT) "Médecin" else "Patient"
        val partnerSubTitle = partnerItem?.subtitle ?: if (currentRole == UserRole.PATIENT) "Spécialiste" else "Patient"
        val partnerInitials = partnerItem?.initials ?: partnerName.take(2).uppercase()

        // Automatically mark conversation as read when viewing discussion
        LaunchedEffect(partnerId) {
            TabibRepository.markChatReadAsync(currentUserId, partnerId)
        }

        MessengerChatDiscussionView(
            partnerId = partnerId,
            partnerName = partnerName,
            partnerSubtitle = partnerSubTitle,
            partnerInitials = partnerInitials,
            currentUserId = currentUserId,
            currentUserName = currentUserName,
            currentRole = currentRole,
            onBackToConversations = {
                selectedPartnerId = null
            }
        )
    }
}

@Composable
fun MessengerConversationsView(
    conversations: List<ContactConversationItem>,
    onConversationClick: (String) -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "UNREAD"

    val totalUnread = remember(conversations) {
        conversations.sumOf { it.unreadCount }
    }

    val filteredConversations = remember(searchQuery, selectedFilter, conversations) {
        conversations.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.subtitle.contains(searchQuery, ignoreCase = true) ||
                    item.lastMessage.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "UNREAD" -> item.unreadCount > 0
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // MESSENGER TOP BAR
        Surface(
            color = Color.White,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onBackClick != null) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Retour",
                                    tint = TextDarkNavy
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Text(
                            text = "Discussions",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDarkNavy
                        )

                        if (totalUnread > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFFEF4444),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "$totalUnread",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        color = com.example.tabibconnect.ui.theme.TealLightContainer,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SEARCH BAR
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Rechercher dans Messenger...", fontSize = 13.5.sp, color = Color(0xFF94A3B8)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Recherche",
                            tint = TealPrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Effacer",
                                    tint = Color.Gray
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF1F5F9),
                        unfocusedContainerColor = Color(0xFFF1F5F9)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // FILTER CHIPS ROW
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("Toutes les conversations (${conversations.size})", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TealPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFF1F5F9),
                            labelColor = TextDarkNavy
                        )
                    )

                    val unreadTotalCount = conversations.count { it.unreadCount > 0 }
                    FilterChip(
                        selected = selectedFilter == "UNREAD",
                        onClick = { selectedFilter = "UNREAD" },
                        label = {
                            Text(
                                text = "Non lus ${if (unreadTotalCount > 0) "($unreadTotalCount) 🔴" else ""}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TealPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFF1F5F9),
                            labelColor = TextDarkNavy
                        )
                    )
                }
            }
        }

        // STORIES / ONLINE USERS HORIZONTAL BAR
        if (conversations.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = "EN LIGNE MAINTENANT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(conversations) { contact ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(60.dp)
                                .clickable { onConversationClick(contact.id) }
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                InitialsAvatar(
                                    initials = contact.initials,
                                    backgroundColor = if (contact.isDoctor) TealPrimary else Color(0xFF3B82F6),
                                    size = 48.dp,
                                    fontSize = 15.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .size(13.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                        .border(2.5.dp, Color.White, CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = contact.name.replace("Dr. ", ""),
                                fontSize = 11.sp,
                                fontWeight = if (contact.unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
                                color = TextDarkNavy,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

        // CONVERSATIONS LIST
        if (filteredConversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = null,
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (selectedFilter == "UNREAD") "Aucune conversation non lue." else "Aucune conversation enregistrée dans la BDD.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                items(filteredConversations) { item ->
                    ConversationRowCard(
                        item = item,
                        onClick = { onConversationClick(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationRowCard(
    item: ContactConversationItem,
    onClick: () -> Unit
) {
    val isUnread = item.unreadCount > 0

    Surface(
        color = if (isUnread) Color(0xFFF0FDF4) else Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AVATAR WITH ONLINE STATUS & UNREAD INDICATOR DOT
                Box {
                    InitialsAvatar(
                        initials = item.initials,
                        backgroundColor = if (item.isDoctor) TealPrimary else Color(0xFF3B82F6),
                        size = 52.dp,
                        fontSize = 16.sp
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                            .border(2.5.dp, if (isUnread) Color(0xFFF0FDF4) else Color.White, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // CENTER CONTENT: NAME + SUBTITLE + LAST MESSAGE PREVIEW
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.name,
                            // EN GRAS SI NON LU
                            fontWeight = if (isUnread) FontWeight.ExtraBold else FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextDarkNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (item.timestamp.isNotBlank()) {
                            Text(
                                text = item.timestamp,
                                fontSize = 11.5.sp,
                                // EN GRAS ET TEAL SI NON LU
                                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                                color = if (isUnread) TealPrimary else Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.subtitle,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LAST MESSAGE SNIPPET: EN GRAS SI NON LU
                        Text(
                            text = item.lastMessage,
                            fontSize = 13.sp,
                            fontWeight = if (isUnread) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isUnread) TextDarkNavy else Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // UNREAD BADGE COUNTER PILL
                        if (isUnread) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = TealPrimary,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "${item.unreadCount}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(start = 82.dp),
                color = Color(0xFFF1F5F9),
                thickness = 1.dp
            )
        }
    }
}

@Composable
fun MessengerChatDiscussionView(
    partnerId: String,
    partnerName: String,
    partnerSubtitle: String,
    partnerInitials: String,
    currentUserId: String,
    currentUserName: String,
    currentRole: UserRole,
    onBackToConversations: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var showQuickTemplates by remember { mutableStateOf(false) }
    var isSendingMessage by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val allMessages = TabibRepository.chatMessages

    val sendMessageAction = {
        if (messageText.isNotBlank() && !isSendingMessage) {
            val textToSend = messageText.trim()
            messageText = ""
            isSendingMessage = true
            coroutineScope.launch {
                TabibRepository.sendChatMessageAsync(
                    senderId = currentUserId,
                    senderName = currentUserName,
                    receiverId = partnerId,
                    receiverName = partnerName,
                    isDoctor = (currentRole == UserRole.DOCTOR),
                    text = textToSend
                )
                isSendingMessage = false
            }
        }
    }

    // Retain ALL messages for this discussion context without eliminating local ones
    val conversationMessages = remember(allMessages.toList(), partnerId, partnerName, currentUserId) {
        val filtered = allMessages.filter { msg ->
            isMessageForPartner(msg, partnerId = partnerId, partnerName = partnerName)
        }
        if (filtered.isNotEmpty()) filtered else allMessages.toList()
    }

    // Auto-scroll to bottom on new message
    LaunchedEffect(conversationMessages.size) {
        if (conversationMessages.isNotEmpty()) {
            listState.animateScrollToItem(conversationMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // DISCUSSION TOP HEADER WITH BACK BUTTON
        Surface(
            color = Color.White,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // BACK BUTTON TO RETURN TO CONVERSATIONS LIST
                IconButton(onClick = onBackToConversations) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour aux conversations",
                        tint = TextDarkNavy
                    )
                }

                // AVATAR WITH ONLINE BADGE
                Box {
                    InitialsAvatar(
                        initials = partnerInitials,
                        backgroundColor = if (currentRole == UserRole.PATIENT) TealPrimary else Color(0xFF3B82F6),
                        size = 42.dp,
                        fontSize = 15.sp
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                            .border(2.dp, Color.White, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // PARTNER DETAILS
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = partnerName,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = TextDarkNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "🟢 En ligne • $partnerSubtitle",
                        fontSize = 11.5.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // CHAT MESSAGES LIST
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(conversationMessages) { msg ->
                // ACCURATE IS_ME CHECK FOR BOTH ROLES
                val isMe = if (currentRole == UserRole.DOCTOR) {
                    msg.isDoctor || msg.senderId.trim().equals(currentUserId.trim(), ignoreCase = true)
                } else {
                    !msg.isDoctor || msg.senderId.trim().equals(currentUserId.trim(), ignoreCase = true)
                }

                ChatBubbleItem(
                    message = msg,
                    isMe = isMe,
                    partnerInitials = partnerInitials
                )
            }
        }

        // QUICK MESSAGE TEMPLATES ROW
        AnimatedVisibility(visible = showQuickTemplates) {
            val templates = if (currentRole == UserRole.PATIENT) listOf(
                "Bonjour Docteur 👋",
                "J'ai une question sur mon traitement 💊",
                "La tension est stable aujourd'hui 🩺",
                "Merci pour votre aide 🙏"
            ) else listOf(
                "Bonjour, comment vous sentez-vous ?",
                "Vos résultats sont rassurants 👍",
                "Poursuivez le traitement prescrit",
                "Passez au cabinet pour contrôle"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                templates.forEach { text ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable {
                            messageText = text
                            showQuickTemplates = false
                        }
                    ) {
                        Text(
                            text = text,
                            fontSize = 11.sp,
                            color = TextDarkNavy,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // INPUT BAR AT BOTTOM
        Surface(
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick template toggle (+)
                IconButton(
                    onClick = { showQuickTemplates = !showQuickTemplates }
                ) {
                    Icon(
                        imageVector = if (showQuickTemplates) Icons.Default.Close else Icons.Default.AddCircleOutline,
                        contentDescription = "Modèles de message",
                        tint = TealPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Text Input Field
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Écrire un message...", fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    singleLine = false,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = { sendMessageAction() }
                    )
                )

                // Send Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (messageText.isNotBlank() && !isSendingMessage)
                                Brush.verticalGradient(listOf(TealPrimary, TealDark))
                            else
                                Brush.verticalGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)))
                        )
                        .clickable(enabled = messageText.isNotBlank() && !isSendingMessage) {
                            sendMessageAction()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Envoyer",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(
    message: ChatMessage,
    isMe: Boolean,
    partnerInitials: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            if (!isMe) {
                InitialsAvatar(
                    initials = partnerInitials,
                    backgroundColor = TealPrimary,
                    size = 28.dp,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 2.dp,
                    bottomEnd = if (isMe) 2.dp else 16.dp
                ),
                color = if (isMe) TealPrimary else Color.White,
                shadowElevation = if (isMe) 2.dp else 1.dp,
                border = if (!isMe) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)) else null
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                    Text(
                        text = message.text,
                        color = if (isMe) Color.White else TextDarkNavy,
                        fontSize = 13.5.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message.timestamp,
                            color = if (isMe) Color.White.copy(alpha = 0.75f) else Color.Gray,
                            fontSize = 9.5.sp
                        )

                        if (isMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Lu",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
