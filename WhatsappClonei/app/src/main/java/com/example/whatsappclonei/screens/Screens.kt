package com.example.whatsappclonei.screens

import com.example.whatsappclonei.Constants.MESSAGES_SCREEN
import com.example.whatsappclonei.Constants.PROFILE_SCREEN
import com.example.whatsappclonei.Constants.SIGN_IN_SCREEN
import com.example.whatsappclonei.Constants.SIGN_UP_SCREEN
import com.example.whatsappclonei.Constants.VERIFY_EMAIL_SCREEN
const val SPLASH_SCREEN = "SplashScreen"
const val SETTINGS_SCREEN = "SettingsScreen"
const val LOGIN_SCREEN = "LoginScreen"
const val MESSAGE_SCREEN = "MessageScreen"
const val SIGN_UP_SCREEN = "SignUpScreen"
const val CHAT_SCREEN = "ChatScreen"
const val TASKS_SCREEN = "TasksScreen"
const val EDIT_TASK_SCREEN = "EditTaskScreen"
const val PROFILE_SCREEN = "ProfileScreen"

const val CHAT_ID = "chatId"
const val CHAT_DEFAULT_ID = "-1"
const val CHAT_ID_ARG = "?$CHAT_ID={$CHAT_ID}"

sealed class Screen(val route: String) {
    object SignInScreen: Screen(SIGN_IN_SCREEN)
    object SignUpScreen: Screen(SIGN_UP_SCREEN)
  /*  object ForgotPasswordScreen: Screen(FORGOT_PASSWORD_SCREEN)
    object VerifyEmailScreen: Screen(VERIFY_EMAIL_SCREEN)
    object ProfileScreen: Screen(PROFILE_SCREEN)*/
    object VerifyEmailScreen: Screen(VERIFY_EMAIL_SCREEN)
    object ProfileScreen: Screen(PROFILE_SCREEN)
    object MessagesScreen: Screen(MESSAGES_SCREEN)
}