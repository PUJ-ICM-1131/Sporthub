pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://api.mapbox.com/downloads/v2/releases/maven") }
    }
}

rootProject.name = "SportHub"
include(":app")

val carpetaDeCompilacion: String? = run {
    val propiedades = java.util.Properties()
    rootDir.resolve("local.properties").takeIf { it.isFile }?.inputStream()?.use { propiedades.load(it) }
    propiedades.getProperty("sporthub.buildDir")?.takeIf { it.isNotBlank() }
        ?: System.getenv("SPORTHUB_BUILD_DIR")?.takeIf { it.isNotBlank() }
}

if (carpetaDeCompilacion != null) {
    gradle.beforeProject {
        val subcarpeta = path.trim(':').replace(':', '/').ifEmpty { "raiz" }
        layout.buildDirectory.set(File(carpetaDeCompilacion, subcarpeta))
    }
}
