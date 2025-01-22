plugins {
    id("fpgradle-minecraft") version ("0.10.1")
}

group = "com.falsepattern"

minecraft_fp {
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
        pluginClass = "mixin.plugin.standard.MixinPlugin"
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
    exclusive(mavenpattern(), "com.falsepattern", "makamys")
    exclusive(mega(), "codechicken", "mega")
    exclusive(mega_uploads(), "optifine")
    exclusive(jitpack(), "com.github.basdxz", "com.github.jss2a98aj")
    exclusive(horizon(), "com.github.GTNewHorizons", "com.gtnewhorizons.retrofuturabootstrap")
    exclusive(ivy("https://files.vexatos.com/", "[module]/[artifact]-[revision].[ext]"), "vexatos")
    exclusive(ivy("https://downloads.gtnewhorizons.com/", "[organisation]/[artifact]-[revision].[ext]"), "Mods_for_Twitch")
}

dependencies {
    implementationSplit("com.falsepattern:falsepatternlib-mc1.7.10:1.5.9")
    implementation("org.joml:joml:1.10.8")
    implementation("it.unimi.dsi:fastutil:8.5.15")
    implementation("mega:megatraceservice:1.2.0")

    compileOnly("makamys:neodymium-mc1.7.10:0.4.3-unofficial:dev")

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
}