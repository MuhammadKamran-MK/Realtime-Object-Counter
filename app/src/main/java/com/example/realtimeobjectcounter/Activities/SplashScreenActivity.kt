package com.example.realtimeobjectcounter.Activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.realtimeobjectcounter.R
import com.example.realtimeobjectcounter.Utils.Constants.UID
import com.example.realtimeobjectcounter.Utils.NetworkChecker
import com.example.realtimeobjectcounter.Utils.Resource
import com.example.realtimeobjectcounter.Utils.SharedPreferenceManager
import com.example.realtimeobjectcounter.ViewModel.AuthenticationViewModel
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var networkChecker: NetworkChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        networkChecker = NetworkChecker(this)

        // This is used to hide the status bar and make
        // the splash screen as a full screen activity.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION") window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        val user = FirebaseAuth.getInstance().currentUser

        if (networkChecker.isOnline()) {

            Handler(
                Looper.getMainLooper()
            ).postDelayed(
                {

                    if (user != null) {
                        // User is signed in, start main activity
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra(UID, user.uid)
                        startActivity(intent)
                        finish()
                    } else {
                        // User is not signed in, start login activity
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }


                }, 2000 // Delayed time in milliseconds.
            )
        }

    }

}