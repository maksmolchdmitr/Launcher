package maks.molch.dmitr.makslauncher.repository

import maks.molch.dmitr.makslauncher.data.GlobalSetting

interface GlobalSettingRepository {
    suspend fun save(globalSetting: GlobalSetting)
    suspend fun get(): GlobalSetting?
}