package com.justus0405.ntmfluidconverters;

import net.minecraftforge.common.MinecraftForge;

import com.justus0405.ntmfluidconverters.client.FluidTextureHandler;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MinecraftForge.EVENT_BUS.register(new FluidTextureHandler());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        FluidConverter.registerFluidLangFallbacks();
    }
}
