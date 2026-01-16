package com.ampliguitar

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.ampliguitar.auth.LoginActivity

class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val guitarIcon = findViewById<ImageView>(R.id.guitar_icon)
        val appNameText = findViewById<TextView>(R.id.app_name_text)

        // Animasi fade-in sederhana untuk ikon dan teks
        val iconFadeIn = ObjectAnimator.ofFloat(guitarIcon, View.ALPHA, 0f, 1f)
        val textFadeIn = ObjectAnimator.ofFloat(appNameText, View.ALPHA, 0f, 1f)

        val animatorSet = AnimatorSet().apply {
            playTogether(iconFadeIn, textFadeIn)
            duration = 1500 // Durasi 1.5 detik
        }

        animatorSet.start()

        // Pindah ke aktivitas berikutnya setelah animasi selesai
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 2000) // Beri sedikit jeda setelah animasi
    }
}
