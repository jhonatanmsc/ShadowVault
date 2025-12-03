package com.example.shadowvault

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.io.File

class FileListActivity : AppCompatActivity() {
    private lateinit var bottomTaskbar: View
    private lateinit var topTaskbar: View
    private lateinit var selectedCountText: TextView
    private lateinit var taskbarController: TaskbarController
    private lateinit var cancelButton: ImageButton
    private lateinit var selectAllBtn: ImageButton
    private lateinit var renameBtn: ImageButton
    private lateinit var deleteBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_file_list)
        cancelButton = findViewById(R.id.cancel_btn)
        selectAllBtn = findViewById(R.id.select_all_btn)
        renameBtn = findViewById(R.id.rename_btn)
        deleteBtn = findViewById(R.id.delete_btn)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val swipe = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)

        swipe.setOnRefreshListener {
            reloadData()
            swipe.isRefreshing = false
        }

        reloadData()
    }

    private fun reloadData() {
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        val noFilesText: TextView = findViewById(R.id.nofiles_textview)
        bottomTaskbar = findViewById(R.id.bottom_taskbar)
        topTaskbar = findViewById(R.id.top_taskbar)
        selectedCountText = findViewById(R.id.selected_count)
        taskbarController = TaskbarController(topTaskbar, bottomTaskbar, selectedCountText)

        val path: String? = intent.getStringExtra("path")

        val root = File(path)
        val filesAndFolders: Array<File> = root.listFiles()

        if (filesAndFolders.isEmpty()) {
            noFilesText.visibility = View.VISIBLE
            return
        }

        noFilesText.visibility = View.INVISIBLE

        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = MyAdapter(this, filesAndFolders)
        adapter.onSelectionChanged = { count: Int ->
            if (count > 0) {
                if (count > 1) {
                    renameBtn.alpha = 0.5f
                    renameBtn.isEnabled = false
                } else {
                    renameBtn.alpha = 1.0f
                    renameBtn.isEnabled = true
                }
                taskbarController.show(count)
            } else taskbarController.hide()
        }
        renameBtn.setOnClickListener {
            adapter.renameSelectedFile()
        }
        deleteBtn.setOnClickListener {
            adapter.deleteSelectedFiles()
        }
        cancelButton.setOnClickListener {
            adapter.clearSelection()
        }
        selectAllBtn.setOnClickListener {
            adapter.selectAll()
        }
        recyclerView.adapter = adapter
    }
}