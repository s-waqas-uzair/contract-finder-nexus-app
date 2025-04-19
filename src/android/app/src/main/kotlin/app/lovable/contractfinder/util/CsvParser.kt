
package app.lovable.contractfinder.util

import android.content.ContentResolver
import android.net.Uri
import app.lovable.contractfinder.model.ContractRecord
import app.lovable.contractfinder.model.ContractRecord.Companion.HEADER_CONTRACT
import app.lovable.contractfinder.model.ContractRecord.Companion.HEADER_CONTRACT_ACCOUNT
import app.lovable.contractfinder.model.ContractRecord.Companion.HEADER_IBC_NAME
import app.lovable.contractfinder.model.ContractRecord.Companion.HEADER_PORTFOLIO
import app.lovable.contractfinder.model.ContractRecord.Companion.HEADER_CB_OFFER
import app.lovable.contractfinder.model.ContractRecord.Companion.HEADER_REBATE_OFFER
import app.lovable.contractfinder.model.ContractRecord.Companion.HEADER_PWO_SCHEME
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.collections.ArrayList

object CsvParser {
    
    /**
     * Parses a CSV file from the given URI
     * Uses Apache Commons CSV for reliable parsing of large files
     */
    fun parseCsvFromUri(contentResolver: ContentResolver, uri: Uri): List<ContractRecord> {
        val records = ArrayList<ContractRecord>()
        
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val reader = BufferedReader(InputStreamReader(inputStream))
            
            val csvParser = CSVParser(reader, CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim()
            )
            
            for (csvRecord in csvParser) {
                val contract = csvRecord.get(HEADER_CONTRACT) ?: ""
                val contractAccount = csvRecord.get(HEADER_CONTRACT_ACCOUNT) ?: ""
                val ibcName = csvRecord.get(HEADER_IBC_NAME) ?: ""
                val portfolio = csvRecord.get(HEADER_PORTFOLIO) ?: ""
                val cbOffer = csvRecord.get(HEADER_CB_OFFER) ?: ""
                val rebateOffer = csvRecord.get(HEADER_REBATE_OFFER) ?: ""
                val pwoScheme = csvRecord.get(HEADER_PWO_SCHEME) ?: ""
                
                val record = ContractRecord(
                    contract = contract,
                    contractAccount = contractAccount,
                    ibcName = ibcName,
                    portfolio = portfolio,
                    cbOffer = cbOffer,
                    rebateOffer = rebateOffer,
                    pwoScheme = pwoScheme
                )
                
                records.add(record)
                
                // Optional: implement batch processing for extremely large files
                // if (records.size % 10000 == 0) {
                //     // Process in batches of 10,000
                // }
            }
        }
        
        return records
    }
}
