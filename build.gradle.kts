import com.falsepattern.zanama.tasks.ZanamaTranslate
import com.falsepattern.zigbuild.options.ZigBuildOptions
import com.falsepattern.zigbuild.tasks.ZigBuildTask
import com.falsepattern.zigbuild.toolchain.ZigVersion

plugins {
    id("com.falsepattern.fpgradle-mc") version "2.1.1"
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
    this.artifactVersion = "1.0.0"
}
tasks.named<JavaCompile>(panamaNatives.compileJavaTaskName) {
    dependsOn(zigTranslateCpuID, zigTranslateFalseTweaks)
}

panamaNatives.java.srcDirs(translateJavaSourcesCpuID, translateJavaSourcesFalseTweaks)

tasks.processResources.configure {
    dependsOn(zigBuildTask)
    into("META-INF/falsepatternlib_repo/mega/megatraceservice/1.2.0/") {
        from(configurations.compileClasspath.map { it.filter { file -> file.name.contains("megatraceservice") } })
    }
    into(minecraft_fp.mod.rootPkg.map { "/" + it.replace('.', '/') + "/modules/natives" } ) {
        from(zigPrefix.map { it.dir("lib") }) {
            include("*.pak")
        }
    }
}

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
    compileOnly("mega:megatraceservice:1.2.0")
    compileOnly("com.ventooth:swansong-mc1.7.10:1.2.1:dev")
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
    compileOnly("com.github.GTNewHorizons:GTNHLib:0.5.21:api")

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
}