package embeddedsystem.testalgorithms

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


class ReferencePointAdapter  : ListAdapter<ReferencePoint, ReferencePointAdapter.ViewHolder>(ReferencePointDiffUtilCallback()) {

    interface ReferencePointAdapterListener{
        fun onClickItem(referencePoint: ReferencePoint)
    }
    var listener : ReferencePointAdapterListener? = null

    class ReferencePointDiffUtilCallback : DiffUtil.ItemCallback<ReferencePoint>(){
        override fun areItemsTheSame(oldItem: ReferencePoint, newItem: ReferencePoint): Boolean {
            return oldItem.referencePoint == newItem.referencePoint
        }

        override fun areContentsTheSame(oldItem: ReferencePoint, newItem: ReferencePoint): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // when you scroll, this function helps control display itemView follow position parameter
        val item = getItem(position)
        holder.bind(item, listener)
    }

    class ViewHolder private constructor(val itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvReferencePoint = itemView.findViewById<TextView>(R.id.tvReferencePoint)
        val tvRSSIAP1 = itemView.findViewById<TextView>(R.id.tvRSSIAP1)
        val tvRSSIAP2 = itemView.findViewById<TextView>(R.id.tvRSSIAP2)
        val tvRSSIAP3 = itemView.findViewById<TextView>(R.id.tvRSSIAP3)

        companion object {
            fun from(parent: ViewGroup) : ViewHolder {
                var layoutInflater = LayoutInflater.from(parent.context)
                var view = layoutInflater.inflate(R.layout.student_item_view, parent, false)
                return ViewHolder(view)
            }
        }

        fun bind(item: ReferencePoint, listener: ReferencePointAdapterListener?) {
            tvReferencePoint.text = item.referencePoint
            tvRSSIAP1.text = item.rssiMeanValueAP1.toString()
            tvRSSIAP2.text = item.rssiMeanValueAP2.toString()
            tvRSSIAP3.text = item.rssiMeanValueAP3.toString()
            itemView.setOnClickListener {
                listener?.onClickItem(item)
            }
        }
    }

}