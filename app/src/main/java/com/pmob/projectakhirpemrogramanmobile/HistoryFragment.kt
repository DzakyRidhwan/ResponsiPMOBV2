package com.pmob.projectakhirpemrogramanmobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pmob.projectakhirpemrogramanmobile.R
import com.pmob.projectakhirpemrogramanmobile.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private lateinit var binding: FragmentHistoryBinding
    private val purchaseList = mutableListOf<Purchase>()
    private lateinit var adapter: PurchaseAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHistoryBinding.bind(view)

        adapter = PurchaseAdapter(purchaseList) { purchase ->

            val intent = Intent(requireContext(), PurchaseDetailActivity::class.java)
            intent.putExtra("PURCHASE", purchase)
            startActivity(intent)
        }
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter

        loadHistory()
    }

    private fun loadHistory() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val dbRef = FirebaseDatabase.getInstance()
            .getReference("purchases")
            .child(uid)

        dbRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                purchaseList.clear()

                for (data in snapshot.children) {
                    val purchase = data.getValue(Purchase::class.java)
                    if (purchase != null) {
                        purchaseList.add(purchase)
                    }
                }

                purchaseList.sortByDescending { it.timestamp }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Gagal memuat riwayat pembelian",
                    Toast.LENGTH_SHORT
                ).show()

                Log.e("HistoryFragment", error.message)
            }

        })
    }
}
