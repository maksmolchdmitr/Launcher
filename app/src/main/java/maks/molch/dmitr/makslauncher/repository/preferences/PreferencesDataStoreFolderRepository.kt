package maks.molch.dmitr.makslauncher.repository.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import maks.molch.dmitr.makslauncher.data.Folder
import maks.molch.dmitr.makslauncher.data.gson
import maks.molch.dmitr.makslauncher.repository.FolderRepository

class PreferencesDataStoreFolderRepository(context: Context) : FolderRepository {
    private val dataStore = context.createDataStore(name = "folders")
    override suspend fun save(name: String, folder: Folder) {
        val value = gson.toJson(folder)
        val dataStoreKey = preferencesKey<String>(name)
        dataStore.edit { settings ->
            settings[dataStoreKey] = value
        }
    }

    override suspend fun get(name: String): Folder? {
        val dataStoreKey = preferencesKey<String>(name)
        val preferences = dataStore.data.first()
        val value = preferences[dataStoreKey]
        val type = object : TypeToken<Folder>() {}.type
        return gson.fromJson(value, type)
    }
}