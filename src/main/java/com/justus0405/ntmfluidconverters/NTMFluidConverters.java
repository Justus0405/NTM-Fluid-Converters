package com.justus0405.ntmfluidconverters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = NTMFluidConverters.MODID,
    version = Tags.VERSION,
    name = "NTM Fluid Converters",
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-after:hbm")
public class NTMFluidConverters {

    public static final String MODID = "ntmfluidconverters";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(
        clientSide = "com.justus0405.ntmfluidconverters.ClientProxy",
        serverSide = "com.justus0405.ntmfluidconverters.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
