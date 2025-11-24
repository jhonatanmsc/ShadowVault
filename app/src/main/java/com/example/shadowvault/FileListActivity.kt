package com.example.shadowvault

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FileListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_file_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        val noFilesText: TextView = findViewById(R.id.nofiles_textview)

        val path: String? = intent.getStringExtra("path")

        val root = File(path)
        val filesAndFolders: Array<File> = root.listFiles()

        if (filesAndFolders==null || filesAndFolders.isEmpty()) {
            noFilesText.visibility = View.VISIBLE
            return
        }

        noFilesText.visibility = View.INVISIBLE

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MyAdapter(
            applicationContext,
            filesAndFolders
        )
    }
}