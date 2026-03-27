#!/usr/bin/env python3

import os
import struct
import zlib

# Complete fluid registry sourced from both HBM versions (Currently using JameH2).
#   HBM original : https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT/blob/master/src/main/java/com/hbm/inventory/fluid/Fluids.java
#   JameH2 fork  : https://github.com/JameH2/Hbm-s-Nuclear-Tech-GIT/blob/space-travel-twopointfive/src/main/java/com/hbm/inventory/fluid/Fluids.java

FLUIDS = {
    "custom_demo": 0xff0000,
    "none": 0x888888,
    "water": 0x3333FF,
    "steam": 0xe5e5e5,
    "hotsteam": 0xE7D6D6,
    "superhotsteam": 0xE7B7B7,
    "ultrahotsteam": 0xE39393,
    "coolant": 0xd8fcff,
    "lava": 0xFF3300,
    "deuterium": 0x0000FF,
    "tritium": 0x000099,
    "oil": 0x020202,
    "hotoil": 0x300900,
    "heavyoil": 0x141312,
    "bitumen": 0x1f2426,
    "smear": 0x190f01,
    "heatingoil": 0x211806,
    "reclaimed": 0x332b22,
    "petroil": 0x44413d,
    "lubricant": 0x606060,
    "naphtha": 0x595744,
    "diesel": 0xf2eed5,
    "lightoil": 0x8c7451,
    "kerosene": 0xffa5d2,
    "gas": 0xfffeed,
    "petroleum": 0x7cb7c9,
    "lpg": 0x4747EA,
    "biogas": 0xbfd37c,
    "biofuel": 0xeef274,
    "nitan": 0x8018ad,
    "uf6": 0xD1CEBE,
    "puf6": 0x4C4C4C,
    "sas3": 0x4ffffc,
    "schrabidic": 0x006B6B,
    "amat": 0x010101,
    "aschrab": 0xb50000,
    "peroxide": 0xfff7aa,
    "watz": 0x86653E,
    "cryogel": 0x32ffff,
    "hydrogen": 0x4286f4,
    "oxygen": 0x98bdf9,
    "xenon": 0xba45e8,
    "balefire": 0x28e02e,
    "mercury": 0x808080,
    "pain": 0x938541,
    "wastefluid": 0x544400,
    "wastegas": 0xB8B8B8,
    "gasoline": 0x445772,
    "coalgas": 0x445772,
    "spentsteam": 0x445772,
    "fracksol": 0x798A6B,
    "plasma_dt": 0xF7AFDE,
    "plasma_hd": 0xF0ADF4,
    "plasma_ht": 0xD1ABF2,
    "plasma_xm": 0xC6A5FF,
    "plasma_bf": 0xA7F1A3,
    "carbondioxide": 0x404040,
    "plasma_dh3": 0xFF83AA,
    "helium3": 0xFCF0C4,
    "death": 0x717A88,
    "ethanol": 0xe0ffff,
    "heavywater": 0x00a0b0,
    "crackoil": 0x020202,
    "coaloil": 0x020202,
    "hotcrackoil": 0x300900,
    "naphtha_crack": 0x595744,
    "lightoil_crack": 0x8c7451,
    "diesel_crack": 0xf2eed5,
    "aromatics": 0x68A09A,
    "unsaturateds": 0x628FAE,
    "salient": 0x457F2D,
    "xpjuice": 0xBBFF09,
    "enderjuice": 0x127766,
    "petroil_leaded": 0x44413d,
    "gasoline_leaded": 0x445772,
    "coalgas_leaded": 0x445772,
    "sulfuric_acid": 0xB0AA64,
    "coolant_hot": 0x99525E,
    "mug": 0x4B2D28,
    "mug_hot": 0x6B2A20,
    "woodoil": 0x847D54,
    "coalcreosote": 0x51694F,
    "seedslurry": 0x7CC35E,
    "nitrogen": 0xB3C6D2,
    "blood": 0xB22424,
    "nitric_acid": 0xBB7A1E,
    "ammonia": 0x00A0F7,
    "hydrazine": 0x31517D,
    "bloodgas": 0x591000,
    "earthair": 0xD1CEEE,
    "blood_hot": 0xF22419,
    "solvent": 0xE4E3EF,
    "hcl": 0x00D452,
    "minsol": 0xFADF6A,
    "syngas": 0x131313,
    "oxyhydrogen": 0x483FC1,
    "radiosolvent": 0xA4D7DD,
    "chlorine": 0xBAB572,
    "heavyoil_vacuum": 0x131214,
    "reformate": 0x835472,
    "lightoil_vacuum": 0x8C8851,
    "sourgas": 0xC9BE0D,
    "xylene": 0x5C4E76,
    "neon": 0xF1F600,
    "argon": 0xFD70D0,
    "krypton": 0x9AC6E6,
    "coffee": 0x57493D,
    "tea": 0x76523C,
    "honey": 0xD99A02,
    "heatingoil_vacuum": 0x211D06,
    "diesel_reform": 0xCDC3C6,
    "diesel_crack_reform": 0xCDC3CC,
    "kerosene_reform": 0xFFA5F3,
    "reformgas": 0x6362AE,
    "milk": 0xCFCFCF,
    "smilk": 0xF5DEE4,
    "ccl": 0x0C3B2F,
    "colloid": 0x787878,
    "eveair": 0xDCABF8,
    "kmno4": 0x560046,
    "chloromethane": 0xD3CF9E,
    "methanol": 0x88739F,
    "bromine": 0xAF2214,
    "chloroethane": 0xBBA9A0,
    "polythylene": 0x35302E,
    "fluorine": 0xC5C539,
    "tektoair": 0x245F46,
    "phosgene": 0xCFC4A4,
    "mustardgas": 0xBAB572,
    "iongel": 0xB8FFFF,
    "elbowgrease": 0xCBC433,
    "nmasstetranol": 0xF1DB0F,
    "nmass": 0x53A9F4,
    "scutterblood": 0x6C166C,
    "htco4": 0x675454,
    "oil_coker": 0x001802,
    "naphtha_coker": 0x495944,
    "gas_coker": 0xDEF4CA,
    "egg": 0xD2C273,
    "cholesterol": 0xD6D2BD,
    "estradiol": 0xCDD5D8,
    "fishoil": 0x4B4A45,
    "sunfloweroil": 0xCBAD45,
    "nitroglycerin": 0x92ACA6,
    "redmud": 0xD85638,
    "chlorocalcite_solution": 0x808080,
    "chlorocalcite_mix": 0x808080,
    "chlorocalcite_cleaned": 0x808080,
    "potassium_chloride": 0x808080,
    "calcium_chloride": 0x808080,
    "calcium_solution": 0x808080,
    "smoke": 0x808080,
    "smoke_leaded": 0x808080,
    "smoke_poison": 0x808080,
    "joolgas": 0x829F82,
    "sarnusgas": 0xE47D5C,
    "ugas": 0x718C9A,
    "ngas": 0x8A668A,
    "emilk": 0xCFCFCF,
    "cmilk": 0xCFCFCF,
    "cream": 0xCFCFCF,
    "magma": 0xFF3300,
    "dicyanoacetylene": 0x675A9F,
    "brine": 0xD1A73E,
    "conglomera": 0x364D47,
    "helium4": 0xE54B0A,
    "heavywater_hot": 0x4D007B,
    "sodium": 0xCCD4D5,
    "sodium_hot": 0xE2ADC1,
    "thorium_salt": 0x7A5542,
    "thorium_salt_hot": 0x3E3627,
    "thorium_salt_depleted": 0x302D1C,
    "fullerene": 0xFF7FED,
    "pheromone": 0x5FA6E8,
    "pheromone_m": 0x48C9B0,
    "oil_ds": 0x121212,
    "hotoil_ds": 0x3F180F,
    "crackoil_ds": 0x2A1C11,
    "hotcrackoil_ds": 0x3A1A28,
    "naphtha_ds": 0x63614E,
    "lightoil_ds": 0x63543E,
    "stellar_flux": 0xE300FF,
    "dunaair": 0xD4704E,
    "vitriol": 0x6E5222,
    "slop": 0x929D45,
    "superheated_hydrogen": 0xE39393,
    "lead": 0x666672,
    "lead_hot": 0x776563,
    "gas_watz": 0x86653E,
    "uranium_bromide": 0xD1CEBE,
    "aqueous_copper": 0x4CC2A2,
    "coppersulfate": 0x55E5CF,
    "thorium_bromide": 0x7A5542,
    "gaseous_uranium_bromide": 0xD1CEBE,
    "gaseous_plutonium_bromide": 0x4C4C4C,
    "gaseous_schrabidium_bromide": 0x006B6B,
    "gaseous_thorium_bromide": 0x7A5542,
    "hgas": 0x999368,
    "perfluoromethyl": 0xBDC8DC,
    "perfluoromethyl_cold": 0x99DADE,
    "perfluoromethyl_hot": 0xB899DE,
    "lye": 0xFFECCC,
    "sodium_aluminate": 0xFFD191,
    "bauxite_solution": 0xE2560F,
    "alumina": 0xDDFFFF,
    "aqueous_nickel": 0xDACEBA,
    "concrete": 0xA2A2A2,
    "vinyl": 0xA2A2A2,
    "tcrude": 0x051914,
    "cbenz": 0x91C6BB,
    "halolight": 0xB6F9CF,
    "dhc": 0xD2AFFF,
    "air": 0xE7EAEB,
    "lithydro": 0xD1CEBE,
    "lithcarbonate": 0xD1CEBE,
}

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
OUTPUT_DIR = os.path.join(
    PROJECT_ROOT,
    "src", "main", "resources", "assets", "ntmfluidconverters",
    "textures", "blocks", "fluids",
)


def make_solid_png(r: int, g: int, b: int) -> bytes:
    """Return the bytes of a 16x16 solid-color RGB PNG."""
    width, height = 16, 16

    # IHDR chunk
    ihdr_data = struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0)

    # IDAT: one filter byte (0x00 = None) per row, then 16×3 bytes of RGB
    row = bytes([0]) + bytes([r, g, b] * width)
    raw_pixels = row * height
    idat_data = zlib.compress(raw_pixels, level=9)

    def chunk(tag: bytes, data: bytes) -> bytes:
        c = struct.pack(">I", len(data)) + tag + data
        c += struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)
        return c

    return (
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", ihdr_data)
        + chunk(b"IDAT", idat_data)
        + chunk(b"IEND", b"")
    )


def main():
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    created = 0
    skipped = 0

    for name, color in sorted(FLUIDS.items()):
        r = (color >> 16) & 0xFF
        g = (color >> 8) & 0xFF
        b = color & 0xFF
        png_bytes = make_solid_png(r, g, b)

        for variant in ("still", "flow"):
            filename = f"{name}_{variant}.png"
            path = os.path.join(OUTPUT_DIR, filename)
            if os.path.exists(path):
                print(f"  SKIP  {filename}  (already exists)")
                skipped += 1
            else:
                with open(path, "wb") as f:
                    f.write(png_bytes)
                print(f"  WRITE {filename}  (#{r:02X}{g:02X}{b:02X})")
                created += 1

    print()
    print(f"Done: {created} texture(s) created, {skipped} skipped.")


if __name__ == "__main__":
    main()
