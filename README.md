# MultiCrafter

![GitHub Release](https://img.shields.io/github/v/release/JojoFR1/Multicrafter?display_name=tag&style=for-the-badge)
![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/JojoFR1/MultiCrafter/total?style=for-the-badge)
![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/JojoFR1/MultiCrafter/build.yml?style=for-the-badge)

A modding library for Mindustry that allows you to create blocks with multiple crafting recipes.

The rewrite of the original [MultiCrafterLib](https://github.com/liplum/MultiCrafterLib) with a better codebase and for Mindustry v8.

⚠️ This library is still in development, expect bugs.

## How to Use

There is no official documentation yet. It (might) come in the future.

You can use the development testing mod as an example of how to use the library in both Java and JSON. The source code is available in the `src/testMod` folder, or [here](https://github.com/JojoFR1/MultiCrafter/blob/main/src/testMod/) ([Java Version](https://github.com/JojoFR1/MultiCrafter/tree/main/src/testMod/java/dev/jojofr/multicrafter) or [JSON Version](https://github.com/JojoFR1/MultiCrafter/tree/main/src/testMod/resources/content/blocks))

## Download

### Mod

You can download the latest stable release from the [releases page](https://github.com/JojoFR1/MultiCrafter/releases) or
the latest snapshot from the [actions page](https://github.com/JojoFR1/MultiCrafter/actions/workflows/build.yml) (WARNING: may be unstable).

You can then import the downloaded JAR file in game by putting it in the `mods` folder of your Mindustry installation or by using the import button.

### Library

To use the library in your own mod, you can add it as a dependency in your `mod.(h)json` by adding the following:

```json
"dependencies": ["multicrafter"]
```

This will make user of your mod require the library to be installed.

#### JSON

You can simply use the type `MultiCrafter` or `AttributeMultiCrafter` in your JSON block definitions.

#### Java

Internally, your mod will need the library as a dependency by adding the following to your `build.gradle`:

```gradle
ivy {
    url = 'https://github.com/'
    patternLayout {artifact '/[organisation]/[module]/releases/download/[revision]/MultiCrafter.jar'}
    metadataSources { artifact() }
}
```

Then, add the following to your dependencies:

```gradle
compileOnly 'JojoFR1:MultiCrafter:v1.5.0'
```

## Contributing

I am open to any contributions, feel free to open issues for bug reports, feature request or any questions you may have.
You can also open pull requests if you want to directly contribute to the library.

If you simply want to talk, I created a forum post for the library in the official Mindustry Discord server, you can find it in the `#modding-forum` channel.

**⚠️ Please make sure to have the latest version installed before reporting any bugs, as the modder may be using new features or the bug may have already been fixed.**

## Building

Building requires **JDK 17** or later.

### Desktop

At the root of the project, use the following command: `./gradlew jar` for the mod, or `./gradlew jarLib` for the library.

Once the build process is finished, the output will be present in `./build/libs/MultiCrafterDesktop.jar`.

### Android

Building requires **Android SDK** (requires a `ANDROID_HOME` environment variable) with API and build tools (add it to the `PATH`) version 30 or later.

At the root of the project, use the following command: `./gradlew deploy`

Once the build process is finished, the output will be present in `./build/libs/MultiCrafter.jar`.

## Running

You can simply take the generated (mod) JAR file and put it in the `mods` folder of your Mindustry installation.

For development purposes, you can run the mod directly in a local, separate instance, of Mindustry from the
command line using: `./gradlew run` or `./gradlew runAndroid` for Android testing (doesn't require the Android jar).

## Credits

- [Jojo](https://github.com/JojoFR1) | Author and maintainer of this library.
- [Patou](https://github.com/Patou-todoG) | Created the mod icon.
- [liplum](https://github.com/liplum) | Original author of the deprecated MultiCrafterLib, which this library is a rewrite of.
