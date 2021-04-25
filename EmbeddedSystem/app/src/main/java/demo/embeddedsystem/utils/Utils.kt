package demo.embeddedsystem.utils

import android.content.Context
import android.location.LocationManager
import android.preference.PreferenceManager
import android.util.Log
import demo.embeddedsystem.model.LocDistance
import demo.embeddedsystem.model.LocationWithNearbyPlaces
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.math.sqrt

class Utils {
    fun getDefaultAlgo(context: Context?): String? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString("prefAlgo", "2")
    }


    fun isLocationEnabled(context: Context): Boolean {
        val locManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //GPS enabled
            Log.d("Utils", "isLocationEnabled:" + true)
            true
        } else {
            //GPS disabled
            Log.d("Utils", "isLocationEnabled:" + false)
            false
        }
    }

    fun getTheNearestPoint(loc: LocationWithNearbyPlaces): LocDistance? {
        val places: ArrayList<LocDistance> = loc.getPlaces()
        if (places.size > 0) {
            places.sort()
            return places[0]
        }
        return null
    }

    fun reduceDecimalPlaces(location: String): String {
        val formatter: NumberFormat = DecimalFormat("#0.00")
        val split = location.split(" ").toTypedArray()
        val latValue = java.lang.Double.valueOf(split[0])
        val lonValue = java.lang.Double.valueOf(split[1])
        val latFormat = formatter.format(latValue)
        val lonFormat = formatter.format(lonValue)
        return "$latFormat, $lonFormat"
    }

    fun getTheDistancefromOrigin(location: String): String? {
        val formatter: NumberFormat = DecimalFormat("#0.00")
        val split = location.split(" ").toTypedArray()
        val latValue = java.lang.Double.valueOf(split[0])
        val lonValue = java.lang.Double.valueOf(split[1])
        val distance = sqrt(latValue * latValue + lonValue * lonValue)
        return formatter.format(distance)
    }
}