package com.example.realtimeobjectcounter.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.example.realtimeobjectcounter.R
import com.example.realtimeobjectcounter.Utils.Resource
import com.example.realtimeobjectcounter.ViewModel.AuthenticationViewModel
import com.example.realtimeobjectcounter.databinding.ActivityRegisterBinding
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var authenticationViewModel: AuthenticationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)

        authenticationViewModel = AuthenticationViewModel(this)

        binding.apply {

            btnSingUp.setOnClickListener {
                signUp(name, email, password, dob)
            }

            tvRegister.setOnClickListener {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            }

        }

        bindAuthObservers()

    }

    private fun signUp(
        name: TextInputEditText,
        email: TextInputEditText,
        password: TextInputEditText,
        dob: TextInputEditText
    ) {
        authenticationViewModel.createUser(
            name.text.toString(),
            email.text.toString(),
            password.text.toString(),
            dob.text.toString()
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
                    Toast.makeText(
                        applicationContext,
                        "Registered Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
        }
    }
}