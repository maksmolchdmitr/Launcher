package maks.molch.dmitr.makslauncher.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

abstract class LauncherObject(
    open val name: String,
) {
    operator fun times(i: Int): List<LauncherObject> {
        val res = mutableListOf<LauncherObject>()
        repeat(i) {
            res.add(this)
        }
        return res
    }
}

data class Application(
    override val name: String,
    val packageName: String,
) : LauncherObject(name)

data class Folder(
    override val name: String,
    val objects: List<LauncherObject>
) : LauncherObject(name) {
    constructor(name: String) : this(name, emptyList())
}

// Create a Gson instance
val gson: Gson = GsonBuilder()
    .registerTypeAdapter(LauncherObject::class.java, LauncherObjectTypeAdapter())
    .registerTypeAdapter(Folder::class.java, FolderTypeAdapter())
    .registerTypeAdapter(Application::class.java, ApplicationTypeAdapter())
    .create()

// Define a custom type adapter for LauncherObject
class LauncherObjectTypeAdapter : JsonSerializer<LauncherObject>, JsonDeserializer<LauncherObject> {
    override fun serialize(
        src: LauncherObject?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return when (src) {
            is Application -> context!!.serialize(src, Application::class.java)
            is Folder -> context!!.serialize(src, Folder::class.java)
            else -> throw JsonParseException("Unsupported type")
        }
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LauncherObject? {
        if (json !is JsonObject) throw JsonParseException("Unsupported type")
        return when (json.getAsJsonPrimitive("type").asString) {
            Application::class.java.canonicalName -> context?.deserialize<Application>(
                json,
                Application::class.java
            )

            Folder::class.java.canonicalName -> context?.deserialize<Folder>(
                json,
                Folder::class.java
            )

            else -> throw JsonParseException("Unsupported type")
        }
    }
}

// Define a custom type adapter for Folder
class FolderTypeAdapter : JsonSerializer<Folder>, JsonDeserializer<Folder> {
    override fun serialize(
        src: Folder?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("name", src?.name)
        jsonObject.addProperty("type", Folder::class.java.canonicalName)
        // Add other properties of Folder if needed
        val launcherObjects = src!!.objects
        val jsonArray = JsonArray(launcherObjects.size)
        for (obj in launcherObjects) {
            jsonArray.add(context!!.serialize(obj))
        }
        jsonObject.add("objects", jsonArray)
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Folder {
        if (json !is JsonObject) throw JsonParseException("Unsupported type")
        val name = json.getAsJsonPrimitive("name").asString
        // Deserialize other properties for Folder if needed
        val jsonArray = json.getAsJsonArray("objects")
        val launcherObjects = ArrayList<LauncherObject>(jsonArray.size())
        for (jsonObj in jsonArray) {
            launcherObjects.add(context!!.deserialize(jsonObj, LauncherObject::class.java))
        }
        return Folder(name, launcherObjects)
    }
}

class ApplicationTypeAdapter : JsonSerializer<Application>, JsonDeserializer<Application> {
    override fun serialize(
        src: Application?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("name", src!!.name)
        jsonObject.addProperty("type", Application::class.java.canonicalName)
        jsonObject.addProperty("package_name", src.packageName)
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Application {
        if (json !is JsonObject) throw JsonParseException("Unsupported type")
        val name = json.getAsJsonPrimitive("name").asString
        val packageName = json.getAsJsonPrimitive("package_name").asString
        return Application(name, packageName)
    }
}