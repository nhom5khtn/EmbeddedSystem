package demo.embeddedsystem.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import demo.embeddedsystem.R
import demo.embeddedsystem.model.AccessPoint
import java.util.*

class ReferenceReadingsAdapter : RecyclerView.Adapter<ReferenceReadingsAdapter.ViewHolder?>() {
    private var readings: MutableList<AccessPoint> = ArrayList<AccessPoint>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val linearLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reference_reading, parent, false) as LinearLayout
        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(linearLayout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bssid.setText(readings[position].getBssid())
        holder.ssid.setText(readings[position].getSsid())
        holder.level.setText(String.valueOf(readings[position].getMeanRss()))
    }

    override fun getItemCount(): Int {
        return readings.size
    }

    class ViewHolder(v: LinearLayout) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        var bssid: TextView
        var ssid: TextView
        var level: TextView

        init {
            bssid = v.findViewById(R.id.wifi_bssid)
            ssid = v.findViewById(R.id.wifi_ssid)
            level = v.findViewById(R.id.wifi_level)
        }
    }

    fun getReadings(): List<AccessPoint> {
        return readings
    }

    fun addAP(accessPoint: AccessPoint) {
        readings.add(accessPoint)
    }

    fun setReadings(readings: MutableList<AccessPoint>) {
        this.readings = readings
    }
}
