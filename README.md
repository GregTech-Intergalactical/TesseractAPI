![Tesseract](https://nerdist.com/wp-content/uploads/2019/03/tumblr_o9pm5bI1Kc1sbtt2jo3_540.gif)

# Tesseract: 3D Network Graph Library for Java

This API is for the General purpose of building graphs consisting of nodes and connectors that are divided into groups and grids. 
Groups are sets of nodes and connectors that are located next to each other and may not necessarily have a connection. 
Grids are already sets of connectors connected to each other which can be several in a group. 

![Graph](https://www.mathworks.com/help/examples/matlab/win64/AdjustGraphPlotPropertiesExample_05.png)

Its main purpose is to help create systems for exchanging information between nodes with simplified calculations. 
The library also allows you to create controllers for each group that will be responsible for the logic. 
The API is written without using additional libraries other than FastUtils.

## Artifacts

Currently, two artifacts classes has been defined:
* `tesseract-fabric` which includes (path: `$projectDir/fabric`):
  * *default* is the mod for fabric only,
  * `dev-shadow` is same as `jar` but not obfuscated, made to run in development environments,
  * `sources` is the source code.
* `tesseract-forge` which includes (path: `$projectDir/forge`):
  * *default* is the mod for Forge only,
  * `dev-shadow` is same as `jar` but not obfuscated, made to run in development environments,
  * `sources` is the source code.

The common submodule has no use, cause some parts of the api had to be separated into their respective forge and fabric versions

## Building

It's as simple as running:
```
./gradlew build
```

Then you will find full mod in the right `build/libs` directories.

### Install as a dependency

Use this in your `build.gradle`
```groovy
repositories {
    maven {
        url = 'https://jitpack.io'
    }
}

dependencies {
    //Tag can be a commit hash, a github release tag, or (branch name)-SNAPSHOT
    //forge
    implementation 'com.github.GregTech-Intergalactical.TesseractAPI:TesseractAPI-forge:Tag'
    //forge
    implementation 'com.github.GregTech-Intergalactical.TesseractAPI:TesseractAPI-fabric:Tag'
}
```

### Publishing to a maven repository

First you'll need a repository ([files.axelandre42.ovh/maven](https://files.axelandre42.ovh/maven/) is reserved to official releases).

Then set your environment variables:
* `MAVEN_URL` Maven Repository URL.
* `MAVEN_USERNAME` Your username on the repository.
* `MAVEN_PASSWORD` Your password on the repository.

And finally run: `./gradlew publish`

## Version management

Use the `bumpMajor`, `bumpMinor` and `bumpRevision` to increment version.

If the project is build from CI, it will append the build number automatically.

Otherwise it'll try to get the Git short revision ID.
