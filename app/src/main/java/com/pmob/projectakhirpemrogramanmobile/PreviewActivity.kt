package com.pmob.projectakhirpemrogramanmobile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.pmob.projectakhirpemrogramanmobile.databinding.ActivityPreviewBinding

class PreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra("BOOK_TITLE")
        val author = intent.getStringExtra("BOOK_AUTHOR")
        val cover = intent.getStringExtra("BOOK_COVER")
        val rating = intent.getDoubleExtra("BOOK_RATING", 0.0)
        val pages = intent.getIntExtra("BOOK_PAGES", 0)
        val synopsis = intent.getStringExtra("BOOK_SYNOPSIS")

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.tvTitle.text = title
        binding.tvAuthor.text = author
        binding.tvRating.text = String.format("%.1f", rating)
        binding.tvPages.text = pages.toString()
        binding.tvSynopsis.text = synopsis

        Glide.with(this)
            .load(cover)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(binding.ivCover)

        binding.btnStartRead.setOnClickListener {
            Intent(this, ReadActivity::class.java).apply {
                putExtra("BOOK_TITLE", title)
                putExtra("BOOK_CONTENT", synopsis) // WAJIB
                startActivity(this)
            }
        }
    }
}
