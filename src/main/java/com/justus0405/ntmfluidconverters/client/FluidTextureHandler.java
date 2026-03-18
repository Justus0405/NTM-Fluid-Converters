package com.justus0405.ntmfluidconverters.client;

import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import com.justus0405.ntmfluidconverters.FluidConverter;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

// Stitches textures for fluids we registered into the block atlas.
// Uses a fallback texture for any fluid that doesn't have its own png yet.
@SideOnly(Side.CLIENT)
public class FluidTextureHandler {

    private static final int BLOCK_ATLAS = 0;

    @SubscribeEvent
    public void onTextureStitchPre(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() != BLOCK_ATLAS) return;

        for (String forgeName : FluidConverter.getOurFluids()) {
            Fluid fluid = FluidRegistry.getFluid(forgeName);
            if (fluid == null) continue;

            boolean hasTexture = FluidConverter.hasTexture(forgeName);
            IIcon still = event.map.registerIcon(
                hasTexture ? FluidConverter.stillIconName(forgeName) : FluidConverter.FALLBACK_STILL_ICON);
            IIcon flowing = event.map
                .registerIcon(hasTexture ? FluidConverter.flowIconName(forgeName) : FluidConverter.FALLBACK_FLOW_ICON);
            fluid.setIcons(still, flowing);
        }
    }
}
