package id.kudzoza.packagewriter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.DocumentsContract.buildDocumentUri
import androidx.documentfile.provider.DocumentFile

/**
 * Created by Kudzoza
 * on 25/07/2021
 **/

class DocumentWriter(private val context: Context) {

    private val androidDataUri: Uri = buildDocumentUri(
        "com.android.externalstorage.documents",
        "primary:Android/data"
    )

    private val uriFlag: Int =
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    val androidDataTreeUri: Uri = DocumentsContract.buildTreeDocumentUri(
        "com.android.externalstorage.documents",
        "primary:Android/data"
    )

    val actionOpenDocumentTree: Intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
        flags = uriFlag
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, androidDataUri)
    }

    fun launch(onUriGranted: () -> Unit, onUriDenied: () -> Unit) {
        context.contentResolver.persistedUriPermissions.find {
            it.uri.equals(androidDataTreeUri) && it.isReadPermission
        }?.run {
            onUriGranted.invoke()
        } ?: onUriDenied.invoke()
    }

    fun write(action: (DocumentFile) -> Unit) {
        context.contentResolver.persistedUriPermissions.find {
            it.uri.equals(androidDataTreeUri) && it.isWritePermission
        }?.run {
            val documentTree = DocumentFile.fromTreeUri(context, androidDataTreeUri)
            if (documentTree != null) action.invoke(documentTree)
            else throw RuntimeException("Uri not registered")
        } ?: throw RuntimeException("Denied Permission")
    }

    fun takePersistablePermission(directoryUri: Uri) {
        context.contentResolver.takePersistableUriPermission(directoryUri, uriFlag)
    }
}