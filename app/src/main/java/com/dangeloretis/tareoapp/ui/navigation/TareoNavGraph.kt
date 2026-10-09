package com.dangeloretis.tareoapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dangeloretis.tareoapp.domain.model.UserRole
import com.dangeloretis.tareoapp.ui.screens.home.AdminHomeScreen
import com.dangeloretis.tareoapp.ui.screens.home.TareadorHomeScreen
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

        composable<Screen.TareadorHome> {
            TareadorHomeScreen(
                onLogout = {
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.TareadorHome) { inclusive = true }
                    }
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
