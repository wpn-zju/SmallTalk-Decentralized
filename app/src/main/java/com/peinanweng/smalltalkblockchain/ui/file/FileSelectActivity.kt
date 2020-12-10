package com.peinanweng.smalltalkblockchain.ui.file

import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.OpenableColumns
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.peinanweng.smalltalkblockchain.R
import com.peinanweng.smalltalkblockchain.service.ISmallTalkAPI
import com.peinanweng.smalltalkblockchain.service.ISmallTalkService
import com.peinanweng.smalltalkblockchain.service.ISmallTalkServiceProvider
import com.peinanweng.smalltalkblockchain.service.RootService
import com.peinanweng.smalltalkblockchain.service.model.logic.SmallTalkApplication
import kotlinx.android.synthetic.main.activity_file_select.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class FileSelectActivity
    : AppCompatActivity(), ISmallTalkServiceProvider, FileListAdapter.UploadTaskListener {
    private val fileList: ArrayList<FileUploadTask> = ArrayList()
    private val adapter = FileListAdapter(fileList)

    private lateinit var service: ISmallTalkService
    private var bound: Boolean = false
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            service = (binder as RootService.RootServiceBinder).getService()
            bound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            bound = false
        }
    }

    override fun hasService(): Boolean {
        return bound
    }

    override fun getService(): ISmallTalkService? {
        return if (bound) {
            service
        } else {
            null
        }
    }

    override fun onStart() {
        super.onStart()

        Intent(this, RootService::class.java).also { intent -> bindService(
            intent,
            connection,
            Context.BIND_AUTO_CREATE
        ) }
    }

    override fun onStop() {
        super.onStop()

        unbindService(connection)
        bound = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_select)

        setSupportActionBar(file_select_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        adapter.setFileClickListener(this)

        val layoutManager = LinearLayoutManager(this)
        file_upload_list.layoutManager = layoutManager
        file_upload_list.adapter = adapter

        btn_select.setOnClickListener {
            openContentPicker()
        }

        btn_upload.setOnClickListener {
            uploadFiles()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openContentPicker() {
        Intent(Intent.ACTION_GET_CONTENT).also {
            it.type = "image/*"
            val mimeTypes = arrayOf("image/png", "image/jpeg")
            it.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            it.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(it, REQUEST_CODE_PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val lastCount = fileList.size

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_PICK_IMAGE -> {
                    if (data?.clipData != null) {
                        val clipData = data.clipData!!
                        val count: Int = clipData.itemCount
                        for (i in 0 until count) {
                            val uri = clipData.getItemAt(i).uri
                            val file = FileUploadTask(uri,
                                contentResolver.getFileName(uri),
                                contentResolver.getFileSize(uri))
                            fileList.add(file)
                        }
                    } else if (data?.data != null) {
                        val uri = data.data!!
                        val file = FileUploadTask(uri,
                            contentResolver.getFileName(uri),
                            contentResolver.getFileSize(uri))
                        fileList.add(file)
                    }
                }
            }
        }

        adapter.notifyItemRangeInserted(lastCount, fileList.size - lastCount)
    }

    private fun uploadFiles() {
        fileList.forEach { fileInfo ->
            fileInfo.onStartUpload()

            val parcelFileDescriptor = contentResolver
                .openFileDescriptor(fileInfo.fileUri, "r", null) ?: return
            val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
            val file = File(cacheDir, contentResolver.getFileName(fileInfo.fileUri))
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            val body = UploadRequestBody(file, fileInfo,"application")
            ISmallTalkAPI()
                .uploadFile(
                    MultipartBody.Part.createFormData("file", fileInfo.fileName, body),
                    "base", "json".toRequestBody("multipart/form-data".toMediaTypeOrNull()))
                .enqueue(object : Callback<Void> {
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        fileInfo.onUploadFailed()
                    }

                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        fileInfo.onUploadResponse()
                        response.body().let { file }

                        val channel = intent.getStringExtra("channel")

                        if (channel != null && hasService()) {
                            service.messageForward(
                                channel, SmallTalkApplication.BASE_URL + "/download/base/" + fileInfo.fileName, false)
                        }
                    }
                })
        }
    }

    override fun onItemCanceledListener(view: View, fileId: Int) {
        fileList.removeAt(fileId)
        adapter.notifyItemRemoved(fileId)
    }

    companion object {
        const val REQUEST_CODE_PICK_IMAGE = 101
    }
}

class UploadRequestBody(
    private val file: File,
    private val fileUploader: FileUploadTask,
    private val contentType: String
) : RequestBody() {

    override fun contentType(): MediaType? {
        return "$contentType/*".toMediaTypeOrNull()
    }

    override fun contentLength(): Long {
        return file.length()
    }

    override fun writeTo(sink: BufferedSink) {
        val length = file.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val fileInputStream = FileInputStream(file)
        var uploaded = 0L
        fileInputStream.use { inputStream ->
            var read: Int
            val handler = Handler(Looper.getMainLooper())
            while (inputStream.read(buffer).also { read = it } != -1) {
                handler.post(ProgressUpdater(uploaded, length))
                uploaded += read
                sink.write(buffer, 0, read)
            }
        }
    }

    interface UploadCallback {
        fun onStartUpload()
        fun onUploadFailed()
        fun onUploadResponse()
        fun onProgressUpdate(percentage: Int)
    }

    inner class ProgressUpdater(private val uploaded: Long, private val total: Long) : Runnable {
        override fun run() {
            fileUploader.onProgressUpdate((100 * uploaded / total).toInt())
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }
}

fun ContentResolver.getFileName(uri: Uri): String {
    var name = String()
    val returnCursor = this.query(uri, null, null, null, null)
    if (returnCursor != null) {
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        name = returnCursor.getString(nameIndex)
        returnCursor.close()
    }
    return name
}

fun ContentResolver.getFileSize(uri: Uri): Long {
    var size = 0L
    val returnCursor = this.query(uri, null, null, null, null)
    if (returnCursor != null) {
        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        size = returnCursor.getLong(sizeIndex)
        returnCursor.close()
    }
    return size
}
