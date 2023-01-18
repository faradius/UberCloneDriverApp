package com.alex.uberclonedriverapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alex.uberclonedriverapp.R
import com.alex.uberclonedriverapp.databinding.ActivityProfileBinding
import com.alex.uberclonedriverapp.models.Driver
import com.alex.uberclonedriverapp.providers.AuthProvider
import com.alex.uberclonedriverapp.providers.DriverProvider
import com.alex.uberclonedriverapp.utils.Config

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    val driverProvider = DriverProvider()
    val authProvider = AuthProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Config.setVersionCompatibilityStatusBar(window)

        getDriver()
        binding.ivBack.setOnClickListener { finish() }
    }

    private fun getDriver(){
        driverProvider.getDriver(authProvider.getId()).addOnSuccessListener { document ->
            if (document.exists()){
                val driver = document.toObject(Driver::class.java)
                binding.tvEmail.text = driver?.email
                binding.etName.setText(driver?.name)
                binding.etLastName.setText(driver?.lastname)
                binding.etPhone.setText(driver?.phone)
                binding.etCarBrand.setText(driver?.brandCar)
                binding.etCarColor.setText(driver?.colorCar)
                binding.etCarPlate.setText(driver?.plateNumber)
            }
        }
    }
}