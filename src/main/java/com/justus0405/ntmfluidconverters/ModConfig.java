package com.justus0405.ntmfluidconverters;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ModConfig {

    public static int hbmToForgeBufferSize = 8000;
    public static int forgeToHbmBufferSize = 8000;

    static final String CATEGORY = "buffer_sizes";

    @SuppressWarnings("TextBlockMigration")
    public static void loadConfig(FMLPreInitializationEvent event) {
        Configuration config = new Configuration(
            new File(event.getModConfigurationDirectory(), "ntmfluidconverters.cfg"));

        config.load();
        config.setCategoryComment(
            CATEGORY,
            "Internal fluid tank capacity for each converter block.\n"
                + "Larger buffers smooth out throughput spikes but mean more fluid\n"
                + "is lost if the block is broken.");

        hbmToForgeBufferSize = config.getInt(
            "hbm_to_forge_buffer_size",
            CATEGORY,
            8000,
            1000,
            10000000,
            "Capacity in mB of the HBM-to-Forge converter's internal tank.");

        forgeToHbmBufferSize = config.getInt(
            "forge_to_hbm_buffer_size",
            CATEGORY,
            8000,
            1000,
            10000000,
            "Capacity in mB of the Forge-to-HBM converter's internal tank.");

        if (config.hasChanged()) {
            config.save();
        }
    }
}
