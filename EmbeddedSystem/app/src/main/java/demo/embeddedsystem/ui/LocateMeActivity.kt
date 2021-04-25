package demo.embeddedsystem.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import demo.embeddedsystem.R
import demo.embeddedsystem.adapter.NearbyReadingsAdapter
import demo.embeddedsystem.core.Algorithms
import demo.embeddedsystem.core.WifiService
import demo.embeddedsystem.model.IndoorProject
import demo.embeddedsystem.model.LocDistance
import demo.embeddedsystem.model.LocationWithNearbyPlaces
import demo.embeddedsystem.model.WifiData
import demo.embeddedsystem.utils.AppContants
import demo.embeddedsystem.utils.Utils
import io.realm.Realm


class LocateMeActivity : AppCompatActivity() {
    private var mWifiData: WifiData? = null
    private val algorithms: Algorithms = Algorithms()
    private var projectId: String? = null
    private var defaultAlgo: String? = null
    private var project: IndoorProject? = null
    private val mReceiver: MainActivityReceiver = MainActivityReceiver()
    private var wifiServiceIntent: Intent? = null
    private var tvLocation: TextView? = null
    private var tvNearestLocation: TextView? = null
    private var tvDistance: TextView? = null
    private var rvPoints: RecyclerView? = null
    private var layoutManager: LinearLayoutManager? = null
    private val readingsAdapter: NearbyReadingsAdapter = NearbyReadingsAdapter()
    protected override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mWifiData = null

        // set receiver
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mReceiver, IntentFilter(AppContants.INTENT_FILTER))

        // launch WiFi service
        wifiServiceIntent = Intent(this, WifiService::class.java)
        startService(wifiServiceIntent)

        // recover retained object
        mWifiData = getLastNonConfigurationInstance() as WifiData?

        // set layout
        setContentView(R.layout.activity_locate_me)
        initUI()
        defaultAlgo = Utils.getDefaultAlgo(this)
        projectId = getIntent().getStringExtra("projectId")
        if (projectId == null) {
            Toast.makeText(getApplicationContext(), "Project Not Found", Toast.LENGTH_LONG).show()
            this.finish()
        }
        val realm = Realm.getDefaultInstance()
        project = realm.where(IndoorProject::class.java).equalTo("id", projectId).findFirst()
        Log.v("LocateMeActivity", "onCreate")
    }

    private fun initUI() {
        layoutManager = LinearLayoutManager(this)
        tvLocation = findViewById(R.id.tv_location)
        tvNearestLocation = findViewById(R.id.tv_nearest_location)
        tvDistance = findViewById(R.id.tv_distance_origin)
        rvPoints = findViewById(R.id.rv_nearby_points)
        rvPoints.setLayoutManager(layoutManager)
        rvPoints.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rvPoints.setAdapter(readingsAdapter)
    }

    override fun onRetainCustomNonConfigurationInstance(): Any? {
        return mWifiData
    }

    inner class MainActivityReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.v("LocateMeActivity", "MainActivityReceiver")
            mWifiData = intent.getParcelableExtra<Parcelable>(AppContants.WIFI_DATA) as WifiData?
            if (mWifiData != null) {
                val loc: LocationWithNearbyPlaces? = project?.let { Algorithms.processingAlgorithms(mWifiData.getNetworks(), it, defaultAlgo!!.toInt())
                }
                Log.v("LocateMeActivity", "loc:$loc")
                if (loc == null) {
                    tvLocation!!.text =
                        "Location: NA\nNote:Please switch on your wifi and location services with permission provided to App"
                } else {
                    val locationValue: String = Utils.reduceDecimalPlaces(loc.getLocation())
                    tvLocation!!.text = "Location: $locationValue"
                    val theDistancefromOrigin: String =
                        Utils.getTheDistancefromOrigin(loc.getLocation())
                    tvDistance!!.text =
                        "The distance from stage area is: " + theDistancefromOrigin + "m"
                    val theNearestPoint: LocDistance = Utils.getTheNearestPoint(loc)
                    if (theNearestPoint != null) {
                        tvNearestLocation!!.text = "You are near to: " + theNearestPoint.getName()
                    }
                    readingsAdapter.setReadings(loc.getPlaces())
                    readingsAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    protected override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver)
        stopService(wifiServiceIntent)
    }
}
