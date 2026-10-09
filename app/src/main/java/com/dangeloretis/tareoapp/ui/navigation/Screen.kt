package com.dangeloretis.tareoapp.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Login : Screen

    @Serializable
    data object WorkerHome : Screen

    @Serializable
    data object TareadorHome : Screen

    @Serializable
    data object AdminHome : Screen
}
