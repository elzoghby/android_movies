pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        
        // Dynamic Flutter module repository path
        val flutterModuleRepoPath = file("../flutter_movies_module/build/host/outputs/repo")
        if (flutterModuleRepoPath.exists()) {
            maven {
                url = flutterModuleRepoPath.toURI()
            }
        } else {
            // Fallback notice
            logger.warn("⚠️  Flutter module repository not found at: ${flutterModuleRepoPath.absolutePath}")
            logger.warn("    Run 'flutter pub get' in flutter_movies_module to generate it")
        }
        
        maven {
            url = uri("https://storage.googleapis.com/download.flutter.io")
        }
    }
}

rootProject.name = "My Applicationxx"
include(":app")
