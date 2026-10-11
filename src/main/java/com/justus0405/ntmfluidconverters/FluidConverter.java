package com.justus0405.ntmfluidconverters;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;

import cpw.mods.fml.common.registry.LanguageRegistry;

public class FluidConverter {

    // tracks fluids we registered so the texture handler knows which ones to stitch.
    private static final Set<String> OUR_FLUIDS = new HashSet<>();

    private static final HashMap<String, FluidType> FORGE_TO_HBM = new HashMap<>();

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
        return hasTexture(forgeName, "still");
    }

    // Still and flow are checked separately. The shipped pngs come in pairs, but a fork can ship only one.
    public static boolean hasTexture(String forgeName, String variant) {
        String path = "/assets/" + NTMFluidConverters.MODID
            + "/textures/blocks/fluids/"
            + forgeName
            + "_"
            + variant
            + ".png";
        return FluidConverter.class.getResource(path) != null;
    }

    // Fallback display name for fluids not in the lang file, just capitalize each word.
    @SuppressWarnings("SizeReplaceableByIsEmpty")
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
        FluidType result = FORGE_TO_HBM.get(forgeFluid.getName());
        return result != null ? result : Fluids.NONE;
    }

    // HBM never promised getColor as API, so look it up once instead of linking against it.
    private static final Method HBM_GET_COLOR = findHbmGetColor();

    private static Method findHbmGetColor() {
        try {
            return FluidType.class.getMethod("getColor");
        } catch (NoSuchMethodException e) {
            NTMFluidConverters.LOG
                .warn("This HBM version does not expose fluid colors, only shipped textures will be used!");
            return null;
        }
    }

    // The RGB color HBM uses for this fluid, or -1 when this HBM version cannot provide one.
    public static int getHbmFluidColor(FluidType hbmFluid) {
        if (hbmFluid == null || hbmFluid == Fluids.NONE || HBM_GET_COLOR == null) return -1;
        try {
            return ((Integer) HBM_GET_COLOR.invoke(hbmFluid)) & 0xFFFFFF;
        } catch (ReflectiveOperationException e) {
            return -1;
        }
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
                    NTMFluidConverters.LOG
                        .info("No shipped texture for fluid '{}'! Generating from the HBM color", forgeName);
                }
            }

            FORGE_TO_HBM.put(forgeName, hbmFluid);
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
                NTMFluidConverters.LOG
                    .warn("No lang entry found for fluid '{}'! Using generated name '{}'", forgeName, generated);
                LanguageRegistry.instance()
                    .addStringLocalization(key, "en_US", generated);
            }
        }
    }
}
