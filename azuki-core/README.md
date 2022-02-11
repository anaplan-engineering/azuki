# azuki-core

This project contains mechanisms that assist with applying BDS in practice and contains all the mandatory components required to use Azuki.

## Contents
- [Introduction](#introduction)
- [Using the library](#using-the-plugin)

## Introduction

This projects assists with one method of adapting a BDS DSL to a VDM-SL specification.

## Using the library

### Gradle setup

<!--- have to link to the README.md -- https://stackoverflow.com/questions/40422790/relative-link-to-repos-root-from-markdown-file -->
This library has not yet been published to a central repository.
You will need to build and publish to a local Maven repository using instructions in the [parent project](../README.md).
Once published, you can use azuki-core by adding a dependency to a Gradle build file in the usual way.
For example:

```groovy
repositories {
    mavenLocal()
}
dependencies {
    implementation group: 'com.anaplan.engineering', name: 'azuki-core', version: 'X.X.X'
}
```
### Using Azuki and BDS on a project

The best way to understand how azuki-core can be used to construct BDS scenarios is to work through the [tictactoe](../tictactoe) example provided.


