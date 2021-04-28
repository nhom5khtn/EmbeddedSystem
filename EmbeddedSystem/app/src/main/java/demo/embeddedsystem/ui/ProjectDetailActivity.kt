package demo.embeddedsystem.ui


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import demo.embeddedsystem.R
import demo.embeddedsystem.adapter.ReferencePointAdapter
import demo.embeddedsystem.adapter.holder.ProjectDetailViewModel
import demo.embeddedsystem.core.Algorithms
import demo.embeddedsystem.databinding.ActivityProjectDetailBinding
import demo.embeddedsystem.model.AccessPoint
import demo.embeddedsystem.model.ReferencePoint
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.get
import kotlinx.coroutines.NonCancellable.isActive
import org.json.JSONException

class ProjectDetailActivity : AppCompatActivity() {

    // Init notification
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "demo.embeddedsystem.ui"
    private val description = "Test notification"


    private lateinit var binding: ActivityProjectDetailBinding
    private lateinit var viewModel: ProjectDetailViewModel
    private lateinit var adapter: ReferencePointAdapter
    val radioMap = mutableListOf<ReferencePoint>()

    // Init
    var lastestPoint: AccessPoint = AccessPoint(null, null, null, null, null, null)
    var currentPoint: AccessPoint = AccessPoint(null, null, null, null, null, null)

    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_project_detail)
        viewModel = ViewModelProvider(this).get(ProjectDetailViewModel::class.java)
        csvReader().open(resources.openRawResource(R.raw.radiomap)) {
            readAllWithHeaderAsSequence().forEach { row: Map<String, String> ->
                radioMap.add(
                    ReferencePoint(
                        row.get("RefferencePoint").toString(),
                        row.get("AP1")?.toFloat(),
                        row.get("AP2")?.toFloat(),
                        row.get("AP3")?.toFloat()
                    )
                )
                Log.e("read csv", radioMap.toString())
            }
        }
        adapter = ReferencePointAdapter()
        binding.rcList.adapter = adapter
        adapter.listener = object : ReferencePointAdapter.ReferencePointAdapterListener{
            override fun onClickItem(referencePoint: ReferencePoint) {
                Log.e("RP", referencePoint.referencePoint)
            }
        }

        // Loop func getDataFromAPI()
        startRepeatingJob(30000L)

        //To start: Job myJob = startRepeatingJob(1000L)
        //To Stop: myJob .cancel()

        //getDataFromAPI()
        //binding.tvCurPos.text = currentPoint.toString()
        binding.btnGetResult.setOnClickListener {
            val result = Algorithms.KNN_WKNN_Algorithm(currentPoint, radioMap, true)
            ("Weighted: $result").also { binding.tvResult.text = it }
        }
        binding.btnGetResultNoWeighted.setOnClickListener {
            val result = Algorithms.KNN_WKNN_Algorithm(currentPoint, radioMap, false)
            ("No Weighted: $result").also { binding.tvResultNoWeighted.text = it }
        }

        //
        adapter.submitList(radioMap)
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.listener = null
    }


    private fun getDataFromAPI() :AccessPoint {
        // creating a string variable for URL.
        //val sheetID = "1iBj3MY37lybNoOC8TRRoTQicVAY2OndAG9lwF44jwXs"
        val sheetID = "19uL6ahrprYBPcuZ8XfpY91VAb9biN3Uxm6l7k0X7sug"
        val url = "https://spreadsheets.google.com/feeds/list/"+sheetID+"/od6/public/values?alt=json"

        // creating a new variable for our request queue
        val queue = Volley.newRequestQueue(this)

        // creating a variable for our JSON object request and passing our URL to it.
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val feedObj = response.getJSONObject("feed")
                    val entryArray = feedObj.getJSONArray("entry")
                    val temp = currentPoint
                    val entryObj = entryArray.getJSONObject(entryArray.length()-1)
                    val timestamp = entryObj.getJSONObject("gsx\$time").getString("\$t")
                    val rssiAP1 = entryObj.getJSONObject("gsx\$ap1").getString("\$t").toFloat()
                    val rssiAP2 = entryObj.getJSONObject("gsx\$ap2").getString("\$t").toFloat()
                    val rssiAP3 = entryObj.getJSONObject("gsx\$ap3").getString("\$t").toFloat()
                    currentPoint = AccessPoint(null, rssiAP1, rssiAP2, rssiAP3, null, timestamp)
                    binding.tvCurPos.text = currentPoint.toString()
                    if (currentPoint.timestamp != temp.timestamp) {
                        lastestPoint = temp
                    }
                    Log.e("currentPoint", currentPoint.toString())
                    Log.e("lastestPoint", lastestPoint.toString())
                    if(currentPoint.timestamp != lastestPoint.timestamp){
                        Toast.makeText(applicationContext, "out of stock", Toast.LENGTH_SHORT).show()
                        showNotification()
                        lastestPoint = currentPoint
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }) {
        // handline on error listner method.
        }
        // calling a request queue method
        // and passing our json object
        queue.add(jsonObjectRequest)

        return currentPoint
    }


    /**
     * SET UP LOOP FUNCTION FOR getDataFromAPI to update current point
     */
    /**
     * start Job
     * val job = startRepeatingJob()
     * cancels the job and waits for its completion
     * job.cancelAndJoin()
     * Params
     * timeInterval: time milliSeconds
     */
    @InternalCoroutinesApi
    private fun startRepeatingJob(timeInterval: Long): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            while (NonCancellable.isActive) {
                // add your task here
                getDataFromAPI()
                delay(timeInterval)
            }
        }
    }

    /**
     * SET UP NOTIFICATION
     */
    fun showNotification(){

        // it is a class to notify the user of events that happen.
        // This is how you tell the user that something has happened in the
        // background.
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // FLAG_UPDATE_CURRENT specifies that if a previous
        // PendingIntent already exists, then the current one
        // will update it with the latest intent
        // 0 is the request code, using it later with the
        // same method again will get back the same pending
        // intent for future reference
        // intent passed here is to our afterNotification class
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // RemoteViews are used to use the content of
        // some different layout apart from the current activity layout
        val contentView = RemoteViews(packageName, R.layout.activity_notification)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this, channelId)
                    .setContent(contentView)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.ic_launcher_background))
                    .setContentIntent(pendingIntent)
        } else {

            builder = Notification.Builder(this)
                    .setContent(contentView)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.ic_launcher_background))
                    .setContentIntent(pendingIntent)
        }
        notificationManager.notify(1234, builder.build())
    }

}