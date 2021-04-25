package demo.embeddedsystem.adapter

import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import demo.embeddedsystem.R
import java.util.*

class WifiResultsAdapter : RecyclerView.Adapter<WifiResultsAdapter.ViewHolder?>() {
    var results: List<ScanResult> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val linearLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wifi_result, parent, false) as LinearLayout
        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(linearLayout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bssid.text = "MAC: " + results[position].BSSID
        holder.ssid.text = "SSID: " + results[position].SSID
        holder.capabilities.text = "Type: " + results[position].capabilities
        holder.frequency.text = "Frequency: " + results[position].frequency.toString()
        holder.level.text = "RSSI:" + results[position].level
    }

    override fun getItemCount(): Int {
        return results.size
    }

    class ViewHolder(v: LinearLayout) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        var bssid: TextView = v.findViewById(R.id.wifi_bssid)
        var ssid: TextView
        var capabilities: TextView
        var level: TextView
        var frequency: TextView

        init {
            ssid = v.findViewById(R.id.wifi_ssid)
            capabilities = v.findViewById(R.id.wifi_capabilities)
            frequency = v.findViewById(R.id.wifi_frequency)
            level = v.findViewById(R.id.wifi_level)
        }
    }
}
