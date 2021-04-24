package demo.embeddedsystem

import android.app.Application
import io.realm.Realm

class BaseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}