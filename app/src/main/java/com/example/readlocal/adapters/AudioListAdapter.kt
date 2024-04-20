package com.example.readlocal.adapters

import android.app.AlertDialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.readlocal.R
import com.example.readlocal.models.AudioFile
import com.example.readlocal.popup.showEditFileNameDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AudioListAdapter(private val audioFiles: MutableList<AudioFile>) :
    RecyclerView.Adapter<AudioListAdapter.AudioViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_audio_file, parent, false)
        return AudioViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val audioFile = audioFiles[position]
        holder.bind(audioFile)

        holder.iconDelete.setOnClickListener {
            showDeleteConfirmationDialog(holder.itemView.context, audioFile)
        }
    }

    override fun getItemCount(): Int {
        return audioFiles.size
    }

    inner class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener {
        private val textFileName: TextView = itemView.findViewById(R.id.textFileName)
        private val textFileSize: TextView = itemView.findViewById(R.id.textFileSize)
        private val textFileDate: TextView = itemView.findViewById(R.id.textFileDate)
        val iconDelete: ImageView = itemView.findViewById(R.id.imageDelete)

        init {
            itemView.setOnLongClickListener(this)
        }

        fun bind(audioFile: AudioFile) {
            textFileName.text = audioFile.name
            textFileSize.text = getFileSizeString(audioFile.size)
            textFileDate.text = SimpleDateFormat("yy-MM-dd", Locale.getDefault()).format(audioFile.date)
        }

        override fun onLongClick(view: View?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val audioFile = audioFiles[position]
                showEditFileNameDialog(view!!.context, audioFile.name) { newName ->
                    val oldUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    val selection = "${MediaStore.Audio.Media.TITLE}=?"
                    val selectionArgs = arrayOf(audioFile.name)
                    val newValues = ContentValues().apply {
                        put(MediaStore.Audio.Media.TITLE, newName)
                    }

                    val contentResolver = view.context.contentResolver
                    try {
                        val updatedRows = contentResolver.update(oldUri, newValues, selection, selectionArgs)

                        if (updatedRows > 0) {
                            audioFiles[position] = audioFile.copy(name = newName)
                            Toast.makeText(view.context, "Cập nhật tên file thành công", Toast.LENGTH_SHORT).show()
                            notifyItemChanged(position)
                        } else {
                            Log.e("UpdateFileName", "Không có bản ghi nào được cập nhật. File: ${audioFile.name}")
                            Toast.makeText(view.context, "Cập nhật tên file không thành công", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("UpdateFileName", "Lỗi khi cập nhật tên file. File: ${audioFile.name}", e)
                        Toast.makeText(view.context, "Cập nhật tên file không thành công", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            return true
        }


    }

    private fun showDeleteConfirmationDialog(context: Context, audioFile: AudioFile) {
        AlertDialog.Builder(context)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa file này không?")
            .setPositiveButton("Xóa") { dialog, _ ->
                // Xóa file
                deleteAudioFile(context, audioFile)
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteAudioFile(context: Context, audioFile: AudioFile) {
        val contentResolver: ContentResolver = context.contentResolver
        val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val selection = "${MediaStore.Audio.Media.TITLE}=?"
        val selectionArgs = arrayOf(audioFile.name)

        val deletedRows = contentResolver.delete(audioUri, selection, selectionArgs)

        if (deletedRows > 0) {
            Toast.makeText(context, "File đã được xóa.", Toast.LENGTH_SHORT).show()
            audioFiles.remove(audioFile)
            notifyDataSetChanged()
        } else {
            Toast.makeText(context, "Không thể xóa file.", Toast.LENGTH_SHORT).show()
        }
    }



    private fun getFileSizeString(size: Long): String {
        val kb = size / 1024
        val mb = kb / 1024
        return if (mb > 0) {
            String.format(Locale.getDefault(), "%.2f MB", mb.toFloat())
        } else {
            String.format(Locale.getDefault(), "%.2f KB", kb.toFloat())
        }
    }
}

