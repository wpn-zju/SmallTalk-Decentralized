package com.peinanweng.smalltalkblockchain.ui.file

import android.net.Uri
import android.view.View

class FileUploadTask (
    val fileUri: Uri,
    val fileName: String,
    val fileSize: Long
) : UploadRequestBody.UploadCallback {
    var status: String = UPLOAD_STATUS_PENDING
    var progress: Int = 0
    var holder: FileListAdapter.FileListViewHolder? = null

    override fun onStartUpload() {
        status = UPLOAD_STATUS_UPLOADING
        if (holder != null) {
            holder!!.cancel.visibility = View.GONE
        }
        onProgressUpdate(0)
    }

    override fun onUploadFailed() {
        status = UPLOAD_STATUS_FAILED
        progress = 0
        if (holder != null) {
            holder!!.cancel.visibility = View.VISIBLE
        }
        onProgressUpdate(0)
    }

    override fun onUploadResponse() {
        status = UPLOAD_STATUS_UPLOADED
        progress = 100
        if (holder != null) {
            holder!!.cancel.visibility = View.GONE
        }
        onProgressUpdate(100)
    }

    override fun onProgressUpdate(percentage: Int) {
        progress = percentage
        if (holder != null) {
            holder!!.progressBar.progress = progress
        }
    }

    companion object {
        const val UPLOAD_STATUS_PENDING = "pending"
        const val UPLOAD_STATUS_UPLOADING = "uploading"
        const val UPLOAD_STATUS_FAILED = "failed"
        const val UPLOAD_STATUS_UPLOADED = "uploaded"
    }
}
