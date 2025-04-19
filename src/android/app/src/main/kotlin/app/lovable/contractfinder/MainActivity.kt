
package app.lovable.contractfinder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import app.lovable.contractfinder.databinding.ActivityMainBinding
import app.lovable.contractfinder.model.ContractRecord
import app.lovable.contractfinder.viewmodel.ContractViewModel
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ContractViewModel
    private lateinit var adapter: ContractListAdapter
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Snackbar.make(
                binding.root,
                "Storage permission granted. You can now import CSV files.",
                Snackbar.LENGTH_SHORT
            ).show()
        } else {
            Snackbar.make(
                binding.root,
                "Storage permission denied. Cannot import CSV files.",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
    
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { importCsv(it) }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        
        viewModel = ViewModelProvider(this)[ContractViewModel::class.java]
        
        setupRecyclerView()
        setupSearchView()
        setupObservers()
        setupClickListeners()
        
        // Handle intent when app is opened with a CSV file
        handleIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW && intent.type?.startsWith("text/") == true) {
            val uri = intent.data
            uri?.let { importCsv(it) }
        }
    }
    
    private fun setupRecyclerView() {
        adapter = ContractListAdapter()
        binding.recyclerView.adapter = adapter
    }
    
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                search(query)
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    adapter.updateData(emptyList())
                    updateUIForEmptyResults()
                }
                return false
            }
        })
    }
    
    private fun setupObservers() {
        viewModel.searchResults.observe(this) { results ->
            adapter.updateData(results)
            
            if (results.isEmpty()) {
                updateUIForEmptyResults()
            } else {
                binding.emptyView.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                binding.resultsCount.text = "${results.size} contracts found"
                binding.resultsCount.visibility = View.VISIBLE
            }
        }
        
        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.fab.isEnabled = !isLoading
            binding.searchView.isEnabled = !isLoading
        }
        
        viewModel.message.observe(this) { message ->
            if (message.isNotBlank()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            }
        }
        
        viewModel.dataLoaded.observe(this) { isLoaded ->
            binding.noDataView.visibility = if (isLoaded) View.GONE else View.VISIBLE
            binding.searchView.isEnabled = isLoaded
            
            if (isLoaded) {
                binding.dataStatus.text = "Database ready: ${viewModel.getRecordCount()} contracts loaded"
                binding.dataStatus.visibility = View.VISIBLE
            } else {
                binding.dataStatus.visibility = View.GONE
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.fab.setOnClickListener {
            checkPermissionAndOpenFilePicker()
        }
    }
    
    private fun search(query: String?) {
        if (query.isNullOrBlank()) {
            adapter.updateData(emptyList())
            updateUIForEmptyResults()
            return
        }
        
        viewModel.searchContracts(query)
    }
    
    private fun updateUIForEmptyResults() {
        if (binding.searchView.query.isNotBlank()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.emptyView.text = "No contracts found matching '${binding.searchView.query}'"
            binding.recyclerView.visibility = View.GONE
            binding.resultsCount.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.recyclerView.visibility = View.GONE
            binding.resultsCount.visibility = View.GONE
        }
    }
    
    private fun checkPermissionAndOpenFilePicker() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                openFilePicker()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                Snackbar.make(
                    binding.root,
                    "Storage permission is needed to import CSV files",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Grant") {
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                    .show()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
    
    private fun openFilePicker() {
        getContent.launch("text/csv")
    }
    
    private fun importCsv(uri: Uri) {
        viewModel.importCsvFile(contentResolver, uri)
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_import -> {
                checkPermissionAndOpenFilePicker()
                true
            }
            R.id.action_clear -> {
                viewModel.clearData()
                adapter.updateData(emptyList())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
