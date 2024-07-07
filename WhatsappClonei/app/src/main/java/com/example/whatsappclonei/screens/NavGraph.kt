package com.example.whatsappclonei.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.whatsappclonei.WhatsappCloneiAppState
import com.example.whatsappclonei.constants.VERIFY_EMAIL_SCREEN
import com.example.whatsappclonei.ui.chats.MessageScreen
import com.example.whatsappclonei.ui.onboarding.signIn.SignInFullScreen
import com.example.whatsappclonei.ui.onboarding.signUp.SignUpFullScreen
import com.example.whatsappclonei.ui.private_chat.ChatScreen
import com.example.whatsappclonei.ui.profile.ProfileScreen
import com.example.whatsappclonei.ui.profile.VerifyEmailScreen


@Composable
fun rememberAppState(
    navController: NavHostController = rememberNavController(),

    ) = remember(navController) {
    WhatsappCloneiAppState(
        navController
    )
}


@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    startDestination: String = LOGIN_SCREEN,
) {
    val appState = rememberAppState()
    NavHost(
        modifier = modifier,
        navController = appState.navController,
        startDestination = startDestination,
    ) {

        WhatsappCloneNavGraph(appState = appState)
    }
}

fun NavGraphBuilder.WhatsappCloneNavGraph(appState: WhatsappCloneiAppState) {
    composable(LOGIN_SCREEN) {
        SignInFullScreen(
            navigateToSignUpScreen = { appState.navigate(SIGN_UP_SCREEN) },
            openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) })

    }
    composable(SIGN_UP_SCREEN) {
        SignUpFullScreen(openAndPopUp = { route, popUp ->
            appState.navigateAndPopUp(route, popUp)
        },
            navigateBack = { appState.navigate(LOGIN_SCREEN) }
        )
    }

    composable(
        route = VERIFY_EMAIL_SCREEN
    ) {
        VerifyEmailScreen(
            navigateToProfileScreen = {
                /*   appState.navigate(Screen.ProfileScreen.route){
                       popUpTo(navController.graph.id){
                           inclusive =true
                       }
                   }*/
            }
        )
    }

    composable(
        route = PROFILE_SCREEN
    ) {
        ProfileScreen()
    }
    composable(
        route = MESSAGE_SCREEN
    ) {
        MessageScreen(navigateProfile = { appState.navigate(PROFILE_SCREEN) }, openChat = {route ->appState.navigate(route)})
    }

    composable(
        route = "$CHAT_SCREEN$CHAT_ID_ARG",
        arguments = listOf(navArgument(CHAT_ID) { defaultValue = CHAT_DEFAULT_ID })
    ) {
        ChatScreen(
            popUpScreen = { appState.popUp() },
            userId = it.arguments?.getString(CHAT_ID) ?: CHAT_DEFAULT_ID
        )
    }


}