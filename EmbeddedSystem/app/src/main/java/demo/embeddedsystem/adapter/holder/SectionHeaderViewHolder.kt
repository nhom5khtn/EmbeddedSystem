package demo.embeddedsystem.adapter.holder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import demo.embeddedsystem.R

class SectionHeaderViewHolder(headerView: View) : RecyclerView.ViewHolder(headerView) {
    val tvTitle: TextView = headerView.findViewById<View>(R.id.tv_section_name) as TextView

}