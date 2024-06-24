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
package com.falsepattern.falsetweaks.mixin.mixins.common.startup;

import com.falsepattern.falsetweaks.modules.startup.ThreadSafeASMDataTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.discovery.ModCandidate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

@Mixin(value = ASMDataTable.class,
       remap = false)
public abstract class ASMDataTableMixin implements ThreadSafeASMDataTable {
    @Shadow
    private Map<ModContainer, SetMultimap<String, ASMDataTable.ASMData>> containerAnnotationData;

    @Shadow
    private List<ModContainer> containers;

    @Shadow
    private SetMultimap<String, ASMDataTable.ASMData> globalAnnotationData;

    @Shadow
    private SetMultimap<String, ModCandidate> packageMap;
    private ReentrantLock mutex = null;

    @Override
    public void enableMutex(boolean enable) {
        if (enable) {
            mutex = new ReentrantLock();
        } else {
            mutex = null;
        }
    }

    private void lock() {
        if (mutex != null) {
            mutex.lock();
        }
    }

    private void unlock() {
        if (mutex != null) {
            mutex.unlock();
        }
    }

    //Small and actually useful tweak borrowed from hodgepodge

    /**
     * @author glee8e
     * @reason to optimize the embarrassingly inefficient containerAnnotationData build process
     */
    @Overwrite(remap = false)
    public SetMultimap<String, ASMDataTable.ASMData> getAnnotationsFor(ModContainer container) {
        lock();
        try {
            if (containerAnnotationData == null) {
                val mapBuilder = new HashMap<ModContainer, SetMultimap<String, ASMDataTable.ASMData>>();
                val containersMap = Multimaps.index(containers, ModContainer::getSource);
                for (val entry : globalAnnotationData.entries()) {
                    for (ModContainer modContainer : containersMap.get(entry.getValue().getCandidate().getModContainer())) {
                        mapBuilder.computeIfAbsent(modContainer, map -> HashMultimap.create()).put(entry.getKey(), entry.getValue());
                    }
                }
                containerAnnotationData = mapBuilder;
            }
            return containerAnnotationData.get(container);
        } finally {
            unlock();
        }
    }

    /**
     * @author FalsePattern
     * @reason Thread safe
     */
    @Overwrite(remap = false)
    public Set<ASMDataTable.ASMData> getAll(String annotation) {
        lock();
        try {
            return globalAnnotationData.get(annotation);
        } finally {
            unlock();
        }
    }

    /**
     * @author FalsePattern
     * @reason Thread safe
     */
    @Overwrite(remap = false)
    public void addASMData(ModCandidate candidate, String annotation, String className, String objectName, Map<String, Object> annotationInfo) {
        lock();
        try {
            globalAnnotationData.put(annotation, new ASMDataTable.ASMData(candidate, annotation, className, objectName, annotationInfo));
        } finally {
            unlock();
        }
    }

    /**
     * @author FalsePattern
     * @reason Thread safe
     */
    @Overwrite(remap = false)
    public void addContainer(ModContainer container) {
        lock();
        try {
            this.containers.add(container);
        } finally {
            unlock();
        }
    }

    /**
     * @author FalsePattern
     * @reason Thread safe
     */
    @Overwrite(remap = false)
    public void registerPackage(ModCandidate modCandidate, String pkg) {
        lock();
        try {
            this.packageMap.put(pkg, modCandidate);
        } finally {
            unlock();
        }
    }

    /**
     * @author FalsePattern
     * @reason Thread safe
     */
    @Overwrite(remap = false)
    public Set<ModCandidate> getCandidatesFor(String pkg) {
        lock();
        try {
            return this.packageMap.get(pkg);
        } finally {
            unlock();
        }
    }
}
