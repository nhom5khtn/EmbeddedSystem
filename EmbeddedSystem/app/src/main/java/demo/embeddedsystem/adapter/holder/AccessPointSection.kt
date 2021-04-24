package demo.embeddedsystem.adapter.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import demo.embeddedsystem.model.AccessPoint
import java.util.*

class AccessPointSection(sectionParameters: SectionParameters?) :
    StatelessSection(sectionParameters) {
    private var accessPoints: List<AccessPoint> = ArrayList<AccessPoint>()
    fun getContentItemsTotal(): Int {
        return accessPoints.field
    }

    fun getItemViewHolder(view: View?): RecyclerView.ViewHolder {
        return PointViewHolder(view)
    }

    fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemHolder: PointViewHolder = holder as PointViewHolder
        val accessPoint: AccessPoint = accessPoints[position]
        itemHolder.tvIdentifier.setText(accessPoint.getSsid())
        itemHolder.tvIdentifier2.setText(accessPoint.getMac_address())
        itemHolder.tvPointX.setText(String.valueOf(accessPoint.getX()))
        itemHolder.tvPointY.setText(String.valueOf(accessPoint.getY()))
    }

    fun getAccessPoints(): List<AccessPoint> {
        return accessPoints
    }

    fun setAccessPoints(accessPoints: List<AccessPoint>) {
        this.accessPoints = accessPoints
    }

    fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder) {
        super.onBindHeaderViewHolder(holder)
        val headerViewHolder: SectionHeaderViewHolder = holder as SectionHeaderViewHolder
        headerViewHolder.tvTitle.setText("Access Points")
    }

    fun getHeaderViewHolder(view: View?): RecyclerView.ViewHolder {
        return SectionHeaderViewHolder(view)
    }
}
