package maks.molch.dmitr.makslauncher.repository

import maks.molch.dmitr.makslauncher.data.Folder

interface FolderRepository {
    suspend fun save(name: String, folder: Folder)
    suspend fun get(name: String): Folder?
}