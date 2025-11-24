package com.example.shadowvault

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MyAdapter(
    private val context: Context,
    private var filesAndFolders: Array<File>
) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
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
                val intent = Intent(context, FileListActivity::class.java)
                intent.putExtra("path", selectedFile.absolutePath)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } else {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    val type = "image/*"
                    intent.setDataAndType(Uri.parse(selectedFile.absolutePath), type)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot open the file", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // LongClick → popup menu
        holder.itemView.setOnLongClickListener { v ->
            val popupMenu = PopupMenu(context, v)
            popupMenu.menu.add("DELETE")
            popupMenu.menu.add("MOVE")
            popupMenu.menu.add("RENAME")

            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.title) {
                    "DELETE" -> {
                        val deleted = selectedFile.delete()
                        if (deleted) {
                            Toast.makeText(context, "DELETED", Toast.LENGTH_SHORT).show()
                            v.visibility = View.GONE
                        }
                    }

                    "MOVE" -> {
                        Toast.makeText(context, "MOVED", Toast.LENGTH_SHORT).show()
                    }

                    "RENAME" -> {
                        Toast.makeText(context, "RENAME", Toast.LENGTH_SHORT).show()
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
