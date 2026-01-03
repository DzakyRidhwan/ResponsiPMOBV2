package com.pmob.projectakhirpemrogramanmobile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.pmob.projectakhirpemrogramanmobile.databinding.ActivityVerifyEmailBinding

class VerifyEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyEmailBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnContinue.setOnClickListener {
            val user = auth.currentUser

            user?.reload()?.addOnSuccessListener {
                if (user.isEmailVerified) {
                    startActivity(Intent(this, TermsConditionActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Email belum diverifikasi. Silakan cek email Anda.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.btnResend.setOnClickListener {
            auth.currentUser?.sendEmailVerification()
            Toast.makeText(this, "Email verifikasi dikirim ulang", Toast.LENGTH_SHORT).show()
        }
    }
}
