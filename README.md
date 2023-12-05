[![CI](https://github.com/cronn/validation-files-comparison-intellij-plugin/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/cronn/validation-files-comparison-intellij-plugin/actions/workflows/gradle.yml)
[![JetBrains Plugin](https://img.shields.io/jetbrains/plugin/v/12931-validation-file-comparison.svg)](https://plugins.jetbrains.com/plugin/12931-validation-file-comparison/)
[![Apache 2.0](https://img.shields.io/github/license/cronn-de/validation-files-comparison-intellij-plugin.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Valid Gradle Wrapper](https://github.com/cronn/validation-files-comparison-intellij-plugin/workflows/Validate%20Gradle%20Wrapper/badge.svg)](https://github.com/cronn/validation-files-comparison-intellij-plugin/actions/workflows/gradle-wrapper-validation.yml)

# Validation-File Comparison #

IntelliJ plugin as extension to
cronn's [validation file assertion library](https://github.com/cronn/validation-file-assertions).<br>
Compare and accept output and validation files without context switches.

![](doc/usage.gif)

## Installation

Go to [**File | Settings | Plugins**](jetbrains://idea/settings?name=Plugins), search in tab Marketplace
for `Validation-File Comparison` and click on install.

## Usage

Execute tests which use validation files, open the test itself or open the production code for it and run the the
action `Validation-File Comparison`.

You can find the action using ...

* IntelliJ's Find Action (`Ctrl+Shift+A` / `⇧⌘A`) and search for `Validation File Comparison`
* **View | Validation-File Comparison**
* **Right Click | Validation-File Comparison**
* Or the default shortcut `Alt+Shift+V` /`⌥⇧V`

This plugin finds the corresponding (sub-)module for the current dialog and compares the two directories
`[SUBMODULE-PATH]/data/test/validation` and `[SUBMODULE-PATH]/data/test/output`, which are the default directories
for [validation file assertion library](https://github.com/cronn/validation-file-assertions).

### Configuration

As for every IntelliJ action, you can configure your own shortcut for the `Validation-File Comparison` action.
Simply go to [**File | Settings | Keymap**](jetbrains://idea/settings?name=Keymap) , search for `Validation-File
Comparison` and configure a new shortcut.

The behavior of the action can be configured in the dedicated settings dialog under 
[**File | Settings | Tools | Validation-File Comparison Plugin**](jetbrains://idea/settings?name=Tools--Validation-File+Comparison+Plugin).

## Development

### Getting Started

#### TLDR

##### Build  project

```
./gradlew build
```

##### Start IntelliJ instance with plugin installed

```
./gradlew runIde
```

##### Test plugin for compatibility issues

```
./gradlew runPluginVerifier
```

### Deployment

Before deployment make sure the plugin is compatible by running `./gradlew runPluginVerifier
`. For instructions about the actual deployment see https://plugins.jetbrains.com/docs/intellij/deployment.html
