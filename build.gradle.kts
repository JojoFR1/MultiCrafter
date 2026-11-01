import java.net.URI

plugins { java }

group = "dev.jojofr.multicrafter"
version = "1.5.3"

// Mindustry version to depend on.
// Valid values:
// - latest: depend on the latest release of mindustry
// - be: depend on the very latest commit of mindustry
// - v<number>: depend on a specific version
val mindustryVersion = "v160"
// Mindustry game version used by the run task.
val mindustryGameVersion = "v160.7"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val testMod = sourceSets.create("testMod") {
    java.setSrcDirs(listOf("src/testMod/java"))
    resources.setSrcDirs(listOf("src/testMod/resources"))
}

repositories {
    mavenCentral()

    //Downloads the dependencies JAR file from Mindustry releases; does not use any real repository. Surprisingly, this is the most reliable option.
    ivy {
        url = uri("https://github.com/")
        patternLayout {
            val pattern = when (mindustryVersion) {
                "latest" -> "/[organisation]/[module]/releases/[revision]/download/dependencies.jar" // Matest stable release
                "be" -> "/[organisation]/[module]/releases/download/master/[revision].jar" // Latest commit (BE)
                else -> "/[organisation]/[module]/releases/download/[revision]/dependencies.jar" // Specific release
            }
            val path = pattern.substring(0, pattern.lastIndexOf('/'))

            artifact("$path/[classifier].jar")
            artifact(pattern.replaceFirst(Regex("""\.jar$"""), "(-[classifier]).jar"))
        }
        metadataSources { artifact() }

        content {
            //BE artifact version is always 'latest'
            if (mindustryVersion == "be") includeVersion("Anuken", "MindustryBuilds", "latest")
            else includeVersion("Anuken", "Mindustry", mindustryVersion)
        }
    }

    maven {
        url = uri("https://jitpack.io")
        content { includeModule("com.github.EB-wilson", "TooManyItems") }
    }
}

val mindustryDependency = if (mindustryVersion == "be") "Anuken:MindustryBuilds:latest" else "Anuken:Mindustry:$mindustryVersion"
dependencies {
    compileOnly(mindustryDependency)

    compileOnly("com.github.EB-wilson:TooManyItems:3.2")

    add("testModImplementation", sourceSets.main.get().output)
    add("testModCompileOnly", mindustryDependency)
}

tasks.jar {
    archiveFileName.set("${project.name}Desktop.jar")

    from(configurations.runtimeClasspath.map { files ->
        files.map { if (it.isDirectory) it else zipTree(it) }
    })

    from(layout.projectDirectory.files("mod.json", "mod.hjson", "icon.png"))
}

val jarAndroid = tasks.register("jarAndroid") {
    dependsOn(tasks.jar)

    val compileClasspath = files(configurations.compileClasspath)
    val runtimeClasspath = files(configurations.runtimeClasspath)

    val desktopJarFile = tasks.jar.get().archiveFile
    val androidJarFile = layout.buildDirectory.file("libs/${project.name}Android.jar")

    val sdkRoot = providers.environmentVariable("ANDROID_HOME").orElse(providers.environmentVariable("ANDROID_SDK_ROOT"))
    val isWindows = providers.systemProperty("os.name").map { it.lowercase().contains("windows") }

    inputs.files(compileClasspath, runtimeClasspath, desktopJarFile)
    outputs.files(androidJarFile)

    doLast {
        val sdkRootPath = sdkRoot.orNull
            ?: throw GradleException("No valid Android SDK found. Ensure that ANDROID_HOME or ANDROID_SDK_ROOT is set to your Android SDK directory.")
        val sdk = File(sdkRootPath)

        if (!sdk.exists()) throw GradleException("No valid Android SDK found. Ensure that ANDROID_HOME is set to your Android SDK directory.")
        if (!sdk.isDirectory) throw GradleException("No valid Android SDK found: $sdkRootPath is not a directory.")

        val platformRoot = File(sdk, "platforms").listFiles()
            ?.sortedDescending()
            ?.firstOrNull { File(it, "android.jar").exists() }
            ?: throw GradleException("No android.jar found. Ensure that you have an Android platform installed.")

        // Collect dependencies needed for desugaring
        val classpath = (compileClasspath.files + runtimeClasspath.files + File(platformRoot, "android.jar"))

        val d8 = if (isWindows.get()) "d8.bat" else "d8"

        val command = buildList {
            add(d8)
            classpath.forEach { addAll(listOf("--classpath", it.path)) }
            addAll(listOf("--min-api", "21"))
            addAll(listOf("--output", androidJarFile.get().asFile.absolutePath))
            add(desktopJarFile.get().asFile.absolutePath)
        }

        //dex and desugar files - this requires d8 in your PATH
        val exitCode = ProcessBuilder(command)
//            .directory(layout.buildDirectory.dir("libs").get().asFile)
            .directory(androidJarFile.get().asFile.parentFile)
            .inheritIO()
            .start()
            .waitFor()

        if(exitCode != 0) throw GradleException("d8 failed with exit code $exitCode")
    }
}

tasks.register<Jar>("deploy"){
    dependsOn(jarAndroid, tasks.jar)
    archiveFileName.set("${project.name}.jar")

    val libs = layout.buildDirectory.dir("libs")
    val desktopJar = tasks.jar.get().archiveFile
    val androidJar = jarAndroid.get().outputs.files.singleFile
    from(zipTree(desktopJar), zipTree(androidJar))

    doLast {
        androidJar.delete();
        desktopJar.get().asFile.delete();
    }
}

tasks.register<Jar>("jarTest") {
    archiveFileName.set("${project.name}-test.jar")
    from(testMod.output)
}

val gameFile = layout.projectDirectory.file("run/Mindustry.jar")
val gameDataDir = layout.projectDirectory.dir("run/data")

tasks.register("downloadGame") {
    outputs.file(gameFile)

    doLast {
        val url = "https://github.com/Anuken/Mindustry/releases/download/${mindustryGameVersion}/Mindustry.jar"
        val targetFile = gameFile.asFile
        targetFile.parentFile.mkdirs()

        URI.create(url).toURL().openStream().use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}

tasks.register<Copy>("copyLibMod") {
    val renamedJar = "${project.name}.jar"

    from(tasks.jar)
    into(gameDataDir.dir("mods"))
    rename { renamedJar }
}
tasks.register<Copy>("copyTestMod") {
    dependsOn(tasks.named("jarTest"))
    val renamedJar = "${project.name}-test.jar"

    from(tasks.named("jarTest"))
    into(gameDataDir.dir("mods"))
    rename { renamedJar }
}
tasks.register<Delete>("removeMods") {
    delete(gameDataDir.file("mods/${project.name}.jar"))
    delete(gameDataDir.file("mods/${project.name}-test.jar"))
}

tasks.register<JavaExec>("run") {
    dependsOn("downloadGame", "copyLibMod", "copyTestMod")

    environment("MINDUSTRY_DATA_DIR", gameDataDir.asFile.absolutePath)
    mainClass.set("-jar")
    args(listOf(gameFile.asFile.absolutePath))
}
