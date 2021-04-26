package embeddedsystem.testalgorithms


import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import embeddedsystem.testalgorithms.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: ReferencePointAdapter
    val radioMap = mutableListOf<ReferencePoint>()
    val curPoint = AccessPoint(null, -37F,-41F,-36F)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
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