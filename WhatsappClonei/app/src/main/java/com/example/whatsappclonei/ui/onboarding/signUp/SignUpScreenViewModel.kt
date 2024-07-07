package com.example.whatsappclonei.ui.onboarding.signUp

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.whatsappclonei.Constants.SIGN_UP_SCREEN
import com.example.whatsappclonei.R
import com.example.whatsappclonei.components.ext.isValidEmail
import com.example.whatsappclonei.components.ext.isValidPassword
import com.example.whatsappclonei.data.AuthRepository
import com.example.whatsappclonei.data.model.Response
import com.example.whatsappclonei.data.SendEmailVerificationResponse
import com.example.whatsappclonei.data.SignUpResponse
import com.example.whatsappclonei.screens.MESSAGE_SCREEN
import com.example.whatsappclonei.screens.SETTINGS_SCREEN
import com.example.whatsappclonei.ui.onboarding.signIn.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val context: Context,
    private val repo: AuthRepository
): ViewModel(){
    // Initialize Firebase Auth
    data class SignUpUiState(
        val username: String = "",
        val email: String = "",
        val password: String = ""
    )
    var uiState = mutableStateOf(SignUpUiState())
        private set
    private val username
        get() = uiState.value.username
    private val email
        get() = uiState.value.email
    private val password
        get() = uiState.value.password

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    fun postMessage(message: String) {
        // Do something with the message
        _message.value = message
    }

    fun onUsernameChange(newValue: String) {
        uiState.value = uiState.value.copy(username = newValue)
    }
    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue)
    }

    fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue)
    }
var signUpResponse by mutableStateOf<SignUpResponse>(Response.Success(false))
private set

    var sendEmailVerificationResponse by mutableStateOf<SendEmailVerificationResponse>(Response.Success(false))
    private set

    private fun signUpWithEmailAndPassword(email: String, password: String, username: String) = viewModelScope.launch {
        signUpResponse = Response.Loading
        signUpResponse = repo.firebaseSignUpWithEmailAndPassword(email,password,username)
    }
    fun sendEmailVerification() = viewModelScope.launch {
        sendEmailVerificationResponse = Response.Loading
        sendEmailVerificationResponse = repo.sendEmailVerification()
    }
/*fun onEvent(event:SignInEvent){
    when(event){
        is SignInEvent.SignIn -> {
TODO()
        }
        is SignInEvent.SignInCredentials -> {
TODO()
        }
        is SignInEvent.SignOut -> {
TODO()
        }
    }
}


 */


   fun onSignUpClick(openAndPopUp: (String, String) -> Unit) {
      /*  if (!email.isValidEmail()) {
            postMessage(R.string.email_error.toString())
            return
        }

        if (!password.isValidPassword()) {
           postMessage(R.string.password_error.toString())
            return
        }*/


       signUpWithEmailAndPassword(email, password,username)
            openAndPopUp(MESSAGE_SCREEN, SIGN_UP_SCREEN)

    }


}
