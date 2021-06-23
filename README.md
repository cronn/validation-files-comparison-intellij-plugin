[![CI](https://github.com/cronn/validation-files-comparison-intellij-plugin/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/cronn/validation-files-comparison-intellij-plugin/actions/workflows/gradle.yml)
[![JetBrains Plugin](https://img.shields.io/jetbrains/plugin/v/12931-validation-file-comparison.svg)](https://plugins.jetbrains.com/plugin/12931-validation-file-comparison/)
[![Apache 2.0](https://img.shields.io/github/license/cronn-de/validation-files-comparison-intellij-plugin.svg)](http://www.apache.org/licenses/LICENSE-2.0)

# Validation-File Comparison #

This plugin enables you to open the built-in Intellij-IDEA diff-viewer to
compare validation- and output-files in the current project or module using a
configurable keyboard shortcut.

## Getting Started

### TLDR
####Build  project
```
./gradlew build
```

####Start IntelliJ instance with plugin installed
```
./gradlew runIde
```

####Test plugin for compatibility issues
```
./gradlew runPluginVerifier
```

## Deployment
Before deployment make sure the plugin is compatible by running `./gradlew runPluginVerifier
`. For instructions about the actual deployment see https://plugins.jetbrains.com/docs/intellij/deployment.html