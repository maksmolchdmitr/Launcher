package maks.molch.dmitr.makslauncher

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import maks.molch.dmitr.makslauncher.data.Application
import maks.molch.dmitr.makslauncher.data.Folder
import maks.molch.dmitr.makslauncher.data.LauncherObject
import maks.molch.dmitr.makslauncher.repository.FolderRepository
import maks.molch.dmitr.makslauncher.repository.preferences.PreferencesDataStoreFolderRepository
import maks.molch.dmitr.makslauncher.ui.theme.MaksLauncherTheme

class MainActivity : ComponentActivity() {
    //    private val folderRepository: FolderRepository = MockFolderRepository()
    private val folderRepository: FolderRepository = PreferencesDataStoreFolderRepository(this)
    private val folderNameKey = "FOLDER_NAME_KEY"
    private lateinit var folderName: String
    private lateinit var installedApplications: List<LauncherObject>
    private lateinit var folderLauncherObjects: MutableState<List<LauncherObject>>

    @SuppressLint("MutableCollectionMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installedApplications = getInstalledAppInfoList()

        folderName = intent.getStringExtra(folderNameKey) ?: "MAIN_FOLDER"
        val folder = runBlocking {
            folderRepository.get(folderName)
        } ?: run {
            val res = Folder(folderName)
            lifecycleScope.launch {
                folderRepository.save(folderName, res)
            }
            res
        }
        val launcherObjects = folder.objects
        setContent {
            MainContent(launcherObjects)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun getInstalledAppInfoList(): List<LauncherObject> {
        val installedApplications = packageManager.getInstalledApplications(0)
        val applicationList: MutableList<LauncherObject> = installedApplications
            .map { appInfo ->
                Application(
                    name = appInfo.loadLabel(packageManager).toString(),
                    packageName = appInfo.packageName
                )
            }
            .toMutableList()
        applicationList.add(Folder("Some Folder"))
        return applicationList
    }

    @Composable
    fun MainContent(launcherObjectList: List<LauncherObject>) {
        folderLauncherObjects = remember {
            mutableStateOf(
                launcherObjectList
            )
        }

        val context: Context = this
        MaksLauncherTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val startLauncherObject: (LauncherObject) -> Unit = { launcherObject ->
                    when (launcherObject) {
                        is Application -> {
                            packageManager
                                .getLaunchIntentForPackage(launcherObject.packageName)
                                ?.let { intent ->
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                }
                        }

                        is Folder -> {
                            val intent = Intent(context, MainActivity::class.java)
                            intent.putExtra(folderNameKey, launcherObject.name)
                            startActivity(intent)
                        }
                    }
                }
                val showLauncherObjectSettings: (LauncherObject) -> Unit = { launcherObject ->
                    Toast.makeText(context, "Some add box launcher object acts", Toast.LENGTH_SHORT)
                        .show()
                }
                LauncherObjects(
                    5,
                    launcherObjects = folderLauncherObjects.value,
                    onObjectClick = startLauncherObject,
                    onObjectLongPress = showLauncherObjectSettings,
                )
            }
        }

        val addBoxVisible = remember { mutableStateOf(false) }

        Box(
            Modifier.fillMaxSize()
        ) {
            FloatingActionButton(
                onClick = {
                    addBoxVisible.value = true
                },
                modifier = Modifier
                    .padding(16.dp)
                    .size(50.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }

        if (addBoxVisible.value) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray)
            ) {
                val addLauncherObjectToFolderAndCloseAddBox: (LauncherObject) -> Unit =
                    { launcherObject ->
                        Toast.makeText(
                            context,
                            "addLauncherObjectToFolderAndCloseAddBox with $launcherObject",
                            Toast.LENGTH_LONG
                        ).show()
                        addToFolder(launcherObject)
                        addBoxVisible.value = false
                    }
                val some: (LauncherObject) -> Unit = { launcherObject ->
                    Toast.makeText(
                        context,
                        "Some add box launcher object acts with $launcherObject",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                LauncherObjects(
                    1,
                    launcherObjects = installedApplications,
                    onObjectClick = addLauncherObjectToFolderAndCloseAddBox,
                    onObjectLongPress = some,
                )
            }
        }
    }

    private fun addToFolder(launcherObject: LauncherObject) {
        val folder = runBlocking {
            folderRepository.get(folderName)
        } ?: Folder(folderName)
        val objects = ArrayList(folder.objects)
        objects.add(launcherObject)
        folderLauncherObjects.value = objects
        lifecycleScope.launch {
            folderRepository.save(folderName, Folder(folderName, objects))
        }
    }

    @Composable
    fun LauncherObjects(
        rowsCount: Int,
        launcherObjects: List<LauncherObject>,
        onObjectClick: (LauncherObject) -> Unit,
        onObjectLongPress: (LauncherObject) -> Unit,
    ) {
        val columns = launcherObjects.chunked(rowsCount)
        LazyColumn {
            items(columns) { row ->
                LauncherObjectRow(row, onObjectClick, onObjectLongPress)
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables", "CoroutineCreationDuringComposition")
    @Composable
    fun LauncherObjectRow(
        row: List<LauncherObject>,
        onObjectClick: (LauncherObject) -> Unit,
        onObjectLongPress: (LauncherObject) -> Unit
    ) {
        Row {
            for (launcherObject in row) {
                LauncherObjectItem(
                    rowScope = this,
                    launcherObject,
                    onObjectClick,
                    onObjectLongPress
                )
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @SuppressLint("UseCompatLoadingForDrawables")
    @Composable
    fun LauncherObjectItem(
        rowScope: RowScope,
        launcherObject: LauncherObject,
        onObjectClick: (LauncherObject) -> Unit,
        onObjectLongPress: (LauncherObject) -> Unit,
    ) {
        rowScope.apply {
            Box(
                modifier = Modifier
                    .padding(5.dp)
                    .weight(1f, true)
                    .align(Alignment.CenterVertically)
                    .background(Color.LightGray)
                    .combinedClickable(
                        onClick = { onObjectClick.invoke(launcherObject) },
                        onLongClick = { onObjectLongPress.invoke(launcherObject) })
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    val icon = when (launcherObject) {
                        is Application -> {
                            try {
                                packageManager.getApplicationIcon(launcherObject.packageName)
                            } catch (e: PackageManager.NameNotFoundException) {
                                getDrawable(R.mipmap.ic_folder)!!
                            }
                        }

                        is Folder -> getDrawable(R.mipmap.ic_folder)!!
                        else -> getDrawable(R.mipmap.ic_folder)!!
                    }
                    DrawableImage(drawable = icon)
                    Text(text = launcherObject.name, color = Color.Black)
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
