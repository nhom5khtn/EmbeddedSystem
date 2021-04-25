package demo.embeddedsystem.adapter.holder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import demo.embeddedsystem.R

class PointViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tvIdentifier: TextView = itemView.findViewById(R.id.point_identifier)
    val tvIdentifier2: TextView = itemView.findViewById(R.id.point_identifier2)
    val tvPointX: TextView = itemView.findViewById(R.id.point_x)
    val tvPointY: TextView = itemView.findViewById(R.id.point_y)

}