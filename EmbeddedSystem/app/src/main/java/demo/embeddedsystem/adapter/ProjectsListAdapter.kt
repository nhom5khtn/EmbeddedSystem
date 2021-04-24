package demo.embeddedsystem.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import demo.embeddedsystem.R
import demo.embeddedsystem.model.IndoorProject
import io.realm.RealmResults

class ProjectsListAdapter(myDataset: RealmResults<IndoorProject>) :
    RecyclerView.Adapter<ProjectsListAdapter.ViewHolder?>() {
    private val mDataset: RealmResults<IndoorProject> = TODO()

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class ViewHolder(v: LinearLayout) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        var name: TextView

        init {
            name = v.findViewById(R.id.tv_project_name)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val linearLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project_list, parent, false) as LinearLayout
        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(linearLayout)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.name.setText(mDataset[position].getName())
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    override fun getItemCount(): Int {
        return mDataset.size
    }
}
