package com.alex.uberclonedriverapp.activities

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alex.uberclonedriverapp.R
import com.alex.uberclonedriverapp.databinding.ActivityMapBinding
import com.alex.uberclonedriverapp.fragments.ModalBottomSheetBooking
import com.alex.uberclonedriverapp.models.Booking
import com.alex.uberclonedriverapp.providers.AuthProvider
import com.alex.uberclonedriverapp.providers.BookingProvider
import com.alex.uberclonedriverapp.providers.GeoProvider
import com.alex.uberclonedriverapp.utils.Config
import com.alex.uberclonedriverapp.utils.Constants
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.ListenerRegistration

class MapActivity : AppCompatActivity(), OnMapReadyCallback,Listener {
    private var bookingListener: ListenerRegistration? = null
    private val TAG = "LOCALIZACIÓN"

    private lateinit var binding: ActivityMapBinding
    private var googleMap:GoogleMap? = null
    var easyWayLocation:EasyWayLocation? = null
    private var myLocationLatLng: LatLng? = null
    private var markerDriver: Marker? = null
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private val bookingProvider = BookingProvider()
    private val modalBooking = ModalBottomSheetBooking()

    private val timer = object: CountDownTimer(30000,1000){
        override fun onTick(counter: Long) {
            Log.d("TIMER", "Counter: $counter")
        }

        override fun onFinish() {
            Log.d("TIMER", "ON FINISH")
            modalBooking.dismiss()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Config.setVersionCompatibilityStatusBar(window)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f

        }

        easyWayLocation = EasyWayLocation(this, locationRequest, false, false,this)

        locationPermission.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        listenerBooking()

        binding.btnConnect.setOnClickListener { connectDriver() }
        binding.btnDisconnect.setOnClickListener { disconnectDriver() }

    }

    val locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permission ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            when {
                permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.d(TAG, "Permiso concedido")
//                    easyWayLocation?.startLocation()
                    checkIfDriverisConnected()
                }
                permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.d(TAG, "Permiso concedido con limitación")
//                    easyWayLocation?.startLocation()
                    checkIfDriverisConnected()
                }
                else -> {
                    Log.d(TAG, "Permiso no concedido")
                }
            }
        }
    }

    private fun showModalBooking(booking: Booking){
        //Se envian los datos en formato json al fragment
        val bundle = Bundle()
        bundle.putString(Constants.BOOKING, booking.toJson())
        modalBooking.arguments = bundle

        modalBooking.show(supportFragmentManager, ModalBottomSheetBooking.TAG)
        timer.start()
    }

    private fun listenerBooking(){
        bookingListener = bookingProvider.getBooking().addSnapshotListener { snapshot, error ->
            if (error != null){
                Log.d("FIRESTORE", "Error: ${error.message}")
                //Deje de escuchar cuando haya encontrado un error
                return@addSnapshotListener
            }

            if (snapshot != null){
                if (snapshot.documents.size > 0){
                    val booking = snapshot.documents[0].toObject(Booking::class.java)
                    if(booking?.status == "create"){
                        Log.d("FIRESTORE", "DATA: ${booking?.toJson()}")
                        showModalBooking(booking!!)
                    }

                }
            }
        }
    }

    private fun checkIfDriverisConnected(){
        geoProvider.getLocation(authProvider.getId()).addOnSuccessListener { document->
            if (document.exists()){
                if (document.contains("l")){
                    connectDriver()
                }else{
                    showButtonConnect()
                }
            }else{
                showButtonConnect()
            }
        }
    }

    private fun saveLocation(){
        if (myLocationLatLng != null){
            geoProvider.saveLocation(authProvider.getId(), myLocationLatLng!!)
        }
    }

    private fun disconnectDriver(){
        easyWayLocation?.endUpdates()
        if (myLocationLatLng != null){
            geoProvider.removeLocation(authProvider.getId())
            showButtonConnect()
        }
    }

    private fun connectDriver(){
        easyWayLocation?.endUpdates()
        easyWayLocation?.startLocation()
        showButtonDisconnect()
    }

    private fun showButtonConnect(){
        binding.btnDisconnect.visibility = View.GONE
        binding.btnConnect.visibility = View.VISIBLE

    }

    private fun showButtonDisconnect(){
        binding.btnDisconnect.visibility = View.VISIBLE
        binding.btnConnect.visibility = View.GONE

    }

    private fun addMarker(){
        val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.uber_car)
        val markerIcon = getMarkerFromDrawable(drawable!!)
        if (markerDriver != null){
            markerDriver?.remove() //No redibujar el icono
        }

        if (myLocationLatLng != null){
            markerDriver = googleMap?.addMarker(
                MarkerOptions()
                    .position(myLocationLatLng!!)
                    .anchor(0.5f,0.5f)
                    .flat(true)
                    .icon(markerIcon))
        }
    }

    private fun getMarkerFromDrawable(drawable: Drawable): BitmapDescriptor{
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            70,
            150,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0,0,70,150)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        easyWayLocation?.endUpdates()
        bookingListener?.remove()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
//        easyWayLocation?.startLocation()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        //Desactivar el marcador por defecto de google
        googleMap?.isMyLocationEnabled = false

        try{
            val success = googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this,R.raw.style)
            )
            
            if (!success!!){
                Log.d(TAG, "onMapReady: No se pudo encontrar el estilo")
            }
            
        }catch (e: Resources.NotFoundException){
            Log.d(TAG, "Error: ${e.toString()}")
        }
    }

    override fun locationOn() {

    }

    //Actualización de la posición en tiempo real
    override fun currentLocation(location: Location) {
        //Obteniendo la latitud y longitud de la posición actual
        myLocationLatLng = LatLng(location.latitude, location.longitude)

        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
            CameraPosition.builder().target(myLocationLatLng!!).zoom(17f).build()
        ))

        addMarker()
        saveLocation()
    }

    override fun locationCancelled() {

    }


}