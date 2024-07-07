package com.example.whatsappclonei.data

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.whatsappclonei.Constants.TAG
import com.example.whatsappclonei.components.ext.idFromParameter
import com.example.whatsappclonei.data.model.MessageModel
import com.example.whatsappclonei.data.model.MessageType
import com.example.whatsappclonei.data.model.Response
import com.example.whatsappclonei.data.model.User
import com.example.whatsappclonei.screens.LOGIN_SCREEN
import com.example.whatsappclonei.screens.MESSAGE_SCREEN
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val fireStorage: FirebaseStorage,
) : AuthRepository {

    override val currentUser get() = auth.currentUser


    override suspend fun firebaseSignUpWithEmailAndPassword(
        email: String, password: String, username: String
    ): SignUpResponse {
        val user = hashMapOf(
            "email" to currentUser?.email,
            "username" to username,
            "userId" to currentUser?.uid,
        )
        return try {

            auth.createUserWithEmailAndPassword(email, password).await()
            val id: String? = currentUser?.providerId
            if (id != null) {
                firestore.collection("Users").add(user).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User added successfully!")
                    } else {
                        Log.w(TAG, "Error adding user", task.exception)
                    }
                }
            }
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override suspend fun sendEmailVerification(): SendEmailVerificationResponse {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override suspend fun firebaseSignInWithEmailAndPassword(
        email: String, password: String, openAndPopUp: (String, String) -> Unit
    ): SignInResponse = suspendCoroutine { continuation ->

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(TAG, "signIn:success")
                continuation.resume(Response.Success(true))
                openAndPopUp.invoke(MESSAGE_SCREEN, LOGIN_SCREEN)
            } else {
                it.exception?.let { it1 ->
                    Log.d(TAG, "login Failed")
                    continuation.resume(Response.Failure(it1))
                }
            }
        }

    }


    override suspend fun reloadFirebaseUser(): ReloadUserResponse {
        return try {
            auth.currentUser?.reload()?.await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): SendPasswordResetEmailResponse {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override fun signOut() = auth.signOut()

    override suspend fun revokeAccess(): RevokeAccessResponse {
        return try {
            auth.currentUser?.delete()?.await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override fun getAuthState(viewModelScope: CoroutineScope) = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser == null)
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), auth.currentUser == null)

    override suspend fun getAccounts(): AccountsResponse {
        val users = mutableStateListOf<User>()



        return try {

            firestore.collection("Users")
                .whereNotEqualTo("userId", currentUser?.uid)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val user = document.toObject(User::class.java)
                        if (user.userId != currentUser?.uid) {
                            users.add(user)
                        }
                    }
                    Log.d("messagevmm", "$result")
                }.await()

            Log.d("messagevmmm", "$users")
            Response.Success(users)

        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override suspend fun getUser(userId: String): Flow<User> {
        return flow {
            val user: User
            try {
                val documents = firestore.collection("Users")
                    .whereEqualTo("userId", userId)
                    .limit(1)
                    .get()
                    .await() // Suspend until the task is complete
                val document = documents.first()
                user = document.toObject(User::class.java)
                Log.d(TAG, "${document.id} => ${user}")
                emit(user) // Emit the user as a flow value
            } catch (e: Exception) {
                Log.w(TAG, "Error getting documents: ", e)
                throw e // Rethrow the exception
            }
        }
    }

    override suspend fun createChats(message1: String, receiverId: String): Flow<Boolean> {

// Create an instance of the model
        val message = MessageModel(
            uid = "${currentUser?.uid}",
            message = message1,
            messageId = "someMessageId",
            timestamp = System.currentTimeMillis(),
            audioUrl = null,
            messageType = MessageType.MESSAGE
        )


// Create the document names
        val senderRoom = "${currentUser?.uid}${receiverId.idFromParameter()}"
        val receiverRoom = "${receiverId.idFromParameter()}${currentUser?.uid}"

        return flow {
            // Add the message to the collection
            try {

                firestore.collection("messages")
                    .document(senderRoom)
                    .collection("chats")
                    .add(message)

                    .await() // Suspend until the task is complete
                Log.d(TAG, "Message added successfully")
                // Add another document receiverRoom and set the same message
                firestore.collection("messages")
                    .document(receiverRoom)
                    .collection("chats")
                    .add(message)
                    .await() // Suspend until the task is complete
                Log.d(TAG, "Message added successfully")
                emit(true) // Emit true as a flow value
            } catch (e: Exception) {
                Log.w(TAG, "Error adding message", e)
                emit(false) // Emit false as a flow value
            }
        }

    }

    override suspend fun getChats(receiverId: String): Flow<List<MessageModel>> {
        val messages = mutableStateListOf<MessageModel?>()

        return callbackFlow {

            // Create the document name
            val senderRoom = "${currentUser?.uid}${receiverId.idFromParameter()}"

            firestore.collection("messages")
                .document(senderRoom)
                .collection("chats")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val model = document.toObject<MessageModel>()
                        messages.add(model)
                    }

                    // Send the list of messages through the flow
                    trySend(messages.toList().filterNotNull())

                    // Log the messages
                    Log.d(TAG, "Messages: $messages")
                }
                .addOnFailureListener { exception ->
                    // Handle the failure
                    Log.e(TAG, "Error getting messages: $exception")

                }

            // Note: Don't close the flow here

              awaitClose()

            // The flow will be closed when the coroutine is canceled
        }

    }

    override suspend fun loadPrivateChats(receiverId: String): Flow<List<MessageModel>> {
        val messages = mutableStateListOf<MessageModel?>()

        return callbackFlow {

            // Create the document name
            val senderRoom = "${currentUser?.uid}${receiverId.idFromParameter()}"

            val listener = firestore.collection("messages")
                .document(senderRoom)
                .collection("chats")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { querySnapshot, exception ->
                    if (exception != null) {
                        // Handle the error
                        return@addSnapshotListener
                    }

                    if (querySnapshot != null) {
                        messages.clear() // Clear the existing list before adding new data

                        // Loop through the query snapshot to get each document
                        for (documentSnapshot in querySnapshot.documents) {
                            // Convert the document data to your model class
                            val messageModel = documentSnapshot.toObject(MessageModel::class.java)
                            if (messageModel != null) {
                                messages.add(messageModel)
                            }
                        }

                        // Send the updated list of messages through the flow
                        trySend(messages.toList().filterNotNull())
                    }
                }

            awaitClose {
                // Remove the listener when the flow is cancelled
                listener.remove()
            }
        }
    }

    override suspend fun sendRecordingMessage(audioPath: String,receiverId: String): Flow<Boolean> {
        var message =MessageModel()
        val senderRoom = "${currentUser?.uid}${receiverId.idFromParameter()}"
        val receiverRoom = "${receiverId.idFromParameter()}${currentUser?.uid}"
        //Create a Uri from the audio path
        val audioUri = Uri.fromFile(File(audioPath))

        //Create a child reference for the audio file under the user's UID
        val audioRef =
            fireStorage.reference.child(FirebaseAuth.getInstance().currentUser!!.uid + "/" + audioUri.lastPathSegment)

        //Upload the audio file to Firebase Storage
        val uploadTask = audioRef.putFile(audioUri)

        //Register listeners for the upload task
        uploadTask.addOnSuccessListener {
            //Get the download URL of the audio file from Firebase Storage
            audioRef.downloadUrl.addOnSuccessListener { uri ->

                val audioUrl = uri.toString()

                //Create a MessageModel object with the audio URL
                message = MessageModel(
                    uid = "${currentUser?.uid}",
                    message = " ",
                    messageId = "audioUri.lastPathSegment",
                    timestamp = System.currentTimeMillis(),
                    messageType = MessageType.RECORDING,
                    audioUrl = audioUrl
                )
                // Create the document names


            }

        }
        return flow {
            // Add the message to the collection
            try {

                firestore.collection("messages")
                    .document(senderRoom)
                    .collection("chats")
                    .add(message)

                    .await() // Suspend until the task is complete
                Log.d(TAG, "Message added successfully")
                // Add another document receiverRoom and set the same message
                firestore.collection("messages")
                    .document(receiverRoom)
                    .collection("chats")
                    .add(message)
                    .await() // Suspend until the task is complete
                Log.d(TAG, "Message added successfully")
                emit(true) // Emit true as a flow value
            } catch (e: Exception) {
                Log.w(TAG, "Error adding message", e)
                emit(false) // Emit false as a flow value
            }
        }
    }
    }