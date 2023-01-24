package com.alex.uberclonedriverapp.providers

import com.alex.uberclonedriverapp.api.IFCMApi
import com.alex.uberclonedriverapp.api.RetrofitClient
import com.alex.uberclonedriverapp.models.FCMBody
import com.alex.uberclonedriverapp.models.FCMResponse
import retrofit2.Call

class NotificationProvider {

    private val URL = "https://fcm.googleapis.com"

    fun sendNotification(body: FCMBody):Call<FCMResponse>{
        return RetrofitClient.getClient(URL).create(IFCMApi::class.java).send(body)
    }
}