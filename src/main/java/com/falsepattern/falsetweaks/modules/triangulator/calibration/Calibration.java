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

package com.falsepattern.falsetweaks.modules.triangulator.calibration;

import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.config.TriangulatorConfig;
import com.falsepattern.lib.config.ConfigurationManager;
import com.falsepattern.lib.text.FormattedText;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Calibration {
    private static final Calibration INSTANCE = new Calibration();

    private static boolean calibrateRequest = false;

    public static void registerBus() {
        MinecraftForge.EVENT_BUS.register(INSTANCE);
        FMLCommonHandler.instance().bus().register(INSTANCE);
    }

    @SneakyThrows
    public static void setCalibration(boolean flip) {
        TriangulatorConfig.Calibration.FLIP_DIAGONALS = flip;
        TriangulatorConfig.Calibration.GPU_HASH = gpuHash();
        ConfigurationManager.saveToFile(true, TriangulatorConfig.Calibration.class);
        Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }

    @SneakyThrows
    private static String gpuHash() {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(gpu().getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static String gpu() {
        return GL11.glGetString(GL11.GL_RENDERER) +
               " GL version " +
               GL11.glGetString(GL11.GL_VERSION) +
               ", " +
               GL11.glGetString(GL11.GL_VENDOR);
    }

    @SneakyThrows
    @SubscribeEvent
    public void onSinglePlayer(EntityJoinWorldEvent e) {
        if (TriangulatorConfig.SUPPRESS_CALIBRATION) {
            return;
        }

        if (!(e.entity instanceof EntityPlayerSP)) {
            return;
        }
        if (gpuHash().equals(TriangulatorConfig.Calibration.GPU_HASH)) {
            return;
        }
        if (ModuleConfig.TRIANGULATOR()) {
            val alert = FormattedText.parse(EnumChatFormatting.RED +
                                            I18n.format("chat.triangulator.calibration.message"));
            val text = alert.toChatText();
            val ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/triangulator_calibrate");
            for (val t : text) {
                t.getChatStyle()
                 .setChatClickEvent(ce);
                ((EntityPlayerSP) e.entity).addChatMessage(t);
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (calibrateRequest) {
            calibrateRequest = false;
            Minecraft.getMinecraft()
                     .displayGuiScreen(new CalibrationGUI());
        }
    }

    public static class CalibrationCommand extends CommandBase {
        @Override
        public String getCommandName() {
            return "triangulator_calibrate";
        }

        @Override
        public int getRequiredPermissionLevel() {
            return 0;
        }

        @Override
        public String getCommandUsage(ICommandSender sender) {
            return "/triangulator_calibrate";
        }

        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            calibrateRequest = true;
        }
    }
}
