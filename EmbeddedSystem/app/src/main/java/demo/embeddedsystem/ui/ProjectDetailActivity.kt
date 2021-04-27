package demo.embeddedsystem.ui


import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import demo.embeddedsystem.R
import demo.embeddedsystem.adapter.ReferencePointAdapter
import demo.embeddedsystem.adapter.holder.MainViewModel
import demo.embeddedsystem.adapter.holder.ProjectDetailViewModel
import demo.embeddedsystem.model.AccessPoint
import demo.embeddedsystem.model.ReferencePoint
import demo.embeddedsystem.core.Algorithms
import demo.embeddedsystem.databinding.ActivityProjectDetailBinding
import kotlinx.android.synthetic.main.activity_project_detail.*
import java.util.*

class ProjectDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProjectDetailBinding
    private lateinit var viewModel: ProjectDetailViewModel
    private lateinit var adapter: ReferencePointAdapter
    val radioMap = mutableListOf<ReferencePoint>()
    val curPoint = AccessPoint(null, -52F,-37F,-23F)
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
        binding.tvCurPos.text = curPoint.toString()
        binding.btnGetResult.setOnClickListener {
            val result = Algorithms.KNN_WKNN_Algorithm(curPoint, radioMap, true)
            ("Weighted: $result").also { binding.tvResult.text = it }
        }
        binding.btnGetResultNoWeighted.setOnClickListener {
            val result = Algorithms.KNN_WKNN_Algorithm(curPoint, radioMap, false)
            ("No Weighted: $result").also { binding.tvResultNoWeighted.text = it }
        }
        adapter.submitList(radioMap)
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.listener = null
    }

}