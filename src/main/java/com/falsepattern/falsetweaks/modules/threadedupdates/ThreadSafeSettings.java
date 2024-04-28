package com.falsepattern.falsetweaks.modules.threadedupdates;

import com.falsepattern.falsetweaks.asm.modules.threadedupdates.settings.Threading_GameSettingsRedirector;

/**
 * These methods are injected as redirects using ASM.
 *
 * @implNote I REALLY hate that people tamper with this mid-renders. -Ven
 * @see Threading_GameSettingsRedirector
 */
@SuppressWarnings("unused")
public interface ThreadSafeSettings {
    void ft$update();

    void ft$fancyGraphics(boolean fancyGraphics);

    boolean ft$fancyGraphics();
}
