package com.justus0405.ntmfluidconverters.client;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import com.justus0405.ntmfluidconverters.NTMFluidConverters;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

// A flat 16x16 sprite built in memory for fluids that have no shipped png.
// Uses Forge's custom loader so the icon name does not need a file on disk.
@SideOnly(Side.CLIENT)
public class GeneratedFluidSprite extends TextureAtlasSprite {

    private static final int SIZE = 16;

    private final BufferedImage image;

    public GeneratedFluidSprite(String iconName, int color) {
        super(iconName);

        // Build the image up front so the handler can fall back if this fails.
        int argb = 0xFF000000 | (color & 0xFFFFFF);
        int[] pixels = new int[SIZE * SIZE];
        Arrays.fill(pixels, argb);

        image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, SIZE, SIZE, pixels, 0, SIZE);
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            // One slot per mipmap level. Higher levels stay null and get derived from level 0.
            BufferedImage[] frames = new BufferedImage[1 + mc.gameSettings.mipmapLevels];
            frames[0] = image;
            loadSprite(frames, null, mc.gameSettings.anisotropicFiltering > 1);

            // False puts this sprite into the stitcher. The javadoc on load() has it backwards.
            return false;
        } catch (RuntimeException e) {
            NTMFluidConverters.LOG.error("Failed to build generated texture '{}'!", getIconName(), e);
            // True keeps a broken sprite out of the stitcher, it falls back to missingno instead of crashing.
            return true;
        }
    }
}
