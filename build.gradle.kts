import com.falsepattern.jtweaker.readZipInputStreamFor
import com.falsepattern.zanama.tasks.ZanamaTranslate
import com.falsepattern.zigbuild.tasks.ZigBuildTask
import com.falsepattern.zigbuild.toolchain.ZigVersion
import com.gtnewhorizons.retrofuturagradle.mcp.ReobfuscatedJar
import net.darkhax.curseforgegradle.TaskPublishCurseForge
import java.nio.file.StandardOpenOption
import kotlin.io.path.outputStream

plugins {
    id("com.falsepattern.fpgradle-mc") version "3.1.0"
    id("com.falsepattern.zanama") version "0.2.0"
    id("com.falsepattern.zigbuild")
}

group = "com.falsepattern"

minecraft_fp {
    java {
        modernRuntimeVersion = JavaVersion.VERSION_25
    }
    mod {
        modid = "falsetweaks"
        name = "FalseTweaks"
        rootPkg = "$group.falsetweaks"
    }

    api {
        packages = listOf("api")
    }

    mixin {
        pkg = "mixin.mixins"
        extraConfigs = listOf("mixins.falsetweaks.init.json")
    }

    core {
        accessTransformerFile = "falsetweaks_at.cfg"
        coreModClass = "asm.CoreLoadingPlugin"
    }

    tokens {
        tokenClass = "Tags"
    }

    publish {
        changelog = "https://github.com/FalsePattern/FalseTweaks/releases/tag/$version"
        maven {
            repoName = "mavenpattern"
            repoUrl = "https://mvn.falsepattern.com/releases/"
        }
        curseforge {
            projectId = "665744"
            dependencies {
                required("fplib")
                optional("mcpatcher")
                optional("swansong")
                optional("beddium")
                optional("rple")
                optional("lumi")
            }
        }
        modrinth {
            projectId = "VTGi3upD"
            dependencies {
                required("fplib")
                optional("mcpatcher")
                optional("swansong")
                optional("beddium")
                optional("rple")
                optional("lumi1710")
            }
        }
    }
}

zig {
    toolchain {
        version = ZigVersion.of("0.15.2")
    }
    defaultCacheDirs = false
}

val zigPrefix = layout.buildDirectory.dir("zig-build")

val zigBuildTask = tasks.register<ZigBuildTask>("buildNatives") {
    options {
        steps.add("install")
    }
    workingDirectory = layout.projectDirectory
    prefixDirectory = zigPrefix
    clearPrefixDirectory = true
    sourceFiles.from(layout.projectDirectory.dir("src/main/zig"))
    sourceFiles.from(layout.projectDirectory.dir("zig-util"))
    sourceFiles.from(layout.projectDirectory.file("build.zig"))
    sourceFiles.from(layout.projectDirectory.file("build.zig.zon"))

    dependsOn("extractZanama")
}

val translateJavaSourcesCpuID = layout.buildDirectory.dir("generated/sources/zanama/cpuid")
val translateJavaSourcesFalseTweaks = layout.buildDirectory.dir("generated/sources/zanama/falsetweaks")

val zigTranslateCpuID = tasks.register<ZanamaTranslate>("zigTranslateCpuID") {
    from = zigPrefix.map { it.file("cpuid.json") }
    into = translateJavaSourcesCpuID
    rootPkg = "com.falsepattern.falsetweaks.modules.natives.panama"
    bindRoot = "falsetweaks"
    className = "CpuID_z"
    dependsOn(zigBuildTask)
}
val zigTranslateFalseTweaks = tasks.register<ZanamaTranslate>("zigTranslateFalseTweaks") {
    from = zigPrefix.map { it.file("FalseTweaks.json") }
    into = translateJavaSourcesFalseTweaks
    rootPkg = "com.falsepattern.falsetweaks.modules.natives.panama"
    bindRoot = "falsetweaks"
    className = "FalseTweaks_z"
    dependsOn(zigBuildTask)
}


val panamaNatives = jarInJar_fp("panama") {
    this.javaCompatibility = modern
    this.javaVersion = JavaVersion.VERSION_25
    this.artifactName = "falsetweaks-panama"
}
tasks.named<JavaCompile>(panamaNatives.compileJavaTaskName) {
    dependsOn(zigTranslateCpuID, zigTranslateFalseTweaks)
}

tasks.named<Jar>(panamaNatives.jarTaskName) {
    destinationDirectory = layout.buildDirectory.dir("tmp/panama-libs")
}

tasks.named<ReobfuscatedJar>("reobf${panamaNatives.jarTaskName}") {
    destinationDirectory = layout.buildDirectory.dir("tmp/panama-libs")
}

panamaNatives.java.srcDirs(translateJavaSourcesCpuID, translateJavaSourcesFalseTweaks)

val packNatives = tasks.register<Zip>("packNatives") {
    this.archiveFileName = "natives.zip"
    from(zigPrefix.map { it.dir("lib") }) {
        include("*.so", "*.dll", "*.dylib")
    }
    this.entryCompression = ZipEntryCompression.STORED
    dependsOn(zigBuildTask)
}

tasks.processResources.configure {
    dependsOn(zigBuildTask, packNatives)
    into("META-INF/falsepatternlib_repo/mega/megatraceservice/1.3.0/") {
        from(configurations.compileClasspath.map { it.filter { file -> file.name.contains("megatraceservice") } })
    }
    into(minecraft_fp.mod.rootPkg.map { "/assets/falsetweaks" } ) {
        from(packNatives.map { it.outputs.files })
    }
    val ver = minecraft_fp.mod.version
    filesMatching("META-INF/deps_modern.json") {
        expand("modVersion" to ver.get())
    }
}

//region curseforge jank

publishing {
    publications {
        val mvn = minecraft_fp.publish.maven
        val jar = tasks.named<Jar>(panamaNatives.jarTaskName)
        val reobfJar = tasks.named<ReobfuscatedJar>("reobf${panamaNatives.jarTaskName}")
        create<MavenPublication>("mavenPanama") {
            setArtifacts(emptyList<Any>())
            artifact(jar)
            artifact(reobfJar)
            groupId = mvn.group.get()
            artifactId = jar.flatMap { it.archiveBaseName }.get()
            version = mvn.version.get()
        }
    }
}

val reobfJarTask = tasks.reobfJar

abstract class CurseStripJar @Inject constructor(project: Project): Jar() {
    @get:Inject
    abstract val archive: ArchiveOperations

    @get:InputFile
    abstract val inputFile: RegularFileProperty

    @TaskAction
    fun run() {
        inputFile.asFile.get().toPath().readZipInputStreamFor("META-INF/MANIFEST.MF", false) { inp ->
            // write to temp file
            val inpTmp = temporaryDir.resolve("input-manifest.MF").toPath()
            inpTmp.outputStream(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).use { out ->
                inp.copyTo(out)
            }
            this.manifest {
                this@manifest.from(inpTmp)
            }
        }

        from(archive.zipTree(inputFile)) {
            exclude("META-INF/falsepatternlib_repo/com/falsepattern/falsetweaks-panama/**/*")
        }
        copy()
    }

    init {
        this.destinationDirectory.set(project.layout.buildDirectory.dir("libs-cf"))
    }
}

val cfJar = tasks.register<CurseStripJar>("curseforgeJar") {
    inputFile = reobfJarTask.flatMap { it.archiveFile }
    dependsOn(reobfJarTask)
    archiveFileName = reobfJarTask.flatMap { it.archiveFileName }
}

afterEvaluate {
    tasks.named<TaskPublishCurseForge>("curseforge") {
        dependsOn(cfJar)
    }
}

minecraft_fp.publish.curseforge.toUpload.set(cfJar.map { it.archiveFile })

// endregion

repositories {
    cursemavenEX()
    modrinthEX()
    exclusive(mavenpattern(), "makamys", "org.embeddedt.celeritas") {
        includeModule("com.falsepattern", "falsepatternlib-mc1.7.10")
    }
    exclusive(mega(), "codechicken", "mega")
    exclusive(mega_uploads(), "optifine")
    exclusive(venmaven(), "com.ventooth")
    exclusive(jitpack(), "com.github.basdxz", "com.github.jss2a98aj")
    exclusive(horizon(), "com.github.GTNewHorizons", "com.gtnewhorizons.retrofuturabootstrap")
    exclusive(ivy("vexatos", "https://files.vexatos.com/", "[module]/[artifact]-[revision].[ext]"), "vexatos")
    exclusive(ivy("horizon-arr", "https://downloads.gtnewhorizons.com/", "[organisation]/[artifact]-[revision].[ext]"), "Mods_for_Twitch")
}

dependencies {
    implementationSplit("com.falsepattern:falsepatternlib-mc1.7.10:1.9.1")
    compileOnly("org.joml:joml:1.10.8")
    compileOnly("it.unimi.dsi:fastutil:8.5.16")
    compileOnly("mega:megatraceservice:1.3.0")
    compileOnly("com.ventooth:swansong-mc1.7.10:1.2.5:dev")
    compileOnly("maven.modrinth:etfuturum:2.6.2:dev")
    add(panamaNatives.compileOnlyConfigurationName, "com.falsepattern:zanama-rt:0.2.0")

    val beddiumVersion = "1.0.4"
    val beddiumVersionJ21 = "$beddiumVersion-j21"
    val beddiumVersionJ8 = "$beddiumVersion-j8"
    compileOnly("com.ventooth:beddium-mc1.7.10:$beddiumVersionJ8:dev")
    modernJavaPatchDeps("com.ventooth:beddium-mc1.7.10:$beddiumVersionJ21:dev") {
        excludeDeps()
    }

    compileOnly("com.github.GTNewHorizons:lwjgl3ify:2.1.5:dev")

    compileOnly(deobf("optifine:optifine:1.7.10_hd_u_e7"))

    compileOnly("com.gtnewhorizons.retrofuturabootstrap:RetroFuturaBootstrap:1.0.7")
    compileOnly("com.github.GTNewHorizons:GTNHLib:0.8.4")

    compileOnly("com.github.basdxz:Apparatus:2.12.3:dev") { excludeDeps() }
    compileOnly("com.github.jss2a98aj:NotFine:0.2.5:dev") { excludeDeps() }
    compileOnly(deobf("vexatos:Computronics:1.7.10-1.6.6"))
    compileOnly(deobf("Mods_for_Twitch:ExtraCells:1.7.10-2.5.0-14"))
    // Redstone Paste 1.6.2
    compileOnly(deobfCurse("redstonepastemod-67508:2211249"))
    // DragonAPI V33b
    compileOnly(deobfCurse("dragonapi-235591:4722480"))
    // RailCraft 9.12.2.1
    compileOnly(deobfCurse("railcraft-51195:2458987"))
    // Nuclear Control 2 2.4.5a
    compileOnly(deobfCurse("nuclear-control-2-236813:3802063"))
    // OpenComputers MC1.7.10-1.8.3+089dd28
    compileOnly(deobfCurse("opencomputers-223008:4630534"))
    // Automagy 0.28.2
    compileOnly(deobfCurse("automagy-222153:2285272"))
    // NTM 1.0.27_X5027
    compileOnly(deobfCurse("hbm-ntm-235439:5534354"))
    // TechGuns 1.2
    compileOnly(deobfCurse("techguns-244201:2429662"))
    // Malisis Core 0.14.3
    compileOnly(deobfCurse("malisiscore-223896:2283267"))
    // SecurityCraft 1.8.13
    compileOnly(deobfCurse("securitycraft-64760:2818228"))
    // Storage Drawers 1.7.10-1.10.9
    compileOnly(deobfCurse("storage-drawers-223852:2469586"))
    // CoFH Core [1.7.10]3.1.4-329
    compileOnly(deobfCurse("cofh-core-69162:2388750"))
    // Thermal Foundation [1.7.10]1.2.6-118
    compileOnly(deobfCurse("thermal-foundation-222880:2388752"))
    // Thermal Expansion [1.7.10]4.1.5-248
    compileOnly(deobfCurse("thermal-expansion-69163:2388758"))
    // LittleTiles 1.2.0
    compileOnly(deobfCurse("littletiles-257818:2462370"))
//    runtimeOnlyNonPublishable(deobfCurse("creativecore-257814:2462369"))
    compileOnly(deobfModrinth("backhand:1.4.1"))
    compileOnly(deobfCurse("mb-battlegear-2-59710:2286765"))
    compileOnly("com.github.GTNewHorizons:Battlegear2:1.4.3:dev")
}