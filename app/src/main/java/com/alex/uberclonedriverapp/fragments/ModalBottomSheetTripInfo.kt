package com.alex.uberclonedriverapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alex.uberclonedriverapp.R
import com.alex.uberclonedriverapp.activities.*
import com.alex.uberclonedriverapp.models.Driver
import com.alex.uberclonedriverapp.providers.AuthProvider
import com.alex.uberclonedriverapp.providers.DriverProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ModalBottomSheetTripInfo: BottomSheetDialogFragment() {

    val driverProvider = DriverProvider()
    val authProvider = AuthProvider()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ):View?{
        val view = inflater.inflate(R.layout.modal_bottom_sheet_trip_info, container, false)


        //getDriver()

        return view
    }

    private fun goToProfile(){
        val i = Intent(activity, ProfileActivity::class.java)
        startActivity(i)
    }

    private fun goToHistories(){
        val i = Intent(activity, HistoriesActivity::class.java)
        startActivity(i)
    }

    private fun goToMain(){
        authProvider.logout()
        val i = Intent(activity, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    private fun getDriver(){
        driverProvider.getDriver(authProvider.getId()).addOnSuccessListener { document ->
            if (document.exists()){
                val driver = document.toObject(Driver::class.java)
//                tvUserName?.text = "${driver?.name} ${driver?.lastname}"
            }
        }
    }

    companion object{
        const val TAG = "ModalBottomSheetTripInfo"
    }
}