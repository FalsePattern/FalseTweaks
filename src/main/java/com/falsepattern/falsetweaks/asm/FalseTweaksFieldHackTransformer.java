package com.falsepattern.falsetweaks.asm;

import com.falsepattern.falsetweaks.asm.modules.threadedupdates.block.Threading_BlockMinMax;
import com.falsepattern.falsetweaks.asm.modules.threadedupdates.block.Threading_BlockMinMaxRedirector;
import com.falsepattern.falsetweaks.asm.modules.threadedupdates.settings.Threading_GameSettings;
import com.falsepattern.falsetweaks.asm.modules.threadedupdates.settings.Threading_GameSettingsRedirector;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.lib.turboasm.MergeableTurboTransformer;
import com.falsepattern.lib.turboasm.TurboClassTransformer;
import lombok.val;

import cpw.mods.fml.relauncher.FMLLaunchHandler;

import java.util.ArrayList;
import java.util.List;

public class FalseTweaksFieldHackTransformer extends MergeableTurboTransformer {
    private static List<TurboClassTransformer> transformers() {
        val transformers = new ArrayList<TurboClassTransformer>();
        if (FMLLaunchHandler.side().isClient() && ModuleConfig.THREADED_CHUNK_UPDATES()) {
            transformers.add(new Threading_GameSettings());
            transformers.add(new Threading_GameSettingsRedirector());
            transformers.add(new Threading_BlockMinMax());
            transformers.add(new Threading_BlockMinMaxRedirector());
        }
        return transformers;
    }

    public FalseTweaksFieldHackTransformer() {
        super(transformers());
    }
}
