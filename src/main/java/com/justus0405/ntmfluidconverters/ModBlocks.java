package com.justus0405.ntmfluidconverters;

import net.minecraft.block.Block;

import com.justus0405.ntmfluidconverters.block.BlockForgeToHbm;
import com.justus0405.ntmfluidconverters.block.BlockHbmToForge;
import com.justus0405.ntmfluidconverters.tile.TileEntityForgeToHbm;
import com.justus0405.ntmfluidconverters.tile.TileEntityHbmToForge;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModBlocks {

    public static Block hbmToForge;
    public static Block forgeToHbm;

    public static void init() {
        hbmToForge = new BlockHbmToForge();
        GameRegistry.registerBlock(hbmToForge, "hbm_to_forge");
        GameRegistry.registerTileEntity(TileEntityHbmToForge.class, NTMFluidConverters.MODID + ":hbm_to_forge");

        forgeToHbm = new BlockForgeToHbm();
        GameRegistry.registerBlock(forgeToHbm, "forge_to_hbm");
        GameRegistry.registerTileEntity(TileEntityForgeToHbm.class, NTMFluidConverters.MODID + ":forge_to_hbm");
    }
}
