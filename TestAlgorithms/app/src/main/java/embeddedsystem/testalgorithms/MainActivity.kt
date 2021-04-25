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
    private val K = 16
    val curPoint = AccessPoint(null, -37F, -54F, -49F)
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
            val result = KNN_WKNN_Algorithm(curPoint, radioMap, true)
            ("Weighted: $result").also { binding.tvResult.text = it }
        }
        binding.btnGetResultNoWeighted.setOnClickListener {
            val result = KNN_WKNN_Algorithm(curPoint, radioMap, false)
            ("No Weighted: $result").also { binding.tvResultNoWeighted.text = it }
        }
        adapter.submitList(radioMap)
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.listener = null
    }




    /**
     * Calculates user location based on Weighted/Not Weighted K Nearest
     * Neighbor (KNN) Algorithm
     *
     * @param proj
     * the project details from db for current area
     *
     * @param observedRSSValues
     * RSS values currently observed
     * @param parameter
     *
     * @param isWeighted
     * To be weighted or not
     *
     * @return The estimated user location
     */
    private fun KNN_WKNN_Algorithm(
            curPos: AccessPoint,
            radioMap: List<ReferencePoint>,
            isWeighted: Boolean
    ): String? {
        var rssiOnline = arrayOf(
                curPos.rssiCurrValueAP1,
                curPos.rssiCurrValueAP2,
                curPos.rssiCurrValueAP3
        )
        var distanceEclide: Float
        val locDistanceResultsList: ArrayList<LocDistance> = ArrayList<LocDistance>()
        var myLocation: String?

        // Construct a list with locations-distances pairs for currently
        // observed RSS values
        for (referencePoint in radioMap) {
            var rssiOffline = arrayOf(
                    referencePoint.rssiMeanValueAP1,
                    referencePoint.rssiMeanValueAP2,
                    referencePoint.rssiMeanValueAP3
            )
            Log.e(
                    "KNN- Calculate D[j=${referencePoint.referencePoint}]",
                    "\nrssiOnline{${rssiOnline[0]},${rssiOnline[1]},${rssiOnline[2]}} " +
                            "+ rssiOffline{${rssiOffline[0]},${rssiOffline[1]},${rssiOffline[2]}}")
            distanceEclide = calculateEuclideanDistance(rssiOnline, rssiOffline)
            Log.e("KNN- Calculate D[j=${referencePoint.referencePoint}] = ", distanceEclide.toString())
            if (distanceEclide == Float.NEGATIVE_INFINITY) return null
            locDistanceResultsList.add(
                    0,
                    LocDistance(distanceEclide, referencePoint.referencePoint)
            )
        }
        var msg1 = "\nlist Dj"
        for(loc in locDistanceResultsList){
            msg1 += "\n{${loc.location},${loc.distance}}"
        }
        Log.e("KNN- List Dj", msg1)

        // Sort locations-distances pairs based on minimum distances
        locDistanceResultsList.sortWith { gd1, gd2 -> if (gd1.distance > gd2.distance) 1 else if (gd1.distance === gd2.distance) 0 else -1 }
        var msg2 = "\nsorted list Dj"
        for(loc in locDistanceResultsList){
            msg2 += "\n{${loc.location},${loc.distance}}"
        }
        Log.e("KNN- Sorted List Dj", msg2)

        if (!isWeighted) {
            myLocation = calculateAverageKDistanceLocations(locDistanceResultsList);
        } else {
            myLocation = calculateWeightedAverageKDistanceLocations(locDistanceResultsList);
        }
        return myLocation
    }

    /**
     * Calculates the Euclidean distance between the currently observed RSS
     * values and the RSS values for a specific location.
     *
     * @param rssiOnlineVector
     * RSS values of a location in stored in AP obj of locations
     * @param rssiOfflineVector
     * RSS values currently observed
     *
     * @return The Euclidean distance, or MIN_VALUE for error
     */
    private fun calculateEuclideanDistance(
            rssiOnlineVector: Array<Float>,
            rssiOfflineVector: Array<Float?>
    ): Float {
        var finalResult = 0f
        var v1: Float
        var v2: Float
        var temp: Float
        for (i in 0 until 3) {
            try {
                v1 = rssiOnlineVector[i]
                v2 = rssiOfflineVector[i]!!
            } catch (e: Exception) {
                return Float.NEGATIVE_INFINITY
            }

            // do the procedure
            temp = v1 - v2
            temp *= temp

            // do the procedure
            finalResult += temp
        }
        return Math.sqrt(finalResult.toDouble()).toFloat()
    }


    /**
     * Calculates the Average of the K locations that have the shortest
     * distances D
     *
     * @param LocDistance_Results_List
     * Locations-Distances pairs sorted by distance
     * @param K
     * The number of locations used
     * @return The estimated user location, or null for error
     */
    private fun calculateAverageKDistanceLocations(
            locDistanceResultsList: ArrayList<LocDistance>
    ): String? {
        var sumX = 0.0f
        var sumY = 0.0f
        var x: Float
        var y: Float
        var location: String
        val K_Min = if (K < locDistanceResultsList.size) K else locDistanceResultsList.size

        // Calculate the sum of X and Y
        for (z in 0 until K_Min) {
            location = locDistanceResultsList[z].location
            x = location.substring(location.indexOf('[') + 1, location.indexOf('.')).toFloat()
            y = location.substring(location.indexOf('.') + 1, location.indexOf(']')).toFloat()
            sumX += x
            sumY += y
            Log.e("KNN- Calculate the sum of X and Y: z = $z", "pos[$x,$y]")
            Log.e("KNN- Calculate the sum of X and Y: z = $z", "pos[$x,$y]")
        }

        // Calculate the average
        sumX /= K_Min.toFloat()
        sumY /= K_Min.toFloat()
        Log.e("KNN- Calculate the average", "pos[$sumX,$sumY]")
        return sumX.toString().substring(0, sumX.toString().indexOf('.')+3) +
        " " + sumY.toString().substring(0, sumY.toString().indexOf('.')+3)
    }

    /**
     * Calculates the Weighted Average of the K locations that have the shortest
     * distances D
     *
     * @param LocDistance_Results_List
     * Locations-Distances pairs sorted by distance
     * @param K
     * The number of locations used
     * @return The estimated user location, or null for error
     */
    private fun calculateWeightedAverageKDistanceLocations(locDistanceResultsList: ArrayList<LocDistance>): String? {
        var LocationWeight: Double
        var sumWeights = 0.0
        var WeightedSumX = 0.0
        var WeightedSumY = 0.0
        var x: Float
        var y: Float
        var location: String
        val K_Min = if (K < locDistanceResultsList.size) K else locDistanceResultsList.size

        // Calculate the weighted sum of X and Y
        for (z in 0 until K_Min) {
            LocationWeight = (1 / locDistanceResultsList[z].distance).toDouble()
            location = locDistanceResultsList[z].location
            x = location.substring(location.indexOf('[') + 1, location.indexOf('.')).toFloat()
            y = location.substring(location.indexOf('.') + 1, location.indexOf(']')).toFloat()
            sumWeights += LocationWeight
            WeightedSumX += LocationWeight * x
            WeightedSumY += LocationWeight * y
            Log.e("KNN- Calculate the weighted sum of X and Y: z = $z", "pos[$x,$y]")
        }
        WeightedSumX /= sumWeights
        WeightedSumY /= sumWeights
        Log.e("KNN- Calculate the average", "pos[$WeightedSumX,$WeightedSumY]")
        return WeightedSumX.toString().substring(0, WeightedSumX.toString().indexOf('.')+3) +
                " " + WeightedSumY.toString().substring(0, WeightedSumY.toString().indexOf('.')+3)
    }
}