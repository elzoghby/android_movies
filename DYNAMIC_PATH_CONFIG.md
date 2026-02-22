# Dynamic Build Path Configuration

## Overview
Updated the hardcoded Android build path to use dynamic relative path resolution across different systems and installation locations.

## What Changed

### Before (Hardcoded)
```kotlin
maven {
    url = uri("C:\\Users\\mahme\\Downloads\\task\\flutter_movies_module\\build\\host\\outputs\\repo")
}
```

**Problems**:
- ❌ Windows-specific backslashes
- ❌ Machine-specific absolute path
- ❌ Fails on different systems/drives
- ❌ Breaks if project is moved
- ❌ Not portable for CI/CD environments

### After (Dynamic)
```kotlin
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
```

**Benefits**:
- ✅ Cross-platform compatible (Windows, macOS, Linux)
- ✅ Relative paths work from any installation location
- ✅ Works with different drive letters/mount points
- ✅ Helpful error messages if path doesn't exist
- ✅ CI/CD friendly
- ✅ Easy to maintain

## How It Works

### Path Resolution
```
settings.gradle.kts location
└── MyApplicationxx/
    └── settings.gradle.kts

../flutter_movies_module/  ← One level up, then into flutter_movies_module
└── build/host/outputs/repo/

Resolves to:
workspace/flutter_movies_module/build/host/outputs/repo/
```

### File System Independence
```
Windows:
- Relative: ../flutter_movies_module/build/host/outputs/repo
- Gradle converts to: C:\path\to\flutter_movies_module\build\host\outputs\repo

macOS:
- Relative: ../flutter_movies_module/build/host/outputs/repo
- Gradle converts to: /Users/path/to/flutter_movies_module/build/host/outputs/repo

Linux:
- Relative: ../flutter_movies_module/build/host/outputs/repo
- Gradle converts to: /home/path/to/flutter_movies_module/build/host/outputs/repo
```

### Path Validation
```kotlin
if (flutterModuleRepoPath.exists()) {
    // Path is valid, use it
    maven { url = flutterModuleRepoPath.toURI() }
} else {
    // Path doesn't exist, warn user
    logger.warn("Repository not found at: ${flutterModuleRepoPath.absolutePath}")
}
```

## Usage Examples

### Project Structure
```
workspace/
├── MyApplicationxx/                          ← Android host
│   └── settings.gradle.kts                   ← This file
├── ios_movies_host/                          ← iOS host
└── flutter_movies_module/                    ← Flutter module
    └── build/
        └── host/
            └── outputs/
                └── repo/                     ← Maven repository
```

### Build Command
```bash
# Works from any directory
cd MyApplicationxx
./gradlew build

# Or from workspace root
./gradlew -p MyApplicationxx build

# Or on Windows
cd MyApplicationxx
gradlew.bat build
```

### CI/CD Integration
```yaml
# GitHub Actions example
- name: Build Android App
  run: |
    cd flutter_movies_module
    flutter pub get
    
    cd ../MyApplicationxx
    ./gradlew build
```

## Configuration Options

### Option 1: Relative Path (Current Implementation)
```kotlin
val flutterModuleRepoPath = file("../flutter_movies_module/build/host/outputs/repo")
```
**Best for**: Most projects, simple structure

### Option 2: Environment Variable
```kotlin
val flutterModuleRepoPath = System.getenv("FLUTTER_MODULE_REPO")?.let { file(it) }
    ?: file("../flutter_movies_module/build/host/outputs/repo")
```
**Best for**: Complex build environments, CI/CD

### Option 3: Gradle Property
```kotlin
// In local.properties
val flutterModuleRepoPath = project.properties["flutter.module.repo"]?.toString()?.let { file(it) }
    ?: file("../flutter_movies_module/build/host/outputs/repo")
```
**Best for**: Per-developer configuration

### Option 4: Build Parameter
```bash
./gradlew build -Pflutter.module.repo="../flutter_movies_module/build/host/outputs/repo"
```
**Best for**: Dynamic builds

## Integration with Other Parts

### Local Properties File
If you want full flexibility, create a `local.properties` entry:

```properties
# local.properties
flutter.module.path=../flutter_movies_module
```

Then reference it in settings.gradle.kts:
```kotlin
val props = java.util.Properties()
file("local.properties").takeIf { it.exists() }?.inputStream()?.use { props.load(it) }
val flutterModulePath = props.getProperty("flutter.module.path", "../flutter_movies_module")
val flutterModuleRepoPath = file("$flutterModulePath/build/host/outputs/repo")
```

### Environment Variables
For CI/CD pipelines:

```bash
# Set environment variable
export FLUTTER_MODULE_PATH="/path/to/flutter_movies_module"

# Then gradle can use it
```

## Error Handling

### Scenario: Path Not Found
```
⚠️  Flutter module repository not found at: 
    /Users/john/Projects/task/flutter_movies_module/build/host/outputs/repo
    Run 'flutter pub get' in flutter_movies_module to generate it
```

**Solution**:
```bash
cd flutter_movies_module
flutter pub get
flutter build aar
cd ../MyApplicationxx
./gradlew build
```

### Scenario: Wrong Working Directory
```
# ❌ This fails - called from wrong directory
cd flutter_movies_module
../../gradlew build

# ✅ This works - called from correct directory
cd MyApplicationxx
./gradlew build
```

## Best Practices

### 1. Always Use Relative Paths
```kotlin
// ✅ Good
val repoPath = file("../flutter_movies_module/build/host/outputs/repo")

// ❌ Bad
val repoPath = file("C:\\Users\\mahme\\Downloads\\task\\flutter_movies_module\\build\\host\\outputs\\repo")
```

### 2. Include Path Validation
```kotlin
// ✅ Good
if (repoPath.exists()) {
    maven { url = repoPath.toURI() }
} else {
    logger.warn("Path not found: ${repoPath.absolutePath}")
}

// ❌ Bad
maven { url = repoPath.toURI() }  // Fails silently if path doesn't exist
```

### 3. Use Forward Slashes (Platform Independent)
```kotlin
// ✅ Good
file("../flutter_movies_module/build/host/outputs/repo")

// ⚠️ Works but not ideal
file("..\\flutter_movies_module\\build\\host\\outputs\\repo")
```

### 4. Document the Structure
```kotlin
// ✅ Good with comments
// Resolves to: <workspace>/flutter_movies_module/build/host/outputs/repo
val flutterModuleRepoPath = file("../flutter_movies_module/build/host/outputs/repo")

// ❌ No context
val x = file("../flutter_movies_module/build/host/outputs/repo")
```

## Testing the Configuration

### Verify Path Resolution
```bash
cd MyApplicationxx

# Print the resolved path
./gradlew -q -S debugDependencies 2>&1 | grep flutter_movies_module

# Or create a gradle task to print it
./gradlew printPaths
```

### Create Debug Task
Add to settings.gradle.kts:
```kotlin
// Debug: Print resolved paths
println("🔍 Flutter Module Repo Path:")
println("   Relative: ../flutter_movies_module/build/host/outputs/repo")
println("   Absolute: ${flutterModuleRepoPath.absolutePath}")
println("   Exists:   ${flutterModuleRepoPath.exists()}")
```

## Migration Guide

### If Moving Project to Different Location

1. **Verify structure maintained**:
   ```
   new_location/
   ├── MyApplicationxx/
   ├── ios_movies_host/
   └── flutter_movies_module/
   ```

2. **No settings changes needed**:
   - Relative paths automatically resolve to new location
   - No hardcoded paths to update

3. **Rebuild**:
   ```bash
   cd MyApplicationxx
   ./gradlew clean build
   ```

## Summary

| Aspect | Before | After |
|--------|--------|-------|
| **Portability** | ❌ Machine-specific | ✅ Universal |
| **Platform** | ❌ Windows-only | ✅ Cross-platform |
| **Maintenance** | ❌ Update on move | ✅ Auto-resolves |
| **CI/CD** | ❌ Problematic | ✅ Works seamlessly |
| **Error Handling** | ❌ None | ✅ Clear messages |
| **Flexibility** | ❌ Fixed | ✅ Configurable |

## Related Files

- `settings.gradle.kts` - Gradle configuration
- `local.properties` - Local development settings
- `flutter_movies_module/build/host/outputs/repo/` - Generated repository

## References

- [Gradle File Functions](https://docs.gradle.org/current/dsl/org.gradle.api.file.DirectoryTree.html)
- [Gradle buildscript API](https://docs.gradle.org/current/kotlin-dsl/)
- [File Path Best Practices](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties)
