package maks.molch.dmitr.makslauncher

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import maks.molch.dmitr.makslauncher.ui.theme.MaksLauncherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val installedAppInfoList = getInstalledAppInfoList()
        setContent {
            MainContent(installedAppInfoList)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun getInstalledAppInfoList(): List<AppInfo> {
        val installedApplications = packageManager.getInstalledApplications(0)
        return installedApplications.map { appInfo ->
            AppInfo(
                name = appInfo.loadLabel(packageManager).toString(),
                icon = appInfo.loadIcon(packageManager),
                packageName = appInfo.packageName
            )
        }
    }

    @Composable
    fun MainContent(installedApplications: List<AppInfo>) {
        MaksLauncherTheme {
            // A surface container using the 'background' color from the theme
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MainScreen(installedApplications)
            }
        }
    }

    @Composable
    fun MainScreen(installedApplications: List<AppInfo>) {
        LazyColumn {
            items(installedApplications) { appInfo ->
                AppItem(appInfo)
            }
        }
    }

    @Composable
    fun AppItem(appInfo: AppInfo) {
        Row(
            modifier = Modifier
                .clickable {
                    packageManager.getLaunchIntentForPackage(appInfo.packageName)?.let { intent ->
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                }
        ) {
            DrawableImage(drawable = appInfo.icon)
            Text(text = appInfo.name)
        }
    }

    @Composable
    fun DrawableImage(drawable: Drawable) {
        AndroidView(
            factory = { context ->
                AppCompatImageView(context).apply {
                    setImageDrawable(drawable)
                }
            },
            update = { imageView ->
                imageView.setImageDrawable(drawable)
            }
        )
    }
}
