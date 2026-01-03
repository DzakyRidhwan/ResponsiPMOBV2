package com.pmob.projectakhirpemrogramanmobile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import com.pmob.projectakhirpemrogramanmobile.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Default fragment
        loadFragment(HomeFragment())

        // INIT MIDTRANS (SANDBOX)
        SdkUIFlowBuilder.init()
            .setClientKey("SB-Mid-client-s3Fpp8DYlCOiqsAL")
            .setContext(this)
            .setMerchantBaseUrl("https://midtrans-backend-production-2c60.up.railway.app/")
            .enableLog(true)
            .buildSDK()

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_search -> { //
                    loadFragment(CollectionFragment())
                    true
                }
                R.id.nav_history -> {
                    loadFragment(HistoryFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
