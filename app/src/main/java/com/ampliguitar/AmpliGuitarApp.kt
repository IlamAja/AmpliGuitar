package com.ampliguitar

import android.app.Application
import com.ampliguitar.data.AuthRepository
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Kelas Application utama.
 * Inisialisasi otomatis Firebase diaktifkan kembali di AndroidManifest.
 * Kelas ini sekarang hanya bertanggung jawab untuk konfigurasi tambahan seperti App Check.
 */
class AmpliGuitarApp : Application() {
    private val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // FirebaseApp.initializeApp() tidak lagi diperlukan karena inisialisasi otomatis sudah aktif.

        // Konfigurasi Firebase App Check.
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        if (BuildConfig.DEBUG) {
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }

        // Jalankan tugas latar belakang. Kode ini sekarang aman untuk dijalankan.
        applicationScope.launch {
            AuthRepository.createDefaultAdminIfNeeded()
        }
    }
}
