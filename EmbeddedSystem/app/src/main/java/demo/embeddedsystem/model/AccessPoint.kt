package demo.embeddedsystem.model

import android.os.Parcel
import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

class AccessPoint : RealmObject, Parcelable {
    @PrimaryKey
    var id: String? = UUID.randomUUID().toString()
    var createdAt = Date()
    var bssid //identifier
            : String? = null
    var description: String? = null
    var ssid: String? = null
    var mac_address: String? = null
    var x = 0.0
    var y = 0.0
    var sequence = 0
    var meanRss //for RP (-50 to -100)
            = 0.0

    //    High quality: 90% ~= -55db
    //    Medium quality: 50% ~= -75db
    //    Low quality: 30% ~= -85db
    //    Unusable quality: 8% ~= -96db
    constructor() {}
    constructor(another: AccessPoint) {
        id = UUID.randomUUID().toString()
        createdAt = Calendar.getInstance().time
        bssid = another.bssid
        description = another.description
        ssid = another.ssid
        mac_address = another.mac_address
        x = another.x
        y = another.y
        sequence = another.sequence
        meanRss = another.meanRss
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(id)
        parcel.writeString(bssid)
        parcel.writeString(description)
        parcel.writeString(ssid)
        parcel.writeString(mac_address)
        parcel.writeDouble(x)
        parcel.writeDouble(y)
        parcel.writeInt(sequence)
        parcel.writeDouble(meanRss)
    }

    protected constructor(`in`: Parcel) {
        id = `in`.readString()
        bssid = `in`.readString()
        description = `in`.readString()
        ssid = `in`.readString()
        mac_address = `in`.readString()
        x = `in`.readDouble()
        y = `in`.readDouble()
        sequence = `in`.readInt()
        meanRss = `in`.readDouble()
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<AccessPoint?> = object : Parcelable.Creator<AccessPoint?> {
            override fun createFromParcel(`in`: Parcel): AccessPoint? {
                return AccessPoint(`in`)
            }

            override fun newArray(size: Int): Array<AccessPoint?> {
                return arrayOfNulls(size)
            }
        }
    }
}
