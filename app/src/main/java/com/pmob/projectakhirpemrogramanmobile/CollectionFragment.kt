package com.pmob.projectakhirpemrogramanmobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.pmob.projectakhirpemrogramanmobile.databinding.FragmentCollectionBinding

class CollectionFragment : Fragment(R.layout.fragment_collection) {

    private lateinit var binding: FragmentCollectionBinding
    private val collectionList = mutableListOf<Purchase>()
    private lateinit var adapter: CollectionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCollectionBinding.bind(view)

        adapter = CollectionAdapter(collectionList) { purchase ->
            val intent = Intent(requireContext(), PurchaseDetailActivity::class.java)
            intent.putExtra("PURCHASE", purchase)
            startActivity(intent)
        }

        binding.rvCollection.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvCollection.adapter = adapter

        loadCollection()
    }

    private fun loadCollection() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("purchases")
            .child(uid)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    collectionList.clear()

                    for (data in snapshot.children) {
                        val purchase = data.getValue(Purchase::class.java)
                        Log.d("COLLECTION", purchase.toString())

                        // FILTER PALING AMAN
                        if (purchase != null &&
                            (purchase.status.equals("success", true)
                                    || purchase.status.equals("settlement", true)
                                    || purchase.status.equals("SUCCESS", true))
                        ) {
                            collectionList.add(purchase)
                        }
                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        requireContext(),
                        "Gagal memuat koleksi",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
