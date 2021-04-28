package demo.embeddedsystem.ui


import android.os.Bundle
import android.util.Log
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
import org.json.JSONException

class ProjectDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProjectDetailBinding
    private lateinit var viewModel: ProjectDetailViewModel
    private lateinit var adapter: ReferencePointAdapter
    val radioMap = mutableListOf<ReferencePoint>()
    var lastestPoint: AccessPoint = AccessPoint(null, null, null, null, null, null)
    var currentPoint: AccessPoint = AccessPoint(null, null, null, null, null, null)
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
        getDataFromAPI()
        binding.tvCurPos.text = currentPoint.toString()
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


    private fun getDataFromAPI() {
        // creating a string variable for URL.
        val sheetID = "1iBj3MY37lybNoOC8TRRoTQicVAY2OndAG9lwF44jwXs"
        val url = "https://spreadsheets.google.com/feeds/list/"+ sheetID +"/od6/public/values?alt=json"

        // creating a new variable for our request queue
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
                    if (currentPoint.timestamp != temp.timestamp) {
                        lastestPoint = temp
                    }
                    Log.e("currentPoint", currentPoint.toString())
                    Log.e("lastestPoint", lastestPoint.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }) {
        // handline on error listner method.
        }
        // calling a request queue method
        // and passing our json object
        queue.add(jsonObjectRequest)
    }
}