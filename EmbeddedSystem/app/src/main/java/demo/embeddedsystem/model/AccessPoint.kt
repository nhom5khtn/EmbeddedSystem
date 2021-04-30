package demo.embeddedsystem.model

class AccessPoint(
    val currPosition: String?,
    val rssiCurrValueAP1: Float?,
    val rssiCurrValueAP2: Float?,
    val rssiCurrValueAP3: Float?,
    val name: String?,
    val timestamp: String?

){
    override fun toString(): String {
        return "-currPosition: $currPosition{rssiAP1: $rssiCurrValueAP1, rssiAP2: $rssiCurrValueAP2, rssiAP3: $rssiCurrValueAP3}"
    }
}