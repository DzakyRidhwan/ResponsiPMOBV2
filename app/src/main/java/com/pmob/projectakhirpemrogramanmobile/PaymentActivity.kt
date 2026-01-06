package com.pmob.projectakhirpemrogramanmobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.pmob.projectakhirpemrogramanmobile.databinding.ActivityPaymentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding

    private var bookTitle = ""
    private var bookPrice = 0.0
    private var bookCover: String? = null
    private var currentOrderId: String? = null

    private val API_URL = "https://traducianistic-unexaggeratory-tessa.ngrok-free.dev/create_transaction.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookTitle = intent.getStringExtra("BOOK_TITLE") ?: ""
        bookPrice = intent.getDoubleExtra("BOOK_PRICE", 0.0)
        bookCover = intent.getStringExtra("BOOK_COVER")

        binding.tvTitle.text = bookTitle
        binding.tvPrice.text = formatRupiah(bookPrice)
        binding.tvTotal.text = formatRupiah(bookPrice)

        Glide.with(this)
            .load(bookCover)
            .placeholder(R.drawable.ic_book_placeholder)
            .error(R.drawable.ic_book_placeholder)
            .into(binding.ivCover)

        binding.btnPayNow.setOnClickListener {
            Log.d("PaymentActivity", "Button clicked, processing payment...")
            processPayment()
        }
    }

    private fun processPayment() {

        binding.btnPayNow.isEnabled = false
        binding.btnPayNow.text = "Memproses..."

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        currentOrderId = "ORDER-${System.currentTimeMillis()}"

        generateMidtransToken()
    }

    private fun generateMidtransToken() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(API_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                val jsonPayload = JSONObject().apply {
                    put("order_id", currentOrderId)
                    put("gross_amount", bookPrice.toInt())
                    put("title", bookTitle)
                    put("customer_name", FirebaseAuth.getInstance().currentUser?.displayName ?: "Customer")
                    put("customer_email", FirebaseAuth.getInstance().currentUser?.email ?: "customer@example.com")
                }

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonPayload.toString())
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                Log.d("PaymentActivity", "Response Code: $responseCode")
                Log.d("PaymentActivity", "Response: $response")

                val jsonResponse = JSONObject(response)

                withContext(Dispatchers.Main) {
                    if (jsonResponse.getBoolean("success")) {
                        val snapToken = jsonResponse.getString("snap_token")
                        val redirectUrl = jsonResponse.getString("redirect_url")

                        saveOrderToFirebase(snapToken, redirectUrl)

                        openMidtransPayment(redirectUrl)
                    } else {
                        val errorMsg = jsonResponse.optString("message", "Unknown error")
                        Toast.makeText(
                            this@PaymentActivity,
                            "Error: $errorMsg",
                            Toast.LENGTH_LONG
                        ).show()
                        resetButton()
                    }
                }

            } catch (e: Exception) {
                Log.e("PaymentActivity", "Error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@PaymentActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    resetButton()
                }
            }
        }
    }

    private fun saveOrderToFirebase(snapToken: String, redirectUrl: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val db = FirebaseDatabase.getInstance()
            .getReference("orders")
            .child(uid)
            .child(currentOrderId ?: return)

        val orderData = HashMap<String, Any>()
        orderData["order_id"] = currentOrderId ?: ""
        orderData["title"] = bookTitle
        orderData["price"] = bookPrice
        orderData["timestamp"] = System.currentTimeMillis()
        orderData["status_pembayaran"] = "Success"
        orderData["method"] = "Midtrans"
        orderData["snap_token"] = snapToken
        orderData["redirect_url"] = redirectUrl

        db.setValue(orderData)
            .addOnSuccessListener {
                Log.d("PaymentActivity", "Order saved to Firebase")
                savePurchaseHistory(uid)
            }
            .addOnFailureListener { e ->
                Log.e("PaymentActivity", "Failed to save order", e)
            }
    }

    private fun savePurchaseHistory(uid: String) {
        val purchaseDb = FirebaseDatabase.getInstance()
            .getReference("purchases")
            .child(uid)
            .child(currentOrderId ?: return)

        val purchaseData = HashMap<String, Any>()
        purchaseData["id"] = currentOrderId ?: ""
        purchaseData["title"] = bookTitle
        purchaseData["price"] = bookPrice
        purchaseData["timestamp"] = System.currentTimeMillis()
        purchaseData["status"] = "Success"
        purchaseData["method"] = "QRIS"
        purchaseData["orderId"] = currentOrderId ?: ""
        purchaseData["transactionId"] = ""
        purchaseData["bookCover"] = bookCover ?: ""

        purchaseDb.setValue(purchaseData)
            .addOnSuccessListener {
                Log.d("PaymentActivity", "Purchase history saved to Firebase")
            }
            .addOnFailureListener { e ->
                Log.e("PaymentActivity", "Failed to save purchase history", e)
            }
    }

    private fun openMidtransPayment(redirectUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl))
            startActivity(intent)

            Toast.makeText(
                this,
                "Silahkan selesaikan pembayaran",
                Toast.LENGTH_LONG
            ).show()

            resetButton()

        } catch (e: Exception) {
            Log.e("PaymentActivity", "Error opening browser", e)
            Toast.makeText(
                this,
                "Gagal membuka browser: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            resetButton()
        }
    }

    private fun resetButton() {
        binding.btnPayNow.isEnabled = true
        binding.btnPayNow.text = "Bayar Sekarang"
    }

    private fun formatRupiah(value: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return formatter.format(value)
    }
}