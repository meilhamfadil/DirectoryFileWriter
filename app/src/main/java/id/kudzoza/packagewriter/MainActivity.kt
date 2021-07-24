package id.kudzoza.packagewriter

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.GridLayoutManager
import id.kudzoza.packagewriter.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private val uri by lazy {
        DocumentsContract.buildDocumentUri(
            "com.android.externalstorage.documents",
            "primary:Android/data"
        )
    }
    private val treeUri by lazy {
        DocumentsContract.buildTreeDocumentUri(
            "com.android.externalstorage.documents",
            "primary:Android/data"
        )
    }

    private val adapter by lazy { MainAdapter() }
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var intentDocumentTree: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.recycle.adapter = adapter
        binding.recycle.layoutManager = GridLayoutManager(this, 2)

        intentDocumentTree = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
        }

        binding.request.setOnClickListener {
            contentResolver.persistedUriPermissions.find {
                it.uri.equals(treeUri) && it.isReadPermission
            }?.run {
                showData(treeUri)
            } ?: handleIntentActivityResult.launch(intentDocumentTree)
        }

        binding.copy.setOnClickListener {
            contentResolver.persistedUriPermissions.find {
                it.uri.equals(treeUri) && it.isWritePermission
            }?.run {
                val tree = DocumentFile.fromTreeUri(application, treeUri)
                try {
                    val targetFile =
                        DocumentFile.fromFile(File("/storage/emulated/0/Download/tes.txt"))
                    val newFile = tree?.findFile("id.co.pqm.lms")?.createFile(
                        "text/plain",
                        "new-injected-file.txt"
                    )
//                    val outputStream = newFile?.uri?.openOutputStream(application)
//                    val inputStream = targetFile.uri.openInputStream(application)!!
//
//                    val buffer = ByteArray(1024)
//                    var len: Int
//                    while (inputStream.read(buffer).also { len = it } != -1) {
//                        outputStream?.write(buffer, 0, len)
//                    }
//
//                    inputStream.close()
//                    outputStream?.flush()
//                    outputStream?.close()
                    Log.d("DATA :", contentResolver.openInputStream(targetFile.uri).toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, "Cannot Write", Toast.LENGTH_LONG).show()
                }
            }
        }

        showData(treeUri)
    }

    private val handleIntentActivityResult = registerForActivityResult(StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val directoryUri = it.data?.data ?: return@registerForActivityResult
            contentResolver.takePersistableUriPermission(
                directoryUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            showData(directoryUri)
        }
    }

    private fun showData(uri: Uri) {
        val dir = DocumentFile.fromTreeUri(application, uri)
        val child = dir?.listFiles().orEmpty()
        adapter.items.addAll(child.map { d -> d.name ?: " - " })
        adapter.notifyDataSetChanged()
    }

}