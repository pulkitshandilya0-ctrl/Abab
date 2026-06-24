package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.data.PdfFile
import com.example.data.StudyItem
import com.example.viewmodel.PdfViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    viewModel: PdfViewModel,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedPdf by viewModel.selectedPdf.collectAsState()
    val allPdfs by viewModel.allPdfs.collectAsState()

    val activeSession by viewModel.activeChatSession.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()

    val pdfSummary by viewModel.pdfSummary.collectAsState()
    val isSummarizing by viewModel.isSummarizing.collectAsState()

    val studyItems by viewModel.studyItems.collectAsState()

    var activeAiTab by remember { mutableStateOf(0) } // 0 = Chat, 1 = Summary, 2 = Study Prep

    var chatInputText by remember { mutableStateOf("") }

    // Translation control
    var showTranslateDropdown by remember { mutableStateOf(false) }
    val languages = listOf("Spanish", "French", "German", "Japanese", "Hindi", "Mandarin")

    // Sync selected PDF if null
    LaunchedEffect(selectedPdf) {
        if (selectedPdf == null && allPdfs.isNotEmpty()) {
            viewModel.selectPdf(allPdfs.first())
        }
    }

    // Initialize chat session for active PDF
    LaunchedEffect(selectedPdf) {
        selectedPdf?.let { pdf ->
            viewModel.getActiveSessionOrCreate(pdf)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text("Gemini AI Assistant", fontWeight = FontWeight.Bold)
                            selectedPdf?.let {
                                Text(
                                    "File: ${it.name}",
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    // PDF Selector dropdown in top bar
                    var showPdfSelector by remember { mutableStateOf(false) }
                    IconButton(onClick = { showPdfSelector = true }) {
                        Icon(Icons.Default.SwapCalls, contentDescription = "Change Document")
                    }
                    DropdownMenu(
                        expanded = showPdfSelector,
                        onDismissRequest = { showPdfSelector = false }
                    ) {
                        allPdfs.forEach { pdf ->
                            DropdownMenuItem(
                                text = { Text(pdf.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showPdfSelector = false
                                    viewModel.selectPdf(pdf)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs Header
            TabRow(
                selectedTabIndex = activeAiTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeAiTab == 0,
                    onClick = { activeAiTab = 0 },
                    text = { Text("Chat with PDF", fontSize = 13.sp) },
                    modifier = Modifier.testTag("tab_ai_chat")
                )
                Tab(
                    selected = activeAiTab == 1,
                    onClick = { activeAiTab = 1 },
                    text = { Text("Insights / Summary", fontSize = 13.sp) },
                    modifier = Modifier.testTag("tab_ai_summary")
                )
                Tab(
                    selected = activeAiTab == 2,
                    onClick = { activeAiTab = 2 },
                    text = { Text("Study Center", fontSize = 13.sp) },
                    modifier = Modifier.testTag("tab_ai_study")
                )
            }

            if (selectedPdf == null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(72.dp)
                        )
                        Text(
                            "No PDF Selected",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Text(
                            "Import or select a document in Workspace to leverage Gemini AI features.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                Crossfade(targetState = activeAiTab, modifier = Modifier.weight(1f)) { page ->
                    when (page) {
                        0 -> ChatView(
                            chatMessages = chatMessages,
                            isAiThinking = isAiThinking,
                            inputText = chatInputText,
                            onInputChanged = { chatInputText = it },
                            onSendMessage = {
                                viewModel.sendChatMessage(chatInputText)
                                chatInputText = ""
                            }
                        )
                        1 -> SummaryView(
                            pdfSummary = pdfSummary,
                            isSummarizing = isSummarizing,
                            onSummarize = { viewModel.summarizePdf() },
                            showTranslateDropdown = showTranslateDropdown,
                            onTranslateToggle = { showTranslateDropdown = !showTranslateDropdown },
                            languages = languages,
                            onLanguageSelect = { lang ->
                                showTranslateDropdown = false
                                viewModel.translateDocument(lang)
                            }
                        )
                        2 -> StudyView(
                            studyItems = studyItems,
                            isAiThinking = isAiThinking,
                            onGenerateQuiz = { viewModel.generateQuizOrFlashcards("quiz") },
                            onGenerateFlashcards = { viewModel.generateQuizOrFlashcards("flashcard") },
                            onDelete = { item -> viewModel.deleteStudyItem(item.id) }
                        )
                    }
                }
            }
        }
    }
}

// --- SUB-VIEWS FOR AI TABS ---

@Composable
fun ChatView(
    chatMessages: List<ChatMessage>,
    isAiThinking: Boolean,
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(chatMessages) { message ->
                ChatBubble(message = message)
            }

            if (isAiThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Gemini is typing...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Chat text entry field
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChanged,
                placeholder = { Text("Ask about document content...") },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input_field")
            )
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(
                onClick = onSendMessage,
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("ai_send_message_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send message")
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val containerColor = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (message.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = if (message.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Column(
        horizontalAlignment = alignment,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentWidth(alignment)
                .clip(shape)
                .background(containerColor)
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
        Text(
            text = if (message.isUser) "You" else "Gemini AI",
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
        )
    }
}

@Composable
fun SummaryView(
    pdfSummary: String,
    isSummarizing: Boolean,
    onSummarize: () -> Unit,
    showTranslateDropdown: Boolean,
    onTranslateToggle: () -> Unit,
    languages: List<String>,
    onLanguageSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onSummarize,
                enabled = !isSummarizing,
                modifier = Modifier
                    .weight(1.5f)
                    .testTag("generate_summary_button")
            ) {
                Icon(Icons.Default.Segment, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("Summarize File")
            }

            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = onTranslateToggle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("translate_pdf_button")
                ) {
                    Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text("Translate")
                }

                DropdownMenu(
                    expanded = showTranslateDropdown,
                    onDismissRequest = onTranslateToggle
                ) {
                    languages.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang) },
                            onClick = { onLanguageSelect(lang) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isSummarizing) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        pdfSummary.ifEmpty { "Generating Document Insights..." },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else if (pdfSummary.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Summarize,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(72.dp)
                    )
                    Text(
                        "No Summary Generated",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        "Click the button above to extract executive highlights, metrics, and key takeaways instantly.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = pdfSummary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudyView(
    studyItems: List<StudyItem>,
    isAiThinking: Boolean,
    onGenerateQuiz: () -> Unit,
    onGenerateFlashcards: () -> Unit,
    onDelete: (StudyItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onGenerateFlashcards,
                enabled = !isAiThinking,
                modifier = Modifier
                    .weight(1f)
                    .testTag("gen_flashcard_button")
            ) {
                Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                Text("Gen Flashcards")
            }

            OutlinedButton(
                onClick = onGenerateQuiz,
                enabled = !isAiThinking,
                modifier = Modifier
                    .weight(1f)
                    .testTag("gen_quiz_button")
            ) {
                Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                Text("Gen Quiz")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (studyItems.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(72.dp)
                    )
                    Text(
                        "Study Center Empty",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        "Generate highly tailored multi-choice quizzes and visual flashcards directly from your active document to accelerate learning.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(studyItems, key = { it.id }) { item ->
                    if (item.type == "flashcard") {
                        FlashcardItem(card = item, onDelete = { onDelete(item) })
                    } else {
                        QuizQuestionItem(quiz = item, onDelete = { onDelete(item) })
                    }
                }
            }
        }
    }
}

@Composable
fun FlashcardItem(
    card: StudyItem,
    onDelete: () -> Unit
) {
    var isFlipped by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFlipped) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isFlipped = !isFlipped }
            .testTag("flashcard_${card.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "FLASHCARD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isFlipped) "A: ${card.answer}" else "Q: ${card.question}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isFlipped) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            )

            Text(
                text = if (isFlipped) "Tap to see question" else "Tap to flip and reveal answer",
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun QuizQuestionItem(
    quiz: StudyItem,
    onDelete: () -> Unit
) {
    // quiz.answer format -> options||Correct: A
    val split = quiz.answer.split("||Correct:")
    val optionsString = split.getOrNull(0) ?: ""
    val correctAnswerLetter = split.getOrNull(1)?.trim() ?: "A"
    
    val optionsList = optionsString.split("|")

    var selectedOptionLetter by remember { mutableStateOf<String?>(null) }
    var isCorrectFeedback by remember { mutableStateOf<Boolean?>(null) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quiz_${quiz.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "MULTIPLE CHOICE QUIZ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = quiz.question,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            optionsList.forEach { option ->
                val optionLetter = option.firstOrNull()?.toString() ?: ""
                val isSelected = selectedOptionLetter == optionLetter
                val isCorrect = optionLetter == correctAnswerLetter

                val borderStroke = if (isSelected) {
                    if (isCorrectFeedback == true) BorderStroke(1.5.dp, Color.Green)
                    else BorderStroke(1.5.dp, Color.Red)
                } else BorderStroke(1.dp, Color.LightGray)

                val background = if (isSelected) {
                    if (isCorrectFeedback == true) Color.Green.copy(0.15f)
                    else Color.Red.copy(0.15f)
                } else Color.Transparent

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(background)
                        .border(borderStroke, RoundedCornerShape(8.dp))
                        .clickable {
                            selectedOptionLetter = optionLetter
                            isCorrectFeedback = (optionLetter == correctAnswerLetter)
                        }
                        .padding(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray)
                    ) {
                        Text(
                            optionLetter,
                            color = if (isSelected) Color.White else Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        option,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            isCorrectFeedback?.let { correct ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Icon(
                        imageVector = if (correct) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (correct) Color.Green else Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (correct) "Correct answer! Excellent job." else "Incorrect. The correct answer was $correctAnswerLetter.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (correct) Color.Green else Color.Red
                    )
                }
            }
        }
    }
}
