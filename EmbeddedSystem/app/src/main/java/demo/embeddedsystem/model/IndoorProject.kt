package demo.embeddedsystem.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

class IndoorProject : RealmObject {
    @PrimaryKey
    var id = UUID.randomUUID().toString()
    var createdAt = Date()
    var name: String? = null
    var desc: String? = null
    var aps: RealmList<AccessPoint>? = null
    var rps: RealmList<ReferencePoint>? = null

    constructor() {}
    constructor(createdAt: Date, name: String?, desc: String?) {
        this.createdAt = createdAt
        this.name = name
        this.desc = desc
    }
}
