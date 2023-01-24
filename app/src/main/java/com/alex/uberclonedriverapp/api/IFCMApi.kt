package com.alex.uberclonedriverapp.api

import com.alex.uberclonedriverapp.models.FCMBody
import com.alex.uberclonedriverapp.models.FCMResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMApi {

    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAkoLHlNg:APA91bGxc4DiIQEYYtxA1VtEkf8e5av3li4GNb8ZoRKQHS9J3IR5EhzhZGrjpEnXOvgMS3P645Nmh9tKaysJOkzs8Z0xP05mZtA-fpHBaO84mLW2aqzPRKDzkKreCZhlu15TYK9vaLp4"
    )
    @POST("fcm/send")
    fun send(@Body body: FCMBody): Call<FCMResponse>
}