package com.example.realtimeobjectcounter.ViewModel

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimeobjectcounter.Repository.AuthenticationRepository
import com.example.realtimeobjectcounter.Utils.Resource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthenticationViewModel(context: Context) : ViewModel() {

    private val _authStatus = MutableLiveData<Resource<AuthResult>>()
    val authStatus: LiveData<Resource<AuthResult>> = _authStatus

    private val authenticationRepository = AuthenticationRepository(context = context)

    fun createUser(
        userName: String,
        userEmailAddress: String,
        userLoginPassword: String,
        dataOfBirth: String
    ) {
        val error =
            if (userEmailAddress.isEmpty() || userName.isEmpty() || userLoginPassword.isEmpty() || dataOfBirth.isEmpty()) {
                "Empty Strings"
            } else if (!Patterns.EMAIL_ADDRESS.matcher(userEmailAddress).matches()) {
                "Not a valid Email"
            } else null

        error?.let {
            _authStatus.postValue(Resource.Error(it))
            return
        }
        _authStatus.postValue(Resource.Loading())

        viewModelScope.launch(Dispatchers.Main) {
            val registerResult = authenticationRepository.createUser(
                userName,
                userEmailAddress,
                userLoginPassword,
                dataOfBirth
            )
            _authStatus.postValue(registerResult)
        }
    }

    fun loginInUser(userEmailAddress: String, userLoginPassword: String) {
        if (userEmailAddress.isEmpty() || userLoginPassword.isEmpty()) {
            _authStatus.postValue(Resource.Error("Empty Strings"))
        } else {
            _authStatus.postValue(Resource.Loading())
            viewModelScope.launch(Dispatchers.Main) {
                val loginResult =
                    authenticationRepository.login(userEmailAddress, userLoginPassword)
                _authStatus.value = Resource.Success(loginResult.data!!)
            }
        }
    }

    private val _loginStatus = MutableLiveData<Resource<Pair<FirebaseUser?, String?>>>()
    val loginStatus: LiveData<Resource<Pair<FirebaseUser?, String?>>> = _loginStatus

    fun checkLoginStatus() {
        val firebaseUser = authenticationRepository.isUserLoggedIn().first
        val userId = authenticationRepository.isUserLoggedIn().second
        _loginStatus.postValue(Resource.Success(Pair(firebaseUser, userId.toString())))
    }

    fun logout() {
        authenticationRepository.logout()
    }

}