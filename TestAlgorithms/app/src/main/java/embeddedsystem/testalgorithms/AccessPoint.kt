package embeddedsystem.testalgorithms

/**
 * Created by suyashg on 25/08/17.
 */
class AccessPoint (
    val currPosition: String?,
    val rssiCurrValueAP1: Float,
    val rssiCurrValueAP2: Float,
    val rssiCurrValueAP3: Float
){
    override fun toString(): String {
        return "-currPosition: $currPosition{rssiAP1: $rssiCurrValueAP1, rssiAP2: $rssiCurrValueAP2, rssiAP3: $rssiCurrValueAP3}"
    }
}