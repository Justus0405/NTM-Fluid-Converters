package com.justus0405.ntmfluidconverters;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;

import cpw.mods.fml.common.registry.LanguageRegistry;

// HBM uses ALL_UPPER_CASE for fluid names, Forge uses lower_case.
// Simple toLowerCase/toUpperCase is good enough to map between them.
public class FluidConverter {

    // tracks fluids we registered so the texture handler knows which ones to stitch.
    private static final Set<String> OUR_FLUIDS = new HashSet<>();

    public static Set<String> getOurFluids() {
        return Collections.unmodifiableSet(OUR_FLUIDS);
    }

    // Icon name helpers.
    public static String stillIconName(String forgeName) {
        return NTMFluidConverters.MODID + ":fluids/" + forgeName + "_still";
    }

    public static String flowIconName(String forgeName) {
        return NTMFluidConverters.MODID + ":fluids/" + forgeName + "_flow";
    }

    public static final String FALLBACK_STILL_ICON = NTMFluidConverters.MODID + ":fluids/fallback_still";
    public static final String FALLBACK_FLOW_ICON = NTMFluidConverters.MODID + ":fluids/fallback_flow";

    // Asset checks.
    public static boolean hasTexture(String forgeName) {
        String path = "/assets/" + NTMFluidConverters.MODID + "/textures/blocks/fluids/" + forgeName + "_still.png";
        return FluidConverter.class.getResource(path) != null;
    }

    // Fallback display name for fluids not in the lang file, just capitalize each word.
    public static String generateFallbackName(String forgeName) {
        String[] parts = forgeName.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (sb.length() > 0) sb.append(' ');
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

    // Fluid translation
    public static Fluid getForgeFluid(FluidType hbmFluid) {
        if (hbmFluid == null || hbmFluid == Fluids.NONE) return null;
        return FluidRegistry.getFluid(
            hbmFluid.getName()
                .toLowerCase(Locale.ROOT));
    }

    public static FluidType getHbmFluid(Fluid forgeFluid) {
        if (forgeFluid == null) return Fluids.NONE;
        FluidType result = Fluids.fromName(
            forgeFluid.getName()
                .toUpperCase(Locale.ROOT));
        return result != null ? result : Fluids.NONE;
    }

    // Registration
    public static void registerHbmFluidsInForge() {
        for (FluidType hbmFluid : Fluids.getAll()) {
            if (hbmFluid == Fluids.NONE) continue;
            String forgeName = hbmFluid.getName()
                .toLowerCase(Locale.ROOT);
            if (!FluidRegistry.isFluidRegistered(forgeName)) {
                FluidRegistry.registerFluid(new Fluid(forgeName));
                OUR_FLUIDS.add(forgeName);

                if (!hasTexture(forgeName)) {
                    NTMFluidConverters.LOG.warn(
                        "[{}] No texture found for fluid '{}' — using fallback texture",
                        NTMFluidConverters.MODID,
                        forgeName);
                }
            }
        }
    }

    // Injects generated display names for any fluids missing a lang entry.
    // Call client-side after lang files are loaded. The lang file covers all known HBM
    // fluids already, so this only matters for future ones we haven't added yet.
    // LanguageRegistry.addStringLocalization is deprecated in favor of .lang files, but
    // we can't use static .lang files for runtime-generated names, so we suppress the warning. ¯\_(ツ)_/¯
    @SuppressWarnings("deprecation")
    public static void registerFluidLangFallbacks() {
        for (String forgeName : OUR_FLUIDS) {
            String key = "fluid." + forgeName;
            if (StatCollector.translateToLocal(key)
                .equals(key)) {
                String generated = generateFallbackName(forgeName);
                NTMFluidConverters.LOG.warn(
                    "[{}] No lang entry found for fluid '{}' — using generated name '{}'",
                    NTMFluidConverters.MODID,
                    forgeName,
                    generated);
                LanguageRegistry.instance()
                    .addStringLocalization(key, "en_US", generated);
            }
        }
    }
}
