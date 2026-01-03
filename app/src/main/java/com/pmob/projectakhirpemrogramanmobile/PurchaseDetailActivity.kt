package com.pmob.projectakhirpemrogramanmobile

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pmob.projectakhirpemrogramanmobile.databinding.ActivityPurchaseDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PurchaseDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPurchaseDetailBinding
    private var purchase: Purchase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPurchaseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil purchase dari intent
        purchase = intent.getParcelableExtra("PURCHASE")

        if (purchase == null) {
            Toast.makeText(this, "Data pembelian tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupUI() {
        val p = purchase ?: return

        binding.tvTitle.text = p.title

        binding.tvPrice.text = formatRupiah(p.price)

        binding.tvMethod.text = p.method.ifEmpty { "QRIS" }

        binding.tvStatus.text = p.status.uppercase()

        when (p.status.lowercase()) {
            "success" -> binding.tvStatus.setTextColor(0xFF4CAF50.toInt())
            "pending" -> binding.tvStatus.setTextColor(0xFFFFA500.toInt())
            "failed" -> binding.tvStatus.setTextColor(0xFFFF0000.toInt())
            else -> binding.tvStatus.setTextColor(0xFF666666.toInt())
        }

        binding.tvDate.text = formatDate(p.timestamp)

        binding.tvOrderId.text = p.orderId.ifEmpty { p.id }
    }

    private fun formatRupiah(value: Double): String {
        val localeID = Locale("in", "ID")
        val formatter = NumberFormat.getCurrencyInstance(localeID)
        return formatter.format(value)
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy • HH:mm:ss", Locale("in", "ID"))
        return sdf.format(Date(timestamp))
    }
}
