# MultiCrafter

A modding library for Mindustry that allows you to create blocks with multiple crafting recipes.

The rewrite of the original [MultiCrafterLib](https://github.com/liplum/MultiCrafterLib) with a better codebase and for Mindustry v8.

⚠️ This library is still in development, expect bugs.

## How to Use

There is no official documentation yet. It (might) come in the future.

You can use the development testing mod as an example of how to use the library. The source code is available in the `src/testMod` folder, or [here](https://github.com/JojoFR1/MultiCrafter/blob/main/src/testMod/java/dev/jojofr/multicrafter/)
It contains both Java and JSON examples of how to use the library.

## Download

### Mod

You can download the latest stable release from the [releases page](https://github.com/JojoFR1/MultiCrafter/releases) or
the latest snapshot from the [actions page](https://github.com/JojoFR1/MultiCrafter/actions/workflows/build.yml) (WARNING: may be unstable).

You can then import the downloaded JAR file in game by putting it in the `mods` folder of your Mindustry installation or by using the import button.

### Library (not yet available)

#### JSON

You can add the library as a dependency in your mod by adding the following to your `mod.(h)json`:

```json
"dependencies": ["multicrafter"]
```

This will make user of your mod require the library to be installed. You can then use the type `MultiCrafter` in your JSON block definitions.

#### Java

You need to follow the JSON instructions above to make sure your users have the library installed.

Internally, your mod will need the library as a dependency by adding the following to your `build.gradle`:

```gradle
ivy {
    url = 'https://github.com/'
    patternLayout {artifact '/[organisation]/[module]/releases/download/[revision]/MultiCrafter-lib.jar'}
    metadataSources { artifact() }
}
```

Then, add the following to your dependencies:

```gradle
compileOnly 'JojoFR1:MultiCrafter:v1.0.0'
```

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

## Contributing

I am open to any contributions.

Feel free to open issues for bug reports, feature request or any questions you may have.

You can also open pull requests if you want to directly contribute to the library.

## Credits

- [liplum](https://github.com/liplum) | Original author of the MultiCrafterLib.
- [Jojo](https://github.com/JojoFR1) | Author and maintainer of MultiCrafter.
