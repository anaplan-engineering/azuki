# azuki-vdm

This project contains mechanisms that assist with creating a BDS adapter for VDM and Azuki.
It is not essential to use this project when using VDM with Azuki, but these facilities are provided to ease adoption.

## Contents
- [Introduction](#introduction)
- [Using the library](#using-the-plugin)

## Introduction

This projects assists with one method of adapting a BDS DSL to a VDM-SL specification.

With this method we iteratively construct an `Animation` module by processing the various actions and checks that represent a scenario and then use Overture to animate its single operation.
This operation will return `true` if the generated checks are satisfied and `false` otherwise.

To use this approach an implementor must have their actions and checks extend the [`VdmAction`](src/main/kotlin/com/anaplan/engineering/azuki/vdm/VdmAction.kt) and [`VdmCheck`](src/main/kotlin/com/anaplan/engineering/azuki/vdm/VdmCheck.kt) interfaces, such that each adds the appropriate detail to the [`ModuleBuilder`](src/main/kotlin/com/anaplan/engineering/azuki/vdm/ModuleBuilder.kt) that is used to construct the `Animation` module.
When constructing a VDM `SystemFactory` one should then use the [`VdmEacAnimator`](src/main/kotlin/com/anaplan/engineering/azuki/vdm/animation/VdmEacAnimator.kt) to generate the appropriate file and animate the specification.

Coverage statistics are captured during animation and can be composed from multiple scenarios.

## Using the library

### Gradle setup

<!--- have to link to the README.md -- https://stackoverflow.com/questions/40422790/relative-link-to-repos-root-from-markdown-file -->
This library has not yet been published to a central repository.
You will need to build and publish to a local Maven repository using instructions in the [parent project](../README.md).
Once published, you can use azuki-vdm by adding a dependency to a Gradle build file in the usual way.
For example:

```groovy
repositories {
    mavenLocal()
}
dependencies {
    implementation group: 'com.anaplan.engineering', name: 'azuki-vdm', version: 'X.X.X'
}
```
### Using Azuki and BDS on a project

The best way to understand how azuki-vdm can help with using BDS on a new project is to work through the [tictactoe](../tictactoe) example provided, specifically the [VDM adapter](../tictactoe/adapter-vdm).





