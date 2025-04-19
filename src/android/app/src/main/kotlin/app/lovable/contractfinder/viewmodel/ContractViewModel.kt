
package app.lovable.contractfinder.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.lovable.contractfinder.model.ContractRecord
import app.lovable.contractfinder.util.CsvParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList

class ContractViewModel : ViewModel() {
    
    // Thread-safe list to store all contracts
    private val contractData = CopyOnWriteArrayList<ContractRecord>()
    
    private val _searchResults = MutableLiveData<List<ContractRecord>>()
    val searchResults: LiveData<List<ContractRecord>> = _searchResults
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message
    
    private val _dataLoaded = MutableLiveData<Boolean>()
    val dataLoaded: LiveData<Boolean> = _dataLoaded
    
    fun searchContracts(query: String) {
        if (contractData.isEmpty()) {
            _message.value = "No data available. Please import a CSV file first."
            _searchResults.value = emptyList()
            return
        }
        
        _loading.value = true
        
        viewModelScope.launch {
            val results = withContext(Dispatchers.Default) {
                val lowerQuery = query.lowercase()
                contractData.filter {
                    it.contract.lowercase().contains(lowerQuery) ||
                    it.contractAccount.lowercase().contains(lowerQuery)
                }
            }
            
            _searchResults.value = results
            _loading.value = false
            
            if (results.isEmpty()) {
                _message.value = "No contracts found matching '$query'"
            } else {
                _message.value = "${results.size} contracts found"
            }
        }
    }
    
    fun importCsvFile(contentResolver: ContentResolver, uri: Uri) {
        _loading.value = true
        _message.value = "Importing CSV file..."
        
        viewModelScope.launch {
            try {
                val records = withContext(Dispatchers.IO) {
                    CsvParser.parseCsvFromUri(contentResolver, uri)
                }
                
                if (records.isEmpty()) {
                    _message.value = "The CSV file is empty or invalid."
                } else {
                    contractData.clear()
                    contractData.addAll(records)
                    _message.value = "Successfully imported ${records.size} contracts."
                    _dataLoaded.value = true
                }
            } catch (e: Exception) {
                _message.value = "Error importing CSV: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
    
    fun clearData() {
        contractData.clear()
        _searchResults.value = emptyList()
        _dataLoaded.value = false
        _message.value = "Data cleared."
    }
    
    fun getRecordCount(): Int = contractData.size
}
