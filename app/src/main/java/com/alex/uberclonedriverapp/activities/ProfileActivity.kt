package com.alex.uberclonedriverapp.activities

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.alex.uberclonedriverapp.R
import com.alex.uberclonedriverapp.databinding.ActivityProfileBinding
import com.alex.uberclonedriverapp.models.Driver
import com.alex.uberclonedriverapp.providers.AuthProvider
import com.alex.uberclonedriverapp.providers.DriverProvider
import com.alex.uberclonedriverapp.utils.Config
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    val driverProvider = DriverProvider()
    val authProvider = AuthProvider()

    private var imageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Config.setVersionCompatibilityStatusBar(window)

        getDriver()
        binding.ivBack.setOnClickListener { finish() }
        binding.btnUpdate.setOnClickListener { updateInfo() }
        binding.cvProfileImage.setOnClickListener { selectImage() }
    }

    private fun updateInfo(){
        val name = binding.etName.text.toString()
        val lastname = binding.etLastName.text.toString()
        val phone = binding.etPhone.text.toString()
        val carBrand = binding.etCarBrand.text.toString()
        val carColor = binding.etCarColor.text.toString()
        val carPlate = binding.etCarPlate.text.toString()

        val driver = Driver(
            id = authProvider.getId(),
            name = name,
            lastname = lastname,
            phone = phone,
            colorCar = carColor,
            brandCar = carBrand,
            plateNumber = carPlate
        )

        if (imageFile != null){
            driverProvider.uploadImage(authProvider.getId(), imageFile!!).addOnSuccessListener { taskSnapshot->
                driverProvider.getImageUrl().addOnSuccessListener { url ->
                    val imageUrl = url.toString()
                    driver.image = imageUrl
                    driverProvider.update(driver).addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(this@ProfileActivity, "Datos actualizados correctamente", Toast.LENGTH_LONG).show()
                        }else{
                            Toast.makeText(this@ProfileActivity, "No se pudo actualizar la información", Toast.LENGTH_LONG).show()
                        }
                    }
                    Log.d("STORAGE", "$imageUrl")
                }
            }
        }else{
            driverProvider.update(driver).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(this@ProfileActivity, "Datos actualizados correctamente", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this@ProfileActivity, "No se pudo actualizar la información", Toast.LENGTH_LONG).show()
                }
            }
        }


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

                if(driver?.image != null){
                    if (driver?.image != ""){
                        Glide.with(this).load(driver?.image).into(binding.cvProfileImage)
                    }
                }
            }
        }
    }

    private val startImageForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK){
            val fileUri = data?.data
            imageFile = File(fileUri?.path)
            binding.cvProfileImage.setImageURI(fileUri)
        }
        else if (resultCode == ImagePicker.RESULT_ERROR){
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(this, "Tarea cancelada", Toast.LENGTH_LONG).show()
        }
    }
    private fun selectImage(){
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }
}