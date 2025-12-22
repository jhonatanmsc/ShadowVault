package com.example.shadowvault

import android.app.Activity
import android.content.Intent
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.shadowvault.models.FileListViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyAdapter(
    private val viewModel: FileListViewModel,
    private val activity: Activity
) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    private var files = listOf<File>()
    private var selected = emptySet<File>()

    fun submitList(list: List<File>) {
        files = list
        notifyDataSetChanged()
    }

    fun notifySelectionChanged(sel: Set<File>) {
        selected = sel
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_item, parent, false)
        val outValue = TypedValue()
        val theme = parent.context.theme
        if (theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)) {
            view.setBackgroundResource(outValue.resourceId)
        }
        view.isClickable = true
        view.isFocusable = true
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]

        // UI binding...
        holder.textView.text = file.name

        if (file.isDirectory) {
            val count = file.listFiles()?.size ?: 0
            holder.itemsTextView.text = "$count items"
            holder.imageView.setImageResource(R.drawable.baseline_folder_24)
        } else {
            holder.itemsTextView.text = readableFileSize(file.length())
            holder.dateTextView.text = formatDate(file.lastModified())
            val ext = file.extension.lowercase(Locale.getDefault())
            val textExtensions = setOf("txt", "md", "log", "csv", "json", "xml", "html", "htm", "yaml", "yml", "properties")
            val audioExtentions = setOf("mp3", "wav", "flac", "aac", "ogg", "m4a")
            if (ext in textExtensions) {
                holder.imageView.setImageResource(R.drawable.baseline_description_24)
            } else if (ext == "pdf") {
                holder.imageView.setImageResource(R.drawable.baseline_article_24)
            } else if (ext in audioExtentions) {
                holder.imageView.setImageResource(R.drawable.baseline_music_note_24)
            } else {
                holder.imageView.setImageResource(R.drawable.baseline_insert_drive_file_24)
            }

        }

        holder.itemView.setOnClickListener {
            if (selected.isNotEmpty()) {
                viewModel.toggleSelection(file)
                return@setOnClickListener
            }

            if (file.isDirectory) {
                val intent = Intent(activity, FileListActivity::class.java)
                intent.putExtra("path", file.absolutePath)
                activity.startActivity(intent)
            } else {
                openFile(file)
            }
        }

        holder.itemView.setOnLongClickListener {
            viewModel.toggleSelection(file)
            true
        }

        if (selected.contains(file)) {
            holder.itemView.setBackgroundColor(0xFFE0F0FF.toInt())
        } else {
            val outValue = TypedValue()
            if (activity.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)) {
                holder.itemView.setBackgroundResource(outValue.resourceId)
            }
        }
    }

    private fun readableFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format(Locale.getDefault(), "%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    private fun formatDate(timestamp: Long): String {
        if (timestamp <= 0L) return ""
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun openFile(file: File) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(file.toURI().toString().toUri(), "image/*")
            activity.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(activity, "Cannot open file", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = files.size

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val textView: TextView = v.findViewById(R.id.file_name_text_view)
        val itemsTextView: TextView = v.findViewById(R.id.file_items_text_view)
        val dateTextView: TextView = v.findViewById(R.id.file_date_text_view)
        val imageView: ImageView = v.findViewById(R.id.icon_view)
        val divider: View = v.findViewById(R.id.divider)
    }
}
