package demo.embeddedsystem.core

import android.app.Service
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import demo.embeddedsystem.model.WifiData
import demo.embeddedsystem.utils.AppContants
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class WifiService : Service() {
    private val TAG = "WifiService"
    private var mWifiManager: WifiManager? = null
    private var scheduleReaderHandle: ScheduledFuture<*>? = null
    private var mScheduler: ScheduledExecutorService? = null
    private var mWifiData: WifiData? = null
    private val initialDelay: Long = 0
    private val periodReader: Long = AppContants.FETCH_INTERVAL

    /**
     * It creates a new Thread that it is executed periodically reading the last
     * scanning of WiFi networks (if WiFi is available).
     */
    override fun onCreate() {
        Log.d(TAG, "WifiService onCreate")
        mWifiData = WifiData()
        mWifiManager = this.getSystemService(WIFI_SERVICE) as WifiManager
        mScheduler = Executors.newScheduledThreadPool(1)
        scheduleReaderHandle = mScheduler.scheduleAtFixedRate(
            ScheduleReader(), initialDelay, periodReader,
            TimeUnit.MILLISECONDS
        )
    }

    /**
     * Kills the periodical Thread before destroying the service
     */
    override fun onDestroy() {
        // stop read thread
        scheduleReaderHandle!!.cancel(true)
        mScheduler!!.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * Performs a periodical read of the WiFi scan result, then it creates a new
     * [] object containing the list of networks and finally it
     * sends it to the main activity for being displayed.
     */
    internal inner class ScheduleReader : Runnable {
        override fun run() {
            if (mWifiManager!!.isWifiEnabled) {
                // get networks
                val mResults = mWifiManager!!.scanResults
                Log.d(TAG, "New scan result: (" + mResults.size + ") networks found")
                // store networks
                mWifiData?.addNetworks(mResults)
                // send data to UI
                val intent = Intent(AppContants.INTENT_FILTER)
                intent.putExtra(AppContants.WIFI_DATA, mWifiData)
                LocalBroadcastManager.getInstance(this@WifiService).sendBroadcast(intent)
            }
        }
    }
}
