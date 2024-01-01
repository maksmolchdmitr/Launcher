package maks.molch.dmitr.makslauncher

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import maks.molch.dmitr.makslauncher.data.Application
import maks.molch.dmitr.makslauncher.data.Folder
import maks.molch.dmitr.makslauncher.data.LauncherObject
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
    private fun getInstalledAppInfoList(): List<LauncherObject> {
        val installedApplications = packageManager.getInstalledApplications(0)
        val applicationList: MutableList<LauncherObject> = installedApplications
            .map { appInfo ->
                Application(
                    name = appInfo.loadLabel(packageManager).toString(),
                    icon = appInfo.loadIcon(packageManager),
                    packageName = appInfo.packageName
                )
            }
            .toMutableList()
        applicationList.add(Folder("Some Folder"))
        return applicationList
    }

    @Composable
    fun MainContent(installedApplications: List<LauncherObject>) {
        MaksLauncherTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MainScreen(5, installedApplications)
            }
        }
    }

    @Composable
    fun MainScreen(rowsCount: Int, installedApplications: List<LauncherObject>) {
        val columns = installedApplications.chunked(rowsCount)
        LazyColumn {
            items(columns) { row ->
                LauncherObjectRow(row)
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Composable
    fun LauncherObjectRow(row: List<LauncherObject>) {
        Row {
            for (launcherObject in row) {
                when (launcherObject) {
                    is Application -> {
                        Box(modifier = Modifier
                            .padding(5.dp)
                            .weight(1f, true)
                            .align(Alignment.CenterVertically)
                            .background(Color.LightGray)
                            .clickable {
                                packageManager
                                    .getLaunchIntentForPackage(launcherObject.packageName)
                                    ?.let { intent ->
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startActivity(intent)
                                    }
                            }
                        ) {
                            Column(
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                DrawableImage(drawable = launcherObject.icon)
                                Text(text = launcherObject.name)
                            }
                        }
                    }

                    is Folder -> {
                        Box(modifier = Modifier
                            .padding(5.dp)
                            .weight(1f, true)
                            .align(Alignment.CenterVertically)
                            .background(Color.LightGray)
                            .clickable {

                            }
                        ) {
                            Column(
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                getDrawable(R.mipmap.ic_folder)?.let { DrawableImage(drawable = it) }
                                Text(text = launcherObject.name)
                            }
                        }
                    }
                }
            }
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
