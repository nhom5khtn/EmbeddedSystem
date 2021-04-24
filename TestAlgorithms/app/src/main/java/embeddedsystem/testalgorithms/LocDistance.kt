package embeddedsystem.testalgorithms

import androidx.annotation.NonNull

class LocDistance(val distance: Float, val location: String) :
    Comparable<LocDistance> {
    override fun compareTo(@NonNull obj: LocDistance): Int {
        //ascending
        return if (distance == obj.distance) 0 else if (distance > obj.distance) 1 else -1
    }
}