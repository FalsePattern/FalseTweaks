plugins {
    id("fpgradle-minecraft") version("0.8.2")
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
    cursemavenEX()
    mavenpattern() {
        content {
            includeGroups("com.falsepattern", "makamys")
        }
    }
    mavenLocal {
        content {
            includeGroup("com.falsepattern")
        }
    }
    exclusive(jitpack(), "com.github.basdxz")
    exclusive(maven("mega_uploads", "https://mvn.falsepattern.com/gtmega_uploads"), "optifine")
    exclusive(mega(), "codechicken")
    exclusive(ivy("https://files.vexatos.com/", "[module]/[artifact]-[revision].[ext]"), "vexatos")
    exclusive(ivy("https://downloads.gtnewhorizons.com/", "[organisation]/[artifact]-[revision].[ext]"), "Mods_for_Twitch")
}

dependencies {
    implementationSplit("com.falsepattern:falsepatternlib-mc1.7.10:1.4.4")

    compileOnly(deobf("optifine:optifine:1.7.10_hd_u_e7"))

    implementation("org.joml:joml:1.10.5")

    compileOnly(deobfCurse("redstone-paste-67508:2211249"))

    //V33a
    compileOnly(deobfCurse("chromaticraft-235590:4721192"))

    //V33b
    compileOnly(deobfCurse("dragonapi-235591:4722480"))

    compileOnly("makamys:neodymium-mc1.7.10:0.3.3-unofficial:dev")

    compileOnly("com.github.basdxz:Apparatus:2.12.3:dev") {
        excludeDeps()
    }

    compileOnly(deobfCurse("railcraft-51195:2458987"))

    // Nuclear Control 2 2.4.5a
    compileOnly(deobfCurse("nuclear-control-2-236813:3802063"))
    // OpenComputers MC1.7.10-1.8.3+089dd28
    compileOnly(deobfCurse("opencomputers-223008:4630534"))
    // Computronics 1.7.10-1.6.6
    compileOnly(deobf("vexatos:Computronics:1.7.10-1.6.6"))
    // ExtraCells 1.7.10-2.5.0-14
    compileOnly(deobf("Mods_for_Twitch:ExtraCells:1.7.10-2.5.0-14"))
    // Automagy 0.28.2
    compileOnly(deobfCurse("automagy-222153:2285272"))
    // NTM 1.0.27_X5027
    compileOnly(deobfCurse("hbm-ntm-235439:5534354"))

    compileOnly("codechicken:codechickencore-mc1.7.10:1.4.0-mega:dev")
}