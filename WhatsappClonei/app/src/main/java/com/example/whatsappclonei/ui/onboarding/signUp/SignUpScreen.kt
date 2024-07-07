package com.example.whatsappclonei.ui.onboarding.signUp

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.whatsappclonei.R
import com.example.whatsappclonei.data.model.Response
import com.example.whatsappclonei.constants.VERIFY_EMAIL_MESSAGE
import com.example.whatsappclonei.ui.onboarding.signUp.components.SignUpContent
import com.example.whatsappclonei.utils.Utils.Companion.showMessage


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SignUpFullScreen(
    openAndPopUp: (String, String) -> Unit,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SignUpScreenViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { innerPadding ->
          /*  SignUpScreen(
                modifier = Modifier
                    .padding(innerPadding)
                    .wrapContentSize(),
                navigateBack = navigateBack,
                snackBar = {

                    // show snackbar as a suspend function
                    scope.launch {

                        snackbarHostState.showSnackbar(
                            "Enter Credential"
                        )
                    }
                },
                signUp = { email, password ->
                    viewModel.signUpWithEmailAndPassword(
                        email,
                        password
                    )
                }
            )*/
            SignUpContent(
                padding = innerPadding,
                onSignUp = openAndPopUp,
                snackBar = { /*TODO*/ },
                navigateToSignInScreen = navigateBack,
                viewModel = viewModel
            )

            SignUp(
                sendEmailVerification = { viewModel.sendEmailVerification() },
                showVerifyEmailMessage = {
                    showMessage(context, VERIFY_EMAIL_MESSAGE)
                }
            )
            SendEmailVerification()
        }
    )
}

@Composable
fun SendEmailVerification(
    viewModel: SignUpScreenViewModel = hiltViewModel()
) {
    when (val sendEmailVerificationResponse = viewModel.sendEmailVerificationResponse) {
        is Response.Loading -> ProgressBar()
        is Response.Success -> Unit
        is Response.Failure -> sendEmailVerificationResponse.apply {
            LaunchedEffect(e) {
                print(e)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    navigateBack: (String,String) -> Unit,
    snackBar: () -> Unit,
    signUp: (email: String, password: String) -> Unit
) {
    var usernameText by remember { mutableStateOf("") }
    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    val context = LocalContext.current
    var progressDialog by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        Image(
            contentScale = ContentScale.FillBounds,
            painter = painterResource(id = R.drawable.background),
            contentDescription = "",
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            //CircularProgress
            if (progressDialog) {
                CircularProgressIndicator()
            }
            Spacer(Modifier.requiredHeight(30.dp))


                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = stringResource(id = R.string.whatsapp_content_description),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                    )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome to WhatsAppClonei",
// Geometria 24 | Medium
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF000000),
                    textAlign = TextAlign.Center,
                )
            )
                TextField(
                    value = usernameText,
                    onValueChange = { usernameText = it },
                    placeholder = { Text(text = "Username") },



                )
                TextField(
                    value = emailText,
                    onValueChange = { emailText = it },
                    placeholder = { Text(text = "Email") },


                )
                TextField(
                    value = passwordText,
                    onValueChange = { passwordText = it },
                    label = { Text("Enter password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )
                Spacer(Modifier.size(4.dp))
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Spacer(Modifier.size(width = 220.dp, height = 0.dp))
                    TextButton(
                        onClick = {  },
                    ) {
                        Text(
                            fontSize = 8.sp,
                            text = "Already Have Account",
                            color = Color.LightGray
                        )
                    }
                }
                Button(
                    onClick = {
                        if (usernameText.isNotEmpty() && emailText.isNotEmpty() && passwordText.isNotEmpty()) {
                            progressDialog = true
                            signUp.invoke(emailText, passwordText)

                        } else {
                            snackBar.invoke()
                        }
                    },
                    shape = RectangleShape
                ) {
                    Text(text = "Sign Up")
                }


        }

    }

}

