package com.example.shadowvault

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
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

class MyAdapter(
    private val activity: Activity,
    private var filesAndFolders: Array<File>
) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(activity)
            .inflate(R.layout.recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val selectedFile = filesAndFolders[position]

        holder.textView.text = selectedFile.name

        if (selectedFile.isDirectory) {
            holder.imageView.setImageResource(R.drawable.baseline_folder_24)
        } else {
            holder.imageView.setImageResource(R.drawable.baseline_description_24)
        }

        // OnClick → open file or load new directory
        holder.itemView.setOnClickListener {
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
        }

        // LongClick → popup menu
        holder.itemView.setOnLongClickListener { v ->
            val themedContext = ContextThemeWrapper(activity, R.style.CustomPopupMenu)
            val popupMenu = PopupMenu(themedContext, v)
            popupMenu.menu.add("DELETE")
            popupMenu.menu.add("MOVE")
            popupMenu.menu.add("RENAME")

            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.title) {
                    "DELETE" -> {
                        val deleted = selectedFile.delete()
                        if (deleted) {
                            Toast.makeText(activity, "DELETED", Toast.LENGTH_SHORT).show()
                            v.visibility = View.GONE
                        }
                    }

                    "MOVE" -> {
                        Toast.makeText(activity, "MOVED", Toast.LENGTH_SHORT).show()
                    }

                    "RENAME" -> {
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

                                        // Update your list and notify the adapter
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
                }
                true
            }

            popupMenu.show()
            true
        }
    }

    override fun getItemCount(): Int = filesAndFolders.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.file_name_text_view)
        val imageView: ImageView = itemView.findViewById(R.id.icon_view)
    }
}
