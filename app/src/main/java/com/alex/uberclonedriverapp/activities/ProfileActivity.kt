package com.alex.uberclonedriverapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alex.uberclonedriverapp.R
import com.alex.uberclonedriverapp.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}