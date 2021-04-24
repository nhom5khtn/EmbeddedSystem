package demo.embeddedsystem.model

import android.net.wifi.ScanResult
import android.os.Parcel
import android.os.Parcelable
import java.sql.Types.NULL

class WifiDataNetwork : Comparable<WifiDataNetwork?>, Parcelable {
    var bssid: String?
    var ssid: String?
    var capabilities: String?
    var frequency: Int
    var level: Int
    var timestamp: Long

    constructor(result: ScanResult) {
        bssid = result.BSSID
        ssid = result.SSID
        capabilities = result.capabilities
        frequency = result.frequency
        level = result.level
        timestamp = System.currentTimeMillis()
    }

    constructor(`in`: Parcel) {
        bssid = `in`.readString()
        ssid = `in`.readString()
        capabilities = `in`.readString()
        frequency = `in`.readInt()
        level = `in`.readInt()
        timestamp = `in`.readLong()
    }

    override operator fun compareTo(another: WifiDataNetwork?): Int {
        if (another != null) {
            return another.level - this.level
        }
        return NULL
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(bssid)
        dest.writeString(ssid)
        dest.writeString(capabilities)
        dest.writeInt(frequency)
        dest.writeInt(level)
        dest.writeLong(timestamp)
    }

    override fun toString(): String {
        return ssid + " addr:" + bssid + " lev:" + level + "dBm freq:" + frequency + "MHz cap:" + capabilities
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<WifiDataNetwork?> =
            object : Parcelable.Creator<WifiDataNetwork?> {
                override fun createFromParcel(`in`: Parcel): WifiDataNetwork? {
                    return WifiDataNetwork(`in`)
                }

                override fun newArray(size: Int): Array<WifiDataNetwork?> {
                    return arrayOfNulls(size)
                }
            }

        /**
         * Converts a WiFi frequency to the corresponding channel.
         *
         * @param freq
         * frequency as given by
         * [frequency][ScanResult]
         * @return the channel associated with the given frequency
         */
        fun convertFrequencyToChannel(freq: Int): Int {
            return if (freq in 2412..2484) {
                (freq - 2412) / 5 + 1
            } else if (freq in 5170..5825) {
                (freq - 5170) / 5 + 34
            } else {
                -1
            }
        }
    }
}
