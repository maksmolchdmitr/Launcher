package maks.molch.dmitr.makslauncher

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import maks.molch.dmitr.makslauncher.data.GlobalSetting
import maks.molch.dmitr.makslauncher.repository.GlobalSettingRepository
import maks.molch.dmitr.makslauncher.repository.preferences.PreferencesDataStoreGlobalSettingRepository

class SettingActivity : ComponentActivity() {
    private val repository: GlobalSettingRepository =
        PreferencesDataStoreGlobalSettingRepository(this as Context)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainContent()
        }
    }

    @Composable
    fun MainContent() {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            MainScreen()
        }
    }

    @Composable
    fun MainScreen() {
        val globalSetting = runBlocking {
            repository.get() ?: GlobalSetting(5, 5)
        }
        val globalSettingState = remember {
            mutableStateOf(globalSetting)
        }
        Column {
            NumberTextField(
                globalSettingState.value.objectCountInRow,
                onValueChange = {
                    globalSettingState.value.objectCountInRow = it
                },
            ) {
                Text(text = "Objects in a row on adding box")
            }
            NumberTextField(
                globalSettingState.value.objectCountInRowInAdding,
                onValueChange = {
                    globalSettingState.value.objectCountInRowInAdding = it
                },
            ) {
                Text(text = "Objects in a row on adding box")
            }
            Button(onClick = {
                save(globalSettingState.value)
            }) {
                Text("Save")
            }
        }

    }

    private fun save(value: GlobalSetting) {
        val context: Context = this
        lifecycleScope.launch {
            repository.save(value)
            Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    fun NumberTextField(
        value: Int,
        onValueChange: (Int) -> Unit,
        label: @Composable (() -> Unit)?,
    ) {
        val valueState = remember {
            mutableStateOf(value.toString())
        }
        TextField(
            label = label,
            value = valueState.value,
            onValueChange = { newValue ->
                valueState.value = newValue
                valueState.value.toIntOrNull()?.let { onValueChange(it) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }

}