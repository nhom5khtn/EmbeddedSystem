package demo.embeddedsystem.utils

import demo.embeddedsystem.model.WifiData

//class AppContants {
//
//    //Reference points
//    private val FETCH_INTERVAL = 3000 //3 secs
//
//    private val READINGS_BATCH = 10 //10 values in every 3 secs
//
//
//    private val NaN = -110.0f //RSSI value for no reception
//
//
//    private val INTENT_FILTER = "ANDROID_WIFI_SCANNER"
//    private val WIFI_DATA = "WIFI_DATA"
//
//    fun getFetchInterval(): Int {
//        return FETCH_INTERVAL
//    }
//
//    fun getReadingBatch(): Int {
//        return READINGS_BATCH
//    }
//
//    @JvmName("getNaN1")
//    fun getNaN(): Float {
//        return NaN
//    }
//
//    fun getIntentFilter():String {
//        return INTENT_FILTER
//    }
//
//    fun getWifiData():String {
//        return WIFI_DATA
//    }
//
//}



public class AppContants private constructor(){

    init {
        println("Initialize const data ... ")
    }

    companion object {

        val FETCH_INTERVAL:Int? = 3000
        val READINGS_BATCH:Int? = 10
        val NaN:Float? = -110.0f
        val INTENT_FILTER:String? = "ANDROID_WIFI_SCANNER"
        val WIFI_DATA:String? = "WIFI_DATA"

        init{
            println("---CONSTANT PARAMETERS---")
        }

    }

}