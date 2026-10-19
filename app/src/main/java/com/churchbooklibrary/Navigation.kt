package com.churchbooklibrary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.churchbooklibrary.feature.admin.AdminDashboardScreen
import com.churchbooklibrary.feature.admin.AdminViewModel
import com.churchbooklibrary.feature.admin.PinScreen
import com.churchbooklibrary.feature.admin.PinViewModel
import com.churchbooklibrary.feature.borrow.BorrowerInfoScreen
import com.churchbooklibrary.feature.borrow.BorrowSuccessScreen
import com.churchbooklibrary.feature.borrow.BorrowViewModel
import com.churchbooklibrary.feature.borrow.SelectBooksScreen
import com.churchbooklibrary.feature.home.HomeScreen
import com.churchbooklibrary.feature.home.HomeViewModel
import com.churchbooklibrary.feature.returns.ReturnBooksScreen
import com.churchbooklibrary.feature.returns.ReturnViewModel

import kotlinx.coroutines.launch

@Composable
fun MainNavigation(
    openSync: Boolean = false,
    onClearOpenSync: () -> Unit = {}
) {
    val navController: NavHostController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as LibraryApplication

    val borrowViewModel: BorrowViewModel = viewModel(
        factory = BorrowViewModel.Factory(app.repository, app.settingsDataStore)
    )
    val returnViewModel: ReturnViewModel = viewModel(
        factory = ReturnViewModel.Factory(app.repository)
    )
    val pinViewModel: PinViewModel = viewModel(
        factory = PinViewModel.Factory(app.pinDataStore)
    )
    val adminViewModel: AdminViewModel = viewModel(
        factory = AdminViewModel.Factory(app.repository, app.pinDataStore, app.settingsDataStore, app.syncManager)
    )
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(app.settingsDataStore)
    )

    val isDemoMode by app.settingsDataStore.isDemoMode.collectAsState(initial = true)
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var lastInteractionTime by androidx.compose.runtime.remember { androidx.compose.runtime.mutableLongStateOf(System.currentTimeMillis()) }
    var showAdminTimeoutWarning by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var adminSecondsRemaining by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(30) }
    var navigateToSyncSettings by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(openSync) {
        if (openSync) {
            navigateToSyncSettings = true
            if (currentRoute != Screen.AdminDashboard.route) {
                pinViewModel.resetAuth()
                navController.navigate(Screen.AdminPin.route) {
                    popUpTo(Screen.Home.route)
                }
            }
        }
    }

    // Patron screens inactivity timeout (60 seconds)
    androidx.compose.runtime.LaunchedEffect(currentRoute, lastInteractionTime) {
        if (currentRoute != Screen.Home.route && currentRoute != Screen.AdminDashboard.route) {
            val timeout = 60_000L // 60 seconds
            val timeToWait = timeout - (System.currentTimeMillis() - lastInteractionTime)
            if (timeToWait > 0) {
                kotlinx.coroutines.delay(timeToWait)
            }
            if (System.currentTimeMillis() - lastInteractionTime >= timeout) {
                // If it's BorrowSuccess, let its own timer finish or timeout naturally. But if not:
                if (currentRoute != Screen.BorrowSuccess.route) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            }
        }
    }

    // Admin dashboard inactivity timeout: 3 minutes idle -> warning dialog with 30s countdown
    androidx.compose.runtime.LaunchedEffect(currentRoute, lastInteractionTime) {
        if (currentRoute == Screen.AdminDashboard.route) {
            val warningTimeout = 180_000L // 3 minutes
            val idleTime = System.currentTimeMillis() - lastInteractionTime
            val timeUntilWarning = warningTimeout - idleTime
            if (timeUntilWarning > 0) {
                showAdminTimeoutWarning = false
                kotlinx.coroutines.delay(timeUntilWarning)
            }
            if (System.currentTimeMillis() - lastInteractionTime >= warningTimeout) {
                showAdminTimeoutWarning = true
                adminSecondsRemaining = 30
                while (adminSecondsRemaining > 0) {
                    kotlinx.coroutines.delay(1000L)
                    if (System.currentTimeMillis() - lastInteractionTime < warningTimeout) {
                        showAdminTimeoutWarning = false
                        break
                    }
                    adminSecondsRemaining--
                }
                if (adminSecondsRemaining <= 0 && showAdminTimeoutWarning) {
                    showAdminTimeoutWarning = false
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            }
        } else {
            showAdminTimeoutWarning = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                awaitPointerEvent(PointerEventPass.Initial)
                lastInteractionTime = System.currentTimeMillis()
            }
        }
    }) {
        if (showAdminTimeoutWarning) {
            AlertDialog(
                onDismissRequest = {
                    lastInteractionTime = System.currentTimeMillis()
                    showAdminTimeoutWarning = false
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "Admin Session Expiring",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "The admin dashboard has been inactive for 3 minutes. To protect patron privacy and library data, this screen will automatically close and lock in $adminSecondsRemaining seconds unless you intervene.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            lastInteractionTime = System.currentTimeMillis()
                            showAdminTimeoutWarning = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Stay Logged In")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAdminTimeoutWarning = false
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    ) {
                        Text("Log Out Now", color = Color(0xFFEF4444))
                    }
                },
                modifier = Modifier.pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent(PointerEventPass.Initial)
                            lastInteractionTime = System.currentTimeMillis()
                            showAdminTimeoutWarning = false
                        }
                    }
                }
            )
        }
        if (isDemoMode) {
            Surface(
                color = Color(0xFFC2410C), // Deep highly-visible orange
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(vertical = 16.dp, horizontal = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.demo_mode),
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.demo_mode_active),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = stringResource(R.string.demo_mode_banner_desc),
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.width(24.dp))
                    androidx.compose.material3.Button(
                        onClick = {
                            coroutineScope.launch {
                                app.settingsDataStore.setDemoMode(false)
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFFD97706)
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(stringResource(R.string.exit_demo_mode), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.weight(1f)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onBorrow = {
                        borrowViewModel.reset()
                        navController.navigate(Screen.SelectBooks.route)
                    },
                    onReturn = { navController.navigate(Screen.ReturnBooks.route) },
                    onAdmin = {
                        pinViewModel.resetAuth()
                        navController.navigate(Screen.AdminPin.route)
                    }
                )
            }

            composable(Screen.SelectBooks.route) {
                SelectBooksScreen(
                    viewModel = borrowViewModel,
                    onProceed = { navController.navigate(Screen.BorrowerInfo.route) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.BorrowerInfo.route) {
                BorrowerInfoScreen(
                    viewModel = borrowViewModel,
                    onSuccess = {
                        navController.navigate(Screen.BorrowSuccess.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.BorrowSuccess.route) {
                BorrowSuccessScreen(
                    viewModel = borrowViewModel,
                    onHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.ReturnBooks.route) {
                ReturnBooksScreen(
                    viewModel = returnViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AdminPin.route) {
                PinScreen(
                    viewModel = pinViewModel,
                    onAuthenticated = {
                        pinViewModel.clearAuthenticatedState()
                        navController.navigate(Screen.AdminDashboard.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onBack = {
                        navigateToSyncSettings = false
                        onClearOpenSync()
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.AdminDashboard.route) {
                val shouldAutoSync = navigateToSyncSettings
                AdminDashboardScreen(
                    viewModel = adminViewModel,
                    initialRoute = if (shouldAutoSync) com.churchbooklibrary.feature.admin.AdminRoute.Settings else com.churchbooklibrary.feature.admin.AdminRoute.Menu,
                    autoStartPhoneSync = shouldAutoSync,
                    onLogout = {
                        navigateToSyncSettings = false
                        onClearOpenSync()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
                if (shouldAutoSync) {
                    onClearOpenSync()
                }
            }
        }
    }
}

