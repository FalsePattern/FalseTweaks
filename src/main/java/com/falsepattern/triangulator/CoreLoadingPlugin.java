package com.falsepattern.triangulator;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

import static cpw.mods.fml.relauncher.IFMLLoadingPlugin.*;

@MCVersion("1.7.10")
@Name(ModInfo.MODID)
@SortingIndex(750)
public class CoreLoadingPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return ModInfo.GROUPNAME + ".triangulator.Triangulator";
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
