package com.ampliguitar

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ampliguitar.auth.LoginActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Panggil installSplashScreen() SEBELUM super.onCreate().
        // Ini akan menampilkan tema splash screen yang sudah kita atur.
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Setelah splash screen selesai, langsung arahkan ke LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Selesaikan MainActivity agar tidak ada di back stack
    }
}
