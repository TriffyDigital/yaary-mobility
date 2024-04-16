package com.yaary.yaary_mobility.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yaary.android.mobility.Yaary
import com.yaary.android.mobility.enviroment.Environment
import com.yaary.android.mobility.error.Error
import com.yaary.yaary_mobility.R
import com.yaary.yaary_mobility.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.init.setOnClickListener {
            Yaary.initialise(
                applicationContext,
                "ABHIBUS",
                "ABHIBUSSECRET",
                "yaary.abhibus.sdk",
                Environment.PREPROD,
                object : Yaary.OnSdkInitialiseResultListener {
                    override fun onSuccess() {
                        Toast.makeText(this@MainActivity, "onSuccess", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(error: Error) {
                        Toast.makeText(this@MainActivity, "onFailure", Toast.LENGTH_SHORT).show()
                    }

                }
            )
        }

        binding.authorize.setOnClickListener {
            Yaary.authorize(this, "7289806907", object : Yaary.OnAuthorizeUserResultListener {
                override fun onSuccess(instance: Yaary) {
                    Toast.makeText(this@MainActivity, "onSuccess", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(error: Error) {
                    Toast.makeText(this@MainActivity, "onFailure", Toast.LENGTH_SHORT).show()
                }

            })
        }
    }
}