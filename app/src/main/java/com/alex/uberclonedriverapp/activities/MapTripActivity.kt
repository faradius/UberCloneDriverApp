package com.alex.uberclonedriverapp.activities

import androidx.appcompat.app.AppCompatActivity
import com.alex.uberclonedriverapp.R

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alex.uberclonedriverapp.databinding.ActivityMapBinding
import com.alex.uberclonedriverapp.databinding.ActivityMapTripBinding
import com.alex.uberclonedriverapp.fragments.ModalBottomSheetBooking
import com.alex.uberclonedriverapp.fragments.ModalBottomSheetTripInfo
import com.alex.uberclonedriverapp.models.Booking
import com.alex.uberclonedriverapp.models.History
import com.alex.uberclonedriverapp.models.Prices
import com.alex.uberclonedriverapp.providers.*
import com.alex.uberclonedriverapp.utils.Config
import com.alex.uberclonedriverapp.utils.Constants
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.example.easywaylocation.draw_path.DirectionUtil
import com.example.easywaylocation.draw_path.PolyLineDataBean
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.ListenerRegistration
import java.util.Date

class MapTripActivity : AppCompatActivity(), OnMapReadyCallback,Listener, DirectionUtil.DirectionCallBack, SensorEventListener{
    private var totalPrice = 0.0
    private val configProvider = ConfigProvider()
    private var markerDestination: Marker? = null
    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null
    private var booking: Booking? = null
    private var markerOrigin: Marker? = null
    private var bookingListener: ListenerRegistration? = null
    private val TAG = "LOCALIZACIÓN"

    private lateinit var binding: ActivityMapTripBinding
    private var googleMap:GoogleMap? = null
    var easyWayLocation:EasyWayLocation? = null
    private var myLocationLatLng: LatLng? = null
    private var markerDriver: Marker? = null
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private val bookingProvider = BookingProvider()
    private val historyProvider = HistoryProvider()

    private var wayPoints: ArrayList<LatLng> = ArrayList()
    private val WAY_POINT_TAG = "way_point_tag"
    private lateinit var directionUtil: DirectionUtil

    private var isLocationEnabled = false
    private var isCloseToOrigin = false

    //Distancia
    private var meters = 0.0
    private var km = 0.0
    private var currentLocation = Location("")
    private var previusLocation = Location("")
    private var isStartedTrip = false

    //Modal
    private var modalTrip = ModalBottomSheetTripInfo()

    //sensor camera
    private var angle = 0
    private val rotationMatrix = FloatArray(16)
    private var sensorManager: SensorManager? = null
    private var vectSensor: Sensor? = null
    private var declination = 0.0f
    private var isFistTimeOnResume = false
    private var isFistLocation = false

    //Temporizador
    private var counter = 0
    private var min = 0
    private var handler = Handler(Looper.myLooper()!!)
    private var runnable = Runnable {
        kotlin.run {
            counter++

            //Funcionando el Timer
            if (min == 0){
                binding.tvTimer.text = "$counter Seg"
            }else{
                binding.tvTimer.text = "$min Min $counter Seg"
            }

            if (counter == 60){
                min = min + (counter / 60)
                counter = 0
                binding.tvTimer.text = "$min Min $counter Seg"
            }

            startTimer()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapTripBinding.inflate(layoutInflater)
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

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
        vectSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        easyWayLocation = EasyWayLocation(this, locationRequest, false, false,this)

        locationPermission.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        binding.btnStartTrip.setOnClickListener { updateToStarted() }
        binding.btnFinishTrip.setOnClickListener { updateToFinish() }
        binding.ivClientInfo.setOnClickListener { showModalInfo() }
//        binding.btnStartTrip.setOnClickListener { connectDriver() }
//        binding.btnFinishTrip.setOnClickListener { disconnectDriver() }

    }

    val locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permission ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            when {
                permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.d(TAG, "Permiso concedido")
                    easyWayLocation?.startLocation()

                }
                permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.d(TAG, "Permiso concedido con limitación")
                    easyWayLocation?.startLocation()

                }
                else -> {
                    Log.d(TAG, "Permiso no concedido")
                }
            }
        }
    }

    private fun showModalInfo(){

        if(booking != null){
            val bundle = Bundle()
            bundle.putString(Constants.BOOKING, booking?.toJson())
            modalTrip.arguments = bundle
            modalTrip.show(supportFragmentManager, ModalBottomSheetTripInfo.TAG)
        }else{
            Toast.makeText(this, "No se pudo cargar la información", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startTimer(){
        handler.postDelayed(runnable, 1000)
    }

    //Calcular la distancia entre dos puntos
    private fun getDistanceBetween(originLatLng: LatLng, destinationLatLng: LatLng): Float{
        var distance = 0.0f
        val originLocation = Location("")
        val destinationLocation = Location("")

        originLocation.latitude = originLatLng.latitude
        originLocation.longitude = originLatLng.longitude

        destinationLocation.latitude = destinationLatLng.latitude
        destinationLocation.longitude = destinationLatLng.longitude

        //Con esto calculamos la distancia entre dos puntos
        distance = originLocation.distanceTo(destinationLocation)
        return distance
    }

    private fun getBooking(){
        //el metodo get nos trae la información una sola vez, mientras el addSnapshotListener nos va a traer la información en tiempo real
        bookingProvider.getBooking().get().addOnSuccessListener { query->
            if (query != null){
                if (query.size() > 0){
                    booking = query.documents[0].toObject(Booking::class.java)
                    Log.d("FIRESTORE", "BOOKING: ${booking?.toJson()}")
                    originLatLng = LatLng(booking?.originLat!!, booking?.originLng!!)
                    destinationLatLng = LatLng(booking?.destinationLat!!, booking?.destinationLng!!)
                    easyDrawRoute(originLatLng!!)
                    addOriginMarker(originLatLng!!)

                }
            }
        }
    }

    private fun easyDrawRoute(position: LatLng){
        wayPoints.clear()
        wayPoints.add(myLocationLatLng!!)
        wayPoints.add(position)
        directionUtil = DirectionUtil.Builder()
            .setDirectionKey(resources.getString(R.string.google_maps_key))
            .setOrigin(myLocationLatLng!!)
            .setWayPoints(wayPoints)
            .setGoogleMap(googleMap!!)
            .setPolyLinePrimaryColor(R.color.black)
            .setPolyLineWidth(10)
            .setPathAnimation(true)
            .setCallback(this)
            .setDestination(position)
            .build()

        directionUtil.initPath()
    }

    private fun addOriginMarker(position: LatLng){
        markerOrigin = googleMap?.addMarker(MarkerOptions().position(position!!).title("Recoger aqui")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_person)))
    }

    private fun addDestinationMarker(){
        if(destinationLatLng != null){
            markerDestination = googleMap?.addMarker(MarkerOptions().position(destinationLatLng!!).title("Recoger aqui")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_pin)))
        }
    }

    private fun saveLocation(){
        if (myLocationLatLng != null){
            geoProvider.saveLocationWorking(authProvider.getId(), myLocationLatLng!!)
        }
    }

    private fun disconnectDriver(){
        easyWayLocation?.endUpdates()
        if (myLocationLatLng != null){
            geoProvider.removeLocation(authProvider.getId())
        }
    }

    private fun showButtonFinish(){
        binding.btnStartTrip.visibility = View.GONE
        binding.btnFinishTrip.visibility = View.VISIBLE

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

    override fun onDestroy() {
        super.onDestroy()
        easyWayLocation?.endUpdates()
        handler.removeCallbacks(runnable)
        stopSensor()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        startSensor()

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

    private fun updateToStarted(){
        if(isCloseToOrigin){
            bookingProvider.updateStatus(booking?.idClient!!, "started").addOnCompleteListener {
                if (it.isSuccessful){
                    if (destinationLatLng != null){
                        isStartedTrip = true
                        //Eliminar toodo del mapa
                        googleMap?.clear()
                        //Despues llamar al addmarker
                        addMarker()
                        easyDrawRoute(destinationLatLng!!)
                        markerOrigin?.remove()
                        addDestinationMarker()
                        //INICIALIZAR EL CONTADOR
                        startTimer()
                    }
                    showButtonFinish()
                }
            }

        }else{
            Toast.makeText(this, "Debes estar mas cerca a la posición de recogida", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateToFinish(){

        handler.removeCallbacks(runnable) //Detener contador
        isStartedTrip = false
        easyWayLocation?.endUpdates()
        geoProvider.removeLocationWorking(authProvider.getId())

        if(min == 0){
            min = 1
        }
        getPrices(km, min.toDouble())

    }

    private fun createHistory(){
        val history = History(
            idDriver = authProvider.getId(),
            idClient = booking?.idClient,
            origin = booking?.origin,
            destination = booking?.destination,
            originLat = booking?.originLat,
            originLng = booking?.originLng,
            destinationLat = booking?.destinationLat,
            destinationLng = booking?.destinationLng,
            time = min,
            km = km,
            price = totalPrice,
            timestamp = Date().time

        )
        historyProvider.create(history).addOnCompleteListener {
            if (it.isSuccessful){
                bookingProvider.updateStatus(booking?.idClient!!, "finished").addOnCompleteListener {
                    if (it.isSuccessful){
                        goToCalificationClient()
                    }
                }
            }
        }
    }

    private fun getPrices(distance: Double, time: Double){
        configProvider.getPrices().addOnSuccessListener { document ->
            if (document.exists()){
                val prices = document.toObject(Prices::class.java) //Obtenemos la información del documento

                val totalDistance = distance * prices?.km!!  //Valor por kilometro
                Log.d("PRICES", "totalDistance: $totalDistance")

                val totalTime = time * prices.min!!  //Valor por minuto
                Log.d("PRICES", "TotalTime: $totalTime")

                totalPrice = totalDistance + totalTime
                Log.d("PRICES", "total: $totalPrice")

                totalPrice = if(totalPrice < 10.0) prices.minValue!! else totalPrice
                createHistory()

            }
        }
    }

    private fun goToCalificationClient(){
        val i = Intent(this,CalificationClientActivity::class.java)
        i.putExtra(Constants.PRICE, totalPrice)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    override fun locationOn() {

    }

    //Actualización de la posición en tiempo real
    override fun currentLocation(location: Location) {
        //Obteniendo la latitud y longitud de la posición actual
        myLocationLatLng = LatLng(location.latitude, location.longitude)
        currentLocation = location

        if(isStartedTrip){
            meters = meters + previusLocation.distanceTo(currentLocation)
            km = meters / 1000
            binding.tvDistance.text = "${String.format("%.1f", km)} km"
        }

        previusLocation = location

//        if(!isFistLocation){
//            isFistLocation = true
//            googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
//                CameraPosition.builder().target(myLocationLatLng!!).zoom(19f).build()
//            ))
//        }

        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
            CameraPosition.builder().target(myLocationLatLng!!).zoom(19f).build()
        ))

        addDirectionMarker(myLocationLatLng!!, angle)
        saveLocation()

        if (booking != null && originLatLng != null){
            var distance = getDistanceBetween(myLocationLatLng!!, originLatLng!!)
            if (distance <= 100){
                isCloseToOrigin = true
            }
            Log.d("LOCATION", "DISTANCE: ${distance}") //la distancia esta dada en metros
        }

        //Si sigue siendo false
        if (!isLocationEnabled){
            //la pasamos a true
            isLocationEnabled = true
            getBooking()
        }

    }

    override fun locationCancelled() {

    }

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) {
        directionUtil.drawPath(WAY_POINT_TAG)
    }

    private fun updateCamera(bearing: Float){
        val oldPos = googleMap?.cameraPosition
        val pos = CameraPosition.builder(oldPos!!).bearing(bearing).tilt(50f).build()
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(pos))
        if (myLocationLatLng != null){
            addDirectionMarker(myLocationLatLng!!, angle)
        }

    }

    private fun addDirectionMarker(latLng: LatLng, angle: Int){
        val circleDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.ic_up_arrow_circle)
        val markerIcon = getMarkerFromDrawable(circleDrawable!!)
        if(markerDriver != null){
            markerDriver?.remove()
        }
        markerDriver = googleMap?.addMarker(
            MarkerOptions()
                .position(latLng)
                .anchor(0.5f, 0.5f)
                .rotation(angle.toFloat())
                .flat(true)
                .icon(markerIcon)
        )
    }

    private fun getMarkerFromDrawable(drawable: Drawable): BitmapDescriptor{
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            100,
            100,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0,0,100,100)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    //Esto se ejecuta cada vez que se mueve el dispositivo
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR){
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            if (Math.abs(Math.toDegrees(orientation[0].toDouble()) - angle) > 0.8){
                val bearing = Math.toDegrees(orientation[0].toDouble()).toFloat() + declination
                updateCamera(bearing)
            }
            angle = Math.toDegrees(orientation[0].toDouble()).toInt()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    private fun startSensor(){
        if (sensorManager != null){
            sensorManager?.registerListener(this, vectSensor, SensorManager.SENSOR_STATUS_ACCURACY_LOW)
        }
    }

    private fun stopSensor(){
        sensorManager?.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (!isFistTimeOnResume){
            isFistTimeOnResume = true
        }else{
            startSensor()
        }
    }

    override fun onPause() {
        super.onPause()
        stopSensor()
    }
}