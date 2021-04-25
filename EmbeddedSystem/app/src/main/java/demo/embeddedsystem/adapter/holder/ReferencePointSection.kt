package demo.embeddedsystem.adapter.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import demo.embeddedsystem.model.ReferencePoint
import java.util.*

class ReferencePointSection(sectionParameters: SectionParameters?) :
    StatelessSection(sectionParameters) {
    private var referencePoints: List<ReferencePoint> = ArrayList<ReferencePoint>()

    fun getContentItemsTotal(): Int {
        return referencePoints.size
    }

    fun getItemViewHolder(view: View?): RecyclerView.ViewHolder {
        return PointViewHolder(view!!)
    }

    fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemHolder = holder as PointViewHolder
        val referencePoint: ReferencePoint = referencePoints[position]
        itemHolder.tvIdentifier.setText(referencePoint.getName())
        itemHolder.tvPointX.setText(String.valueOf(referencePoint.getX()))
        itemHolder.tvPointY.setText(String.valueOf(referencePoint.getY()))
    }

    fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder) {
        super.onBindHeaderViewHolder(holder)
        val headerViewHolder: SectionHeaderViewHolder = holder as SectionHeaderViewHolder
        headerViewHolder.tvTitle.setText("Reference Points")
    }

    fun getHeaderViewHolder(view: View?): SectionHeaderViewHolder? {
        return view?.let { SectionHeaderViewHolder(it) }
    }

    fun getReferencePoints(): List<ReferencePoint> {
        return referencePoints
    }

    fun setReferencePoints(referencePoints: List<ReferencePoint>) {
        this.referencePoints = referencePoints
    }
}
