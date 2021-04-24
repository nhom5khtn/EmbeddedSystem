package demo.embeddedsystem.adapter.holder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import demo.embeddedsystem.R

class PointViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tvIdentifier: TextView
    val tvIdentifier2: TextView
    val tvPointX: TextView
    val tvPointY: TextView

    init {
        tvIdentifier = itemView.findViewById(R.id.point_identifier)
        tvIdentifier2 = itemView.findViewById(R.id.point_identifier2)
        tvPointX = itemView.findViewById(R.id.point_x)
        tvPointY = itemView.findViewById(R.id.point_y)
    }
}