package com.example.shadowvault

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
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
            topTaskbar = findViewById(R.id.top_taskbar),
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
            viewModel.deleteSelected()
        }

        findViewById<ImageButton>(R.id.rename_btn).setOnClickListener {
            val sel = viewModel.selected.value.firstOrNull() ?: return@setOnClickListener
            showRenameDialog(sel)
        }
    }

    private fun showRenameDialog(file: File) {
        val edit = EditText(this)
        edit.setText(file.name)

        AlertDialog.Builder(this)
            .setTitle("Rename")
            .setView(edit)
            .setPositiveButton("OK") { _, _ ->
                val name = edit.text.toString()
                if (name.isNotBlank())
                    viewModel.renameFile(file, name)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
