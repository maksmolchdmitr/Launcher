package maks.molch.dmitr.makslauncher.repository.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import kotlinx.coroutines.flow.first
import maks.molch.dmitr.makslauncher.data.GlobalSetting
import maks.molch.dmitr.makslauncher.repository.GlobalSettingRepository

class PreferencesDataStoreGlobalSettingRepository(context: Context) : GlobalSettingRepository {
    private val dataStore = context.createDataStore(name = "global_settings")
    private val launcherKey = preferencesKey<Int>("launcher_key")
    private val addingKey = preferencesKey<Int>("adding_key")
    override suspend fun save(globalSetting: GlobalSetting) {
        dataStore.edit { settings ->
            settings[launcherKey] = globalSetting.objectCountInRow
            settings[addingKey] = globalSetting.objectCountInRowInAdding
        }
    }

    override suspend fun get(): GlobalSetting? {
        val preferences = dataStore.data.first()
        val inRow = preferences[launcherKey] ?: run { return null }
        val inAdding = preferences[addingKey] ?: run { return null }
        return GlobalSetting(inRow, inAdding)
    }
}