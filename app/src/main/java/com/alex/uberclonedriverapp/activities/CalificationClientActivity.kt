package com.alex.uberclonedriverapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alex.uberclonedriverapp.R
import com.alex.uberclonedriverapp.databinding.ActivityCalificationClientBinding
import com.alex.uberclonedriverapp.utils.Constants

class CalificationClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalificationClientBinding
    private var extraPrice = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalificationClientBinding.inflate(layoutInflater)
        setContentView(binding.root)
        extraPrice = intent.getDoubleExtra(Constants.PRICE, 0.0)
        binding.tvPrice.text = "Precio: $extraPrice"
    }
}