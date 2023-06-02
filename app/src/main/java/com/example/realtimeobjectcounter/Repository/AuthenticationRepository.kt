package com.example.realtimeobjectcounter.Repository

import android.content.Context
import com.example.realtimeobjectcounter.Models.User
import com.example.realtimeobjectcounter.Utils.Constants.UID
import com.example.realtimeobjectcounter.Utils.Resource
import com.example.realtimeobjectcounter.Utils.SharedPreferenceManager
import com.example.realtimeobjectcounter.Utils.safeCall
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthenticationRepository(context: Context) {

    private val sharedPreferenceManager = SharedPreferenceManager(context)
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().getReference("users")

    suspend fun createUser(
        userName: String,
        userEmailAddress: String,
        userLoginPassword: String,
        dateOfBirth: String
    ): Resource<AuthResult> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val registrationResult =
                    firebaseAuth.createUserWithEmailAndPassword(userEmailAddress, userLoginPassword)
                        .await()

                val userId = registrationResult.user?.uid!!
                sharedPreferenceManager.saveStringInPref(UID, userId)
                val newUser = User(userName, userEmailAddress, userLoginPassword, dateOfBirth)
                databaseReference.child(userId).setValue(newUser).await()
                Resource.Success(registrationResult)
            }
        }
    }

    suspend fun login(email: String, password: String): Resource<AuthResult> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                sharedPreferenceManager.saveStringInPref(UID, result.user?.uid)
                Resource.Success(result)
            }
        }
    }

    fun isUserLoggedIn(): Pair<FirebaseUser?, String?> {
        var firebaseUser: FirebaseUser? = null
        FirebaseAuth.AuthStateListener { firebaseAuth ->
            firebaseUser = firebaseAuth.currentUser!!
        }
        return Pair(firebaseUser, firebaseAuth.uid.toString())
    }

    fun logout() {
        firebaseAuth.signOut()
    }

}