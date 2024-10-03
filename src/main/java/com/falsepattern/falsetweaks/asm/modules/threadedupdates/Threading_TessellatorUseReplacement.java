/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FalseTweaks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalseTweaks. If not, see <https://www.gnu.org/licenses/>.
 */
package com.falsepattern.falsetweaks.asm.modules.threadedupdates;

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.falsetweaks.config.ThreadingConfig;
import com.falsepattern.lib.turboasm.ClassNodeHandle;
import com.falsepattern.lib.turboasm.TurboClassTransformer;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO ASM Logging
public class Threading_TessellatorUseReplacement implements TurboClassTransformer {
    private static final String TARGET_DESC_NOTCH = "Lbmh;";
    private static final String TARGET_DESC_SRG = "Lnet/minecraft/client/renderer/Tessellator;";

    private static final String REPLACEMENT_OWNER = "com/falsepattern/falsetweaks/api/ThreadedChunkUpdates";
    private static final String REPLACEMENT_NAME = "getThreadTessellator";
    private static final String REPLACEMENT_DESC = "()Lnet/minecraft/client/renderer/Tessellator;";

    private static final Set<String> CLASS_NAMES = new HashSet<>();
    private static final List<String> PREFIXES = new ArrayList<>();

    private static final String[] HARDCODED = new String[] {
            "appeng.client.render.*",
            "appeng.facade.FacadePart",
            "appeng.parts.*",
            "binnie.extratrees.block.DoorBlockRenderer",
            "biomesoplenty.client.render.blocks.*",
            "buildcraft.core.render.RenderingMarkers",
            "buildcraft.silicon.render.RenderLaserTable",
            "buildcraft.transport.render.PipeRendererWorld",
            "codechicken.lib.render.CCRenderState",
            "com.carpentersblocks.renderer.*",
            "com.enderio.core.client.render.*",
            "com.github.technus.tectech.thing.metaTileEntity.multi.base.render.TT_RenderedExtendedFacingTexture",
            "com.github.technus.tectech.util.LightingHelper",
            "com.hbm.render.block.*",
            "com.jaquadro.minecraft.storagedrawers.util.*",
            "com.rwtema.extrautils.block.render.*",
            "com.thecodewarrior.catwalks.render.*",
            "crazypants.enderio.conduit.*",
            "crazypants.enderio.machine.*",
            "extracells.part.PartECBase",
            "extracells.render.block.RendererHardMEDrive$",
            "flaxbeard.thaumicexploration.client.render.BlockEverfullUrnRenderer",
            "forestry.apiculture.render.RenderCandleBlock",
            "forestry.core.render.*",
            "gcewing.architecture.BaseWorldRenderTarget",
            "gregapi.old.GT_RenderUtil",
            "gregapi.render.*",
            "gregtech.GT_Client",
            "gregtech.api.objects.GT_RenderedTexture",
            "gregtech.api.util.LightingHelper",
            "gregtech.common.render.GT_Renderer_Block",
            "ic2.core.block.RenderBlockCrop",
            "lotr.client.render.*",
            "lumien.randomthings.Client.Renderer.RenderWirelessLever",
            "mods.natura.client.LeverRender",
            "mods.natura.client.SaguaroRenderer",
            "mods.railcraft.client.render.RenderFakeBlock",
            "net.malisis.core.renderer.MalisisRenderer",
            "net.minecraftforge.fluids.RenderBlockFluid",
            "openmods.renderer.FixedRenderBlocks",
            "openmods.renderer.TweakedRenderBlocks",
            "tb.client.render.block.ThaumicRelocatorRenderer",
            "team.chisel.ctmlib.*",
            "thaumcraft.client.renderers.block.*",
            "thaumic.tinkerer.client.render.block.kami.RenderWarpGate",
            "thaumicenergistics.client.render.RenderBlockProviderBase",
            "thaumicenergistics.common.parts.ThEPartBase",
            "tuhljin.automagy.renderers.RenderBlockGlowOverlay",
            "twilightforest.client.renderer.blocks.RenderBlockTFCastleMagic",
            "vswe.stevescarts.Renders.RendererUpgrade",
            "vswe.stevesfactory.blocks.RenderCamouflage",
    };
    public static void addAll(String... names) {
        for (val targetName: names) {
            if (!targetName.endsWith("*")) {
                CLASS_NAMES.add(targetName);
            } else {
                PREFIXES.add(targetName.substring(0, targetName.length() - 1));
            }
        }
    }
    static {
        addAll(HARDCODED);
        addAll(ThreadingConfig.TESSELLATOR_USE_REPLACEMENT_TARGETS);
    }

    @Override
    public String owner() {
        return Tags.MOD_NAME;
    }

    @Override
    public String name() {
        return "Threading_TessellatorUseReplacement";
    }

    @Override
    public boolean shouldTransformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        if (CLASS_NAMES.contains(className))
            return true;
        for (val prefix: PREFIXES) {
            if (className.startsWith(prefix))
                return true;
        }
        return false;
    }

    @Override
    public boolean transformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        val cn = classNode.getNode();
        if (cn == null)
            return false;
        boolean modified = false;
        val methods = cn.methods;
        for (val classMethod : methods) {
            val insnList = classMethod.instructions.iterator();
            while (insnList.hasNext()) {
                val insn = insnList.next();
                if (insn instanceof FieldInsnNode) {
                    val fieldInst = (FieldInsnNode) insn;
                    if (!(TARGET_DESC_NOTCH.equals(fieldInst.desc) || TARGET_DESC_SRG.equals(fieldInst.desc))) {
                        continue;
                    }

                    val opcode = insn.getOpcode();
                    if (opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC) {
                        insnList.add(new InsnNode(Opcodes.POP));
                        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, REPLACEMENT_OWNER, REPLACEMENT_NAME, REPLACEMENT_DESC, false));
                        modified = true;
                    }
                }
            }
        }
        return modified;
    }
}
