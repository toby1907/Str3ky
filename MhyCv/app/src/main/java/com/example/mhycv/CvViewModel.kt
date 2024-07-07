package com.example.mhycv

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

// Define a data class that holds the information for the CV
data class CvData(
    val firstName: String,
    val lastName: String,
    val jobTitle: String,
    val slack: String,
    val location: String,
    val github: String,
    val bio: String
)

// Define a view model class that holds a mutable state of the CV data and exposes functions to update the state
class CvViewModel : ViewModel() {
    // Create a mutable state of the CV data with some sample values
    init {

    }
    private val _cvData = mutableStateOf(
        CvData(
            firstName = "Paul",
            lastName = "Olaleye",
            jobTitle = "Software Engineer",
            slack = "Toby1907",
            location = "Abeokuta, Nigeria",
            github = "https://github.com/toby1907",
            bio = "I am a software engineer with over 5 years of experience in developing Android applications using Kotlin and Jetpack Compose. I have a passion for creating beautiful and functional user interfaces that enhance the user experience. I am always eager to learn new technologies and frameworks that can help me improve my skills and deliver high-quality products."
        )
    )

    // Create a public state of the CV data that can be observed by other composables
    val cvData: State<CvData> = _cvData

    // Create a function to update the first name in the CV data state
    fun updateFirstName(newFirstName: String) {
        _cvData.value = _cvData.value.copy(firstName = newFirstName)
    }

    // Create a function to update the last name in the CV data state
    fun updateLastName(newLastName: String) {
        _cvData.value = _cvData.value.copy(lastName = newLastName)
    }

    // Create a function to update the job title in the CV data state
    fun updateJobTitle(newJobTitle: String) {
        _cvData.value = _cvData.value.copy(jobTitle = newJobTitle)
    }

    // Create a function to update the email address in the CV data state
    fun updateEmail(newEmail: String) {
        _cvData.value = _cvData.value.copy(slack = newEmail)
    }

    // Create a function to update the location in the CV data state
    fun updateLocation(newLocation: String) {
        _cvData.value = _cvData.value.copy(location = newLocation)
    }

    // Create a function to update the phone number in the CV data state
    fun updatePhone(newPhone: String) {
        _cvData.value = _cvData.value.copy(github = newPhone)
    }

    // Create a function to update the personal bio in the CV data state
    fun updateBio(newBio: String) {
        _cvData.value = _cvData.value.copy(bio = newBio)
    }
}
