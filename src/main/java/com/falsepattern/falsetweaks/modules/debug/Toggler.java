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
package com.falsepattern.falsetweaks.modules.debug;

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import lombok.val;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.util.function.Consumer;

public class Toggler extends JFrame {
    public Toggler() {
        setTitle("FalseTweaks Debug Menu");
        setLayout(new FlowLayout());


        if (ModuleConfig.THREADED_CHUNK_UPDATES()) {
            createToggle("Occlusion", Debug.occlusionChecks, x -> Debug.occlusionChecks = x);
            createToggle("Occlusion Mask (SEIZURE WARNING)", Debug.occlusionMask, x -> Debug.occlusionMask = x);
            createToggle("Frustum", Debug.frustumChecks, x -> Debug.frustumChecks = x);
            if (Compat.optiFineHasShaders()) {
                createToggle("Shadow Pass", Debug.shadowPass, x -> Debug.shadowPass = x);
                createToggle("Shadow Occlusion", Debug.shadowOcclusionChecks, x -> Debug.shadowOcclusionChecks = x);
                createToggle("Shadow Occlusion Mask (SEIZURE WARNING)",
                             Debug.shadowOcclusionMask,
                             x -> Debug.shadowOcclusionMask = x);
            }
            createToggle("Chunk Baking", Debug.chunkRebaking, x -> Debug.chunkRebaking = x);
            createToggle("Translucency sorting", Debug.translucencySorting, x -> Debug.translucencySorting = x);
            createToggle("TESR Rendering (Chests, stolen!)", Debug.tesrRendering, x -> Debug.tesrRendering = x);
            createToggle("Java Fine Logging", Debug.fineLogJava, x -> Debug.fineLogJava = x);
            createToggle("Java Trace Logging", Debug.fineLogJavaTrace, x -> Debug.fineLogJavaTrace = x);
            createToggle("MEGATrace Fine Logging", Debug.fineLogMegaTrace, x -> Debug.fineLogMegaTrace = x);
        }

        setSize(300, 200);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        setVisible(true);
        pack();
        setLocationRelativeTo(null);
    }

    private void createToggle(String name, boolean defaultState, Consumer<Boolean> onUpdate) {
        val box = new JCheckBox(name);
        box.setSelected(defaultState);
        box.addItemListener(e -> onUpdate.accept(e.getStateChange() == ItemEvent.SELECTED));
        add(box);
    }
}
