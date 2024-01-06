package maks.molch.dmitr.makslauncher

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.runBlocking
import maks.molch.dmitr.makslauncher.data.Application
import maks.molch.dmitr.makslauncher.data.Folder
import maks.molch.dmitr.makslauncher.data.GlobalSetting
import maks.molch.dmitr.makslauncher.data.LauncherObject
import maks.molch.dmitr.makslauncher.repository.FolderRepository
import maks.molch.dmitr.makslauncher.repository.GlobalSettingRepository
import maks.molch.dmitr.makslauncher.repository.preferences.PreferencesDataStoreFolderRepository
import maks.molch.dmitr.makslauncher.repository.preferences.PreferencesDataStoreGlobalSettingRepository
import maks.molch.dmitr.makslauncher.ui.theme.MaksLauncherTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    //    private val folderRepository: FolderRepository = MockFolderRepository()
    private val folderRepository: FolderRepository =
        PreferencesDataStoreFolderRepository(this as Context)
    private val globalSettingRepository: GlobalSettingRepository =
        PreferencesDataStoreGlobalSettingRepository(this as Context)
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
            runBlocking {
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

    private fun getGlobalSetting(): GlobalSetting {
        return runBlocking {
            globalSettingRepository.get() ?: GlobalSetting(5, 5)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun MainContent(launcherObjectList: List<LauncherObject>) {
        folderLauncherObjects = remember {
            mutableStateOf(
                launcherObjectList
            )
        }

        val context: Context = this
        val currentSettingLauncherObject: MutableState<LauncherObject?> = remember {
            mutableStateOf(null)
        }
        val launcherObjectSettingDialog = remember { mutableStateOf(false) }

        MaksLauncherTheme {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        onClick = {},
                        onLongClick = {
                            val intent = Intent(
                                context,
                                SettingActivity::class.java
                            )
                            startActivity(intent)
                        }),
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
                    currentSettingLauncherObject.value = launcherObject
                    launcherObjectSettingDialog.value = true
                }
                LauncherObjects(
                    getGlobalSetting().objectCountInRow,
                    launcherObjects = folderLauncherObjects.value,
                    onObjectClick = startLauncherObject,
                    onObjectLongPress = showLauncherObjectSettings,
                )
            }
        }

        val addApplicationBoxVisible = remember { mutableStateOf(false) }
        val addFolderBoxVisible = remember { mutableStateOf(false) }

        Box(
            Modifier.fillMaxSize()
        ) {
            FloatingActionButton(
                onClick = {
                    addApplicationBoxVisible.value = true
                },
                modifier = Modifier
                    .padding(16.dp)
                    .size(50.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Application")
            }
            FloatingActionButton(
                onClick = {
                    addFolderBoxVisible.value = true
                },
                modifier = Modifier
                    .padding(16.dp)
                    .size(50.dp)
                    .align(Alignment.BottomStart)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = "Add Folder")
            }
        }

        if (addApplicationBoxVisible.value) {
            DialogBlock(onDismissRequest = { addApplicationBoxVisible.value = false }) {
                AddApplicationBox(
                    context,
                    addApplicationBoxVisible,
                    getGlobalSetting().objectCountInRowInAdding
                )
            }
        }

        if (addFolderBoxVisible.value) {
            DialogBlock(onDismissRequest = { addFolderBoxVisible.value = false }) {
                AddFolderBox(addFolderBoxVisible)
            }
        }

        if (launcherObjectSettingDialog.value) {
            LauncherObjectSettingDialog(onDismissRequest = {
                launcherObjectSettingDialog.value = false
            }, currentSettingLauncherObject)
        }
    }

    @Composable
    fun DialogBlock(
        onDismissRequest: () -> Unit,
        content: @Composable () -> Unit,
    ) {
        Dialog(onDismissRequest = { onDismissRequest() }) {
            content()
        }
    }

    @Composable
    fun LauncherObjectSettingDialog(
        onDismissRequest: () -> Unit,
        currentSettingLauncherObject: MutableState<LauncherObject?>
    ) {
        Dialog(onDismissRequest = { onDismissRequest() }) {
            // Draw a rectangle shape with rounded corners inside the dialog
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                val objName = "`${currentSettingLauncherObject.value?.name}`"
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .height(500.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "This is settings for $objName",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = TextUnit(5f, TextUnitType.Em)
                        ),
                        modifier = Modifier.padding(16.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        ApplicationSettingButton(
                            columnScope = this,
                            text = "Delete object $objName",
                            icon = Icons.Default.Clear
                        ) {
                            onDismissRequest()
                            removeFromCurrentFolder(currentSettingLauncherObject.value!!)
                        }
                        if (currentSettingLauncherObject.value is Application) {
                            ApplicationSettingButton(
                                columnScope = this,
                                text = "Remove application from your device $objName",
                                icon = Icons.Default.Delete
                            ) {
                                onDismissRequest()
                                deleteApplicationFromDevice(currentSettingLauncherObject.value as Application)
                            }
                        }
                        ApplicationSettingButton(
                            columnScope = this,
                            text = "Cancel",
                            icon = Icons.Default.Delete
                        ) {
                            onDismissRequest()
                        }
                    }
                }
            }
        }
    }

    private val uninstallActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(
                this,
                "Application was successfully removed!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                "Application removing - Failed: with result $result",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun deleteApplicationFromDevice(app: Application) {
        val packageUri = Uri.parse("package:${app.packageName}")
        val uninstallIntent = Intent(Intent.ACTION_DELETE, packageUri)
        uninstallActivityResultLauncher.launch(uninstallIntent)
        removeFromCurrentFolder(app)
    }

    @Composable
    fun ApplicationSettingButton(
        columnScope: ColumnScope,
        text: String,
        icon: ImageVector,
        onClick: () -> Unit,
    ) {
        columnScope.apply {
            TextButton(
                onClick = onClick,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .weight(1f, true)
                    .background(Color.LightGray, CircleShape)
            ) {
                Icon(
                    icon,
                    contentDescription = "Button icon",
                )
                Text(text)
            }
        }
    }

    @Composable
    fun AddFolderBox(addFolderBoxVisible: MutableState<Boolean>) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.DarkGray)
        ) {
            val folderNameText = remember { mutableStateOf("") }
            Row(
                Modifier.align(Alignment.Center)
            ) {
                TextField(
                    value = folderNameText.value,
                    onValueChange = { folderNameText.value = it },
                    label = { Text("Folder Name") },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(2f, true),
                )

                Button(
                    onClick = {
                        addToCurrentFolder(Folder(folderNameText.value))
                        addFolderBoxVisible.value = false
                    },
                    modifier = Modifier.weight(1f, true),
                ) {
                    Text("Add")
                }
            }
        }
    }

    @Composable
    fun AddApplicationBox(
        context: Context,
        addApplicationBoxVisible: MutableState<Boolean>,
        objectCountInRowInAdding: Int
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.DarkGray)
        ) {
            Column {
                val objects = remember {
                    mutableStateOf(installedApplications)
                }
                SearchRowBlock { searchPrefix ->
                    objects.value = installedApplications.filter { launcherObject ->
                        searchFilter(
                            launcherObject.name.lowercase(Locale.getDefault()),
                            searchPrefix.lowercase(Locale.getDefault())
                        )
                    }
                }
                val addLauncherObjectToFolderAndCloseAddBox: (LauncherObject) -> Unit =
                    { launcherObject ->
                        Toast.makeText(
                            context,
                            "addLauncherObjectToFolderAndCloseAddBox with $launcherObject",
                            Toast.LENGTH_LONG
                        ).show()
                        addToCurrentFolder(launcherObject)
                        addApplicationBoxVisible.value = false
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
                    objectCountInRowInAdding,
                    launcherObjects = objects.value,
                    onObjectClick = addLauncherObjectToFolderAndCloseAddBox,
                    onObjectLongPress = some,
                )
            }
        }
    }

    private fun searchFilter(name: String, searchPrefix: String): Boolean {
        val words = name.split(Regex("\\P{L}+"))
        for (word in words) {
            if (word.startsWith(searchPrefix)) {
                return true
            }
        }
        return false
    }

    @SuppressLint("UnrememberedMutableState")
    @Preview(showBackground = true)
    @Composable
    fun Preview() {
        DialogBlock(onDismissRequest = {}) {
            SearchRowBlock(onSearchClick = {})
        }
    }

    @Composable
    fun SearchRowBlock(
        onSearchClick: (String) -> Unit,
    ) {
        Row {
            val value = remember {
                mutableStateOf("")
            }
            TextField(
                value = value.value, onValueChange = { value.value = it },
                modifier = Modifier.weight(2f, true),
            )
            Button(
                onClick = { onSearchClick(value.value) },
                modifier = Modifier.weight(1f, true)
            ) {
                Text("Search")
            }
        }
    }

    private fun addToCurrentFolder(launcherObject: LauncherObject) {
        val folder = runBlocking {
            folderRepository.get(folderName)
        } ?: Folder(folderName)
        val objects = ArrayList(folder.objects)
        objects.add(launcherObject)
        folderLauncherObjects.value = objects
        runBlocking {
            folderRepository.save(folderName, Folder(folderName, objects))
        }
    }

    private fun removeFromCurrentFolder(launcherObject: LauncherObject) {
        val folder = runBlocking {
            folderRepository.get(folderName)
        } ?: Folder(folderName)
        val objects = ArrayList(folder.objects)
        objects.remove(launcherObject)
        folderLauncherObjects.value = objects
        runBlocking {
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
                    .background(Color.LightGray, RoundedCornerShape(5.dp))
                    .combinedClickable(
                        onClick = { onObjectClick.invoke(launcherObject) },
                        onLongClick = { onObjectLongPress.invoke(launcherObject) })
                    .padding(5.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    val icon = when (launcherObject) {
                        is Application -> {
                            try {
                                packageManager.getApplicationIcon(launcherObject.packageName)
                            } catch (e: PackageManager.NameNotFoundException) {
                                getDrawable(R.mipmap.ic_application)
                            }
                        }

                        is Folder -> getDrawable(R.mipmap.ic_folder)
                        else -> getDrawable(R.mipmap.ic_application)
                    }!!
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

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (folderName != "MAIN_FOLDER") {
            super.onBackPressed()
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
