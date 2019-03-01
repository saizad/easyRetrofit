package sa.zad.easyretrofitexample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_upload_observable.*
import sa.zad.easyretrofit.ProgressListener
import sa.zad.easyretrofit.lib.UploadApiObservable
import sa.zad.easyretrofit.utils.FileUtils
import sa.zad.easyretrofit.utils.ObjectUtils
import java.io.File


class UploadActivity : BaseActivity() {

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_upload_observable)
        progress_fab.hide(false)

        var selectedFile: File = File("")

        selected_file_bg.setOnClickListener {
            val perms = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            ActivityCompat.requestPermissions(this, perms, 1)
        }

        permissionResult(1)
                .filter {
                    if (!it) {
                        toast("One of the permisssion is not granted")
                    }
                    return@filter it
                }
                .subscribe({ callIntent() }, { toast(it.toString()) }, { toast("Completed") })

        result(1)
                .subscribeOn(Schedulers.io())
                .filter { ObjectUtils.isNotNull(it.data) }
                .map {
                    val file = File.createTempFile("asdf", null, cacheDir)
                    FileUtils.writeStreamToFile(contentResolver.openInputStream(it.data), file)
                    return@map file
                }.observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ file ->
                    file_size.text = (file.length() / 1000f / 1000f).toString() + " Mb"
                    file_name.text = file.name
                    selectedFile = file
                    progress_fab.show(true)
                }, {
                    showError(it.message!!)
                })


        progress_fab.setOnClickListener {
            progress_fab.setIndeterminate(true)
            progress_fab.setShowProgressBackground(true)
            service.upload("http://www.csm-testcenter.org/test", UploadApiObservable.part(selectedFile))
                    .progressUpdate {
                        updateStatus(it)
                        progress_fab.setIndeterminate(false)
                        progress_fab.setProgress(it.progress.toInt(), false)
                    }.onProgressCompleted {
                        toast(it.progress.toString())
                        updateStatus(it)
                    }.neverException {
                        showError(it.message!!)
                    }.doFinally {
                        progress_fab.hideProgress()
                    }.subscribe()
        }
    }

    private fun callIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, 1)
    }

    private fun updateStatus(progress: ProgressListener.Progress<*>) {
        time_remaining.text = (progress.timeRemaining() / 1000).toString() + " Sec"
        size_remaining.text = ((progress.size - progress.written) / (1024 * 1000)).toString() + " Mb"
        elapsed_time.text = (progress.elapsedTime() / 1000).toString() + " Sec"
    }

    private fun showError(string: String){
        request_error.text = string
        request_error.visibility = View.VISIBLE
    }
}