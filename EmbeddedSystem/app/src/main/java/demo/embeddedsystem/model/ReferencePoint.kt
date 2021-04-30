package demo.embeddedsystem.model

data class ReferencePoint(
        val referencePoint: String,
        val rssiMeanValueAP1: Float?,
        val rssiMeanValueAP2: Float?,
        val rssiMeanValueAP3: Float?
){
    override fun toString(): String {
        return "-referencePoint: $referencePoint{rssiAP1: $rssiMeanValueAP1, rssiAP2: $rssiMeanValueAP2, rssiAP3: $rssiMeanValueAP3}"
    }
}