package csv.kotlin.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


class StudentAdapter  : ListAdapter<Student, StudentAdapter.ViewHolder>(StudentDiffUtilCallback()) {

    interface StudentAdapterListener{
        fun onClickItem(student: Student)
    }
    var listener : StudentAdapterListener? = null

    class StudentDiffUtilCallback : DiffUtil.ItemCallback<Student>(){
        override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean {
            return oldItem.studentId == newItem.studentId
        }

        override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean {
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
        val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
        val tvDescription = itemView.findViewById<TextView>(R.id.tvDescription)

        companion object {
            fun from(parent: ViewGroup) : ViewHolder {
                var layoutInflater = LayoutInflater.from(parent.context)
                var view = layoutInflater.inflate(R.layout.student_item_view, parent, false)
                return ViewHolder(view)
            }
        }

        fun bind(item: Student, listener: StudentAdapterListener?) {
            tvTitle.text = item.firstName
            tvDescription.text = item.lastName
            itemView.setOnClickListener {
                listener?.onClickItem(item)
            }
        }
    }

}