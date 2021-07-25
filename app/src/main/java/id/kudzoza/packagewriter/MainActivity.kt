package id.kudzoza.packagewriter

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import id.kudzoza.packagewriter.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity(), OnDownloadListener {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val documentWriter by lazy { DocumentWriter(this) }
    private val source by lazy { getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath }

    private val targetPackage = "id.co.pqm.knowledge"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (File("$source/inject.jpeg").exists()) launchWriter()
        else getPermission()

        binding.actionCopy.setOnClickListener {
            documentWriter.write {
                try {
                    binding.copy.text = "Copying"
                    binding.actionCopy.isEnabled = false
                    val sourceFile = it.findFile(packageName)
                        ?.findFile("files")
                        ?.findFile("Download")
                        ?.findFile("inject.jpeg")
                    val inputStream = contentResolver.openInputStream(sourceFile?.uri!!)!!

                    val targetFile = it.findFile(targetPackage)?.createFile(
                        "image/jpeg",
                        "hehe.jpeg"
                    )
                    val outputStream = contentResolver.openOutputStream(targetFile?.uri!!)!!

                    val buffer = ByteArray(1024)
                    var len: Int
                    while (inputStream.read(buffer).also { stream -> len = stream } != -1) {
                        outputStream.write(buffer, 0, len)
                    }

                    outputStream.close()
                    inputStream.close()
                    binding.copy.text = "OK"
                    binding.actionCopy.isEnabled = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun launchWriter() {
        documentWriter.launch(
            onUriGranted = {
                binding.download.text = "OK"
                binding.permission.text = "OK"
                binding.actionCopy.visibility = View.VISIBLE
            },
            onUriDenied = {
                handleIntentActivityResult.launch(documentWriter.actionOpenDocumentTree)
            }
        )
    }

    private fun getPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val readPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (readPermission == PackageManager.PERMISSION_GRANTED) {
                binding.download.text = "Downloading"
                download()
            } else {
                binding.download.text = "OK"
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
        }
    }

    private fun download() {
        PRDownloader.download(
            "https://meilhamfadil.github.io/yong.jpeg",
            source,
            "inject.jpeg"
        ).build()
            .start(this)
    }

    override fun onDownloadComplete() {
        binding.download.text = "OK"
        launchWriter()
    }

    override fun onError(error: Error?) {
        Toast.makeText(this, "Download Errror", Toast.LENGTH_SHORT).show()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            Log.d("STATUS", isGranted.toString())
            if (isGranted) download()
            else Toast.makeText(this, "Please restart app", Toast.LENGTH_SHORT).show()
        }

    private val handleIntentActivityResult = registerForActivityResult(StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val directoryUri = it.data?.data ?: return@registerForActivityResult
            documentWriter.takePersistablePermission(directoryUri)
            binding.permission.text = "OK"
            binding.actionCopy.visibility = View.VISIBLE
        } else {
            Toast.makeText(this, "Please restart app", Toast.LENGTH_SHORT).show()
        }
    }
}