package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.data.entity.SubjectEntity
import com.example.data.entity.TimetableSlotEntity
import kotlinx.coroutines.launch

object TimetableConstants {
    val FULL_DAYS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    
    val TIME_SLOTS = listOf(
        "09:00 AM - 10:00 AM",
        "10:00 AM - 11:00 AM",
        "11:00 AM - 12:00 PM",
        "12:00 PM - 01:00 PM", // Lunch Break
        "01:00 PM - 02:00 PM",
        "02:00 PM - 03:00 PM",
        "03:00 PM - 04:00 PM"
    )

    fun isLunchSlot(timeSlot: String): Boolean {
        return timeSlot.startsWith("12:00 PM") || timeSlot.contains("12:00 PM - 01:00 PM") || (timeSlot.contains("12:00") && timeSlot.contains("01:00"))
    }

    fun getSessionName(timeSlot: String): String {
        return when {
            timeSlot.startsWith("09:00") -> "Session 1"
            timeSlot.startsWith("10:00") -> "Session 2"
            timeSlot.startsWith("11:00") -> "Session 3"
            isLunchSlot(timeSlot) -> "Lunch Break"
            timeSlot.startsWith("01:00") -> "Session 4"
            timeSlot.startsWith("02:00") -> "Session 5"
            timeSlot.startsWith("03:00") -> "Session 6"
            else -> "Session"
        }
    }
}

enum class TimetableDisplayMode {
    FULL_TABLE, DAY_BY_DAY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    subjects: List<SubjectEntity>,
    timetableSlots: List<TimetableSlotEntity>,
    onBack: () -> Unit,
    onAutoSaveSlot: (dayOfWeek: String, timeSlot: String, subjectId: Long?, customSubjectName: String, roomOrNote: String, isLunchBreak: Boolean) -> Unit,
    onClearSlot: (dayOfWeek: String, timeSlot: String) -> Unit,
    onClearAll: () -> Unit
) {
    var displayMode by remember { mutableStateOf(TimetableDisplayMode.FULL_TABLE) }
    var selectedDayIndex by remember { mutableIntStateOf(0) } // Default Monday
    
    // Bottom Sheet state for editing a slot
    var activeEditingSlot by remember { mutableStateOf<Pair<String, String>?>(null) } // (dayOfWeek, timeSlot)
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Map for fast lookup (dayOfWeek, timeSlot) -> TimetableSlotEntity
    val slotsMap = remember(timetableSlots) {
        timetableSlots.associateBy { Pair(it.dayOfWeek, it.timeSlot) }
    }

    // Map subjectId -> SubjectEntity
    val subjectsMap = remember(subjects) {
        subjects.associateBy { it.id }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "My Timetable",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Auto-saved",
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Auto-saved",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF059669),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        Text(
                            text = "9:00 AM – 4:00 PM • Full Days Schedule",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Reset Entire Timetable") },
                            leadingIcon = { Icon(Icons.Default.Clear, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                showResetConfirmDialog = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // View Mode Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.weight(1f)
                ) {
                    SegmentedButton(
                        selected = displayMode == TimetableDisplayMode.FULL_TABLE,
                        onClick = { displayMode = TimetableDisplayMode.FULL_TABLE },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        icon = { Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    ) {
                        Text("Full Weekly Table", maxLines = 1)
                    }
                    SegmentedButton(
                        selected = displayMode == TimetableDisplayMode.DAY_BY_DAY,
                        onClick = { displayMode = TimetableDisplayMode.DAY_BY_DAY },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        icon = { Icon(Icons.Default.ViewDay, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    ) {
                        Text("Day View", maxLines = 1)
                    }
                }
            }

            HorizontalDivider()

            when (displayMode) {
                TimetableDisplayMode.FULL_TABLE -> {
                    FullTimetableTableView(
                        slotsMap = slotsMap,
                        subjectsMap = subjectsMap,
                        onSlotClick = { day, time ->
                            activeEditingSlot = Pair(day, time)
                        }
                    )
                }
                TimetableDisplayMode.DAY_BY_DAY -> {
                    DayByDayTimetableScreen(
                        selectedDayIndex = selectedDayIndex,
                        onDaySelected = { selectedDayIndex = it },
                        slotsMap = slotsMap,
                        subjectsMap = subjectsMap,
                        onSlotClick = { day, time ->
                            activeEditingSlot = Pair(day, time)
                        }
                    )
                }
            }
        }
    }

    // Active Slot Edit Sheet
    activeEditingSlot?.let { (day, time) ->
        val currentSlot = slotsMap[Pair(day, time)]
        val isLunch = TimetableConstants.isLunchSlot(time)

        SlotEditorBottomSheet(
            dayOfWeek = day,
            timeSlot = time,
            isLunchSlot = isLunch,
            currentSlot = currentSlot,
            subjects = subjects,
            onDismiss = { activeEditingSlot = null },
            onSave = { subjId, customName, roomNote, isLunchBreak ->
                onAutoSaveSlot(day, time, subjId, customName, roomNote, isLunchBreak)
                activeEditingSlot = null
            },
            onClear = {
                onClearSlot(day, time)
                activeEditingSlot = null
            }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset Timetable?") },
            text = { Text("Are you sure you want to clear all subject assignments from your weekly timetable?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAll()
                        showResetConfirmDialog = false
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Full Grid Matrix Table showing Days down rows (Monday - Sunday) and Time Sessions across columns
 */
@Composable
fun FullTimetableTableView(
    slotsMap: Map<Pair<String, String>, TimetableSlotEntity>,
    subjectsMap: Map<Long, SubjectEntity>,
    onSlotClick: (String, String) -> Unit
) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(verticalScrollState)
    ) {
        // Timetable header info banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Weekly Schedule: Days are listed as Rows and Time Sessions as Columns. Tap any cell to assign a subject from 'My Subjects' or edit room/notes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Table container with horizontal scroll
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clip(RoundedCornerShape(14.dp))
            ) {
                // Table Header Row: Day Column + 7 Session Columns
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Leftmost Header: Day / Session
                    Text(
                        text = "Day / Session",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .width(115.dp)
                            .padding(horizontal = 4.dp)
                    )

                    // Session Columns Headers (9 AM - 4 PM)
                    TimetableConstants.TIME_SLOTS.forEach { timeSlot ->
                        val isLunch = TimetableConstants.isLunchSlot(timeSlot)
                        val sessionTitle = TimetableConstants.getSessionName(timeSlot)

                        Column(
                            modifier = Modifier
                                .width(140.dp)
                                .padding(horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isLunch) "🍱 Lunch Break" else sessionTitle,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isLunch) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = timeSlot,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Days Rows (Monday through Sunday)
                TimetableConstants.FULL_DAYS.forEachIndexed { index, day ->
                    val isEvenRow = index % 2 == 0
                    Row(
                        modifier = Modifier
                            .background(
                                if (isEvenRow) MaterialTheme.colorScheme.surface
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Day Name Column Cell
                        Box(
                            modifier = Modifier
                                .width(115.dp)
                                .height(90.dp)
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = day.take(3).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = day,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Session Cells for this Day
                        TimetableConstants.TIME_SLOTS.forEach { timeSlot ->
                            val isLunch = TimetableConstants.isLunchSlot(timeSlot)
                            val slot = slotsMap[Pair(day, timeSlot)]
                            val subject = slot?.subjectId?.let { subjectsMap[it] }

                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(90.dp)
                                    .border(
                                        width = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                    .clickable { onSlotClick(day, timeSlot) }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLunch && (slot == null || slot.isLunchBreak)) {
                                    // Lunch slot UI
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Restaurant,
                                                contentDescription = "Lunch Break",
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Lunch Break",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "12:00 - 1:00 PM",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                } else if (subject != null || !(slot?.customSubjectName.isNull_Blank())) {
                                    val colorHex = subject?.colorHex ?: "#4F46E5"
                                    val parsedColor = parseColorSafely(colorHex)

                                    Surface(
                                        color = parsedColor.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .border(1.2.dp, parsedColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(6.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(parsedColor)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = subject?.name ?: slot?.customSubjectName ?: "",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = parsedColor,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            if (!subject?.gradeClass.isNull_Blank()) {
                                                Text(
                                                    text = subject?.gradeClass ?: "",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            if (!slot?.roomOrNote.isNull_Blank()) {
                                                Text(
                                                    text = "📍 " + slot?.roomOrNote,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Empty slot button
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Assign Subject",
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Assign",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

/**
 * Day-by-Day View Mode with full Tab navigation for Days
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayByDayTimetableScreen(
    selectedDayIndex: Int,
    onDaySelected: (Int) -> Unit,
    slotsMap: Map<Pair<String, String>, TimetableSlotEntity>,
    subjectsMap: Map<Long, SubjectEntity>,
    onSlotClick: (String, String) -> Unit
) {
    val currentDay = TimetableConstants.FULL_DAYS.getOrElse(selectedDayIndex) { "Monday" }

    Column(modifier = Modifier.fillMaxSize()) {
        // Day Tabs
        PrimaryTabRow(
            selectedTabIndex = selectedDayIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            TimetableConstants.FULL_DAYS.forEachIndexed { index, day ->
                Tab(
                    selected = selectedDayIndex == index,
                    onClick = { onDaySelected(index) },
                    text = {
                        Text(
                            text = day.take(3), // Mon, Tue, etc.
                            fontWeight = if (selectedDayIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Text(
            text = "$currentDay's Schedule",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(TimetableConstants.TIME_SLOTS) { timeSlot ->
                val isLunch = TimetableConstants.isLunchSlot(timeSlot)
                val slot = slotsMap[Pair(currentDay, timeSlot)]
                val subject = slot?.subjectId?.let { subjectsMap[it] }

                Card(
                    onClick = { onSlotClick(currentDay, timeSlot) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLunch) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Time & Session badge
                        val sessionTitle = TimetableConstants.getSessionName(timeSlot)
                        Surface(
                            color = if (isLunch) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isLunch) "🍱 Lunch" else sessionTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLunch) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = timeSlot,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            if (isLunch && (slot == null || slot.isLunchBreak)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Restaurant,
                                        contentDescription = "Lunch Break",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "12:00 - 1:00 PM Lunch Break",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            } else if (subject != null || !(slot?.customSubjectName.isNull_Blank())) {
                                val colorHex = subject?.colorHex ?: "#4F46E5"
                                val color = parseColorSafely(colorHex)

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = subject?.name ?: slot?.customSubjectName ?: "",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (!subject?.gradeClass.isNull_Blank()) {
                                    Text(
                                        text = "Class: ${subject?.gradeClass}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (!slot?.roomOrNote.isNull_Blank()) {
                                    Text(
                                        text = "📍 Room / Note: ${slot?.roomOrNote}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                Text(
                                    text = "+ Assign Subject",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Slot",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

/**
 * Bottom Sheet modal to pick a subject from "My Subjects" or customize a timetable slot
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotEditorBottomSheet(
    dayOfWeek: String,
    timeSlot: String,
    isLunchSlot: Boolean,
    currentSlot: TimetableSlotEntity?,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onSave: (subjectId: Long?, customSubjectName: String, roomOrNote: String, isLunchBreak: Boolean) -> Unit,
    onClear: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var selectedSubjectId by remember { mutableStateOf(currentSlot?.subjectId) }
    var customSubjectName by remember { mutableStateOf(currentSlot?.customSubjectName ?: "") }
    var roomOrNote by remember { mutableStateOf(currentSlot?.roomOrNote ?: "") }
    var isLunchBreak by remember { mutableStateOf(currentSlot?.isLunchBreak ?: isLunchSlot) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Assign Slot",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$dayOfWeek • $timeSlot",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (currentSlot != null) {
                    TextButton(onClick = onClear) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Slot", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLunchSlot) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Lunch",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Standard 12:00 PM - 1:00 PM Lunch Break",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "You can keep this as Lunch Break or select a Subject below.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Text(
                text = "Select From My Subjects",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            if (subjects.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "No subjects found. Create subjects in the main Dashboard screen to pick from 'My Subjects'.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(subjects) { subject ->
                        val isSelected = selectedSubjectId == subject.id
                        val color = parseColorSafely(subject.colorHex)

                        Surface(
                            onClick = {
                                selectedSubjectId = subject.id
                                customSubjectName = ""
                                isLunchBreak = false
                                // Auto-save selection immediately
                                onSave(subject.id, "", roomOrNote, false)
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, color) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = subject.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = subject.gradeClass,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = color
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Room / Note location field
            OutlinedTextField(
                value = roomOrNote,
                onValueChange = {
                    roomOrNote = it
                    // Auto save note
                    onSave(selectedSubjectId, customSubjectName, it, isLunchBreak)
                },
                label = { Text("Room / Note (e.g., Room 102, Lab A)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isLunchSlot) {
                    TextButton(
                        onClick = {
                            selectedSubjectId = null
                            customSubjectName = ""
                            isLunchBreak = true
                            onSave(null, "", roomOrNote, true)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Set as Lunch Break")
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

// Utility extension
private fun String?.isNull_Blank(): Boolean = this.isNullOrBlank()

// Color parser
private fun parseColorSafely(colorHex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color(0xFF4F46E5)
    }
}
