
package app.lovable.contractfinder.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContractRecord(
    val contract: String,
    val contractAccount: String,
    val ibcName: String,
    val portfolio: String,
    val cbOffer: String,
    val rebateOffer: String,
    val pwoScheme: String
) : Parcelable {
    companion object {
        // Headers expected in the CSV file
        const val HEADER_CONTRACT = "Contract"
        const val HEADER_CONTRACT_ACCOUNT = "Contract Account"
        const val HEADER_IBC_NAME = "IBCName"
        const val HEADER_PORTFOLIO = "Portfolio"
        const val HEADER_CB_OFFER = "CB Offer"
        const val HEADER_REBATE_OFFER = "Rebate Offer"
        const val HEADER_PWO_SCHEME = "PWO Scheme"
    }
}
