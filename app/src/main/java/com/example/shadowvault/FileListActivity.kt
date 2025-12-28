package com.example.shadowvault

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shadowvault.models.FileListViewModel
import androidx.lifecycle.repeatOnLifecycle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.shadowvault.factories.FileListViewModelFactory
import kotlinx.coroutines.launch
import java.io.File

class FileListActivity : AppCompatActivity() {
    private lateinit var taskbarController: TaskbarController

    private val viewModel: FileListViewModel by viewModels() {
        FileListViewModelFactory(intent.getStringExtra("path")!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_file_list)

        val path = intent.getStringExtra("path") ?: return
        viewModel.load(path)

        val swipe = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)

        swipe.setOnRefreshListener {
            viewModel.load(path)
        }

        val recycler = findViewById<RecyclerView>(R.id.recycler_view)
        val adapter = MyAdapter(viewModel, this)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        taskbarController = TaskbarController(
            topTaskbar = findViewById(R.id.top_taskbar_selection),
            bottomTaskbar = findViewById(R.id.bottom_taskbar),
            selectedCountText = findViewById(R.id.selected_count)
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // observe file list
                launch {
                    viewModel.files.collect { list ->
                        adapter.submitList(list)
                        findViewById<TextView>(R.id.nofiles_textview)
                            .visibility = if (list.isEmpty()) View.VISIBLE else View.GONE

                        swipe.isRefreshing = false
                    }
                }

                // observe selection
                launch {
                    viewModel.selected.collect { selected ->
                        val count = selected.size

                        if (count > 0) {
                            taskbarController.show(count)
                        } else {
                            taskbarController.hide()
                        }

                        adapter.notifySelectionChanged(selected)
                    }
                }
                // observe loading
                launch {
                    viewModel.loading.collect { isLoading ->
                        swipe.isRefreshing = isLoading
                    }
                }

            }
        }

        findViewById<ImageButton>(R.id.select_all_btn).setOnClickListener {
            viewModel.selectAll()
        }

        findViewById<ImageButton>(R.id.cancel_btn).setOnClickListener {
            viewModel.clearSelection()
        }

        findViewById<ImageButton>(R.id.delete_btn).setOnClickListener {
            val sel = viewModel.selected.value.firstOrNull() ?: return@setOnClickListener
            showDeleteDialog(sel)
        }

        findViewById<ImageButton>(R.id.rename_btn).setOnClickListener {
            val sel = viewModel.selected.value.firstOrNull() ?: return@setOnClickListener
            showRenameDialog(sel)
        }
    }

    private fun showDeleteDialog(file: File) {
        val selectedItems = viewModel.selected.value
        if (selectedItems.size > 1) {
            AlertDialog.Builder(this)
                .setTitle("${selectedItems.size} items will be deleted, are you sure?")
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteSelected()
                    Toast.makeText(this, "${selectedItems.size} items deleted", Toast.LENGTH_SHORT)
                        .show()
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Delete ${file.name}?")
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteSelected()
                    Toast.makeText(this, "${file.name} deleted", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showRenameDialog(file: File) {
        val edit = EditText(this)
        val oldName = file.name
        edit.setText(file.name)

        AlertDialog.Builder(this)
            .setTitle("Rename")
            .setView(edit)
            .setPositiveButton("OK") { _, _ ->
                val name = edit.text.toString()
                if (name.isNotBlank()) {
                    viewModel.renameFile(file, name)
                    Toast.makeText(this, "$oldName renamed to ${name}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
