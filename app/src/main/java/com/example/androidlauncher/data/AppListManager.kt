import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import com.example.androidlauncher.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class AppListManager(private val context: Context) {
    val apps = mutableStateOf<List<AppInfo>>(emptyList())
    private val cacheFile = File(context.cacheDir, "app_list_cache")

    suspend fun loadApps() {
        withContext(Dispatchers.IO) {
            apps.value = loadFromCache() ?: fetchAppList()
            saveToCache(apps.value)
        }
    }

    private fun loadFromCache(): List<AppInfo>? {
        return try {
            ObjectInputStream(cacheFile.inputStream()).use { it.readObject() as? List<AppInfo> }
        } catch (e: Exception) {
            null
        }
    }

    private fun saveToCache(appList: List<AppInfo>) {
        try {
            ObjectOutputStream(cacheFile.outputStream()).use { it.writeObject(appList) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun fetchAppList(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val packageManager = context.packageManager
        return packageManager.queryIntentActivities(intent, 0).map { ri ->
            AppInfo(
                label = ri.loadLabel(packageManager).toString(),
                packageName = ri.activityInfo.packageName,
                icon = ri.activityInfo.loadIcon(packageManager)
            )
        }.sortedBy { it.label.lowercase() }
    }

    suspend fun refreshAppList() {
        withContext(Dispatchers.IO) {
            apps.value = fetchAppList()
            saveToCache(apps.value)
        }
    }
}