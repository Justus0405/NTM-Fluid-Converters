package com.justus0405.ntmfluidconverters.client;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import com.justus0405.ntmfluidconverters.FluidConverter;
import com.justus0405.ntmfluidconverters.NTMFluidConverters;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

// Stitches textures for fluids we registered into the block atlas.
// Shipped pngs win, the rest get a flat sprite generated from the HBM fluid color.
// The fallback texture is the last resort for fluids we cannot color.
@SideOnly(Side.CLIENT)
public class FluidTextureHandler {

    private static final int BLOCK_ATLAS = 0;

    @SubscribeEvent
    public void onTextureStitchPre(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() != BLOCK_ATLAS) return;

        for (String forgeName : FluidConverter.getOurFluids()) {
            Fluid fluid = FluidRegistry.getFluid(forgeName);
            if (fluid == null) continue;

            IIcon still = stitchVariant(
                event.map,
                fluid,
                forgeName,
                "still",
                FluidConverter.stillIconName(forgeName),
                FluidConverter.FALLBACK_STILL_ICON);
            IIcon flowing = stitchVariant(
                event.map,
                fluid,
                forgeName,
                "flow",
                FluidConverter.flowIconName(forgeName),
                FluidConverter.FALLBACK_FLOW_ICON);
            fluid.setIcons(still, flowing);
        }
    }

    // Variants are checked on their own because a fluid can have a still png without a flow one.
    private IIcon stitchVariant(TextureMap map, Fluid fluid, String forgeName, String variant, String iconName,
        String fallbackIcon) {
        if (FluidConverter.hasTexture(forgeName, variant)) {
            return map.registerIcon(iconName);
        }

        IIcon generated = generate(map, fluid, forgeName, iconName);
        if (generated != null) return generated;

        NTMFluidConverters.LOG
            .warn("No texture for fluid '{}' variant '{}'! Using fallback texture", forgeName, variant);
        return map.registerIcon(fallbackIcon);
    }

    private IIcon generate(TextureMap map, Fluid fluid, String forgeName, String iconName) {
        int color = FluidConverter.getHbmFluidColor(FluidConverter.getHbmFluid(fluid));
        if (color < 0) return null;

        try {
            GeneratedFluidSprite sprite = new GeneratedFluidSprite(iconName, color);
            // setTextureEntry refuses names that are already taken, a duplicate would render wrong.
            return map.setTextureEntry(iconName, sprite) ? sprite : null;
        } catch (RuntimeException e) {
            NTMFluidConverters.LOG.error("Failed to generate texture '{}' for fluid '{}'!", iconName, forgeName, e);
            return null;
        }
    }
}
