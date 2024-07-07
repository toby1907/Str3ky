package com.example.whatsappclonei.data


import androidx.compose.runtime.MutableState
import com.example.whatsappclonei.data.model.MessageModel
import com.example.whatsappclonei.data.model.Response
import com.example.whatsappclonei.data.model.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


typealias SignUpResponse = Response<Boolean>
typealias SendEmailVerificationResponse = Response<Boolean>
typealias SignInResponse = Response<Boolean>
typealias ReloadUserResponse = Response<Boolean>
typealias SendPasswordResetEmailResponse = Response<Boolean>
typealias RevokeAccessResponse = Response<Boolean>
typealias AuthStateResponse = StateFlow<Boolean>
typealias AccountsResponse = Response<List<User>>
typealias  UserResponse = Response<MutableState<User?>>

interface AuthRepository {

    val currentUser: FirebaseUser?

    suspend fun firebaseSignUpWithEmailAndPassword(email: String, password: String,username:String): SignUpResponse

    suspend fun sendEmailVerification(): SendEmailVerificationResponse

    suspend fun firebaseSignInWithEmailAndPassword(email: String, password: String,openAndPopUp: (String, String) -> Unit): SignInResponse

    suspend fun reloadFirebaseUser(): ReloadUserResponse

    suspend fun sendPasswordResetEmail(email: String): SendPasswordResetEmailResponse

    fun signOut()

    suspend fun revokeAccess(): RevokeAccessResponse

    fun getAuthState(viewModelScope: CoroutineScope): AuthStateResponse
   suspend fun getAccounts() : AccountsResponse
   suspend fun getUser(userId:String): Flow<User>

   suspend fun createChats(message1:String,receiverId:String): Flow<Boolean>
   suspend fun getChats(receiverId:String): Flow<List<MessageModel>>
   suspend fun loadPrivateChats(receiverId: String): Flow<List<MessageModel>>
   suspend fun sendRecordingMessage(audioPath:String,receiverId: String): Flow<Boolean>

}