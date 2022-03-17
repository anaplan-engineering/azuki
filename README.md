# Azuki

Azuki is generic framework designed to assist with Behaviour Driven Specification (BDS), in the same way that [Cucumber](https://cucumber.io/) assists with Behaviour driven development (BDD).

## Contents
- [Introduction](#introduction)
- [Project structure](#project-structure)
- [Using the library](#using-the-plugin)
- [Licence](#licence)

## Introduction

BDD is a process used to drive development and testing at the level of a user story.
BDD involves the creation of scenarios written from the point of view of the stakeholder, and which exemplify their requirements.
Each scenario is a form of Hoare Triple where we capture that:

- _given_ an initial context (precondition)
- _when_ the stakeholder performs an action (command)
- _then_ the outcome is as expected (postcondition)

These scenarios are written using a domain-specific language (DSL) for which the developer creates an adapter that enables the scenario to be executed and verified.

BDS was introduced in [Behaviour Driven Specification - Fraser, Pezzoni - 2021](https://arxiv.org/pdf/2110.09371.pdf) and adapts the BDD process to assist the creation of a formal specification using agile principles.
The BDS process is much like BDD, and it too requires a DSL in which to write scenarios.
However, unlike BDD, where the inputs are ACs and the output is code, with BDS we look to establish ACs and use those to drive the creation of the specification, with both being valuable artifacts of the process.

For each behaviour, we first determine a basic AC and then write a natural language description of that AC.
A scenario is then created to exemplify the AC; this may involve the extension of the DSL if new language is needed to express the criterion.
This scenario now forms an executable acceptance criterion (EAC) that we can use to validate the behaviour of both the specification and the target implementation.
We then create/extend the specification to ensure that the EAC is satisfied.
This process is repeated until we have identified all ACs for the behaviour and have constructed a specification that can be shown to satisfy them.

Running scenarios requires a DSL in which to express the actions and checks of the system in the stakeholder's language, and means by which to translate that language into commands and queries that the implementation can execute.
Cucumber is synonymous with BDD, but we found it unsuitable for use with BDS.
Although Cucumber accepts scenarios in the given-when-then format, the distinction between the blocks is lost at runtime and the scenario is reduced to a single sequence of steps.
When executing a program it is perhaps unimportant that certain steps are declarative and others are imperative, but when animating a specification we wanted to transform the declarative steps into a single declaration rather than calling a series of functions.
After some time trying to adapt Cucumber to our needs, we found that we were better able to make progress with a custom Kotlin DSL.
Azuki is a collection of tools that will assist others taking a similar approach, particularly those that are specifying in VDM-SL.

## Project structure

This repository contains a number of projects which are summarized in the table below.

| Project  | Description |
| ------------- | ------------- |
| [azuki-core](azuki-core) | The core of the Azuki project, that defines the structure of scenarios, the bare-bones of a BDS DSL for extension, and the means to use JUnit to run those scenarios through an IDE or a Gradle build.  |
| [azuki-vdm](azuki-vdm) | Additional utilities for the construction of Azuki adapters to VDM specifications.  |
| [tictactoe](tictactoe) | A worked example of using Azuki to create BDS EACs for the game of tic-tac-toe.  |
| [vdm-animation](vdm-animation) | A simple library that uses Overture to animate a VDM specification. |
| [vdm-stub-generator](vdm-stub-generator) | A Gradle plugin that generates Kotlin stubs for the modules and definitions of a VDM-SL specification. These stubs can be used in the construction of adapting VDM, such that we get compile-time checking of adapter dependence on VDM constructs.  |

### Building

A Gradle composite build is provided that enables all projects to be built from a single command:

```shell
./gradlew build
```

However, there is a known issue with Gradle composite builds and publishing to Maven repositories.
A custom task is provided to publish all of the necessary artifacts from the project to the local Maven repository in one go.

```shell
./gradlew publishAllToMavenLocal -Pversion=X.X.X
```

(A `publishAll` task also exists for publishing to a remote Maven repository. This requires various environment variables and properties to be set to configure the remote repository. These can easily be derived from examination from the publishing configuration of [build.gradle](build.gradle).)

N.B. A composite build is necessary for publishing and consuming gradle plugins in the same build.

### IntelliJ IDEA

The composite Gradle build can be opened in IDEA as a single project by opening the [settings.gradle](settings.gradle) file in the root directory.
This is the preferred method of interacting with the code for development.

## Using the library

### Gradle setup

This library has not yet been published to a central repository.
You will need to build and publish to a local Maven repository using instructions above.
Once published, you can consume the various artifacts, as required, by adding dependencies to a Gradle build file in the usual way.
For example:

```groovy
repositories {
    mavenLocal()
}
dependencies {
    implementation group: 'com.anaplan.engineering', name: 'azuki-core', version: 'X.X.X'
}
```
The use of the artifacts required for different aspects is exemplified in the tic-tac-toe example.

### Using Azuki and BDS on a project

The best way to understand how Azuki can help with using BDS on a new project is to work through the [tictactoe](tictactoe/README.md) example provided.

## Licence

See [LICENCE.txt](LICENCE.txt).
