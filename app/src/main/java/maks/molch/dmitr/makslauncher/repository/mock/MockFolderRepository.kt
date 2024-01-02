package maks.molch.dmitr.makslauncher.repository.mock

import maks.molch.dmitr.makslauncher.data.Folder
import maks.molch.dmitr.makslauncher.repository.FolderRepository

class MockFolderRepository : FolderRepository {
    override suspend fun save(name: String, folder: Folder) {
    }

    override suspend fun get(name: String): Folder? {
        return when (name) {
            "MAIN_FOLDER" -> Folder(
                name,
                Folder("Some folder") * 10
            )

            "Some folder" -> Folder(name, Folder("Another Folder") * 100)
            "Another Folder" -> Folder(name)
            else -> null
        }
    }
}