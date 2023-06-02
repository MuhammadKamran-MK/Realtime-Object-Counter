package com.example.realtimeobjectcounter.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.example.realtimeobjectcounter.R
import com.example.realtimeobjectcounter.Utils.Constants
import com.example.realtimeobjectcounter.Utils.Constants.UID
import com.example.realtimeobjectcounter.Utils.Resource
import com.example.realtimeobjectcounter.ViewModel.AuthenticationViewModel
import com.example.realtimeobjectcounter.databinding.ActivityLoginBinding
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authenticationViewModel: AuthenticationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        authenticationViewModel = AuthenticationViewModel(this)

        binding.apply {
            btnLogin.setOnClickListener {
                login(email, password)
            }

            tvLogin.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
        }

        bindAuthObservers()

        // Handle on back press action
        val backPressedCallback = object : OnBackPressedCallback(
            true    // default to enabled
        ) {
            override fun handleOnBackPressed() {
                // Exit the app
                finishAffinity()
            }
        }

        onBackPressedDispatcher.addCallback(
            this@LoginActivity,      // LifecycleOwner
            backPressedCallback
        )

    }

    private fun login(email: TextInputEditText, password: TextInputEditText) {
        authenticationViewModel.loginInUser(
            email.text.toString(),
            password.text.toString()
        )
    }

    private fun bindAuthObservers() {
        authenticationViewModel.authStatus.observe(this) {
            when (it) {
                is Resource.Error -> {
                    binding.spinKitLoader.isVisible = false
                    Toast.makeText(applicationContext, it.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    binding.spinKitLoader.isVisible = true
                }
                is Resource.Success -> {
                    binding.spinKitLoader.isVisible = false
                    Toast.makeText(applicationContext, "Logged In Successfully", Toast.LENGTH_SHORT)
                        .show()
                    startActivity(
                        Intent(this, MainActivity::class.java).putExtra(
                            UID,
                            it.data?.user?.uid
                        )
                    )
                    finish()
                }
            }
        }
    }
}