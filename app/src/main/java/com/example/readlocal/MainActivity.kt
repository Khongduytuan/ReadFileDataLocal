package com.example.readlocal


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.readlocal.adapters.AudioListAdapter
import com.example.readlocal.models.AudioFile
import java.util.*
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log


class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var audioListAdapter: AudioListAdapter
    private val audioFiles = mutableListOf<AudioFile>()

    companion object {
        private const val REQUEST_PERMISSION_CODE = 101
        private const val REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        audioListAdapter = AudioListAdapter(audioFiles)
        recyclerView.adapter = audioListAdapter

        if (checkPermission()) {
            loadAudioFiles()
        } else {
            requestPermission()
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_PERMISSION_CODE
        )
    }

    private fun loadAudioFiles() {
        val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA // Thêm trường DATA để lấy đường dẫn của file audio
        )

        val cursor = contentResolver.query(
            audioUri,
            projection,
            null,
            null,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        )

        cursor?.use { c ->
            val titleColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val sizeColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateAddedColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val pathColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA) // Lấy cột đường dẫn

            while (c.moveToNext()) {
                val title = c.getString(titleColumn)
                val size = c.getLong(sizeColumn)
                val dateAdded = c.getLong(dateAddedColumn)
                val path = c.getString(pathColumn) // Lấy đường dẫn từ cursoro
                Log.e("Path", path)

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = dateAdded * 1000

                val audioFile = AudioFile(title, size, calendar.time, path) // Cung cấp đường dẫn khi tạo đối tượng AudioFile
                audioFiles.add(audioFile)
            }
            audioListAdapter.notifyDataSetChanged()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadAudioFiles()

                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {

                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_PERMISSION_CODE_WRITE_EXTERNAL_STORAGE
                    )
                }
            } else {
                Toast.makeText(
                    this,
                    "Permission denied. Can't load audio files.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // Handle settings action
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}