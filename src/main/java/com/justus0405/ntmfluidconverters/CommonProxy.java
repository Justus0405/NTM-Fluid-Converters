package com.justus0405.ntmfluidconverters;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        ModBlocks.init();
    }

    public void init(FMLInitializationEvent event) {
        FluidConverter.registerHbmFluidsInForge();
    }

    @SuppressWarnings("unused")
    public void postInit(FMLPostInitializationEvent event) {
        ModRecipes.register();
    }
}
