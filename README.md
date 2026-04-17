# MultiCrafter

A modding library for Mindustry that allows you to create blocks with multiple crafting recipes.

The rewrite of the original [MultiCrafterLib](https://github.com/liplum/MultiCrafterLib) with a better codebase and for Mindustry v8.

## How to Use

There is no official documentation yet. Coming soon.

## Building

Building requires **JDK 17** or later.

### Desktop

At the root of the project, use the following command: `./gradlew jar`

Once the build process is finished, the output will be present in `./build/libs/MultiCrafterDesktop.jar`.

### Android

Building requires **Android SDK** (with `ANDROID_HOME`) with API and build tools (add it to the `PATH`).

At the root of the project, use the following command: `./gradlew deploy`

Once the build process is finished, the output will be present in `./build/libs/MultiCrafter.jar`.

## Running

You can simply take the generated JAR file and put it in the `mods` folder of your Mindustry installation.

For development purposes, you can run the mod directly in a local, separate instance, of Mindustry from the
command line using: `./gradlew run` or `./gradlew runAndroid` for Android testing (doesn't require the Android jar).

## Credits

- [liplum](https://github.com/liplum) | Original author of the MultiCrafterLib.
- [Jojo](https://github.com/JojoFR1) | Author and maintainer of MultiCrafter.
