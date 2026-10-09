package com.dangeloretis.tareoapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dangeloretis.tareoapp.domain.model.UserRole
import com.dangeloretis.tareoapp.presentation.tareador.TareadorHomeScreen
import com.dangeloretis.tareoapp.presentation.tareador.QrScannerScreen
import com.dangeloretis.tareoapp.ui.screens.home.admin.AdminHomeScreen
import com.dangeloretis.tareoapp.ui.screens.home.WorkerHomeScreen
import com.dangeloretis.tareoapp.ui.screens.login.LoginScreen

@Composable
fun TareoNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Any = Screen.Login
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<Screen.Login> {
            LoginScreen(
                onNavigateToHome = { role ->
                    val destination = when (role) {
                        UserRole.WORKER -> Screen.WorkerHome
                        UserRole.TAREADOR -> Screen.TareadorHome
                        UserRole.ADMIN -> Screen.AdminHome
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.WorkerHome> {
            WorkerHomeScreen(
                onLogout = {
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.WorkerHome) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.TareadorHome> { backStackEntry ->
            val scannedDni = backStackEntry.savedStateHandle.get<String>("scanned_dni")
            val viewModel = androidx.hilt.navigation.compose.hiltViewModel<com.dangeloretis.tareoapp.presentation.tareador.TareadorViewModel>()
            
            androidx.compose.runtime.LaunchedEffect(scannedDni) {
                if (scannedDni != null) {
                    viewModel.registerTareo(method = "QR", scannedDni = scannedDni)
                    backStackEntry.savedStateHandle.remove<String>("scanned_dni")
                }
            }

            TareadorHomeScreen(
                viewModel = viewModel,
                onLogout = {
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.TareadorHome) { inclusive = true }
                    }
                },
                onScanQrClick = {
                    navController.navigate(Screen.QrScanner)
                }
            )
        }

        composable<Screen.QrScanner> {
            QrScannerScreen(
                onQrScanned = { dni ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("scanned_dni", dni)
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.AdminHome> {
            AdminHomeScreen(
                onLogout = {
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.AdminHome) { inclusive = true }
                    }
                }
            )
        }
    }
}
