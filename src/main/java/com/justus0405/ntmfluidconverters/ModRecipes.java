package com.justus0405.ntmfluidconverters;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModRecipes {

    public static void register() {
        // Look up both ingredients once and share them between the two recipe registrations.
        ItemStack duct = lookupDuct();
        ItemStack circuit = lookupCircuit();

        if (duct == null || circuit == null) {
            // Don't register recipes if either one is empty.
            return;
        }

        registerHbmToForge(duct, circuit);
        registerForgeToHbm(duct, circuit);
    }

    // Looks up the Universal Fluid Duct.
    private static ItemStack lookupDuct() {

        ItemStack duct = GameRegistry.findItemStack("hbm", "tile.fluid_duct_neo", 1);
        if (duct != null) {
            return duct;
        }

        // If not found.
        NTMFluidConverters.LOG
            .warn("[{}] Could not find Universal Fluid Duct! skipping converter recipes", NTMFluidConverters.MODID);
        return null;
    }

    // Looks up the Integrated Circuit Board.
    private static ItemStack lookupCircuit() {

        ItemStack base = GameRegistry.findItemStack("hbm", "item.circuit", 1);
        if (base != null) {
            // Construct a new ItemStack with damage=8 to select the Integrated Circuit Board variant.
            // (For NTM: Space this is the Analog Circuit Board but eh)
            return new ItemStack(base.getItem(), 1, 8);
        }

        // If not found.
        NTMFluidConverters.LOG
            .warn("[{}] Could not find Integrated Circuit Board! skipping converter recipes", NTMFluidConverters.MODID);
        return null;
    }

    private static void registerHbmToForge(ItemStack duct, ItemStack circuit) {
        // HBM to Forge converter recipe.
        GameRegistry.addShapedRecipe(
            new ItemStack(ModBlocks.hbmToForge),
            "IBI",
            "DCD",
            "IBI",
            'I',
            Items.iron_ingot,
            'B',
            Items.bucket,
            'D',
            duct,
            'C',
            circuit);
    }

    private static void registerForgeToHbm(ItemStack duct, ItemStack circuit) {
        // Forge to HBM converter recipe.
        // Same materials as the HBM to Forge recipe but B and D are swapped.
        GameRegistry.addShapedRecipe(
            new ItemStack(ModBlocks.forgeToHbm),
            "IDI",
            "BCB",
            "IDI",
            'I',
            Items.iron_ingot,
            'B',
            Items.bucket,
            'D',
            duct,
            'C',
            circuit);
    }
}
