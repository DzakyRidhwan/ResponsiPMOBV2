package com.pmob.projectakhirpemrogramanmobile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pmob.projectakhirpemrogramanmobile.databinding.ActivityReadBinding

class ReadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra("BOOK_TITLE") ?: ""
        val content = intent.getStringExtra("BOOK_CONTENT") ?: "Preview content..."

        binding.tvReadTitle.text = title
        binding.tvReadContent.text = content

        binding.ivBackRead.setOnClickListener {
            finish()
        }
    }
}
