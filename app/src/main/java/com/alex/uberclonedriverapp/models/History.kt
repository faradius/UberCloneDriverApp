package com.alex.uberclonedriverapp.models

// To parse the JSON, install Klaxon and do:
//
//   val prices = Prices.fromJson(jsonString)

import com.beust.klaxon.*

private val klaxon = Klaxon()

data class History (
    val id: String? = null,
    val idClient: String? = null,
    val idDriver: String? = null,
    val origin: String? = null,
    val destination: String? = null,
    var calificationToClient: Double? = null,
    var calificationToDriver: Double? = null,
    val time: Int? = null,
    val km: Double? = null,
    val originLat: Double? = null,
    val originLng: Double? = null,
    val destinationLat: Double? = null,
    val destinationLng: Double? = null,
    val price: Double? = null,
    val timestamp: Long? = null
) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Booking>(json)
    }
}