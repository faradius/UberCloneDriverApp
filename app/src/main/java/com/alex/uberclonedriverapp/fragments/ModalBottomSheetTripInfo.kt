package com.alex.uberclonedriverapp.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.alex.uberclonedriverapp.R
import com.alex.uberclonedriverapp.activities.*
import com.alex.uberclonedriverapp.models.Booking
import com.alex.uberclonedriverapp.models.Client
import com.alex.uberclonedriverapp.models.Driver
import com.alex.uberclonedriverapp.providers.AuthProvider
import com.alex.uberclonedriverapp.providers.ClientProvider
import com.alex.uberclonedriverapp.providers.DriverProvider
import com.alex.uberclonedriverapp.utils.Constants
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.hdodenhof.circleimageview.CircleImageView

class ModalBottomSheetTripInfo: BottomSheetDialogFragment() {

    private var client: Client? = null
    private lateinit var booking: Booking
    val clientProvider = ClientProvider()
    val authProvider = AuthProvider()

    var tvClientName: TextView? = null
    var tvOrigin: TextView? = null
    var tvDestination: TextView? = null
    var ivPhone: ImageView? = null
    var cvProfileClient: CircleImageView? = null

    val REQUEST_PHONE_CALL = 30


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ):View?{
        val view = inflater.inflate(R.layout.modal_bottom_sheet_trip_info, container, false)
        tvClientName = view.findViewById(R.id.tvClientName)
        tvOrigin = view.findViewById(R.id.tvOrigin)
        tvDestination = view.findViewById(R.id.tvDestination)
        ivPhone = view.findViewById(R.id.ivPhone)
        cvProfileClient = view.findViewById(R.id.cvProfileClient)

        //getDriver()
        val data = arguments?.getString(Constants.BOOKING)
        booking = Booking.fromJson(data!!)!!

        tvOrigin?.text = booking.origin
        tvDestination?.text = booking.destination
        ivPhone?.setOnClickListener {
            if (client?.phone != null){

                if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), REQUEST_PHONE_CALL)
                }
                call(client?.phone!!)
            }
        }

        getClientInfo()
        return view
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PHONE_CALL){
            if(client?.phone != null){
                call(client?.phone!!)
            }
        }
    }
    private fun call(phone:String){
        val i = Intent(Intent.ACTION_CALL)
        i.data = Uri.parse("tel:$phone")

        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            return
        }

        requireActivity().startActivity(i)
    }

    private fun getClientInfo(){
        clientProvider.getClientById(booking?.idClient!!).addOnSuccessListener { document ->
            if (document.exists()){
                client = document.toObject(Client::class.java)
                tvClientName?.text = "${client?.name} ${client?.lastname}"

                if (client?.image != null){
                    if (client?.image != ""){
                        Glide.with(requireActivity()).load(client?.image).into(cvProfileClient!!)
                    }
                }
//                tvUserName?.text = "${driver?.name} ${driver?.lastname}"
            }
        }
    }

    companion object{
        const val TAG = "ModalBottomSheetTripInfo"
    }
}