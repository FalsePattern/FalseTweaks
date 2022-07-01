package com.falsepattern.triangulator.calibration;

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
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Calibration {
    private static final Calibration INSTANCE = new Calibration();

    public static void registerBus() {
        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static void setCalibration(boolean flip) {
        val config = ConfigurationManager.getConfigElements(CalibrationConfig.class);
        for (val element : config) {
            switch (element.getName()) {
                case "FLIP_DIAGONALS":
                    element.set(flip);
                    break;
                case "GPU_HASH":
                    element.set(gpuHash());
                    break;
            }
        }
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
        return GL11.glGetString(GL11.GL_RENDERER) + " GL version " + GL11.glGetString(GL11.GL_VERSION) + ", " +
               GL11.glGetString(GL11.GL_VENDOR);
    }

    @SneakyThrows
    @SubscribeEvent
    public void onSinglePlayer(EntityJoinWorldEvent e) {
        if (!(e.entity instanceof EntityPlayerSP)) {
            return;
        }
        if (gpuHash().equals(CalibrationConfig.GPU_HASH)) {
            return;
        }
        val alert = FormattedText.parse(EnumChatFormatting.RED + I18n.format("chat.triangulator.calibration.message"));
        val text = alert.toChatText();
        val ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "triangulator_calibrate");
        for (val t : text) {
            t.getChatStyle().setChatClickEvent(ce);
            ((EntityPlayerSP) e.entity).addChatMessage(t);
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
            Minecraft.getMinecraft().displayGuiScreen(new CalibrationGUI());
        }
    }
}
