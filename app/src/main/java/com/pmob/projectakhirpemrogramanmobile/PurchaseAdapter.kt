package com.pmob.projectakhirpemrogramanmobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PurchaseAdapter(
    private val list: List<Purchase>,
    private val onItemClick: (Purchase) -> Unit = {}
) : RecyclerView.Adapter<PurchaseAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val price: TextView = view.findViewById(R.id.tvPrice)
        val date: TextView = view.findViewById(R.id.tvDate)
        val status: TextView = view.findViewById(R.id.tvStatus)

        fun bind(item: Purchase) {
            title.text = item.title
            price.text = formatRupiah(item.price)
            date.text = formatDate(item.timestamp)
            status.text = item.status.uppercase()

            when (item.status.lowercase()) {
                "success" -> status.setTextColor(0xFF4CAF50.toInt())
                "pending" -> status.setTextColor(0xFFFFA500.toInt())
                "failed" -> status.setTextColor(0xFFFF0000.toInt())
                else -> status.setTextColor(0xFF666666.toInt())
            }

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_purchase, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    private fun formatRupiah(value: Double): String {
        val localeID = Locale("in", "ID")
        val formatter = NumberFormat.getCurrencyInstance(localeID)
        return formatter.format(value)
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("in", "ID"))
        return sdf.format(Date(timestamp))
    }

    override fun getItemCount() = list.size
}
