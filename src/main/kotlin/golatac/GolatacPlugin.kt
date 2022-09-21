package golatac

import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.parsers.TomlParser
import com.akuleshov7.ktoml.tree.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File


private inline fun <reified T> Any?.cast() = this as T

private val Any?.asMap: Map<String, Any?>
  get() = this.cast()
private val Any?.asList: List<Any?>
  get() = this.cast()
private val Any?.asString: String
  get() = this.cast()
private val Any?.asBoolean: String
  get() = this.cast()
private val Any?.asNumber: Number
  get() = this.cast()


private var libraries: Map<String, String>? = null
private var versions: Map<String, String>? = null

fun init(file: File) {
  val tomlFile = TomlParser(TomlInputConfig()).parseString(file.readText())
  val tables = tomlFile.getRealTomlTables().associateBy { it.name }

  //println(tomlFile.prettyPrint())
  versions = tables["versions"]
    ?.children
    ?.filterIsInstance<TomlKeyValuePrimitive>()
    ?.map {
      it.toPair()
    }
    ?.toMap()
    .orEmpty()

  libraries = tables["libraries"]
    ?.children
    ?.map {
      it.toPair()
    }
    ?.toMap()
    ?.mapValues { entry ->
      when (val value = entry.value) {
        is Map<*, *> -> {

          var version: Any? = value.get("version")
          if (version is Map<*, *>) {
            val versionRef = version.get("ref") as? String ?: error("$version is not supported")
            version = versions!!.get(versionRef.toKebabCase())
          }

          var module = value.get("module") as? String
          if (module == null) {
            val group = value.get("group") as? String
            val name = value.get("name") as? String
            check(group != null && name != null)
            module = "$group:$name"
          }

          buildString {
            append(module)
            if (version != null) {
              append(":")
              append(version)
            }
          }
        }

        is String -> {
          value
        }

        else -> error("")
      }
    }
    .orEmpty()
}

private fun TomlKeyValuePrimitive.toPair(): Pair<String, String> {
  return key.content to value.content.toString()
}

private fun TomlTablePrimitive.toPair(): Pair<String, Map<String, Any?>> {
  return name to children.map {
    when (it) {
      is TomlKeyValuePrimitive -> it.toPair()
      is TomlTablePrimitive -> it.toPair()
      else -> error("$it is not supported")
    }
  }.toMap()
}

private fun TomlNode.toPair(): Pair<String, Any?> {
  return when (this) {
    is TomlKeyValuePrimitive -> toPair()
    is TomlTablePrimitive -> toPair()
    else -> error("$this is not supported")
  }
}


fun lib(name: String): String {
  check(libraries != null) {
    "golatac.init() was not called"
  }
  val kebabName = name.toKebabCase()

  return libraries?.get(kebabName) ?: error("no catalog entry found for lib $kebabName")
}

fun version(name: String): String {
  check(libraries != null) {
    "golatac.init() was not called"
  }

  val kebabName = name.toKebabCase()

  return versions?.get(kebabName) ?: error("no catalog entry found for version $kebabName")
}


private fun String.toKebabCase(): String {
  return replace(".", "-")
}

class GolatacPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    // Nothing to do
  }
}