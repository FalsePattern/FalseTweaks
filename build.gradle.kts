import com.falsepattern.fpgradle.dsl.*
plugins {
    id("fpgradle-minecraft") version("0.3.3")
}

group = "com.falsepattern"

minecraft_fp {
    mod {
        modid   = "falsetweaks"
        name    = "FalseTweaks"
        rootPkg = "$group.falsetweaks"
    }

    api {
        packages = listOf("api")
    }

    mixin {
        pkg = "mixin.mixins"
        pluginClass = "mixin.plugin.standard.MixinPlugin"
    }

    core {
        accessTransformerFile = "falsetweaks_at.cfg"
        coreModClass = "asm.CoreLoadingPlugin"
    }

    tokens {
        tokenClass = "Tags"
        modid = "MODID"
        name = "MODNAME"
        version = "VERSION"
        rootPkg = "GROUPNAME"
    }

    publish {
        changelog = "https://github.com/FalsePattern/FalseTweaks/releases/tag/$version"
        maven {
            repoName = "mavenpattern"
            repoUrl = uri("https://mvn.falsepattern.com/releases/")
        }
        curseforge {
            projectId = "665744"
            dependencies {
                required("fplib")
            }
        }
        modrinth {
            projectId = "VTGi3upD"
            dependencies {
                required("fplib")
            }
        }
    }
}

repositories {
    maven("cursemaven", uri("https://mvn.falsepattern.com/cursemaven/")) {
        content {
            includeGroup("curse.maven")
        }
    }
    maven("mavenpattern", uri("https://mvn.falsepattern.com/releases/")) {
        content {
            includeGroup("com.falsepattern")
            includeGroup("makamys")
        }
    }
    maven("jitpack", uri("https://mvn.falsepattern.com/jitpack/")) {
        content {
            includeGroup("com.github.basdxz")
        }
    }
    maven("mega_uploads", uri("https://mvn.falsepattern.com/gtmega_uploads/")) {
        content {
            includeGroup("optifine")
        }
    }
    maven("mega", uri("https://mvn.falsepattern.com/gtmega_releases/")) {
        content {
            includeGroup("codechicken")
        }
    }
    ivy {
        url = uri("https://files.vexatos.com/")
        patternLayout {
            artifact("[module]/[artifact]-[revision].[ext]")
        }
        content {
            includeGroup("vexatos")
        }
        metadataSources {
            artifact()
        }
    }
    ivy {
        url = uri("https://downloads.gtnewhorizons.com/")
        patternLayout {
            artifact("[organisation]/[artifact]-[revision].[ext]")
        }
        content {
            includeGroup("Mods_for_Twitch")
        }
        metadataSources {
            artifact()
        }
    }
}

dependencies {
    compileOnly("com.falsepattern:falsepatternlib-mc1.7.10:1.2.5:api")
    runtimeOnly("com.falsepattern:falsepatternlib-mc1.7.10:1.2.5:dev")

    compileOnly(rfg.deobf("optifine:optifine:1.7.10_hd_u_e7"))

    implementation("org.joml:joml:1.10.5")

    compileOnly(deobfCurse("redstone-paste-67508:2211249"))

    //V33a
    compileOnly(deobfCurse("chromaticraft-235590:4721192"))

    //V33b
    compileOnly(deobfCurse("dragonapi-235591:4722480"))

    compileOnly("makamys:neodymium-mc1.7.10:0.3.2-unofficial:dev")

    compileOnly("com.github.basdxz:Apparatus:2.12.3:dev") {
        isTransitive = false
    }

    compileOnly(deobfCurse("railcraft-51195:2458987"))

    // Nuclear Control 2 2.4.5a
    compileOnly(deobfCurse("nuclear-control-2-236813:3802063"))
    // OpenComputers MC1.7.10-1.8.3+089dd28
    compileOnly(deobfCurse("opencomputers-223008:4630534"))
    // Computronics 1.7.10-1.6.6
    compileOnly(rfg.deobf("vexatos:Computronics:1.7.10-1.6.6"))
    // ExtraCells 1.7.10-2.5.0-14
    compileOnly(rfg.deobf("Mods_for_Twitch:ExtraCells:1.7.10-2.5.0-14"))
    // Automagy 0.28.2
    compileOnly(deobfCurse("automagy-222153:2285272"))

    compileOnly("codechicken:codechickencore-mc1.7.10:1.4.0-mega:dev")
}