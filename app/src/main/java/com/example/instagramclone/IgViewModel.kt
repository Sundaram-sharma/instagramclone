package com.example.instagramclone

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.instagramclone.data.Event
import com.example.instagramclone.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
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
    val userData = mutableStateOf<UserData?>(null) //default values
    val popupNotification = mutableStateOf<Event<String>?>(null)

    init { //once the system starts our view model below will check we have user or not
        //auth.signOut()
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
        currentUser?.uid?.let{
            uid -> getUserData(uid)
        }
    }

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
                                createOrUpdateProfile(username = username)
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

    private fun createOrUpdateProfile(
        name: String? = null,
        username: String? = null,
        bio: String? = null,
        imageUrl: String? = null
    ){ //create the user and store into Database

        val uid = auth.currentUser?.uid
        val userData = UserData( //from the UserData class
            userId = uid,
            name = name ?: userData.value?.name,
            username = username ?:userData.value?.username,
            bio = bio ?:userData.value?.bio,
            imageUrl = imageUrl ?:userData.value?.imageUrl,
            following = userData.value?.following
        )

        uid?.let{
            uid ->
            inProgress.value = true
            db.collection(USERS).document(uid).get()
                .addOnSuccessListener {
                if(it.exists()){ //if it exists
                    it.reference.update(userData.toMap())
                        .addOnSuccessListener {
                            this.userData.value = userData
                            inProgress.value = false
                        }
                        .addOnFailureListener{
                            handleException(it, "Cannot update user")
                            inProgress.value = false
                        }
                }
                else{
                    db.collection(USERS).document(uid).set(userData)
                    getUserData(uid)
                    inProgress.value = false
                }
            }
                .addOnFailureListener { exc -> handleException(exc, "Cannot create user")
                inProgress.value = false
                }
        }

    }

    private fun getUserData(uid: String){
        inProgress.value = true
        db.collection(USERS).document(uid).get()
            .addOnSuccessListener {
                val user = it.toObject<UserData>()
                    userData.value = user
                    inProgress.value = false
                    popupNotification.value = Event("User data retrieved successfully")

            }
            .addOnFailureListener { exc ->
                handleException(exc, "Cannot retrive user data")
                inProgress.value = false
            }
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