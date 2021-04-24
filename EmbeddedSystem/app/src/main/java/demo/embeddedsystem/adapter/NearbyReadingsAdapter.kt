package demo.embeddedsystem.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import demo.embeddedsystem.R
import demo.embeddedsystem.model.LocDistance
import java.util.*

class NearbyReadingsAdapter : RecyclerView.Adapter<NearbyReadingsAdapter.ViewHolder?>() {
    private var readings: ArrayList<LocDistance> = ArrayList<LocDistance>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val linearLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reference_reading, parent, false) as LinearLayout
        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(linearLayout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.setText(readings[position].getName())
        holder.loc.setText(readings[position].getLocation())
        holder.distance.setText(String.valueOf(readings[position].getDistance()))
    }

    override fun getItemCount(): Int {
        return readings.size
    }

    class ViewHolder(v: LinearLayout) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        var name: TextView
        var loc: TextView
        var distance: TextView

        init {
            name = v.findViewById(R.id.wifi_ssid)
            loc = v.findViewById(R.id.wifi_bssid)
            distance = v.findViewById(R.id.wifi_level)
        }
    }

    fun getReadings(): List<LocDistance> {
        return readings
    }

    fun addAP(locDistance: LocDistance) {
        readings.add(locDistance)
    }

    fun setReadings(readings: ArrayList<LocDistance>) {
        this.readings = readings
    }
}