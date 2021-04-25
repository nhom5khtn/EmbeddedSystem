package demo.embeddedsystem.utils

import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import demo.embeddedsystem.ui.SearchWifiAccessPointActivity


class RecyclerItemClickListener(
    searchWifiAccessPointActivity: SearchWifiAccessPointActivity,
    rvWifis: RecyclerView?,
    searchWifiAccessPointActivity1: SearchWifiAccessPointActivity
) : RecyclerView.OnItemTouchListener{
    private lateinit var mListener: AdapterView.OnItemClickListener
    private lateinit var mGestureDetector: GestureDetector

    interface OnItemClickListener : AdapterView.OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
        fun onLongClick(view: View?, position: Int)
    }

    fun RecyclerItemClickListener(context: Context?, recycleView: RecyclerView, listener: OnItemClickListener) {
        mListener = listener
        mGestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val childView = recycleView.findChildViewUnder(e.x, e.y)
                if (childView != null) {
                    listener.onLongClick(childView, recycleView.getChildAdapterPosition(childView))
                } else {
                    super.onLongPress(e)
                }
            }
        })
    }

    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        val childView = view.findChildViewUnder(e.x, e.y)
        if (childView != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemClick(childView as AdapterView<*>?, view.getChildAdapterPosition(childView))
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        TODO("Not yet implemented")
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        TODO("Not yet implemented")
    }

}



