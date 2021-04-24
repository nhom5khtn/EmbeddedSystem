package demo.embeddedsystem.model

import java.util.*

class LocationWithNearbyPlaces(var location: String, places: ArrayList<LocDistance>) {
    private var places: ArrayList<LocDistance>
    fun getPlaces(): ArrayList<LocDistance> {
        return places
    }

    fun setPlaces(places: ArrayList<LocDistance>) {
        this.places = places
    }

    init {
        this.places = places
    }
}
