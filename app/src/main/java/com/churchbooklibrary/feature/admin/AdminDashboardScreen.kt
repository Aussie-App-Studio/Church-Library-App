package com.churchbooklibrary.feature.admin

import androidx.compose.material3.MaterialTheme

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.activity.compose.BackHandler
import com.churchbooklibrary.core.database.LoanEntity
import com.churchbooklibrary.feature.borrow.BookCoverImage
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.scale
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.churchbooklibrary.R
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.border
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import android.graphics.Bitmap
import java.io.File
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.churchbooklibrary.core.database.BookEntity
import com.churchbooklibrary.core.database.LoanWithBook
import com.churchbooklibrary.theme.Blue100
import com.churchbooklibrary.theme.Blue600
import com.churchbooklibrary.theme.Green100
import com.churchbooklibrary.theme.Green600
import com.churchbooklibrary.theme.Green700
import com.churchbooklibrary.theme.Red100
import com.churchbooklibrary.theme.Red600
import com.churchbooklibrary.theme.Slate100
import com.churchbooklibrary.theme.Slate200
import com.churchbooklibrary.theme.Slate400
import com.churchbooklibrary.theme.Slate50
import com.churchbooklibrary.theme.Slate600
import com.churchbooklibrary.theme.Slate700
import com.churchbooklibrary.theme.Slate800
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onLogout: () -> Unit,
    initialRoute: AdminRoute = AdminRoute.Menu,
    autoStartPhoneSync: Boolean = false
) {
    val context = LocalContext.current
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val libraryName by viewModel.libraryName.collectAsStateWithLifecycle()
    val isDemoMode by viewModel.isDemoMode.collectAsStateWithLifecycle()
    val isLookingUpIsbn by viewModel.isLookingUpIsbn.collectAsStateWithLifecycle()
    val isAddingBook by viewModel.isAddingBook.collectAsStateWithLifecycle()
    val inventoryLoadingMessage by viewModel.inventoryLoadingMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentRoute by remember(initialRoute) { mutableStateOf(initialRoute) }

    LaunchedEffect(autoStartPhoneSync) {
        if (autoStartPhoneSync && com.churchbooklibrary.core.sync.SyncPermissionHelper.hasAllRequiredPermissions(context)) {
            viewModel.startPhoneDiscovery(context)
        }
    }
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    var showAboutDeveloper by remember { mutableStateOf(false) }
    val hasSeenAbout by viewModel.hasSeenAboutDeveloper.collectAsStateWithLifecycle()
    val isDefaultPin by viewModel.isDefaultPin.collectAsStateWithLifecycle()

    BackHandler {
        if (currentRoute != AdminRoute.Menu) {
            currentRoute = AdminRoute.Menu
        } else {
            onLogout()
        }
    }

    // Barcode scanner
    val scannerOptions = remember {
        com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13)
            .enableAutoZoom()
            .build()
    }
    val scanner = remember(context) {
        com.google.mlkit.vision.codescanner.GmsBarcodeScanning.getClient(context, scannerOptions)
    }

    // Dialog states
    var showAddBook by remember { mutableStateOf(false) }
    var pendingManualIsbn by remember { mutableStateOf<String?>(null) }
    var showCsvImport by remember { mutableStateOf(false) }
    var showCategorySettings by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showCatalogExport by remember { mutableStateOf(false) }
    var pendingCatalogFields by remember { mutableStateOf<Set<com.churchbooklibrary.core.backup.CatalogField>?>(null) }
    var pendingCatalogFormat by remember { mutableStateOf<com.churchbooklibrary.core.backup.CatalogFormat?>(null) }
    var showDemoModeDialog by remember { mutableStateOf(false) }
    var demoDialogMessage by remember { mutableStateOf("") }
    var pendingDemoAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val categoriesEnabled by viewModel.categoriesEnabled.collectAsStateWithLifecycle()
    val enabledCategories by viewModel.enabledCategories.collectAsStateWithLifecycle()
    val customCategories by viewModel.customCategories.collectAsStateWithLifecycle()
    val availableCategories = if (categoriesEnabled) {
        (com.churchbooklibrary.core.datastore.SettingsDataStore.DEFAULT_CATEGORIES + customCategories).distinct().filter { it != "Other" } + "Other"
    } else emptyList()

    // SAF launchers
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? -> uri?.let { viewModel.exportBackup(context, it) } }

    val catalogExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri: Uri? ->
        val fields = pendingCatalogFields
        val format = pendingCatalogFormat
        if (uri != null && fields != null && format != null) {
            viewModel.exportCatalog(context, uri, fields, format)
        }
        pendingCatalogFields = null
        pendingCatalogFormat = null
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { viewModel.importBackup(context, it) } }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.saveCustomLogo(context, it) } }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatus()
        }
    }

    // Dialogs
    if (showAboutDeveloper) {
        AboutDeveloperDialog(onDismiss = { showAboutDeveloper = false })
    }
    if (showDemoModeDialog) {
        AlertDialog(
            onDismissRequest = {
                showDemoModeDialog = false
                pendingDemoAction = null
            },
            title = { Text(stringResource(R.string.demo_mode)) },
            text = { Text(demoDialogMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showDemoModeDialog = false
                        viewModel.toggleDemoMode(false)
                        pendingDemoAction?.invoke()
                        pendingDemoAction = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                ) {
                    Text(stringResource(R.string.exit_demo_mode))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDemoModeDialog = false
                        pendingDemoAction = null
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    if (showAddBook) {
        AddBookDialog(
            initialIsbn = pendingManualIsbn ?: "",
            categoriesEnabled = categoriesEnabled,
            availableCategories = availableCategories,
            onDismiss = {
                showAddBook = false
                pendingManualIsbn = null
            },
            onSave = { title, author, isbn, owner, copies, coverUrl, category ->
                viewModel.addBook(title, author, isbn, owner, copies, coverUrl, category)
                showAddBook = false
                pendingManualIsbn = null
            }
        )
    }
    if (showCategorySettings) {
        CategorySettingsDialog(
            categoriesEnabled = categoriesEnabled,
            enabledCategories = enabledCategories,
            customCategories = customCategories,
            onToggleCategoriesEnabled = { viewModel.setCategoriesEnabled(it) },
            onToggleCategory = { cat, enabled ->
                val updated = if (enabled) enabledCategories + cat else enabledCategories - cat
                viewModel.setEnabledCategories(updated)
            },
            onAddCustomCategory = { cat, onError -> viewModel.addCustomCategory(cat, onError) },
            onGetBookCount = { cat -> viewModel.getBookCountForCategory(cat) },
            onRemoveCustomCategory = { viewModel.removeCustomCategory(it) },
            onDismiss = { showCategorySettings = false }
        )
    }
    if (showCsvImport) {
        CsvImportDialog(
            onDismiss = { showCsvImport = false },
            onImport = { csv ->
                viewModel.importCsv(csv)
                showCsvImport = false
            }
        )
    }
    if (showCatalogExport) {
        CatalogExportDialog(
            onDismiss = { showCatalogExport = false },
            onSave = { fields, format ->
                showCatalogExport = false
                pendingCatalogFields = fields
                pendingCatalogFormat = format
                catalogExportLauncher.launch(com.churchbooklibrary.core.backup.CatalogExporter.fileName(format))
            },
            onShare = { fields, format ->
                showCatalogExport = false
                viewModel.shareCatalog(context, fields, format)
            }
        )
    }
    if (showChangePinDialog) {
        ChangePinOverlay(
            onDismiss = { showChangePinDialog = false },
            onSave = { current, new, confirm, onOk, onError ->
                viewModel.changePin(current, new, confirm, onOk, onError)
            }
        )
    }

    val duplicatePrompt by viewModel.duplicateBookPrompt.collectAsStateWithLifecycle()
    if (duplicatePrompt != null) {
        val existing = duplicatePrompt!!.existingBook
        val additional = duplicatePrompt!!.additionalCopies
        val newTotal = existing.totalCopies + additional
        val copyOrdinal = when (newTotal % 100) {
            11, 12, 13 -> "${newTotal}th"
            else -> when (newTotal % 10) {
                1 -> "${newTotal}st"
                2 -> "${newTotal}nd"
                3 -> "${newTotal}rd"
                else -> "${newTotal}th"
            }
        }
        AlertDialog(
            onDismissRequest = { viewModel.dismissDuplicatePrompt() },
            title = { Text("Duplicate Book") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val countPhrase = if (existing.totalCopies == 1) "1 copy" else "${existing.totalCopies} copies"
                    Text(
                        "$countPhrase of \"${existing.title}\" by ${existing.author} is already in your library. Add another?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmAddDuplicateCopy() },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                ) {
                    Text("Add as $copyOrdinal Copy")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDuplicatePrompt() }) {
                    Text("Dismiss")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                title = {
                    Column {
                        Text(stringResource(currentRoute.titleRes), fontWeight = FontWeight.Bold)
                        if (currentRoute == AdminRoute.Menu) {
                            Text(stringResource(R.string.admin_dashboard_subtitle), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                },
                navigationIcon = {
                    if (currentRoute != AdminRoute.Menu) {
                        IconButton(onClick = { currentRoute = AdminRoute.Menu }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
                        }
                    }
                },
                                actions = {
                    val infiniteTransition = rememberInfiniteTransition()
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f, targetValue = if (hasSeenAbout) 1f else 1.2f,
                        animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse)
                    )
                    IconButton(onClick = { 
                        if (!hasSeenAbout) viewModel.markAboutDeveloperSeen()
                        showAboutDeveloper = true 
                    }) {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.Info, 
                            contentDescription = stringResource(R.string.about_developer), 
                            modifier = Modifier.scale(scale), 
                            tint = if (hasSeenAbout) Color.White.copy(alpha = 0.5f) else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentRoute) {
                AdminRoute.Menu -> AdminHubMenu(
                    isDefaultPin = isDefaultPin,
                    totalBooksEverLoaned = viewModel.totalBooksEverLoaned.collectAsStateWithLifecycle().value,
                    onChangePin = { showChangePinDialog = true },
                    onNavigate = { currentRoute = it },
                    onLogout = onLogout
                )
                AdminRoute.Loans -> ActiveLoansPanel(
                    viewModel = viewModel,
                    onMarkReturned = { viewModel.markReturned(it) },
                    onDeleteBook = { book ->
                        viewModel.deleteBook(book) { msg ->
                            // Error shown via StatusMessage flow
                        }
                    }
                )
                AdminRoute.Inventory -> InventoryPanel(
                    viewModel = viewModel,
                    isDemoMode = isDemoMode,
                    onOpenCategorySettings = { showCategorySettings = true },
                    onAddBook = {
                        if (isDemoMode) {
                            demoDialogMessage = "It is not possible to add books in Demo mode. Turn off Demo mode to add your own books."
                            pendingDemoAction = {
                                pendingManualIsbn = null
                                showAddBook = true
                            }
                            showDemoModeDialog = true
                        } else {
                            pendingManualIsbn = null
                            showAddBook = true
                        }
                    },
                    onImportCsv = {
                        if (isDemoMode) {
                            demoDialogMessage = "It is not possible to add books in Demo mode. Turn off Demo mode to add your own books."
                            pendingDemoAction = { showCsvImport = true }
                            showDemoModeDialog = true
                        } else {
                            showCsvImport = true
                        }
                    },
                    onScanIsbn = {
                        if (isDemoMode) {
                            demoDialogMessage = "It is not possible to add books in Demo mode. Turn off Demo mode to add your own books."
                            pendingDemoAction = {
                                scanner.startScan()
                                    .addOnSuccessListener { barcode: com.google.mlkit.vision.barcode.common.Barcode ->
                                        barcode.rawValue?.let { isbn ->
                                            pendingManualIsbn = isbn
                                            viewModel.addBookByIsbn(isbn)
                                        }
                                    }
                                    .addOnCanceledListener {
                                        // User cancelled — do nothing
                                    }
                                    .addOnFailureListener { e: Exception ->
                                        viewModel.setStatusMessage("Scanner error: ${e.localizedMessage ?: "Camera or barcode service unavailable"}")
                                    }
                            }
                            showDemoModeDialog = true
                        } else {
                            scanner.startScan()
                                .addOnSuccessListener { barcode: com.google.mlkit.vision.barcode.common.Barcode ->
                                    barcode.rawValue?.let { isbn ->
                                        pendingManualIsbn = isbn
                                        viewModel.addBookByIsbn(isbn)
                                    }
                                }
                                .addOnCanceledListener {
                                    // User cancelled — do nothing
                                }
                                .addOnFailureListener { e: Exception ->
                                    viewModel.setStatusMessage("Scanner error: ${e.localizedMessage ?: "Camera or barcode service unavailable"}")
                                }
                        }
                    },
                    onDeleteBook = { book ->
                        viewModel.deleteBook(book) { msg ->
                            // Error shown via StatusMessage flow
                        }
                    },
                    onDemoActionBlocked = { message ->
                        demoDialogMessage = message
                        pendingDemoAction = null
                        showDemoModeDialog = true
                    }
                )
                AdminRoute.Settings -> BackupPanel(
                    libraryName = libraryName,
                    ribbonColor = viewModel.ribbonColor.collectAsStateWithLifecycle().value,
                    maxBorrowLimit = viewModel.maxBorrowLimit.collectAsStateWithLifecycle().value,
                    isDemoMode = isDemoMode,
                    hasCustomLogo = viewModel.hasCustomLogo.collectAsStateWithLifecycle().value,
                    logoLastModified = viewModel.logoLastModified.collectAsStateWithLifecycle().value,
                    categoriesEnabled = categoriesEnabled,
                    enabledCategories = enabledCategories,
                    customCategories = customCategories,
                    onOpenCategorySettings = { showCategorySettings = true },
                    syncStatus = syncStatus,
                    syncRole = viewModel.syncRole.collectAsStateWithLifecycle().value,
                    syncDayOfWeek = viewModel.syncDayOfWeek.collectAsStateWithLifecycle().value,
                    syncStartHour = viewModel.syncStartHour.collectAsStateWithLifecycle().value,
                    syncEndHour = viewModel.syncEndHour.collectAsStateWithLifecycle().value,
                    onSetSyncRole = { viewModel.setSyncRole(context, it) },
                    onUpdateSyncSchedule = { day, start, end -> viewModel.updateSyncSchedule(context, day, start, end) },
                    onStartTabletSync = { viewModel.toggleTabletBroadcasting(context, true) },
                    onStopTabletSync = { viewModel.toggleTabletBroadcasting(context, false) },
                    onStartPhoneSync = { viewModel.startPhoneDiscovery(context) },
                    onStopPhoneSync = { viewModel.stopPhoneDiscovery(context) },
                    onToggleDemoMode = { viewModel.toggleDemoMode(it) },
                    onSaveLibraryName = { viewModel.updateLibraryName(it) },
                    onSaveRibbonColor = { viewModel.updateRibbonColor(it) },
                    onSaveMaxBorrowLimit = { viewModel.updateMaxBorrowLimit(it) },
                    borrowPeriods = viewModel.borrowPeriods.collectAsStateWithLifecycle().value,
                    onSaveBorrowPeriods = { viewModel.updateBorrowPeriods(it) },
                    onUploadLogo = { logoPickerLauncher.launch("image/*") },
                    onRemoveLogo = { viewModel.removeCustomLogo(context) },
                    onExport = { exportLauncher.launch("church_library_backup_${System.currentTimeMillis()}.json") },
                    onExportShare = { viewModel.shareBackup(context) },
                    onImport = { importLauncher.launch(arrayOf("application/json")) },
                    onOpenCatalogExport = { showCatalogExport = true },
                    onChangePin = { showChangePinDialog = true }
                )
            }
        }
    }

    val activeOverlayMessage = inventoryLoadingMessage ?: if (isLookingUpIsbn) "Looking up your book..." else null
    if (activeOverlayMessage != null) {
        val canAbandon = isLookingUpIsbn || isAddingBook
        val abandonPromptText = if (isAddingBook) {
            "Still looking for a book cover...\nYou can wait a moment longer, or save now with a default color cover."
        } else {
            "Still searching online...\nYou can wait a moment longer, or skip the search and enter the details manually."
        }
        val abandonActionLabel = if (isAddingBook) "Save with Default Cover" else "Enter Manually"
        val abandonActionIcon = if (isAddingBook) Icons.Default.Check else Icons.Default.Edit

        IsbnLookupOverlay(
            message = activeOverlayMessage,
            canAbandon = canAbandon,
            abandonPromptText = abandonPromptText,
            abandonActionLabel = abandonActionLabel,
            abandonActionIcon = abandonActionIcon,
            onAbandonAction = {
                if (isAddingBook) {
                    viewModel.skipCoverAndSaveNow()
                } else {
                    viewModel.cancelIsbnLookup()
                    showAddBook = true
                }
            },
            onCancel = {
                if (isAddingBook) {
                    viewModel.cancelAddBook()
                } else {
                    viewModel.cancelIsbnLookup()
                    pendingManualIsbn = null
                }
            }
        )
    }
} // Close Box
} // Close AdminDashboardScreen

enum class AdminRoute(val titleRes: Int) {
    Menu(R.string.admin_dashboard_title),
    Loans(R.string.admin_active_loans),
    Inventory(R.string.admin_manage_inventory),
    Settings(R.string.admin_settings_backup)
}

@Composable
private fun DefaultPinWarningBanner(
    onChangePin: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isNarrow = configuration.screenWidthDp < 600

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 800.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFFBEB), // Amber 50
        border = BorderStroke(1.dp, Color(0xFFFDE68A)) // Amber 200
    ) {
        if (isNarrow) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFEF3C7), // Amber 100
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFD97706), // Amber 600
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.default_pin_banner_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E) // Amber 900
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.default_pin_banner_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB45309) // Amber 700
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cancel),
                            tint = Color(0xFFB45309).copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onChangePin,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD97706),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.default_pin_banner_action),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFEF3C7), // Amber 100
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFD97706), // Amber 600
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.default_pin_banner_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E) // Amber 900
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.default_pin_banner_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB45309) // Amber 700
                    )
                }
                Spacer(Modifier.width(16.dp))
                Button(
                    onClick = onChangePin,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD97706),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.default_pin_banner_action),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cancel),
                        tint = Color(0xFFB45309).copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminHubMenu(
    isDefaultPin: Boolean,
    totalBooksEverLoaned: Int,
    onChangePin: () -> Unit,
    onNavigate: (AdminRoute) -> Unit,
    onLogout: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    var bannerDismissed by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.admin_what_to_do),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Slate700
        )
        Spacer(Modifier.height(12.dp))
        Surface(
            shape = RoundedCornerShape(50),
            color = Green100
        ) {
            Text(
                text = "📚 $totalBooksEverLoaned books have been loaned out to our community",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Green700,
                textAlign = TextAlign.Center
            )
        }
        if (isDefaultPin && !bannerDismissed) {
            Spacer(Modifier.height(16.dp))
            DefaultPinWarningBanner(
                onChangePin = onChangePin,
                onDismiss = { bannerDismissed = true }
            )
            Spacer(Modifier.height(if (isPortrait) 16.dp else 24.dp))
        } else {
            Spacer(Modifier.height(48.dp))
        }

        if (isPortrait) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                MenuActionCard(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    title = stringResource(R.string.admin_active_loans),
                    description = stringResource(R.string.admin_active_loans_desc),
                    icon = Icons.Default.Book,
                    iconBg = Blue100,
                    iconColor = Blue600,
                    onClick = { onNavigate(AdminRoute.Loans) }
                )
                MenuActionCard(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    title = stringResource(R.string.admin_manage_inventory),
                    description = stringResource(R.string.admin_manage_inventory_desc),
                    icon = Icons.Default.Add,
                    iconBg = Green100,
                    iconColor = Green700,
                    onClick = { onNavigate(AdminRoute.Inventory) }
                )
                MenuActionCard(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    title = stringResource(R.string.admin_settings_backup),
                    description = stringResource(R.string.admin_settings_backup_desc),
                    icon = Icons.Default.Info,
                    iconBg = Slate100,
                    iconColor = Slate600,
                    onClick = { onNavigate(AdminRoute.Settings) }
                )
                MenuActionCard(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    title = stringResource(R.string.admin_log_out),
                    description = stringResource(R.string.admin_log_out_desc),
                    icon = Icons.Default.Lock,
                    iconBg = Red100,
                    iconColor = Red600,
                    onClick = onLogout
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    MenuActionCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        title = stringResource(R.string.admin_active_loans),
                        description = stringResource(R.string.admin_active_loans_desc),
                        icon = Icons.Default.Book,
                        iconBg = Blue100,
                        iconColor = Blue600,
                        onClick = { onNavigate(AdminRoute.Loans) }
                    )
                    MenuActionCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        title = stringResource(R.string.admin_manage_inventory),
                        description = stringResource(R.string.admin_manage_inventory_desc),
                        icon = Icons.Default.Add,
                        iconBg = Green100,
                        iconColor = Green700,
                        onClick = { onNavigate(AdminRoute.Inventory) }
                    )
                }
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    MenuActionCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        title = stringResource(R.string.admin_settings_backup),
                        description = stringResource(R.string.admin_settings_backup_desc),
                        icon = Icons.Default.Info,
                        iconBg = Slate100,
                        iconColor = Slate600,
                        onClick = { onNavigate(AdminRoute.Settings) }
                    )
                    MenuActionCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        title = stringResource(R.string.admin_log_out),
                        description = stringResource(R.string.admin_log_out_desc),
                        icon = Icons.Default.Lock,
                        iconBg = Red100,
                        iconColor = Red600,
                        onClick = onLogout
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuActionCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(shape = RoundedCornerShape(20.dp), color = iconBg, modifier = Modifier.size(80.dp)) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.padding(20.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            Text(description, style = MaterialTheme.typography.bodyLarge, color = Slate600, textAlign = TextAlign.Center)
        }
    }
}

// ─── Active Loans Panel ───────────────────────────────────────────────────────

@Composable
private fun ActiveLoansPanel(
    viewModel: AdminViewModel,
    onMarkReturned: (Long) -> Unit,
    onDeleteBook: (BookEntity) -> Unit
) {
    val loans by viewModel.activeLoans.collectAsStateWithLifecycle()
    val books by viewModel.books.collectAsStateWithLifecycle()
    var loanToDelete by remember { mutableStateOf<LoanWithBook?>(null) }

    if (loanToDelete != null) {
        val stats = books.find { it.book.id == loanToDelete!!.book.id }
        DeleteBookConfirmationDialog(
            bookWithStats = stats ?: com.churchbooklibrary.core.database.BookWithStats(loanToDelete!!.book, 0, null),
            activeLoan = loanToDelete!!.loan,
            onDismiss = { loanToDelete = null },
            onConfirm = {
                onDeleteBook(loanToDelete!!.book)
                loanToDelete = null
            }
        )
    }

    if (loans.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Slate400, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("No active borrowed books.", color = Slate400, style = MaterialTheme.typography.bodyLarge)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            items(loans, key = { it.loan.id }) { loanWithBook ->
                AdminLoanRow(
                    loanWithBook = loanWithBook, 
                    onMarkReturned = { onMarkReturned(loanWithBook.loan.id) },
                    onDelete = { loanToDelete = loanWithBook }
                )
            }
        }
    }
}

@Composable
private fun AdminLoanRow(loanWithBook: LoanWithBook, onMarkReturned: () -> Unit, onDelete: () -> Unit) {
    val now = System.currentTimeMillis()
    val isOverdue = loanWithBook.loan.dueDate < now
    val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    val bgColor = if (isOverdue) Red100.copy(alpha = 0.5f) else Color.White
    val borderColor = if (isOverdue) Red600 else Slate200

    Card(
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.5.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BookCoverImage(
                title = loanWithBook.book.title,
                author = loanWithBook.book.author,
                coverUrl = loanWithBook.book.coverUrl,
                modifier = Modifier
                    .size(width = 44.dp, height = 66.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(loanWithBook.book.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(loanWithBook.book.author, style = MaterialTheme.typography.bodySmall, color = Slate600)
                Spacer(Modifier.height(4.dp))
                Text("Borrower: ${loanWithBook.loan.borrowerName}", style = MaterialTheme.typography.bodySmall, color = Slate700)
                Text("Email: ${loanWithBook.loan.borrowerEmail.ifBlank { "Not provided" }}", style = MaterialTheme.typography.bodySmall, color = Slate600)
                Text("Phone: ${loanWithBook.loan.borrowerPhone.ifBlank { "Not provided" }}", style = MaterialTheme.typography.bodySmall, color = Slate600)
                Text(
                    text = if (isOverdue) "⚠ OVERDUE — Due: ${dateFormat.format(Date(loanWithBook.loan.dueDate))}"
                    else "Due: ${dateFormat.format(Date(loanWithBook.loan.dueDate))} · ${loanWithBook.loan.durationLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverdue) Red600 else Slate600,
                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.width(androidx.compose.foundation.layout.IntrinsicSize.Max),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = onMarkReturned,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Green600),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Mark Returned", fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Red600),
                    border = BorderStroke(1.dp, Red600.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Delete Permanently", fontSize = 13.sp)
                }
            }
        }
    }
}

// ─── Inventory Panel ──────────────────────────────────────────────────────────

@Composable
private fun InventoryPanel(
    viewModel: AdminViewModel,
    isDemoMode: Boolean,
    onOpenCategorySettings: () -> Unit,
    onAddBook: () -> Unit,
    onImportCsv: () -> Unit,
    onScanIsbn: () -> Unit,
    onDeleteBook: (BookEntity) -> Unit,
    onDemoActionBlocked: ((String) -> Unit)? = null
) {
    val books by viewModel.books.collectAsStateWithLifecycle()
    val query by viewModel.bookSearchQuery.collectAsStateWithLifecycle()
    val activeLoans by viewModel.activeLoans.collectAsStateWithLifecycle()
    val categoriesEnabled by viewModel.categoriesEnabled.collectAsStateWithLifecycle()
    val enabledCategories by viewModel.enabledCategories.collectAsStateWithLifecycle()
    val customCategories by viewModel.customCategories.collectAsStateWithLifecycle()
    val coverSyncProgress by viewModel.coverSyncProgress.collectAsStateWithLifecycle()
    var bookToEdit by remember { mutableStateOf<BookEntity?>(null) }
    var bookToDelete by remember { mutableStateOf<com.churchbooklibrary.core.database.BookWithStats?>(null) }

    if (bookToEdit != null) {
        val availableCategories = if (categoriesEnabled) {
            (com.churchbooklibrary.core.datastore.SettingsDataStore.DEFAULT_CATEGORIES + customCategories).distinct().filter { it != "Other" } + "Other"
        } else emptyList()
        EditBookDialog(
            book = bookToEdit!!,
            onDismiss = { bookToEdit = null },
            onSave = { updatedBook ->
                viewModel.updateBook(updatedBook)
                bookToEdit = null
            },
            categoriesEnabled = categoriesEnabled,
            availableCategories = availableCategories
        )
    }

    if (bookToDelete != null) {
        val loan = activeLoans.find { it.book.id == bookToDelete!!.book.id }?.loan
        DeleteBookConfirmationDialog(
            bookWithStats = bookToDelete!!,
            activeLoan = loan,
            onDismiss = { bookToDelete = null },
            onConfirm = {
                onDeleteBook(bookToDelete!!.book)
                bookToDelete = null
            }
        )
    }

    Column(Modifier.fillMaxSize()) {
        Surface(shadowElevation = 2.dp) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.onBookSearch(it) },
                        placeholder = { Text("Search inventory...") },
                        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Button(
                        onClick = onAddBook,
                        colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Book")
                    }
                    Button(
                        onClick = onScanIsbn,
                        colors = ButtonDefaults.buttonColors(containerColor = Green700),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Scan ISBN")
                    }
                    FilledTonalButton(
                        onClick = onImportCsv,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Import CSV")
                    }
                }
                
                var showConsignorsDialog by remember { mutableStateOf(false) }
    
                if (showConsignorsDialog) {
                    ManageConsignorsDialog(
                        booksWithStats = books,
                        activeLoans = activeLoans,
                        onDismiss = { showConsignorsDialog = false },
                        onDeleteBook = onDeleteBook
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.startDeepCoverSearch() },
                        enabled = !isDemoMode && !coverSyncProgress.isRunning,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Find Covers")
                    }
                    Button(
                        onClick = onOpenCategorySettings,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("📁 Category Settings")
                    }
                    Button(
                        onClick = { showConsignorsDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PersonSearch, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Manage Consignors")
                    }
                }
            }
        }

        AnimatedVisibility(visible = coverSyncProgress.isRunning) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                color = Blue600.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, Blue600.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Blue600
                    )
                    Text(
                        text = "Searching online covers in background: ${coverSyncProgress.completed}/${coverSyncProgress.totalToScan} checked (${coverSyncProgress.foundCount} covers found)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Blue600,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (books.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No books found.", color = Slate400)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
            ) {
                items(books, key = { it.book.id }) { bookWithStats ->
                    BookInventoryRow(
                        bookWithStats = bookWithStats, 
                        isDemoMode = isDemoMode,
                        onEdit = { bookToEdit = bookWithStats.book },
                        onDelete = { bookToDelete = bookWithStats },
                        onDemoActionBlocked = onDemoActionBlocked
                    )
                }
            }
        }
    }
}


@Composable
private fun ManageConsignorsDialog(
    booksWithStats: List<com.churchbooklibrary.core.database.BookWithStats>,
    activeLoans: List<LoanWithBook>,
    onDismiss: () -> Unit,
    onDeleteBook: (BookEntity) -> Unit
) {
    var selectedConsignor by remember { mutableStateOf<String?>(null) }
    var showExplanation by remember { mutableStateOf(true) }
    var bookToDelete by remember { mutableStateOf<com.churchbooklibrary.core.database.BookWithStats?>(null) }
    var showDeleteAllConfirmation by remember { mutableStateOf(false) }

    if (bookToDelete != null) {
        val loan = activeLoans.find { it.book.id == bookToDelete!!.book.id }?.loan
        DeleteBookConfirmationDialog(
            bookWithStats = bookToDelete!!,
            activeLoan = loan,
            onDismiss = { bookToDelete = null },
            onConfirm = {
                onDeleteBook(bookToDelete!!.book)
                bookToDelete = null
            }
        )
    }

    if (showDeleteAllConfirmation && selectedConsignor != null) {
        val consignorBooks = booksWithStats.filter { it.book.owner == selectedConsignor }
        val loanedCount = consignorBooks.count { stat -> activeLoans.any { it.book.id == stat.book.id } }
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirmation = false },
            title = { Text("Delete All Books for $selectedConsignor?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (loanedCount > 0) {
                        Text(
                            "WARNING: $loanedCount of these ${consignorBooks.size} books are currently loaned out. Deleting will permanently remove all books and delete active loan records.",
                            color = Red600,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            "Are you sure you want to permanently delete all ${consignorBooks.size} books belonging to $selectedConsignor and their loan history? This cannot be undone.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        consignorBooks.forEach { onDeleteBook(it.book) }
                        selectedConsignor = null
                        showDeleteAllConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red600)
                ) {
                    Text("Delete All Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val allConsignors = booksWithStats
        .mapNotNull { it.book.owner }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f).padding(vertical = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedConsignor != null) {
                            IconButton(onClick = { selectedConsignor = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Spacer(Modifier.width(16.dp))
                        }
                        Text(
                            text = if (selectedConsignor == null) "Consignors" else "Books by $selectedConsignor", 
                            style = MaterialTheme.typography.titleLarge, 
                            color = MaterialTheme.colorScheme.onPrimaryContainer, 
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { showExplanation = !showExplanation },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Info, 
                                contentDescription = if (showExplanation) "Hide explanation" else "What is a consignor?", 
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    if (showExplanation) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Blue100.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Blue600.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Blue600,
                                    modifier = Modifier.size(24.dp).padding(top = 2.dp)
                                )
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "What is a Consignor?",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate800
                                        )
                                        IconButton(
                                            onClick = { showExplanation = false },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Dismiss",
                                                tint = Slate600,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "A consignor is a church member who loans their personal books to the library for others to borrow, while still retaining ownership.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate700,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "How to use this feature:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate800
                                    )
                                    Text(
                                        text = "When adding or editing a book in inventory, enter the member's name in the \"Owner / Consignor\" field. This screen gives you a clear record of who owns which books, so you can track them and easily return or remove them whenever the member asks.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate600,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    if (selectedConsignor == null) {
                        if (allConsignors.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.PersonSearch, contentDescription = null, tint = Slate400, modifier = Modifier.size(44.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("No consignors found", fontWeight = FontWeight.SemiBold, color = Slate700)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Books with an 'Owner / Consignor' set in inventory will appear here.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate600,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(allConsignors.size) { index ->
                                    val consignor = allConsignors[index]
                                    val count = booksWithStats.count { it.book.owner == consignor }
                                    ListItem(
                                        headlineContent = { Text(consignor) },
                                        supportingContent = { Text("$count books") },
                                        leadingContent = { Icon(Icons.Default.Person, contentDescription = null) },
                                        modifier = Modifier.clickable { selectedConsignor = consignor }
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    } else {
                        val consignorBooks = booksWithStats.filter { it.book.owner == selectedConsignor }
                        if (consignorBooks.isEmpty()) {
                            selectedConsignor = null
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${consignorBooks.size} books found", style = MaterialTheme.typography.titleMedium)
                                Button(
                                    onClick = { showDeleteAllConfirmation = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Red600)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Delete All")
                                }
                            }
                            HorizontalDivider()
                            androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(consignorBooks.size) { index ->
                                    val stat = consignorBooks[index]
                                    ListItem(
                                        leadingContent = {
                                            BookCoverImage(
                                                title = stat.book.title,
                                                author = stat.book.author,
                                                coverUrl = stat.book.coverUrl,
                                                modifier = Modifier
                                                    .size(width = 44.dp, height = 66.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                            )
                                        },
                                        headlineContent = { Text(stat.book.title, fontWeight = FontWeight.Bold) },
                                        supportingContent = { Text(stat.book.author) },
                                        trailingContent = {
                                            IconButton(onClick = { bookToDelete = stat }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Red600)
                                            }
                                        }
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
                
                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("Close") }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySettingsDialog(
    categoriesEnabled: Boolean,
    enabledCategories: Set<String>,
    customCategories: Set<String>,
    onToggleCategoriesEnabled: (Boolean) -> Unit,
    onToggleCategory: (String, Boolean) -> Unit,
    onAddCustomCategory: (String, (String) -> Unit) -> Unit,
    onGetBookCount: suspend (String) -> Int,
    onRemoveCustomCategory: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newCategoryText by remember { mutableStateOf("") }
    var newCategoryError by remember { mutableStateOf<String?>(null) }
    var pendingDeleteCategory by remember { mutableStateOf<String?>(null) }
    var pendingDeleteBookCount by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val allCategories = (com.churchbooklibrary.core.datastore.SettingsDataStore.DEFAULT_CATEGORIES + customCategories).distinct().filter { it != "Other" } + "Other"

    if (pendingDeleteCategory != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteCategory = null },
            title = { Text("Delete Category?") },
            text = {
                if (pendingDeleteBookCount > 0) {
                    Text("There are $pendingDeleteBookCount book(s) currently assigned to \"$pendingDeleteCategory\". Removing this category will reassign those books to \"Other\". Are you sure you want to proceed?")
                } else {
                    Text("Are you sure you want to remove the custom category \"$pendingDeleteCategory\"?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cat = pendingDeleteCategory ?: return@Button
                        pendingDeleteCategory = null
                        onRemoveCustomCategory(cat)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red600)
                ) {
                    Text("Delete Category")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteCategory = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp)
                ) {
                    Text("Category Settings", style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
                }

                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth()
                        .verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Warning card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Info, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(20.dp).padding(top = 2.dp))
                            Text(
                                "We recommend enabling categories only if your library has more than 40 books. Smaller libraries are usually easier to browse without them.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }

                    // Master toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Enable Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Group books into shelves on the borrow screen",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = categoriesEnabled, 
                            onCheckedChange = onToggleCategoriesEnabled,
                            colors = SwitchDefaults.colors(
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Slate400,
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }

                    if (categoriesEnabled) {
                        HorizontalDivider()
                        Text("Active Categories", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Text("Select which categories to show. \"Other\" is always on.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        allCategories.forEach { category ->
                            val isOther = category == "Other"
                            val isChecked = isOther || category in enabledCategories
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(if (!isOther) Modifier.clickable { onToggleCategory(category, !isChecked) } else Modifier)
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    category + if (isOther) " (always on)" else "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isOther) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (category in customCategories) {
                                        IconButton(onClick = {
                                            coroutineScope.launch {
                                                val count = onGetBookCount(category)
                                                pendingDeleteBookCount = count
                                                pendingDeleteCategory = category
                                            }
                                        }) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove Custom Category", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                    androidx.compose.material3.Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = if (isOther) null else { checked -> onToggleCategory(category, checked) },
                                        enabled = !isOther
                                    )
                                }
                            }
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newCategoryText,
                                    onValueChange = {
                                        newCategoryText = it
                                        newCategoryError = null
                                    },
                                    placeholder = { Text("New Custom Category") },
                                    singleLine = true,
                                    isError = newCategoryError != null,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Button(
                                    onClick = {
                                        val cleaned = newCategoryText.replace("|", "").trim()
                                        if (cleaned.isBlank()) {
                                            newCategoryError = "Please enter a valid category name."
                                        } else if (allCategories.any { it.equals(cleaned, ignoreCase = true) }) {
                                            newCategoryError = "Category \"$cleaned\" already exists."
                                        } else {
                                            newCategoryError = null
                                            onAddCustomCategory(cleaned) { err ->
                                                newCategoryError = err
                                            }
                                            newCategoryText = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Add")
                                }
                            }
                            if (newCategoryError != null) {
                                Text(
                                    text = newCategoryError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = onDismiss) { Text("Done") }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteBookConfirmationDialog(
    bookWithStats: com.churchbooklibrary.core.database.BookWithStats,
    activeLoan: LoanEntity?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val book = bookWithStats.book
    val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    val isLoaned = activeLoan != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Book?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isLoaned) {
                    Text(
                        "WARNING: This book is currently loaned out. Deleting it will permanently remove it and its active loan from the library.",
                        color = Red600,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text("Are you sure you want to permanently delete this book and its loan history? This cannot be undone.")
                }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate100),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        BookCoverImage(
                            title = book.title, author = book.author, coverUrl = book.coverUrl,
                            modifier = Modifier.size(50.dp, 75.dp).clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(book.title, fontWeight = FontWeight.Bold)
                            Text(book.author, style = MaterialTheme.typography.bodySmall, color = Slate600)
                            Spacer(Modifier.height(4.dp))
                            Text("Total Borrows: ${bookWithStats.borrowCount}", style = MaterialTheme.typography.labelSmall, color = Slate600)
                            if (bookWithStats.lastBorrowedDate != null) {
                                val lastBorrowedStr = dateFormat.format(Date(bookWithStats.lastBorrowedDate))
                                Text("Last Borrowed: $lastBorrowedStr", style = MaterialTheme.typography.labelSmall, color = Slate600)
                            } else {
                                Text("Last Borrowed: Never", style = MaterialTheme.typography.labelSmall, color = Slate600)
                            }
                        }
                    }
                }

                if (activeLoan != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Red100.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, Red600.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Borrower Information", fontWeight = FontWeight.Bold, color = Red600, style = MaterialTheme.typography.labelLarge)
                            Text("Name: ${activeLoan.borrowerName}", style = MaterialTheme.typography.bodyMedium)
                            Text("Email: ${activeLoan.borrowerEmail.ifBlank { "Not provided" }}", style = MaterialTheme.typography.bodyMedium)
                            Text("Phone: ${activeLoan.borrowerPhone.ifBlank { "Not provided" }}", style = MaterialTheme.typography.bodyMedium)
                            
                            val borrowedStr = dateFormat.format(Date(activeLoan.borrowDate))
                            val overdueMillis = System.currentTimeMillis() - activeLoan.dueDate
                            val isOverdue = overdueMillis > 0
                            Text("Borrowed: $borrowedStr", style = MaterialTheme.typography.bodyMedium)
                            if (isOverdue) {
                                val daysOverdue = (overdueMillis / (1000L * 60 * 60 * 24)).coerceAtLeast(0)
                                val overdueStr = when {
                                    daysOverdue > 365 -> "${daysOverdue / 365} year(s)"
                                    daysOverdue > 30 -> "${daysOverdue / 30} month(s)"
                                    else -> "$daysOverdue day(s)"
                                }
                                Text("Status: Overdue by $overdueStr", style = MaterialTheme.typography.bodyMedium, color = Red600, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Red600)) { Text("Delete Permanently") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun BookInventoryRow(
    bookWithStats: com.churchbooklibrary.core.database.BookWithStats,
    isDemoMode: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDemoActionBlocked: ((String) -> Unit)? = null
) {
    val book = bookWithStats.book
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BookCoverImage(
                title = book.title,
                author = book.author,
                coverUrl = book.coverUrl,
                modifier = Modifier
                    .size(width = 40.dp, height = 60.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(book.title, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(book.author, style = MaterialTheme.typography.bodySmall, color = Slate600)
                if (!book.owner.isNullOrBlank()) {
                    Text("Consignor: ${book.owner}", style = MaterialTheme.typography.labelSmall, color = Slate600)
                }
                Text("Total Borrows: ${bookWithStats.borrowCount}", style = MaterialTheme.typography.labelSmall, color = Slate400)
            }

            IconButton(
                onClick = {
                    if (isDemoMode) {
                        onDemoActionBlocked?.invoke("It is not possible to modify books in Demo mode. Turn off Demo mode to manage your own books.")
                    } else {
                        onEdit()
                    }
                }
            ) {
                Icon(Icons.Default.Edit, "Edit book", tint = if (isDemoMode) Slate400 else Slate600)
            }
            IconButton(
                onClick = {
                    if (isDemoMode) {
                        onDemoActionBlocked?.invoke("It is not possible to modify books in Demo mode. Turn off Demo mode to manage your own books.")
                    } else {
                        onDelete()
                    }
                }
            ) {
                Icon(Icons.Default.Delete, "Delete book", tint = if (isDemoMode) Slate400 else Red600)
            }
        }
    }
}

// ─── Borrow Periods Editor ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BorrowPeriodsEditor(
    periods: List<com.churchbooklibrary.core.datastore.BorrowPeriod>,
    isDemoMode: Boolean,
    onSave: (List<com.churchbooklibrary.core.datastore.BorrowPeriod>) -> Unit
) {
    val defaults = com.churchbooklibrary.core.datastore.BorrowPeriod.DEFAULTS
    val maxCount = com.churchbooklibrary.core.datastore.BorrowPeriod.MAX_COUNT
    val unitLabels = mapOf(
        com.churchbooklibrary.core.datastore.BorrowPeriodUnit.DAYS to "Days",
        com.churchbooklibrary.core.datastore.BorrowPeriodUnit.WEEKS to "Weeks",
        com.churchbooklibrary.core.datastore.BorrowPeriodUnit.MONTHS to "Months"
    )
    val countInputs = remember(periods) { mutableStateListOf(*periods.map { it.count.toString() }.toTypedArray()) }
    val unitInputs = remember(periods) { mutableStateListOf(*periods.map { it.unit }.toTypedArray()) }

    val parsed = countInputs.mapIndexed { i, text ->
        text.toIntOrNull()?.takeIf { it in 1..maxCount }?.let {
            com.churchbooklibrary.core.datastore.BorrowPeriod(it, unitInputs[i])
        }
    }
    val allValid = parsed.all { it != null }
    val hasDuplicates = allValid && parsed.filterNotNull().map { it.value }.distinct().size != parsed.size
    val changed = allValid && parsed.filterNotNull() != periods
    val canSave = !isDemoMode && allValid && !hasDuplicates && changed

    Text("Borrow Periods", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(4.dp))
    Text(
        "The four loan lengths borrowers can choose from at checkout (e.g. 6 Months). Loans already out are not affected.",
        style = MaterialTheme.typography.bodySmall,
        color = Slate600
    )
    Spacer(Modifier.height(8.dp))

    for (i in countInputs.indices) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text("Option ${i + 1}", modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = countInputs[i],
                onValueChange = { countInputs[i] = it.filter { c -> c.isDigit() }.take(2) },
                modifier = Modifier.width(90.dp),
                singleLine = true,
                enabled = !isDemoMode,
                isError = parsed[i] == null,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )
            Spacer(Modifier.width(8.dp))
            unitLabels.forEach { (unit, label) ->
                FilterChip(
                    selected = unitInputs[i] == unit,
                    onClick = { unitInputs[i] = unit },
                    enabled = !isDemoMode,
                    label = { Text(label) },
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }
    }

    if (!allValid) {
        Text("Each option must be a number from 1 to $maxCount.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    } else if (hasDuplicates) {
        Text("The four options must all be different.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }

    Spacer(Modifier.height(8.dp))
    Row {
        Button(
            onClick = { onSave(parsed.filterNotNull()) },
            colors = ButtonDefaults.buttonColors(containerColor = Blue600),
            enabled = canSave
        ) { Text("Save Periods") }
        Spacer(Modifier.width(12.dp))
        OutlinedButton(
            onClick = { onSave(defaults) },
            enabled = !isDemoMode && periods != defaults
        ) { Text("Reset to Defaults") }
    }
}

// ─── Backup Panel ─────────────────────────────────────────────────────────────

@Composable
private fun BackupPanel(
    libraryName: String,
    ribbonColor: Long,
    maxBorrowLimit: Int,
    isDemoMode: Boolean,
    syncStatus: String,
    syncRole: Int,
    syncDayOfWeek: Int,
    syncStartHour: Int,
    syncEndHour: Int,
    onSetSyncRole: (Int) -> Unit,
    onUpdateSyncSchedule: (Int, Int, Int) -> Unit,
    onStartTabletSync: () -> Unit,
    onStopTabletSync: () -> Unit,
    onStartPhoneSync: () -> Unit,
    onStopPhoneSync: () -> Unit,
    onToggleDemoMode: (Boolean) -> Unit,
    onSaveLibraryName: (String) -> Unit,
    onSaveRibbonColor: (Long) -> Unit,
    onSaveMaxBorrowLimit: (Int) -> Unit,
    borrowPeriods: List<com.churchbooklibrary.core.datastore.BorrowPeriod>,
    onSaveBorrowPeriods: (List<com.churchbooklibrary.core.datastore.BorrowPeriod>) -> Unit,
    hasCustomLogo: Boolean,
    logoLastModified: Long,
    categoriesEnabled: Boolean,
    enabledCategories: Set<String>,
    customCategories: Set<String>,
    onOpenCategorySettings: () -> Unit,
    onUploadLogo: () -> Unit,
    onRemoveLogo: () -> Unit,
    onExport: () -> Unit,
    onExportShare: () -> Unit,
    onImport: () -> Unit,
    onOpenCatalogExport: () -> Unit,
    onChangePin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Library Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Customize the app appearance and identity.",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate600
        )
        Spacer(Modifier.height(16.dp))

        val context = LocalContext.current
        var nameInput by remember(libraryName) { mutableStateOf(libraryName) }
        val isNameValid = nameInput.trim().isNotEmpty()
        val canSaveName = !isDemoMode && isNameValid && nameInput.trim() != libraryName.trim()

        Card(
            modifier = Modifier.widthIn(max = 800.dp).fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Library Name", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !isDemoMode,
                        isError = !isNameValid,
                        supportingText = if (!isNameValid) {
                            { Text("Library name cannot be blank.", color = MaterialTheme.colorScheme.error) }
                        } else null
                    )
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = { onSaveLibraryName(nameInput.trim()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                        enabled = canSaveName,
                        modifier = Modifier.padding(top = 8.dp)
                    ) { Text("Save Name") }
                }

                Spacer(Modifier.height(24.dp))

                var limitInput by remember(maxBorrowLimit) { mutableStateOf(maxBorrowLimit.toString()) }
                val parsedLimit = limitInput.toIntOrNull()
                val isLimitValid = parsedLimit != null && parsedLimit in 1..20
                val canSaveLimit = !isDemoMode && isLimitValid && parsedLimit != maxBorrowLimit

                Text("Max Borrow Limit", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("Maximum number of different books a single person can borrow at once.", style = MaterialTheme.typography.bodySmall, color = Slate600)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { limitInput = it.filter { char -> char.isDigit() } },
                        modifier = Modifier.width(140.dp),
                        singleLine = true,
                        enabled = !isDemoMode,
                        isError = !isLimitValid,
                        supportingText = if (!isLimitValid) {
                            { Text("Must be 1–20", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = { 
                            parsedLimit?.let { 
                                if (it in 1..20) onSaveMaxBorrowLimit(it) 
                            } 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                        enabled = canSaveLimit,
                        modifier = Modifier.padding(top = 8.dp)
                    ) { Text("Save Limit") }
                }

                Spacer(Modifier.height(24.dp))

                BorrowPeriodsEditor(
                    periods = borrowPeriods,
                    isDemoMode = isDemoMode,
                    onSave = onSaveBorrowPeriods
                )

                Spacer(Modifier.height(24.dp))

                Text("Book Categories", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("Organize books into shelves on the browse and checkout screens.", style = MaterialTheme.typography.bodySmall, color = Slate600)
                Spacer(Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Slate200),
                    colors = CardDefaults.cardColors(containerColor = Slate50)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (categoriesEnabled) "Categories Enabled" else "Categories Disabled",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate800
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (categoriesEnabled) {
                                    "${enabledCategories.size} active categories (${customCategories.size} custom)"
                                } else {
                                    "Categories are turned off. Books appear in a single catalog list."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        OutlinedButton(
                            onClick = onOpenCategorySettings,
                            enabled = !isDemoMode
                        ) {
                            Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Category Settings")
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                
                var showRemoveLogoDialog by remember { mutableStateOf(false) }

                if (showRemoveLogoDialog) {
                    AlertDialog(
                        onDismissRequest = { showRemoveLogoDialog = false },
                        title = { Text("Reset to Default Logo?") },
                        text = { Text("Are you sure you want to remove the custom logo and restore the default book icon on the home screen?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showRemoveLogoDialog = false
                                    onRemoveLogo()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Red600)
                            ) {
                                Text("Remove Logo")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showRemoveLogoDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                Text("Custom Logo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("Upload a custom logo to replace the default book icon on the home screen.", style = MaterialTheme.typography.bodySmall, color = Slate600)
                Spacer(Modifier.height(12.dp))

                val logoFile = remember(logoLastModified) { File(context.filesDir, "logo.png") }
                var isPreviewCorrupted by remember(logoLastModified, hasCustomLogo) { mutableStateOf(false) }
                val isPreviewValid = hasCustomLogo && !isPreviewCorrupted && logoFile.exists() && logoFile.length() > 0L

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Slate200),
                    colors = CardDefaults.cardColors(containerColor = Slate50)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isPreviewValid) Color.White else Color(ribbonColor))
                                .border(1.dp, Slate200, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPreviewValid) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(logoFile)
                                        .memoryCacheKey("preview_logo_$logoLastModified")
                                        .diskCacheKey("preview_logo_$logoLastModified")
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Current Logo Preview",
                                    contentScale = ContentScale.Crop,
                                    onError = {
                                        isPreviewCorrupted = true
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (hasCustomLogo && !isPreviewCorrupted) "Custom Logo Active"
                                       else if (hasCustomLogo && isPreviewCorrupted) "Logo File Corrupted"
                                       else "Default Book Icon Active",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (hasCustomLogo && isPreviewCorrupted) Red600 else Slate800
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (hasCustomLogo && !isPreviewCorrupted) "Displayed on the home screen header."
                                       else if (hasCustomLogo && isPreviewCorrupted) "Could not decode logo image. Showing default book icon."
                                       else "Upload an image (PNG, JPEG, WebP) to personalize the home screen.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(onClick = onUploadLogo, enabled = !isDemoMode) {
                                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (hasCustomLogo) "Change Logo" else "Upload Logo")
                                }

                                if (hasCustomLogo) {
                                    OutlinedButton(
                                        onClick = { showRemoveLogoDialog = true },
                                        enabled = !isDemoMode,
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Red600),
                                        border = BorderStroke(1.dp, Red100)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Remove Logo")
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text("App Theme Color", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("Select a color for the top ribbon and buttons throughout the app.", style = MaterialTheme.typography.bodySmall, color = Slate600)
                Spacer(Modifier.height(12.dp))

                val presets = listOf(
                    0xFF1E3A8AL, 0xFF991B1B, 0xFF166534, 0xFF6B21A8, 0xFFC2410C,
                    0xFF0F766E, 0xFFBE185D, 0xFF78350F, 0xFF374151, 0xFF111827
                )
                
                var showColorPicker by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { presetColor ->
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = Color(presetColor),
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { onSaveRibbonColor(presetColor) },
                            border = if (presetColor == ribbonColor) BorderStroke(3.dp, Color.Black) else null
                        ) {}
                    }
                    
                    // Custom Color Button
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color.White,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { showColorPicker = true },
                        border = BorderStroke(1.dp, Slate400)
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = "Custom Color", modifier = Modifier.padding(8.dp), tint = Slate600)
                    }
                }
                
                if (showColorPicker) {
                    var r by remember { mutableStateOf(((ribbonColor shr 16) and 0xFF).toFloat()) }
                    var g by remember { mutableStateOf(((ribbonColor shr 8) and 0xFF).toFloat()) }
                    var b by remember { mutableStateOf((ribbonColor and 0xFF).toFloat()) }
                    
                    AlertDialog(
                        onDismissRequest = { showColorPicker = false },
                        title = { Text("Pick Custom Color") },
                        text = {
                            Column {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().height(80.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(android.graphics.Color.rgb(r.toInt(), g.toInt(), b.toInt()))
                                ) {}
                                Spacer(Modifier.height(16.dp))
                                Text("Red: ${r.toInt()}")
                                androidx.compose.material3.Slider(value = r, onValueChange = { r = it }, valueRange = 0f..255f)
                                Text("Green: ${g.toInt()}")
                                androidx.compose.material3.Slider(value = g, onValueChange = { g = it }, valueRange = 0f..255f)
                                Text("Blue: ${b.toInt()}")
                                androidx.compose.material3.Slider(value = b, onValueChange = { b = it }, valueRange = 0f..255f)
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { 
                                val newColor = (0xFFL shl 24) or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()
                                onSaveRibbonColor(newColor)
                                showColorPicker = false
                            }) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showColorPicker = false }) { Text("Cancel") }
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        
        val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "demo_pulse")
        val demoCardColor by infiniteTransition.animateColor(
            initialValue = Color.White,
            targetValue = if (isDemoMode) Color(0xFFFEF3C7) else Color.White,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(1000, easing = androidx.compose.animation.core.LinearEasing),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            ),
            label = "demo_color"
        )

        Card(
            modifier = Modifier.widthIn(max = 800.dp).fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = demoCardColor),
            border = BorderStroke(2.dp, if (isDemoMode) Color(0xFFF59E0B) else Color.Transparent),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Demo Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("Enable demo mode to safely explore the app with 25 sample books. Your real library will be hidden but kept safe.", style = MaterialTheme.typography.bodySmall, color = Slate600)
                }
                Spacer(Modifier.width(16.dp))
                Button(
                    onClick = { onToggleDemoMode(!isDemoMode) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDemoMode) Color(0xFFD97706) else Blue600
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isDemoMode) "Disable Demo Mode" else "Enable Demo Mode")
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "Offline Redundancy & Sync",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Backup your library data or restore from a previous export.",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate600
        )
        Spacer(Modifier.height(32.dp))

        // Offline P2P Sync Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth().widthIn(max = 800.dp).padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                var showSyncInstructions by remember { mutableStateOf(false) }
                var showSyncExplanationDialog by remember { mutableStateOf(false) }
                var pendingSyncAction by remember { mutableStateOf<(() -> Unit)?>(null) }
                var showPermissionRationaleDialog by remember { mutableStateOf(false) }

                val syncPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissionsMap ->
                    val allGranted = permissionsMap.values.all { it }
                    if (allGranted) {
                        pendingSyncAction?.invoke()
                    }
                    pendingSyncAction = null
                }

                val runWithSyncPermissions: (() -> Unit) -> Unit = { action ->
                    if (com.churchbooklibrary.core.sync.SyncPermissionHelper.hasAllRequiredPermissions(context)) {
                        action()
                    } else {
                        pendingSyncAction = action
                        showPermissionRationaleDialog = true
                    }
                }

                if (showPermissionRationaleDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showPermissionRationaleDialog = false
                            pendingSyncAction = null
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        },
                        title = {
                            Text("Nearby Permissions Needed", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        },
                        text = {
                            Text(
                                "Offline sync uses local Bluetooth and Wi-Fi to securely copy catalog records between your church tablet and librarian phones without needing internet.\n\nAndroid will ask for Nearby Devices and Bluetooth permissions to allow this device-to-device connection.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate800
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showPermissionRationaleDialog = false
                                    val missing = com.churchbooklibrary.core.sync.SyncPermissionHelper.getMissingPermissions(context)
                                    syncPermissionLauncher.launch(missing.toTypedArray())
                                }
                            ) {
                                Text("Grant Permissions")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showPermissionRationaleDialog = false
                                    pendingSyncAction = null
                                }
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                if (showSyncExplanationDialog) {
                    AlertDialog(
                        onDismissRequest = { showSyncExplanationDialog = false },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        },
                        title = {
                            Text(
                                text = "How Church Sync Works",
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier.verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "Peer-to-peer sync automatically creates a safe, offline backup of your church catalog and active loans on a volunteer's phone without needing any internet connection or cloud service.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Slate800
                                )

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFFEF3C7)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text("📍 Physical Proximity (Within ~5 Feet)", fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color(0xFF92400E))
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Sync uses direct device-to-device wireless (Bluetooth & Wi-Fi Direct). You must be in the same room, within about 5 feet of the tablet, for the phone to connect and sync. It will not sync from home or across the church building.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = androidx.compose.ui.graphics.Color(0xFF78350F)
                                        )
                                    }
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Slate100),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text("📱 Main Church Tablet (Kiosk Role)", fontWeight = FontWeight.Bold, color = Blue600)
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Turn on 'Kiosk Role' on the main library tablet and set your Sunday schedule (e.g. Sunday 8 AM to 1 PM).\n\nDuring this window, while the tablet sits active on the desk for church members, it automatically broadcasts the database wirelessly. You never have to touch, unlock, or disrupt the tablet on Sunday morning.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate700
                                        )
                                    }
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Slate100),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text("📲 Librarian's Personal Phone (Librarian Role)", fontWeight = FontWeight.Bold, color = Blue600)
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Turn on 'Librarian Role' on your phone with the same schedule.\n\nOn Sunday morning, your phone sends you a notification reminder: 'Library Backup Scheduled'. When you arrive at church and stand near the library desk (within ~5 feet), simply tap the notification.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate700
                                        )
                                    }
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Slate100),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text("⚡ Fast 2-Second Transfer", fontWeight = FontWeight.Bold, color = Blue600)
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Tapping the notification opens the app and connects to the nearby kiosk tablet. In 2–3 seconds, all records are transferred to your phone, and broadcasting stops automatically for the day. Both devices must have Bluetooth and Wi-Fi enabled in Android settings.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate700
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showSyncExplanationDialog = false }) {
                                Text("Got It")
                            }
                        }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Peer-to-Peer Offline Sync", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { showSyncExplanationDialog = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Info, contentDescription = "How this works", tint = Blue600)
                    }
                }
                Spacer(Modifier.height(12.dp))
                
                // Instructions Block
                if (showSyncInstructions) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFEFF6FF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.Info, contentDescription = "Instructions", tint = androidx.compose.ui.graphics.Color(0xFF2563EB), modifier = Modifier.size(24.dp))
                            Column {
                                Text("Why regular syncing matters:", fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color(0xFF1D4ED8), style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(4.dp))
                                Text("""Tablets can fail, break, or be stolen. Regular peer-to-peer syncing ensures your library database (books and current loans) is safely backed up to a secondary device without needing internet access.

1. Physical Proximity: You must be within about 5 feet of the tablet (in the same room) with Bluetooth & Wi-Fi enabled on both devices.

2. On the Tablet: Toggle 'Kiosk Role' on and set your window. It will silently broadcast during this time while open on the library desk.

3. On the Phone: Toggle 'Librarian Role' on with the same window. When you arrive at church near the desk, tap the reminder notification to sync in 2–3 seconds.""", 
                                    style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color(0xFF1D4ED8))
                            }
                        }
                    }
                }
                
                Text("Status: $syncStatus", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                
                // --- SCHEDULE SETTINGS ---
                if (syncRole > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Auto-Sync Schedule Window", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.width(6.dp))
                                IconButton(onClick = { showSyncExplanationDialog = true }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Info, contentDescription = "How sync works", tint = Blue600, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            
                            val days = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                            val hours = (0..23).map { if (it == 0) "12 AM" else if (it < 12) "$it AM" else if (it == 12) "12 PM" else "${it-12} PM" }
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Day Dropdown (Simplified for UI, normally use ExposedDropdownMenu)
                                // Since we don't have a built-in clean spinner without extra state, we'll use simple text clicks or basic drop down
                                Box(modifier = Modifier.weight(1f)) {
                                    var dayExpanded by remember { mutableStateOf(false) }
                                    OutlinedButton(onClick = { dayExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                        Text(days[syncDayOfWeek - 1])
                                    }
                                    DropdownMenu(expanded = dayExpanded, onDismissRequest = { dayExpanded = false }) {
                                        days.forEachIndexed { index, day ->
                                            DropdownMenuItem(
                                                text = { Text(day) },
                                                onClick = {
                                                    onUpdateSyncSchedule(index + 1, syncStartHour, syncEndHour)
                                                    dayExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.weight(1f)) {
                                    var startExpanded by remember { mutableStateOf(false) }
                                    OutlinedButton(onClick = { startExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                        Text("Start: ${hours[syncStartHour]}")
                                    }
                                    DropdownMenu(expanded = startExpanded, onDismissRequest = { startExpanded = false }) {
                                        hours.forEachIndexed { index, hour ->
                                            DropdownMenuItem(text = { Text(hour) }, onClick = { onUpdateSyncSchedule(syncDayOfWeek, index, syncEndHour); startExpanded = false })
                                        }
                                    }
                                }
                                Text("to")
                                Box(modifier = Modifier.weight(1f)) {
                                    var endExpanded by remember { mutableStateOf(false) }
                                    OutlinedButton(onClick = { endExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                        Text("End: ${hours[syncEndHour]}")
                                    }
                                    DropdownMenu(expanded = endExpanded, onDismissRequest = { endExpanded = false }) {
                                        hours.forEachIndexed { index, hour ->
                                            DropdownMenuItem(text = { Text(hour) }, onClick = { onUpdateSyncSchedule(syncDayOfWeek, syncStartHour, index); endExpanded = false })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Kiosk Role (Tablet)", fontWeight = FontWeight.Bold)
                        Text(if (syncRole == 1) "Broadcasts during the active schedule until synced." else "Enable this on the main library tablet.", style = MaterialTheme.typography.bodySmall, color = Slate600)
                    }
                    Switch(
                        checked = syncRole == 1, 
                        onCheckedChange = { if (it) runWithSyncPermissions { onSetSyncRole(1) } else onSetSyncRole(0) },
                        colors = SwitchDefaults.colors(
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Slate400,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
                if (syncRole == 1) {
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { runWithSyncPermissions { onStartTabletSync() } }, modifier = Modifier.weight(1f)) { Text("Test Broadcast Now") }
                        OutlinedButton(onClick = onStopTabletSync, modifier = Modifier.weight(1f)) { Text("Stop") }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Librarian Role (Phone)", fontWeight = FontWeight.Bold)
                        Text(if (syncRole == 2) "Sends 3 notification reminders during the active schedule." else "Enable this on your personal phone.", style = MaterialTheme.typography.bodySmall, color = Slate600)
                    }
                    Switch(
                        checked = syncRole == 2, 
                        onCheckedChange = { if (it) runWithSyncPermissions { onSetSyncRole(2) } else onSetSyncRole(0) },
                        colors = SwitchDefaults.colors(
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Slate400,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
                if (syncRole == 2) {
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { runWithSyncPermissions { onStartPhoneSync() } }, modifier = Modifier.weight(1f)) { Text("Scan & Sync Now") }
                        OutlinedButton(onClick = onStopPhoneSync, modifier = Modifier.weight(1f)) { Text("Stop") }
                    }
                }
            }
        }


        val configuration = LocalConfiguration.current
        val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        if (isPortrait) {
            Column(
                modifier = Modifier.widthIn(max = 500.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                BackupCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Export & Share",
                    description = "Save a JSON backup of all books and loans to your device or cloud storage.",
                    icon = Icons.Default.Share,
                    iconBg = Blue100,
                    iconColor = Blue600,
                    buttonText = "Export to This Device",
                    buttonColor = Blue600,
                    enabled = !isDemoMode,
                    onClick = onExport,
                    secondaryButtonText = "Share to Other Device",
                    onSecondaryClick = onExportShare
                )
                BackupCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Import Backup",
                    description = "Restore books from a previously exported JSON file. Existing books are kept.",
                    icon = Icons.Default.Download,
                    iconBg = Green100,
                    iconColor = Green700,
                    buttonText = "Import Backup",
                    buttonColor = Green700,
                    enabled = !isDemoMode,
                    onClick = onImport
                )
                BackupCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Change Admin PIN",
                    description = "Update the PIN required to access the admin dashboard. The backup PIN 7777777 always works.",
                    icon = Icons.Default.Lock,
                    iconBg = Slate100,
                    iconColor = Slate600,
                    buttonText = "Change PIN",
                    buttonColor = Slate700,
                    enabled = true,
                    onClick = onChangePin
                )
            }
        } else {
            Row(
                modifier = Modifier.widthIn(max = 800.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                BackupCard(
                    modifier = Modifier.weight(1f),
                    title = "Export & Share",
                    description = "Save a JSON backup of all books and loans to your device or cloud storage.",
                    icon = Icons.Default.Share,
                    iconBg = Blue100,
                    iconColor = Blue600,
                    buttonText = "Export to This Device",
                    buttonColor = Blue600,
                    enabled = !isDemoMode,
                    onClick = onExport,
                    secondaryButtonText = "Share to Other Device",
                    onSecondaryClick = onExportShare
                )
                BackupCard(
                    modifier = Modifier.weight(1f),
                    title = "Import Backup",
                    description = "Restore books from a previously exported JSON file. Existing books are kept.",
                    icon = Icons.Default.Download,
                    iconBg = Green100,
                    iconColor = Green700,
                    buttonText = "Import Backup",
                    buttonColor = Green700,
                    enabled = !isDemoMode,
                    onClick = onImport
                )
                BackupCard(
                    modifier = Modifier.weight(1f),
                    title = "Change Admin PIN",
                    description = "Update the PIN required to access the admin dashboard. The backup PIN 7777777 always works.",
                    icon = Icons.Default.Lock,
                    iconBg = Slate100,
                    iconColor = Slate600,
                    buttonText = "Change PIN",
                    buttonColor = Slate700,
                    enabled = true,
                    onClick = onChangePin
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        BackupCard(
            modifier = Modifier.widthIn(max = if (isPortrait) 500.dp else 800.dp).fillMaxWidth(),
            title = "Export Catalog for Website",
            description = "Choose which details to include (e.g. just title and author) and export the book list as CSV, JSON or an HTML table for your church website.",
            icon = Icons.Default.Share,
            iconBg = Green100,
            iconColor = Green700,
            buttonText = "Choose Fields & Export",
            buttonColor = Green700,
            enabled = !isDemoMode,
            onClick = onOpenCatalogExport
        )
    }
}

@Composable
private fun CatalogExportDialog(
    onDismiss: () -> Unit,
    onSave: (Set<com.churchbooklibrary.core.backup.CatalogField>, com.churchbooklibrary.core.backup.CatalogFormat) -> Unit,
    onShare: (Set<com.churchbooklibrary.core.backup.CatalogField>, com.churchbooklibrary.core.backup.CatalogFormat) -> Unit
) {
    var selectedFields by rememberSaveable(
        stateSaver = androidx.compose.runtime.saveable.listSaver(
            save = { set -> set.map { it.name } },
            restore = { list -> list.map { com.churchbooklibrary.core.backup.CatalogField.valueOf(it) }.toSet() }
        )
    ) { mutableStateOf(com.churchbooklibrary.core.backup.CatalogField.DEFAULT_SELECTION) }
    var format by rememberSaveable { mutableStateOf(com.churchbooklibrary.core.backup.CatalogFormat.CSV) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Catalog for Website", fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("Include these details:", fontWeight = FontWeight.SemiBold)
                com.churchbooklibrary.core.backup.CatalogField.entries.forEach { field ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedFields = if (field in selectedFields) selectedFields - field else selectedFields + field
                            }
                    ) {
                        androidx.compose.material3.Checkbox(
                            checked = field in selectedFields,
                            onCheckedChange = {
                                selectedFields = if (it) selectedFields + field else selectedFields - field
                            }
                        )
                        Column {
                            Text(field.label, style = MaterialTheme.typography.bodyMedium)
                            Text(field.hint, style = MaterialTheme.typography.bodySmall, color = Slate600)
                        }
                    }
                }
                if (selectedFields.isEmpty()) {
                    Text("Select at least one detail.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(16.dp))
                Text("File format:", fontWeight = FontWeight.SemiBold)
                com.churchbooklibrary.core.backup.CatalogFormat.entries.forEach { f ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { format = f }
                    ) {
                        androidx.compose.material3.RadioButton(selected = format == f, onClick = { format = f })
                        Column {
                            Text(f.label, style = MaterialTheme.typography.bodyMedium)
                            Text(f.description, style = MaterialTheme.typography.bodySmall, color = Slate600)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = { onShare(selectedFields, format) }, enabled = selectedFields.isNotEmpty()) { Text("Share") }
                Button(
                    onClick = { onSave(selectedFields, format) },
                    enabled = selectedFields.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                ) { Text("Save to Device") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun BackupCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color,
    buttonText: String,
    buttonColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    secondaryButtonText: String? = null,
    onSecondaryClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(shape = RoundedCornerShape(12.dp), color = iconBg.copy(alpha = if (enabled) 1f else 0.5f), modifier = Modifier.size(56.dp)) {
                Icon(icon, null, tint = iconColor.copy(alpha = if (enabled) 1f else 0.5f), modifier = Modifier.padding(14.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = if (enabled) Color.Unspecified else Slate400)
            Spacer(Modifier.height(6.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = if (enabled) Slate600 else Slate400)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(10.dp)
            ) { Text(buttonText) }
            
            if (secondaryButtonText != null && onSecondaryClick != null) {
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = onSecondaryClick,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) { Text(secondaryButtonText) }
            }
        }
    }
}

// ─── Dialogs ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditBookDialog(
    book: BookEntity,
    onDismiss: () -> Unit,
    onSave: (BookEntity) -> Unit,
    categoriesEnabled: Boolean = false,
    availableCategories: List<String> = emptyList()
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(book.title) }
    var author by remember { mutableStateOf(book.author) }
    var isbn by remember { mutableStateOf(book.isbn ?: "") }
    var owner by remember { mutableStateOf(book.owner ?: "") }
    var coverUrl by remember { mutableStateOf(book.coverUrl ?: "") }
    var selectedCategory by remember { mutableStateOf(book.category ?: "") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val file = File(context.filesDir, "cover_${book.id}_${System.currentTimeMillis()}.jpg")
                    file.outputStream().use { output -> inputStream.copyTo(output) }
                    coverUrl = file.absolutePath
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val file = File(context.filesDir, "cover_${book.id}_${System.currentTimeMillis()}.jpg")
                file.outputStream().use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
                }
                coverUrl = file.absolutePath
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp)
                ) {
                    Text("Edit Book", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Image and Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            modifier = Modifier.width(100.dp).height(150.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            BookCoverImage(
                                title = title,
                                author = author,
                                coverUrl = if (coverUrl.isNotBlank()) { if (coverUrl.startsWith("/")) "file://$coverUrl" else coverUrl } else null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Button(
                                onClick = { pickImageLauncher.launch("image/*") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Gallery")
                            }
                            Button(
                                onClick = { takePictureLauncher.launch(null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Camera")
                            }
                            if (coverUrl.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { coverUrl = "" },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Remove Cover")
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    val reqColors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        unfocusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                    
                    val optColors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        unfocusedLabelColor = MaterialTheme.colorScheme.secondary
                    )

                    OutlinedTextField(
                        value = title, onValueChange = { title = it },
                        label = { Text("Title *") }, singleLine = true,
                        colors = reqColors,
                        shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = author, onValueChange = { author = it },
                        label = { Text("Author *") }, singleLine = true,
                        colors = reqColors,
                        shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Optional Details", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)

                    OutlinedTextField(
                        value = isbn, onValueChange = { isbn = it },
                        label = { Text("ISBN (Optional)") }, singleLine = true,
                        colors = optColors,
                        shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = owner, onValueChange = { owner = it },
                        label = { Text("Owner / Consignor (Optional)") }, singleLine = true,
                        colors = optColors,
                        shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = coverUrl, onValueChange = { coverUrl = it },
                        label = { Text("Cover URL (Optional)") }, singleLine = true,
                        colors = optColors,
                        shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                    )

                    if (categoriesEnabled && availableCategories.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCategory.ifBlank { "Select category..." },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                colors = optColors,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                availableCategories.forEach { cat ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            selectedCategory = cat
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Footer Buttons
                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank() && author.isNotBlank()) {
                                    val finalIsbn = isbn.trim().takeIf { it.isNotBlank() }
                                    val finalOwner = owner.trim().takeIf { it.isNotBlank() }
                                    val finalCover = coverUrl.trim().takeIf { it.isNotBlank() }
                                    val finalCategory = selectedCategory.trim().takeIf { it.isNotBlank() }
                                    onSave(book.copy(
                                        title = title.trim(), 
                                        author = author.trim(), 
                                        isbn = finalIsbn, 
                                        owner = finalOwner,
                                        coverUrl = finalCover,
                                        category = finalCategory
                                    ))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                        ) { Text("Save Changes") }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBookDialog(
    initialIsbn: String = "",
    categoriesEnabled: Boolean = false,
    availableCategories: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (String, String, String?, String?, Int, String?, String?) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var isbn by remember(initialIsbn) { mutableStateOf(initialIsbn) }
    var owner by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf("") }
    var copiesText by remember { mutableStateOf("1") }
    var selectedCategory by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val file = File(context.filesDir, "cover_new_${System.currentTimeMillis()}.jpg")
                    file.outputStream().use { output -> inputStream.copyTo(output) }
                    coverUrl = file.absolutePath
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val file = File(context.filesDir, "cover_new_${System.currentTimeMillis()}.jpg")
                file.outputStream().use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
                }
                coverUrl = file.absolutePath
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Book") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        modifier = Modifier.width(75.dp).height(110.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        BookCoverImage(
                            title = title.ifBlank { "Book" },
                            author = author.ifBlank { "Author" },
                            coverUrl = coverUrl.takeIf { it.isNotBlank() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { pickImageLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Gallery", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { takePictureLauncher.launch(null) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Camera", fontSize = 11.sp)
                            }
                        }
                        if (coverUrl.isNotBlank()) {
                            TextButton(
                                onClick = { coverUrl = "" },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Remove Photo", fontSize = 11.sp)
                            }
                        } else {
                            Text(
                                "Take or upload a cover photo (takes precedence over online covers).",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Title *") }, singleLine = true,
                    shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = author, onValueChange = { author = it },
                    label = { Text("Author *") }, singleLine = true,
                    shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = isbn, onValueChange = { isbn = it },
                    label = { Text("ISBN (Optional)") }, singleLine = true,
                    shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = owner, onValueChange = { owner = it },
                    label = { Text("Owner / Consignor (Optional)") }, singleLine = true,
                    shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                )

                if (categoriesEnabled && availableCategories.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory.ifBlank { "Select Category (Optional)" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category (Optional)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Auto-categorize / Default") },
                                onClick = {
                                    selectedCategory = ""
                                    categoryExpanded = false
                                }
                            )
                            availableCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && author.isNotBlank()) {
                        onSave(
                            title,
                            author,
                            isbn.takeIf { it.isNotBlank() },
                            owner.takeIf { it.isNotBlank() },
                            copiesText.toIntOrNull() ?: 1,
                            coverUrl.takeIf { it.isNotBlank() },
                            selectedCategory.trim().takeIf { it.isNotBlank() }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Blue600)
            ) { Text("Save Book") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun CsvImportDialog(onDismiss: () -> Unit, onImport: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var csvText by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val text = stream.bufferedReader().readText()
                    if (text.isNotBlank()) {
                        csvText = text
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bulk Import Books") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Format: Title, Author, [ISBN], [Owner], [Category], [Copies] (one per line). Column headers and commas within quotes are handled automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
                OutlinedButton(
                    onClick = { filePickerLauncher.launch(arrayOf("text/*", "text/comma-separated-values", "application/csv", "*/*")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Select .csv File from Storage")
                }
                Text(
                    "Or paste raw CSV text:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate600
                )
                OutlinedTextField(
                    value = csvText, onValueChange = { csvText = it },
                    placeholder = { Text("The Pilgrim's Progress, John Bunyan, 9780802408540, Church, Classics, 1\nKnowing God, J.I. Packer, , John Doe, Theology, 2") },
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (csvText.isNotBlank()) onImport(csvText) },
                colors = ButtonDefaults.buttonColors(containerColor = Green700)
            ) { Text("Import Data") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun IsbnLookupOverlay(
    message: String = "Looking up your book...",
    canAbandon: Boolean = false,
    abandonPromptText: String = "Still searching online...\nYou can wait a moment longer, or skip the search and enter the details manually.",
    abandonActionLabel: String = "Enter Manually",
    abandonActionIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Edit,
    onAbandonAction: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null
) {
    var showAbandonOption by remember { mutableStateOf(false) }

    if (canAbandon) {
        LaunchedEffect(Unit) {
            delay(3000)
            showAbandonOption = true
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .background(Blue600.copy(alpha = alpha), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Book,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }
            Spacer(Modifier.height(32.dp))
            Text(
                message,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )

            AnimatedVisibility(
                visible = showAbandonOption,
                enter = fadeIn(tween(400)) + expandVertically(),
                exit = fadeOut(tween(200))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(top = 28.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Text(
                        abandonPromptText,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onCancel != null) {
                            OutlinedButton(
                                onClick = onCancel,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cancel")
                            }
                        }
                        if (onAbandonAction != null) {
                            Button(
                                onClick = onAbandonAction,
                                colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(abandonActionIcon, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(abandonActionLabel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangePinOverlay(
    onDismiss: () -> Unit,
    onSave: (String, String, String, () -> Unit, (String) -> Unit) -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        var step by remember { mutableIntStateOf(0) }
        var currentPin by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        val currentInput = when (step) {
            0 -> currentPin
            1 -> newPin
            else -> confirmPin
        }

        val title = when (step) {
            0 -> "Enter Current PIN"
            1 -> "Enter New PIN"
            else -> "Confirm New PIN"
        }

        val subtitle = when (step) {
            1 -> "Minimum 4 digits."
            else -> ""
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = com.churchbooklibrary.theme.DarkSurface2
        ) {
        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(36.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Blue600.copy(alpha = 0.2f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color(0xFF2563EB),
                        modifier = Modifier.padding(20.dp).size(40.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate400
                    )
                }

                Spacer(Modifier.height(32.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    (1..7).forEach { i ->
                        val filled = i <= currentInput.length
                        val dotColor by androidx.compose.animation.animateColorAsState(
                            targetValue = when {
                                errorMessage != null -> Color(0xFFEF4444)
                                filled -> Blue600
                                else -> Color(0xFF334155)
                            },
                            animationSpec = androidx.compose.animation.core.tween(200),
                            label = "dot_color_$i"
                        )
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFEF4444)
                    )
                }

                Spacer(Modifier.height(40.dp))

                val onDigit: (Char) -> Unit = { digit ->
                    errorMessage = null
                    if (currentInput.length < 7) {
                        when (step) {
                            0 -> currentPin += digit
                            1 -> newPin += digit
                            2 -> confirmPin += digit
                        }
                    }
                }

                val onBackspace: () -> Unit = {
                    errorMessage = null
                    when (step) {
                        0 -> if (currentPin.isNotEmpty()) currentPin = currentPin.dropLast(1)
                        1 -> if (newPin.isNotEmpty()) newPin = newPin.dropLast(1)
                        2 -> if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                    }
                }

                val onSubmit: () -> Unit = {
                    when (step) {
                        0 -> {
                            if (currentPin.isEmpty()) {
                                errorMessage = "Please enter current PIN"
                            } else {
                                step = 1
                            }
                        }
                        1 -> {
                            if (newPin.length < 4) {
                                errorMessage = "New PIN must be at least 4 digits"
                            } else {
                                step = 2
                            }
                        }
                        2 -> {
                            if (confirmPin != newPin) {
                                errorMessage = "PINs do not match"
                                confirmPin = ""
                            } else {
                                onSave(currentPin, newPin, confirmPin, {
                                    onDismiss()
                                }, { errorMsg ->
                                    if (errorMsg == "Current PIN is incorrect.") {
                                        step = 0
                                        currentPin = ""
                                        newPin = ""
                                        confirmPin = ""
                                        errorMessage = errorMsg
                                    } else {
                                        errorMessage = errorMsg
                                    }
                                })
                            }
                        }
                    }
                }

                val digits = listOf(
                    listOf('1', '2', '3'),
                    listOf('4', '5', '6'),
                    listOf('7', '8', '9')
                )
                digits.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        row.forEach { digit ->
                            NumpadButton(label = digit.toString()) { onDigit(digit) }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    androidx.compose.material3.FilledTonalIconButton(
                        onClick = onSubmit,
                        modifier = Modifier.size(88.dp),
                        colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = com.churchbooklibrary.theme.DarkSurface
                        )
                    ) {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.Check,
                            contentDescription = "Submit",
                            tint = androidx.compose.ui.graphics.Color(0xFF2563EB),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    NumpadButton(label = "0") { onDigit('0') }
                    androidx.compose.material3.FilledTonalIconButton(
                        onClick = onBackspace,
                        modifier = Modifier.size(88.dp),
                        colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = com.churchbooklibrary.theme.DarkSurface
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Delete",
                            tint = Slate400,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun NumpadButton(label: String, onClick: () -> Unit) {
    androidx.compose.material3.FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(88.dp),
        shape = CircleShape,
        colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = com.churchbooklibrary.theme.DarkSurface
        )
    ) {
        Text(
            text = label,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}












@Composable
private fun AboutDeveloperDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.churchbooklibrary.R.drawable.developer_profile),
                    contentDescription = "Jesse",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "About the Developer",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate800
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Hi! My name is Jesse, and I'm a pastor who loves coding. I love to make apps in my spare time, and figured that this would be a useful app for a lot of smallish churches who have church libraries. I've been in a number of them over the years.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "I think that Sunday Sermons and sermon podcasts are amazing, and should make up the core spiritual diet of most Christians, but I also think that most Christians will not grow into the men and women that the Lord wants them to be without other Spiritual input... and for many Christians - books will be used powerfully by God to mature them.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Obviously the app works for non-church situations as well, and you are welcome to use it however you like. It's totally free, and totally without ads. Made with love for brothers and sisters around the world who I look forward to meeting one day in paradise because of what Jesus has done for us. If you know that you are estranged from God and want to receive eternal life, I suggest that you click the below button and watch a couple of informational videos on Youtube. The whole point of Jesus dying is that sinners like you and me can receive eternal life.",
                    style = MaterialTheme.typography.bodyMedium
                )
                AboutActionCard(
                    title = "Discover Peace with God",
                    subtitle = "Search \"The Gospel in a Nutshell\" on YouTube",
                    icon = androidx.compose.material.icons.Icons.Filled.PlayArrow,
                    iconTint = Color(0xFFCC0000), // YouTube Red
                    onClick = {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW, 
                            android.net.Uri.parse("https://www.youtube.com/results?search_query=The+Gospel+in+a+Nutshell")
                        )
                        try { context.startActivity(intent) } catch (e: Exception) { }
                    }
                )

                Text(
                    text = "PS: The whole design philosophy behind this app is that it is (apart from the book covers) offline. There is no feature to email patrons, because that would add a lot of bloat to the app, require hosting fees, and also require constant updates, which I am trying to avoid. If you need the ability to email patrons when their loan is due, I suggest that you pay a subscription to any number of the other apps that offer that service.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "I'm not taking donations for this app, but if you'd like to support, you can do any of the following things:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                AboutActionCard(
                    title = "Rate the App",
                    subtitle = "Leave a review on the Play Store",
                    icon = androidx.compose.material.icons.Icons.Default.Star,
                    iconTint = Color(0xFFF59E0B), // Amber 500
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=${context.packageName}"))
                        try { context.startActivity(intent) } catch (e: Exception) { }
                    }
                )
                
                AboutActionCard(
                    title = "Spread the Word",
                    subtitle = "Share the app with a pastor/friend who could use it",
                    icon = androidx.compose.material.icons.Icons.Default.Share,
                    iconTint = Blue600,
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, "Check out this free Church Library App: https://play.google.com/store/apps/details?id=${context.packageName}")
                        }
                        try { context.startActivity(android.content.Intent.createChooser(intent, "Spread the Word")) } catch (e: Exception) { }
                    }
                )

                AboutActionCard(
                    title = "Check Out My Other Apps",
                    subtitle = "See what else I've built",
                    icon = androidx.compose.material.icons.Icons.Default.Store,
                    iconTint = Green600,
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://search?q=pub:Aussie App Studio"))
                        try { context.startActivity(intent) } catch (e: Exception) { }
                    }
                )

                Spacer(Modifier.height(24.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}








@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutActionCard(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Slate800)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Slate600)
                }
            }
        }
    }
}














