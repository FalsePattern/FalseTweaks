/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * FalseTweaks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalseTweaks. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.mixin.plugin.standard;

import com.gtnewhorizon.gtnhmixins.builders.ITargetMod;
import com.gtnewhorizon.gtnhmixins.builders.TargetModBuilder;
import lombok.Getter;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public enum TargetMod implements ITargetMod {
    //coremods
    CoreTweaks("makamys.coretweaks.CoreTweaksMod"),
    DragonAPI("Reika.DragonAPI.DragonAPICore"),
    FoamFix("pl.asie.foamfix.coremod.FoamFixCore"),
    FastCraft("fastcraft.Tweaker"),
    NotFine("jss.notfine.NotFine"),
    OpenComputers("li.cil.oc.OpenComputers"),
    OptiFine("Config"),
    OptiFineShadersMod("shadersmod.client.Shaders"),
    OptiFineDynamicLights("DynamicLights"),
    SwanSong("com.ventooth.swansong.asm.CoreLoadingPlugin"),
    //regular mods
    Automagy("tuhljin.automagy.Automagy"),
    Computronics("pl.asie.computronics.Computronics"),
    ExtraCells("extracells.Extracells"),
    LittleTiles("com.creativemd.littletiles.LittleTiles"),
    Malisis("net.malisis.core.MalisisCore"),
    Malisis_NH("net.malisis.core.MalisisCore",
               b -> b.testModAnnotation(null,
                                        null,
                                        ver -> ver.toLowerCase()
                                                  .contains("gtnh"))),
    NuclearControl("shedar.mods.ic2.nuclearcontrol.IC2NuclearControl"),
    RailCraft("mods.railcraft.common.core.Railcraft"),
    RedstonePaste("fyber.redstonepastemod.RedstonePasteMod"),
    SecurityCraft("net.geforcemods.securitycraft.SecurityCraft"),
    StorageDrawers("com.jaquadro.minecraft.storagedrawers.StorageDrawers"),
    StorageDrawers_ThreadSafe("com.jaquadro.minecraft.storagedrawers.StorageDrawers",
                              b -> b.testModAnnotation(null, null, ver -> {
                                  ver = ver.toLowerCase();
                                  return ver.contains("gtnh") || ver.contains("mega");
                              })),
    Techguns("techguns.Techguns"),
    ThermalExpansion("cofh.thermalexpansion.ThermalExpansion"),
    ;

    @Getter
    private final TargetModBuilder builder;

    TargetMod(@Language(value = "JAVA",
                        prefix = "import ",
                        suffix = ";") @NotNull String className) {
        this(className, null);
    }

    TargetMod(@Language(value = "JAVA",
                        prefix = "import ",
                        suffix = ";") @NotNull String className, @Nullable Consumer<TargetModBuilder> cfg) {
        builder = new TargetModBuilder();
        builder.setTargetClass(className);
        if (cfg != null) {
            cfg.accept(builder);
        }
    }
}
