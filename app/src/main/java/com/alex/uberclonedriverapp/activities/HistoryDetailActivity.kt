package com.alex.uberclonedriverapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alex.uberclonedriverapp.R
import com.alex.uberclonedriverapp.databinding.ActivityHistoryDetailBinding
import com.alex.uberclonedriverapp.models.Client
import com.alex.uberclonedriverapp.models.History
import com.alex.uberclonedriverapp.providers.ClientProvider
import com.alex.uberclonedriverapp.providers.HistoryProvider
import com.alex.uberclonedriverapp.utils.Config
import com.alex.uberclonedriverapp.utils.RelativeTime
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.toObject

class HistoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryDetailBinding
    private var historyProvider = HistoryProvider()
    private var clientProvider = ClientProvider()
    private var extraId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Config.setVersionCompatibilityStatusBar(window)

        extraId = intent.getStringExtra("id")!!
        getHistory()

        binding.ivBack.setOnClickListener { finish() }
    }

    private fun getHistory(){
        historyProvider.getHistoryById(extraId).addOnSuccessListener { document ->
            if (document.exists()){
                val history = document.toObject(History::class.java)
                binding.tvOrigin.text = history?.origin
                binding.tvDestination.text = history?.destination
                binding.tvDate.text = RelativeTime.getTimeAgo(history?.timestamp!!, this@HistoryDetailActivity)
                binding.tvPrice.text = "$${String.format("%.1f", history?.price)}"
                binding.tvMyCalification.text = "${history?.calificationToDriver}"
                binding.tvClientCalification.text = "${history?.calificationToClient}"
                binding.tvTimeAndDistance.text = "${history?.time} Min - ${String.format("%.1f", history?.km)} Km"

                getClientInfo(history?.idClient!!)
            }
        }
    }

    private fun getClientInfo(id: String){
        clientProvider.getClientById(id).addOnSuccessListener { document ->
            if (document.exists()){
                val client = document.toObject(Client::class.java)
                binding.tvEmail.text = client?.email
                binding.tvName.text = "${client?.name} ${client?.lastname}"

                if (client?.image != null){
                    if (client?.image != ""){
                        Glide.with(this).load(client?.image).into(binding.cvProfileImage)
                    }
                }
            }
        }
    }
}