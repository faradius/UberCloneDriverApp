package com.alex.uberclonedriverapp.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.alex.uberclonedriverapp.R
import com.alex.uberclonedriverapp.activities.MainActivity
import com.alex.uberclonedriverapp.activities.MapActivity
import com.alex.uberclonedriverapp.activities.MapTripActivity
import com.alex.uberclonedriverapp.activities.ProfileActivity
import com.alex.uberclonedriverapp.models.Booking
import com.alex.uberclonedriverapp.models.Driver
import com.alex.uberclonedriverapp.providers.AuthProvider
import com.alex.uberclonedriverapp.providers.BookingProvider
import com.alex.uberclonedriverapp.providers.DriverProvider
import com.alex.uberclonedriverapp.providers.GeoProvider
import com.alex.uberclonedriverapp.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ModalBottomSheetMenu: BottomSheetDialogFragment() {

    val driverProvider = DriverProvider()
    val authProvider = AuthProvider()

    var tvUserName: TextView? = null
    var lyLogout: LinearLayout? = null
    var lyProfile: LinearLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ):View?{
        val view = inflater.inflate(R.layout.modal_bottom_sheet_menu, container, false)
        tvUserName = view.findViewById(R.id.tvUserName)
        lyLogout = view.findViewById(R.id.lyLogout)
        lyProfile = view.findViewById(R.id.lyProfile)

        getDriver()
        lyLogout?.setOnClickListener { goToMain() }
        lyProfile?.setOnClickListener { goToProfile() }
        return view
    }

    private fun goToProfile(){
        val i = Intent(activity, ProfileActivity::class.java)
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
                tvUserName?.text = "${driver?.name} ${driver?.lastname}"
            }
        }
    }

    companion object{
        const val TAG = "ModalBottomSheetMenu"
    }
}