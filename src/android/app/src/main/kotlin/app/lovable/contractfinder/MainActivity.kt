
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
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import app.lovable.contractfinder.databinding.ActivityMainBinding
import app.lovable.contractfinder.model.ContractRecord
import app.lovable.contractfinder.viewmodel.ContractViewModel
import com.google.android.material.snackbar.Snackbar
import android.util.Log

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ContractViewModel
    private lateinit var adapter: ContractListAdapter
    private  val MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 1001
    
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
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MANAGE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (Environment.isExternalStorageManager()) {
                // If permission is granted, open the file picker
                openFilePicker()
            } else {
                // Show a message if permission is denied
                Snackbar.make(
                    binding.root,
                    "Permission denied. Cannot access files.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
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
        Log.d("FilePicker", "checkPermissionAndOpenFilePicker called")

        when {
            // For Android 11 and later, check for MANAGE_EXTERNAL_STORAGE permission
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                Log.d("FilePicker", "Android 11 or later detected")
                if (!Environment.isExternalStorageManager()) {
                    Log.d("FilePicker", "MANAGE_EXTERNAL_STORAGE permission is not granted")
                    // If the app doesn't have access to all files, request permission
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    // Check if there is an activity that can handle this intent
                    if (intent.resolveActivity(packageManager) != null) {
                        Log.d("FilePicker", "Activity found to handle MANAGE_APP_ALL_FILES_ACCESS_PERMISSION intent")
                        startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST_CODE)
                    } else {
                        // Handle the case where there is no activity to handle the intent
                        Log.e("FilePicker", "No activity found to handle the MANAGE_APP_ALL_FILES_ACCESS_PERMISSION intent.")
                        // Optionally, show a message or provide fallback behavior
                    }
                } else {
                    Log.d("FilePicker", "MANAGE_EXTERNAL_STORAGE permission already granted")
                    // If permission is granted, proceed to open file picker
                    openFilePicker()
                }
            }
            // For Android 10 and below, check for READ_EXTERNAL_STORAGE permission
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("FilePicker", "READ_EXTERNAL_STORAGE permission granted for Android 10 and below")
                // If permission is granted, proceed with file picker
                openFilePicker()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                Log.d("FilePicker", "User has previously denied READ_EXTERNAL_STORAGE permission")
                // If the user has previously denied permission, explain why it's needed
                Snackbar.make(
                    binding.root,
                    "Storage permission is needed to import CSV files",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Grant") {
                        Log.d("FilePicker", "Requesting READ_EXTERNAL_STORAGE permission from user")
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                    .show()
            }
            else -> {
                Log.d("FilePicker", "Requesting READ_EXTERNAL_STORAGE permission for the first time")
                // Request the permission if it's not granted
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
