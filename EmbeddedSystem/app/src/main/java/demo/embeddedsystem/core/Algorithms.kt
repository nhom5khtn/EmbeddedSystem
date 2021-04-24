package demo.embeddedsystem.core

import demo.embeddedsystem.model.*
import demo.embeddedsystem.utils.AppContants
import io.realm.RealmList
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.lang.Double.NaN
import java.util.*
import kotlin.Double.Companion.NaN

object Algorithms {
    const val K = "4"

    /**
     *
     * @param latestScanList
     * the current scan list of APs
     * @param proj
     * the project details from db for current area
     *
     * @param algorithm_choice
     * choice of several algorithms
     *
     * @return the location of user
     */
    fun processingAlgorithms(latestScanList: List<WifiDataNetwork>, proj: IndoorProject, algorithm_choice: Int): LocationWithNearbyPlaces? {
        var i: Int
        var j: Int
        val aps: RealmList<AccessPoint> = proj.getAps()
        val observedRSSValues = ArrayList<Float>()
        var temp_LR: WifiDataNetwork
        var notFoundCounter = 0
        // Read parameter of algorithm
//		String NaNValue = readParameter(RM, 0);

        // Check which mac addresses of radio map, we are currently listening.
        i = 0
        while (i < aps.size) {
            j = 0
            while (j < latestScanList.size) {
                temp_LR = latestScanList[j]
                // MAC Address Matched
                if (aps[i].getMac_address().compareTo(temp_LR.getBssid()) === 0) {
                    observedRSSValues.add(java.lang.Float.valueOf(temp_LR.getLevel()).toFloat())
                    break
                }
                ++j
            }
            // A MAC Address is missing so we place a small value, NaN value
            if (j == latestScanList.size) {
                observedRSSValues.add(AppContants.NaN)
                ++notFoundCounter
            }
            ++i
        }
        if (notFoundCounter == aps.size) return null

        // Read parameter of algorithm
        val parameter = readParameter(algorithm_choice) ?: return null
        when (algorithm_choice) {
            1 -> return KNN_WKNN_Algorithm(proj, observedRSSValues, parameter, false)
            2 -> return KNN_WKNN_Algorithm(proj, observedRSSValues, parameter, true)
            3 -> return MAP_MMSE_Algorithm(proj, observedRSSValues, parameter, false)
            4 -> return MAP_MMSE_Algorithm(proj, observedRSSValues, parameter, true)
        }
        return null
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
        proj: IndoorProject, observedRSSValues: ArrayList<Float>,
        parameter: String, isWeighted: Boolean
    ): LocationWithNearbyPlaces? {
        var rssValues: RealmList<AccessPoint>
        var curResult = 0f
        val locDistanceResultsList: ArrayList<LocDistance> = ArrayList<LocDistance>()
        var myLocation: String? = null
        val K: Int
        K = try {
            parameter.toInt()
        } catch (e: Exception) {
            return null
        }

        // Construct a list with locations-distances pairs for currently
        // observed RSS values
        for (referencePoint in proj.getRps()) {
            rssValues = referencePoint.getReadings()
            curResult = calculateEuclideanDistance(rssValues, observedRSSValues)
            if (curResult == Float.NEGATIVE_INFINITY) return null
            locDistanceResultsList.add(
                0,
                LocDistance(curResult, referencePoint.getLocId(), referencePoint.getName())
            )
        }

        // Sort locations-distances pairs based on minimum distances
        Collections.sort(locDistanceResultsList,
            Comparator<Any?> { gd1, gd2 -> if (gd1.getDistance() > gd2.getDistance()) 1 else if (gd1.getDistance() === gd2.getDistance()) 0 else -1 })
        myLocation = if (!isWeighted) {
            calculateAverageKDistanceLocations(locDistanceResultsList, K)
        } else {
            calculateWeightedAverageKDistanceLocations(locDistanceResultsList, K)
        }
        return myLocation?.let { LocationWithNearbyPlaces(it, locDistanceResultsList) }
    }

    /**
     * Calculates user location based on Probabilistic Maximum A Posteriori
     * (MAP) Algorithm or Probabilistic Minimum Mean Square Error (MMSE)
     * Algorithm
     *
     * @param proj
     * the project details from db for current area
     *
     * @param observedRssValues
     * RSS values currently observed
     * @param parameter
     *
     * @param isWeighted
     * To be weighted or not
     *
     * @return The estimated user location
     */
    private fun MAP_MMSE_Algorithm(
        proj: IndoorProject,
        observedRssValues: ArrayList<Float>,
        parameter: String,
        isWeighted: Boolean
    ): LocationWithNearbyPlaces? {
        var rssValues: RealmList<AccessPoint>
        var curResult = 0.0
        var myLocation: String? = null
        var highestProbability = Double.NEGATIVE_INFINITY
        val locDistanceResultsList: ArrayList<LocDistance> = ArrayList<LocDistance>()
        val sGreek: Float
        sGreek = try {
            parameter.toFloat()
        } catch (e: Exception) {
            return null
        }

        // Find the location of user with the highest probability
        for (referencePoint in proj.getRps()) {
            rssValues = referencePoint.getReadings()
            curResult = calculateProbability(rssValues, observedRssValues, sGreek)
            if (curResult == Double.NEGATIVE_INFINITY) return null else if (curResult > highestProbability) {
                highestProbability = curResult
                myLocation = referencePoint.getLocId()
            }
            if (isWeighted) locDistanceResultsList.add(
                0,
                LocDistance(curResult, referencePoint.getLocId(), referencePoint.getName())
            )
        }
        if (isWeighted) myLocation =
            calculateWeightedAverageProbabilityLocations(locDistanceResultsList)
        return LocationWithNearbyPlaces(myLocation, locDistanceResultsList)
    }

    /**
     * Calculates the Euclidean distance between the currently observed RSS
     * values and the RSS values for a specific location.
     *
     * @param l1
     * RSS values of a location in stored in AP obj of locations
     * @param l2
     * RSS values currently observed
     *
     * @return The Euclidean distance, or MIN_VALUE for error
     */
    private fun calculateEuclideanDistance(
        l1: RealmList<AccessPoint>,
        l2: ArrayList<Float>
    ): Float {
        var finalResult = 0f
        var v1: Float
        var v2: Float
        var temp: Float
        for (i in l1.indices) {
            try {
                l1[i].getMeanRss()
                v1 = l1[i].getMeanRss()
                v2 = l2[i]
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
     * Calculates the Probability of the user being in the currently observed
     * RSS values and the RSS values for a specific location.
     *
     * @param l1
     * RSS values of a location in stored in AP obj of locations
     * @param l2
     * RSS values currently observed
     *
     * @return The Probability for this location, or MIN_VALUE for error
     */
    private fun calculateProbability(
        l1: RealmList<AccessPoint>,
        l2: ArrayList<Float>,
        sGreek: Float
    ): Double {
        var finalResult = 1.0
        var v1: Float
        var v2: Float
        var temp: Double
        for (i in l1.indices) {
            try {
                v1 = l1[i].getMeanRss()
                v2 = l2[i]
            } catch (e: Exception) {
                return Double.NEGATIVE_INFINITY
            }
            temp = (v1 - v2).toDouble()
            temp *= temp
            temp = -temp
            temp /= (sGreek * sGreek).toDouble()
            temp = Math.exp(temp)

            //Do not allow zero instead stop on small possibility
            if (finalResult * temp != 0.0) finalResult = finalResult * temp
        }
        return finalResult
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
        LocDistance_Results_List: ArrayList<LocDistance>,
        K: Int
    ): String? {
        var sumX = 0.0f
        var sumY = 0.0f
        var LocationArray = arrayOfNulls<String>(2)
        var x: Float
        var y: Float
        val K_Min = if (K < LocDistance_Results_List.size) K else LocDistance_Results_List.size

        // Calculate the sum of X and Y
        for (i in 0 until K_Min) {
            LocationArray = LocDistance_Results_List[i].getLocation().split(" ")
            try {
                x = java.lang.Float.valueOf(LocationArray[0]!!.trim { it <= ' ' }).toFloat()
                y = java.lang.Float.valueOf(LocationArray[1]!!.trim { it <= ' ' }).toFloat()
            } catch (e: Exception) {
                return null
            }
            sumX += x
            sumY += y
        }

        // Calculate the average
        sumX /= K_Min.toFloat()
        sumY /= K_Min.toFloat()
        return "$sumX $sumY"
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
    private fun calculateWeightedAverageKDistanceLocations(
        LocDistance_Results_List: ArrayList<LocDistance>,
        K: Int
    ): String? {
        var LocationWeight = 0.0
        var sumWeights = 0.0
        var WeightedSumX = 0.0
        var WeightedSumY = 0.0
        var LocationArray = arrayOfNulls<String>(2)
        var x: Float
        var y: Float
        val K_Min = if (K < LocDistance_Results_List.size) K else LocDistance_Results_List.size

        // Calculate the weighted sum of X and Y
        for (i in 0 until K_Min) {
            if (LocDistance_Results_List[i].getDistance() !== 0.0) {
                LocationWeight = 1 / LocDistance_Results_List[i].getDistance()
            } else {
                LocationWeight = 100.0
            }
            LocationArray = LocDistance_Results_List[i].getLocation().split(" ")
            try {
                x = java.lang.Float.valueOf(LocationArray[0]!!.trim { it <= ' ' }).toFloat()
                y = java.lang.Float.valueOf(LocationArray[1]!!.trim { it <= ' ' }).toFloat()
            } catch (e: Exception) {
                return null
            }
            sumWeights += LocationWeight
            WeightedSumX += LocationWeight * x
            WeightedSumY += LocationWeight * y
        }
        WeightedSumX /= sumWeights
        WeightedSumY /= sumWeights
        return "$WeightedSumX $WeightedSumY"
    }

    /**
     * Calculates the Weighted Average over ALL locations where the weights are
     * the Normalized Probabilities
     *
     * @param LocDistance_Results_List
     * Locations-Probability pairs
     *
     * @return The estimated user location, or null for error
     */
    private fun calculateWeightedAverageProbabilityLocations(LocDistance_Results_List: ArrayList<LocDistance>): String? {
        var sumProbabilities = 0.0
        var WeightedSumX = 0.0
        var WeightedSumY = 0.0
        var NP: Double
        var x: Float
        var y: Float
        var LocationArray = arrayOfNulls<String>(2)

        // Calculate the sum of all probabilities
        for (i in LocDistance_Results_List.indices) sumProbabilities += LocDistance_Results_List[i].getDistance()

        // Calculate the weighted (Normalized Probabilities) sum of X and Y
        for (i in LocDistance_Results_List.indices) {
            LocationArray = LocDistance_Results_List[i].getLocation().split(" ")
            try {
                x = java.lang.Float.valueOf(LocationArray[0]!!.trim { it <= ' ' }).toFloat()
                y = java.lang.Float.valueOf(LocationArray[1]!!.trim { it <= ' ' }).toFloat()
            } catch (e: Exception) {
                return null
            }
            NP = LocDistance_Results_List[i].getDistance() / sumProbabilities
            WeightedSumX += x * NP
            WeightedSumY += y * NP
        }
        return "$WeightedSumX $WeightedSumY"
    }

    /**
     * Reads the parameters from the file
     *
     * @param file
     * the file of radiomap, to read parameters
     *
     * @param algorithm_choice
     * choice of several algorithms
     *
     * @return The parameter for the algorithm
     */
    private fun readParameter(file: File, algorithm_choice: Int): String? {
        var line: String
        var reader: BufferedReader? = null
        var parameter: String? = null
        try {
            val fr = FileReader(file.absolutePath.replace(".txt", "-parameters2.txt"))
            reader = BufferedReader(fr)
            while (reader.readLine().also { line = it } != null) {

                /* Ignore the labels */
                if (line.startsWith("#") || line.trim { it <= ' ' } == "") {
                    continue
                }

                /* Split fields */
                val temp = line.split(":").toTypedArray()

                /* The file may be corrupted so ignore reading it */if (temp.size != 2) {
                    return null
                }
                if (algorithm_choice == 0 && temp[0] == "NaN") {
                    parameter = temp[1]
                    break
                } else if (algorithm_choice == 1 && temp[0] == "KNN") {
                    parameter = temp[1]
                    break
                } else if (algorithm_choice == 2 && temp[0] == "WKNN") {
                    parameter = temp[1]
                    break
                } else if (algorithm_choice == 3 && temp[0] == "MAP") {
                    parameter = temp[1]
                    break
                } else if (algorithm_choice == 4 && temp[0] == "MMSE") {
                    parameter = temp[1]
                    break
                }
            }
        } catch (e: Exception) {
            return null
        } finally {
            if (reader != null) try {
                reader.close()
            } catch (e: IOException) {
            }
        }
        return parameter
    }

    private fun readParameter(algorithm_choice: Int): String? {
        var parameter: String? = null
        if (algorithm_choice == 1) {
            // && ("KNN")
            parameter = K
        } else if (algorithm_choice == 2) {
            // && ("WKNN")
            parameter = K
        } else if (algorithm_choice == 3) {
            // && ("MAP")
            parameter = K
        } else if (algorithm_choice == 4) {
            // && ("MMSE")
            parameter = K
        }
        return parameter
    }
}
