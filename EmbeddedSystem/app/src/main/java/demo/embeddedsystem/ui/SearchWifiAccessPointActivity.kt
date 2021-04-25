package demo.embeddedsystem.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import demo.embeddedsystem.R
import demo.embeddedsystem.adapter.WifiResultsAdapter
import demo.embeddedsystem.model.AccessPoint
import demo.embeddedsystem.utils.RecyclerItemClickListener
import java.util.*

class SearchWifiAccessPointActivity : AppCompatActivity(), View.OnClickListener,
    RecyclerItemClickListener.OnItemClickListener {
    private val TAG = "SearchWifiAccessPointActivity"
    private var rvWifis: RecyclerView? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var mainWifi: WifiManager? = null
    private var receiverWifi: WifiListReceiver? = null
    private val handler = Handler()
    private var btnRefrsh: Button? = null
    private var results: List<ScanResult> = ArrayList()
    private val wifiResultsAdapter: WifiResultsAdapter = WifiResultsAdapter()
    private var wifiWasEnabled = false
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_wifis)
        initUI()
        mainWifi = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        receiverWifi = WifiListReceiver()
        wifiWasEnabled = mainWifi!!.isWifiEnabled
        if (!mainWifi!!.isWifiEnabled) {
            mainWifi!!.isWifiEnabled = true
        }
        layoutManager = LinearLayoutManager(this)
        rvWifis?.setLayoutManager(layoutManager)
        rvWifis?.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rvWifis?.setItemAnimator(DefaultItemAnimator())
        rvWifis?.setAdapter(wifiResultsAdapter)
        rvWifis?.addOnItemTouchListener(RecyclerItemClickListener(this@SearchWifiAccessPointActivity, rvWifis, this))
    }

    fun refresh() {
        handler.postDelayed({
            mainWifi!!.startScan()
            //                refresh();
        }, 1000)
    }

    override fun onResume() {
        registerReceiver(
            receiverWifi, IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
            )
        )
        refresh()
        super.onResume()
    }

    override fun onPause() {
        unregisterReceiver(receiverWifi)
        super.onPause()
    }

    private fun initUI() {
        rvWifis = findViewById(R.id.rv_wifis)
        btnRefrsh = findViewById(R.id.btn_wifi_refresh)
        btnRefrsh.setOnClickListener(this)
    }

    override fun onItemClick(view: View?, position: Int) {
        val accessPoint = AccessPoint()
        val scanResult = results[position]
        accessPoint.setId(UUID.randomUUID().toString())
        accessPoint.setMac_address(scanResult.BSSID)
        accessPoint.setSsid(scanResult.SSID)
        accessPoint.setBssid(scanResult.BSSID)
        accessPoint.setDescription(scanResult.capabilities)
        //        accessPoint.setMeanRss(D-1);
        val intent = Intent()
        intent.putExtra("accessPoint", accessPoint)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onLongClick(view: View?, position: Int) {}
    override fun onClick(view: View) {
        if (view.id == btnRefrsh!!.id) {
            refresh()
        }
    }

    internal inner class WifiListReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            results = mainWifi!!.scanResults
            Collections.sort(results, object : Comparator<ScanResult?> {
                override fun compare(scanResult: ScanResult, scanResult2: ScanResult): Int {
                    //  return 1 if rhs should be before lhs
                    //  return -1 if lhs should be before rhs
                    //  return 0 otherwise
                    if (scanResult.level > scanResult2.level) {
                        return -1
                    } else if (scanResult.level < scanResult2.level) {
                        return 1
                    }
                    return 0
                }
            })
            wifiResultsAdapter.setResults(results)
            wifiResultsAdapter.notifyDataSetChanged()
            val N = results.size
            Log.v(TAG, "Wi-Fi Scan Results ... Count:$N")
            for (i in 0 until N) {
                Log.v(TAG, "  BSSID       =" + results[i].BSSID)
                Log.v(TAG, "  SSID        =" + results[i].SSID)
                Log.v(TAG, "  Capabilities=" + results[i].capabilities)
                Log.v(TAG, "  Frequency   =" + results[i].frequency)
                Log.v(TAG, "  Level       =" + results[i].level)
                Log.v(TAG, "---------------")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!wifiWasEnabled) {
            mainWifi!!.isWifiEnabled = false
        }
    }
}
