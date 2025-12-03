package com.example.shadowvault

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import androidx.core.net.toUri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyAdapter(
    private val activity: Activity,
    private var filesAndFolders: Array<File>
) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    private val selectedItems = mutableSetOf<File>()
    var onSelectionChanged: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(activity)
            .inflate(R.layout.recycler_item, parent, false)
        // Aplica o selectable ripple background programaticamente
        val outValue = TypedValue()
        val resolved = activity.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        if (resolved) {
            view.setBackgroundResource(outValue.resourceId)
        }
        view.isClickable = true
        view.isFocusable = true
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val selectedFile = filesAndFolders[position]

        holder.textView.text = selectedFile.name

        if (selectedFile.isDirectory) {
            val count = selectedFile.listFiles()?.size ?: 0
            holder.itemsTextView.text = "$count items"
            holder.imageView.setImageResource(R.drawable.baseline_folder_24)
        } else {
            holder.itemsTextView.text = readableFileSize(selectedFile.length())
            holder.dateTextView.text = formatDate(selectedFile.lastModified())
            val ext = selectedFile.extension.lowercase(Locale.getDefault())
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

        holder.itemView.setOnClickListener { v ->
            if (selectedItems.isNotEmpty()) {
                toggleSelection(selectedFile)
                return@setOnClickListener
            }

            v.isEnabled = false

            v.postDelayed({
                v.isEnabled = true

                if (selectedFile.isDirectory) {
                    val intent = Intent(activity, FileListActivity::class.java)
                    intent.putExtra("path", selectedFile.absolutePath)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    activity.startActivity(intent)
                } else {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        val type = "image/*"
                        intent.setDataAndType(selectedFile.absolutePath.toUri(), type)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        activity.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(activity, "Cannot open the file", Toast.LENGTH_SHORT).show()
                    }
                }
            }, 120)
        }

        // LongClick → popup menu
        holder.itemView.setOnLongClickListener {
            toggleSelection(selectedFile)
            true
        }
//            val themedContext = ContextThemeWrapper(activity, R.style.CustomPopupMenu)
//            val popupMenu = PopupMenu(themedContext, v)
//            popupMenu.menu.add("DELETE")
//            popupMenu.menu.add("MOVE")
//            popupMenu.menu.add("RENAME")
//
//            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
//                when (item.title) {
//                    "DELETE" -> {
//                        val deleted = selectedFile.delete()
//                        if (deleted) {
//                            Toast.makeText(activity, "DELETED", Toast.LENGTH_SHORT).show()
//                            v.visibility = View.GONE
//                        }
//                    }
//
//                    "MOVE" -> {
//                        Toast.makeText(activity, "MOVED", Toast.LENGTH_SHORT).show()
//                    }
//
//                    "RENAME" -> {
//
//                    }
//                }
//                true
//            }
//
//            popupMenu.show()
//            true
//        }
        if (selectedItems.contains(selectedFile)) {
            holder.itemView.setBackgroundColor(0xFFE0F0FF.toInt()) // azul clarinho
        } else {
            val outValue = TypedValue()
            if (activity.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)) {
                holder.itemView.setBackgroundResource(outValue.resourceId)
            }
        }
        if (position == filesAndFolders.size - 1) {
            holder.divider.visibility = View.GONE
        } else {
            holder.divider.visibility = View.VISIBLE
        }
    }

    private fun toggleSelection(file: File) {
        val index = filesAndFolders.indexOf(file)

        if (selectedItems.contains(file)) {
            selectedItems.remove(file)
        } else {
            selectedItems.add(file)
        }

        if (index != -1) notifyItemChanged(index)

        onSelectionChanged?.invoke(selectedItems.size)
    }

    override fun getItemCount(): Int = filesAndFolders.size

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

    fun clearSelection() {
        val prevSelection = selectedItems.toList()
        selectedItems.clear()

        prevSelection.forEach { file ->
            val index = filesAndFolders.indexOf(file)
            if (index != -1) notifyItemChanged(index)
        }

        onSelectionChanged?.invoke(0)
    }

    fun renameSelectedFile() {
        val selectedFile = selectedItems.firstOrNull() ?: return
        val position = filesAndFolders.indexOf(selectedFile)
        if (position == -1) return
        val editText = EditText(activity)
        editText.setText(selectedFile.name)

        val dialog = AlertDialog.Builder(activity)
            .setTitle("Rename File")
            .setView(editText)
            .setPositiveButton("Rename") { _, _ ->
                val newName = editText.text.toString().trim()

                if (newName.isNotEmpty()) {
                    val newFile = File(selectedFile.parent, newName)

                    val renamed = selectedFile.renameTo(newFile)

                    if (renamed) {
                        Toast.makeText(activity, "Renamed", Toast.LENGTH_SHORT).show()

                        filesAndFolders[position] = newFile
                        notifyItemChanged(position)

                    } else {
                        Toast.makeText(activity, "Rename failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            editText.requestFocus()
            editText.selectAll()
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }

        dialog.show()
    }

    fun deleteSelectedFiles() {
        val filesToDelete = selectedItems.toList()
        val count = filesToDelete.size
        val dialog = AlertDialog.Builder(activity)
            .setTitle("$count items will be deleted, are you sure?")
            .setPositiveButton("Delete") { _, _ ->
                filesToDelete.forEach { file ->
                    val deleted = file.delete()
                    if (deleted) {
                        val index = filesAndFolders.indexOf(file)
                        if (index != -1) {
                            filesAndFolders = filesAndFolders.filter { it != file }.toTypedArray()
                            notifyItemRemoved(index)
                        }
                        selectedItems.remove(file)
                    }
                }
                onSelectionChanged?.invoke(selectedItems.size)
                Toast.makeText(activity, "$count items deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    fun selectAll() {
        if (selectedItems.size == filesAndFolders.size) return
        val alreadySelected = selectedItems.toSet()

        selectedItems.clear()
        selectedItems.addAll(filesAndFolders)

        filesAndFolders.forEachIndexed { index, file ->
            if (!alreadySelected.contains(file)) {
                notifyItemChanged(index)
            }
        }

        onSelectionChanged?.invoke(selectedItems.size)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.file_name_text_view)
        val itemsTextView: TextView = itemView.findViewById(R.id.file_items_text_view)
        val dateTextView: TextView = itemView.findViewById(R.id.file_date_text_view)
        val imageView: ImageView = itemView.findViewById(R.id.icon_view)
        val divider: View = itemView.findViewById(R.id.divider)
    }
}

//<color name="file_image">#8BC34A</color>        <!-- imagens -->
//<color name="file_audio">#9C27B0</color>       <!-- áudio -->
//<color name="file_video">#FF9800</color>       <!-- vídeo -->
//<color name="file_pdf">#B00020</color>         <!-- PDF / documentos importantes -->
//<color name="file_archive">#546E7A</color>     <!-- zip/rar -->
//<color name="file_executable">#3949AB</color>  <!-- executáveis/binários -->
//<color name="file_spreadsheet">#388E3C</color> <!-- planilhas -->
//<color name="file_presentation">#FBC02D</color> <!-- apresentações -->
//<color name="file_code">#0097A7</color>        <!-- código / dev -->
//<color name="file_text">#7FC8F8</color>        <!-- texto (mantido) -->
//<color name="folder_tint">#FAD4A1</color>       <!-- pasta (mantido) -->
