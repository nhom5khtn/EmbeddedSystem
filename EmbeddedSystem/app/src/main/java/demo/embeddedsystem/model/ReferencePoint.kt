package demo.embeddedsystem.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

class ReferencePoint : RealmObject() {
    @PrimaryKey
    var id = UUID.randomUUID().toString()
    var createdAt = Date()
    var name: String? = null
    var description: String? = null
    var x = 0.0
    var y = 0.0

    //    Important: must set it as: x y (space in between)
    var locId: String? = null

    //    Important: These readings list count must be equal to the number of APS in area.
    //    If some AP is not accesible at this RP then put the least RSS value i.e. NaN in Algorithms.java
    var readings: RealmList<AccessPoint>? = null
}