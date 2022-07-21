package com.example.instagramclone

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.instagramclone.data.Event
import com.example.instagramclone.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import javax.inject.Inject

const val USERS = "users" // name of the collection that we will use later on

@HiltViewModel
class IgViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {

    val signedIn = mutableStateOf(false) //default values
    val inProgress = mutableStateOf(false) //default values
    val userDate = mutableStateOf<UserData?>(null) //default values
    val popupNotification = mutableStateOf<Event<String>?>(null)

    fun onSignup(username: String, email: String, pass: String){
        inProgress.value = true

        //check weather the username is unique or not
        db.collection(USERS).whereEqualTo("username", username).get()
            .addOnSuccessListener { documents -> //need to know whether the document collection is empty or not
                if(documents.size() >0 ){
                    handleException(customMessage = "Username already exists ")
                    inProgress.value = false
                }else{
                    //creating a new user name
                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                signedIn.value = true
                                    //Create the profile
                            }else{
                                handleException(task.exception, "Signup failed")
                            }
                            //after all the progress
                            inProgress.value = false
                        }
                }

            }
            .addOnFailureListener {  }
    }
        //if error occurred above then the handleException will get called, update the popupNotification via Event,
        //this will net to the notification
    fun handleException(exception: Exception? = null, customMessage: String =""){
        exception?.printStackTrace()//we dont want to print anything on console
        val errorMsg = exception?.localizedMessage?: ""
        val message = if(customMessage.isEmpty()) errorMsg else "$customMessage: $errorMsg"
        popupNotification.value = Event(message)
    }
}