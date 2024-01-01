package maks.molch.dmitr.makslauncher.data

import android.graphics.drawable.Drawable

data class Application(
    override val name: String,
    val icon: Drawable,
    val packageName: String,
) : LauncherObject(name) {
    operator fun times(i: Int): List<Application> {
        val res = mutableListOf<Application>()
        repeat(i) {
            res.add(this)
        }
        return res
    }
}
