package com.example.shadowvault

import android.os.Bundle
import android.view.View
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
    private lateinit var taskbar: View
//    private lateinit var selectedCountText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_file_list)
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
        taskbar = findViewById(R.id.bottom_taskbar)
//        selectedCountText = findViewById(R.id.selected_count)

        val path: String? = intent.getStringExtra("path")

        val root = File(path)
        val filesAndFolders: Array<File> = root.listFiles()

        if (filesAndFolders.isEmpty()) {
            noFilesText.visibility = View.VISIBLE
            return
        }

        noFilesText.visibility = View.INVISIBLE

        recyclerView.layoutManager = LinearLayoutManager(this)
//        recyclerView.adapter = MyAdapter(
//            this,
//            filesAndFolders
//        )
        val adapter = MyAdapter(this, filesAndFolders)
        adapter.onSelectionChanged = { count: Int ->
            if (count > 0) {
                showTaskbar(count)
            } else hideTaskbar()
        }
        recyclerView.adapter = adapter
    }

    fun showTaskbar(selectedCount: Int) {
//        selectedCountText.text = "$selectedCount selected"
//        Toast.makeText(this, "n", Toast.LENGTH_SHORT).show()
        if (taskbar.visibility == View.VISIBLE) return
//        Toast.makeText(this, "y", Toast.LENGTH_SHORT).show()
        taskbar.visibility = View.VISIBLE

        taskbar.post {
            taskbar.translationY = taskbar.height.toFloat()
            taskbar.animate()
                .translationY(0f)
                .setDuration(250)
                .start()
        }
    }

    fun hideTaskbar() {
        if (taskbar.visibility != View.VISIBLE) return

        taskbar.animate()
            .translationY(taskbar.height.toFloat())
            .setDuration(250)
            .withEndAction {
                taskbar.visibility = View.GONE
            }
            .start()
    }
}