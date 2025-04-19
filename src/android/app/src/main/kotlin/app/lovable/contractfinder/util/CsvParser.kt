
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
import android.util.Log

object CsvParser {

    /**
     * Parses a CSV file from the given URI.
     * Uses Apache Commons CSV for reliable parsing of large files.
     */
    fun parseCsvFromUri(contentResolver: ContentResolver, uri: Uri): List<ContractRecord> {
        val records = ArrayList<ContractRecord>()

        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))

                val csvParser = CSVParser(reader, CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreHeaderCase()
                    .withTrim()
                )

                for (csvRecord in csvParser) {
                    // Parse the records and check for invalid or missing fields
                    val contract = csvRecord.get(HEADER_CONTRACT) ?: ""
                    val contractAccount = csvRecord.get(HEADER_CONTRACT_ACCOUNT) ?: ""
                    if (contract.isEmpty() || contractAccount.isEmpty()) {
                        Log.e("CsvParser", "Missing required fields in row: $csvRecord")
                    }

                    val record = ContractRecord(
                        contract = contract,
                        contractAccount = contractAccount,
                        ibcName = csvRecord.get(HEADER_IBC_NAME) ?: "",
                        portfolio = csvRecord.get(HEADER_PORTFOLIO) ?: "",
                        cbOffer = csvRecord.get(HEADER_CB_OFFER) ?: "",
                        rebateOffer = csvRecord.get(HEADER_REBATE_OFFER) ?: "",
                        pwoScheme = csvRecord.get(HEADER_PWO_SCHEME) ?: ""
                    )
                    records.add(record)
                }
            }
        } catch (e: Exception) {
            Log.e("CsvParser", "Error reading CSV: ${e.message}")
        }

        return records
    }

}

