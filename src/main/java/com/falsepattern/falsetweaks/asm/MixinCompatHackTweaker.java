package com.falsepattern.falsetweaks.asm;

import com.falsepattern.falsetweaks.Tags;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.util.List;

public class MixinCompatHackTweaker implements ITweaker {
    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {

    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {

    }

    @Override
    public String getLaunchTarget() {
        return null;
    }

    @Override
    public String[] getLaunchArguments() {
        Launch.classLoader.registerTransformer(Tags.ROOT_PKG + ".asm.modules.threadedupdates.block.Threading_BlockMinMaxTransformer");
        return new String[0];
    }
}
