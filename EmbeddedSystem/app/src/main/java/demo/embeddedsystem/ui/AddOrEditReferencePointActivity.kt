package demo.embeddedsystem.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.installations.Utils
import io.realm.RealmList
import java.lang.Double
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import demo.embeddedsystem.R
import io.realm.Realm


class AddOrEditReferencePointActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = "AddOrEditReferencePointActivity"
    private var projectId: String? = null

    private var rvPoints: RecyclerView? = null
    private var layoutManager: LinearLayoutManager? = null
    private var etRpName: EditText? = null
    private  var etRpX:EditText? = null
    private  var etRpY:EditText? = null
    private var bnRpSave: Button? = null

    private val readingsAdapter: ReferenceReadingsAdapter = ReferenceReadingsAdapter()
    private val apsWithReading: List<AccessPoint> = ArrayList()
    private val readings: Map<String, List<Int>> = HashMap()
    private val aps: Map<String, AccessPoint> = HashMap()

    private var receiverWifi: AvailableAPsReceiver? = null

    private var wifiWasEnabled = false
    private var mainWifi: WifiManager? = null
    private val handler: Handler = Handler()
    private var isCaliberating = false
    private val readingsCount = 0
    private var isEdit = false
    private var rpId: String? = null
    private var referencePointFromDB: ReferencePoint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reference_point)

        projectId = intent.getStringExtra("projectId")
        if (projectId == null) {
            Toast.makeText(this, "Reference point not found", Toast.LENGTH_LONG).show()
            finish()
        }

        if (intent.getStringExtra("rpId") != null) {
            isEdit = true
            rpId = intent.getStringExtra("rpId")
        }
        initUI()
        val realm: Realm = Realm.getDefaultInstance()
        if (isEdit) {
            referencePointFromDB =
                realm.where(ReferencePoint::class.java).equalTo("id", rpId).findFirst()
            if (referencePointFromDB == null) {
                Toast.makeText(this, "Reference point not found", Toast.LENGTH_LONG).show()
                finish()
            }
            val readings: RealmList<AccessPoint> = referencePointFromDB.getReadings()
            for (ap in readings) {
                readingsAdapter.addAP(ap)
            }
            readingsAdapter.notifyDataSetChanged()
            etRpName.setText(referencePointFromDB.getName())
            etRpX.setText(java.lang.String.valueOf(referencePointFromDB.getX()))
            etRpY.setText(java.lang.String.valueOf(referencePointFromDB.getY()))
        } else {
            mainWifi = applicationContext.getSystemService<Any>(Context.WIFI_SERVICE) as WifiManager
            receiverWifi = AvailableAPsReceiver()
            wifiWasEnabled = mainWifi!!.isWifiEnabled()
            val project: IndoorProject =
                realm.where(IndoorProject::class.java).equalTo("id", projectId).findFirst()
            val points: RealmList<AccessPoint> = project.getAps()
            for (accessPoint in points) {
                aps.put(accessPoint.getMac_address(), accessPoint)
            }
            if (aps.isEmpty()) {
                Toast.makeText(this, "No Access Points Found", Toast.LENGTH_SHORT).show()
            }
            if (!Utils.isLocationEnabled(this)) {
                Toast.makeText(this, "Please turn on the location", Toast.LENGTH_SHORT).show()
            }
        }

    }


    override fun onResume() {
        if (!isEdit) {
            registerReceiver(receiverWifi, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            Log.v(TAG, "caliberationStarted")
            if (!isCaliberating) {
                isCaliberating = true
                refresh()
            }
        }
        super.onResume()
    }

    override fun onPause() {
        if (!isEdit) {
            unregisterReceiver(receiverWifi)
            isCaliberating = false
        }
        super.onPause()
    }

    fun refresh() {
        handler.postDelayed({
            mainWifi!!.startScan()
            if (readingsCount < AppContants.READINGS_BATCH) {
                refresh()
            } else {
                caliberationCompleted()
            }
        }, AppContants.FETCH_INTERVAL)
    }

    private fun caliberationCompleted() {
        isCaliberating = false
        Log.v(TAG, "caliberationCompleted")
        val values = readings
        Log.v(TAG, "values:$values")
        for ((key, readingsOfAMac) in values) {
            val mean = calculateMeanValue(readingsOfAMac)
            Log.v(TAG, "entry.Key:$key aps:$aps")
            val accessPoint: AccessPoint? = aps[key]
            val updatedPoint = AccessPoint(accessPoint)
            updatedPoint.setMeanRss(mean)
            apsWithReading.add(updatedPoint)
        }
        readingsAdapter.setReadings(apsWithReading)
        readingsAdapter.notifyDataSetChanged()
        bnRpSave!!.isEnabled = true
        bnRpSave!!.text = "Save"
    }

    private fun calculateMeanValue(readings: List<Int>): kotlin.Double {
        if (readings.isEmpty()) {
            return 0.0
        }
        var sum = 0
        for (integer in readings) {
            sum = sum + integer
        }
        return Double.valueOf(sum.toDouble()) / Double.valueOf(readings.size.toDouble())
    }

    private fun initUI() {
        layoutManager = LinearLayoutManager(this)
        rvPoints = findViewById(R.id.rv_points)
        rvPoints.setLayoutManager(layoutManager)
        rvPoints.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rvPoints.setAdapter(readingsAdapter)
        bnRpSave = findViewById(R.id.bn_rp_save)
        bnRpSave.setOnClickListener(this)
        if (!isEdit) {
            bnRpSave.setEnabled(false)
            bnRpSave.setText("Caliberating...")
        } else {
            bnRpSave.setEnabled(true)
            bnRpSave.setText("Save")
        }
        etRpName = findViewById(R.id.et_rp_name)
        etRpX = findViewById(R.id.et_rp_x)
        etRpY = findViewById(R.id.et_rp_y)
    }

    override fun onClick(view: View) {
        if (view.id == bnRpSave!!.id && !isEdit) {
            val realm: Realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            var referencePoint = ReferencePoint()
            referencePoint = setValues(referencePoint)
            referencePoint.setCreatedAt(Calendar.getInstance().getTime())
            referencePoint.setDescription("")
            //            apsWithReading = realm.copyToRealmOrUpdate(apsWithReading);
            if (referencePoint.getReadings() == null) {
                val readings: RealmList<AccessPoint> = RealmList<AccessPoint>()
                readings.addAll(apsWithReading)
                referencePoint.setReadings(readings)
            } else {
                referencePoint.getReadings().addAll(apsWithReading)
            }
            referencePoint.setId(UUID.randomUUID().toString())
            val project: IndoorProject =
                realm.where(IndoorProject::class.java).equalTo("id", projectId).findFirst()
            if (project.getRps() == null) {
                val points: RealmList<ReferencePoint> = RealmList<ReferencePoint>()
                points.add(referencePoint)
                project.setRps(points)
            } else {
                project.getRps().add(referencePoint)
            }
            realm.commitTransaction()
            Toast.makeText(this, "Reference Point Added", Toast.LENGTH_SHORT).show()
            finish()
        } else if (view.id == bnRpSave!!.id && isEdit) {
            val realm: Realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            referencePointFromDB = setValues(referencePointFromDB)
            realm.commitTransaction()
            Toast.makeText(this, "Reference Point Updated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setValues(referencePoint: ReferencePoint): ReferencePoint {
        val x = etRpX!!.text.toString()
        val y = etRpY!!.text.toString()
        if (TextUtils.isEmpty(x)) {
            referencePoint.setX(0.0)
        } else {
            referencePoint.setX(java.lang.Double.valueOf(x))
        }
        if (TextUtils.isEmpty(y)) {
            referencePoint.setY(0.0)
        } else {
            referencePoint.setY(java.lang.Double.valueOf(y))
        }
        referencePoint.setLocId(referencePoint.getX().toString() + " " + referencePoint.getY())
        referencePoint.setName(etRpName!!.text.toString())
        return referencePoint
    }

    internal class AvailableAPsReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val scanResults: List<ScanResult> = mainWifi.getScanResults()
            ++readingsCount
            for (entry in aps.entries) {
                var apMac: String? = entry.key
                for (scanResult in scanResults) {
                    if (entry.key == scanResult.BSSID) {
                        checkAndAddApRSS(apMac, scanResult.level)
                        apMac = null //do this after always :|
                        break
                    }
                }
                if (apMac != null) {
                    checkAndAddApRSS(apMac, AppContants.NaN.intValue())
                }
            }
            //            results.put(Calendar.getInstance(), map);
            Log.v(
                TAG,
                "Count:" + readingsCount + " scanResult:" + scanResults.toString() + " aps:" + aps.toString()
            )
            for (i in 0 until readingsCount) {
//                Log.v(TAG, "  BSSID       =" + results.get(i).BSSID);
//                Log.v(TAG, "  SSID        =" + results.get(i).SSID);
//                Log.v(TAG, "  Capabilities=" + results.get(i).capabilities);
//                Log.v(TAG, "  Frequency   =" + results.get(i).frequency);
//                Log.v(TAG, "  Level       =" + results.get(i).level);
//                Log.v(TAG, "---------------");
            }
        }
    }

    private fun checkAndAddApRSS(apMac: String, level: Int) {
        if (readings.containsKey(apMac)) {
            val integers: MutableList<Int>? = readings[apMac]
            integers!!.add(level)
        } else {
            val integers: MutableList<Int> = ArrayList()
            integers.add(level)
            readings.put(apMac, integers)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!wifiWasEnabled && !isEdit) {
            mainWifi!!.isWifiEnabled = false
        }
    }


}