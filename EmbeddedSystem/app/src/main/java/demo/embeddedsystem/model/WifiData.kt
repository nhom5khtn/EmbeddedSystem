package demo.embeddedsystem.model

import android.net.wifi.ScanResult
import android.os.Parcel
import android.os.Parcelable
import java.util.*

class WifiData : Parcelable {
    private var mNetworks: MutableList<WifiDataNetwork>? = null

    constructor() {
        mNetworks = ArrayList<WifiDataNetwork>()
    }

    constructor(`in`: Parcel) {
        `in`.readTypedList(mNetworks!! as List<WifiDataNetwork?>, WifiDataNetwork.CREATOR)
    }

    /**
     * Stores the last WiFi scan performed by manager & creating an object
     * for each network detected.
     *
     * @param results
     * list of networks detected
     */
    fun addNetworks(results: List<ScanResult?>) {
        mNetworks!!.clear()
        for (result in results) {
            result?.let { WifiDataNetwork(it) }?.let { mNetworks!!.add(it) }
        }
        Collections.sort(mNetworks)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeTypedList(mNetworks)
    }

    /**
     * @return Returns a string containing a concise, human-readable description
     * of this object.
     */
    override fun toString(): String {
        return if (mNetworks == null || mNetworks!!.size == 0) "Empty data" else mNetworks!!.size.toString() + " networks data"
    }

    /**
     * @return Returns the list of scanned networks
     */
    val networks: List<Any>?
        get() = mNetworks

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<WifiData?> = object : Parcelable.Creator<WifiData?> {
            override fun createFromParcel(`in`: Parcel): WifiData? {
                return WifiData(`in`)
            }

            override fun newArray(size: Int): Array<WifiData?> {
                return arrayOfNulls(size)
            }
        }
    }
}
