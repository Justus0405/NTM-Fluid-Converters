package com.justus0405.ntmfluidconverters.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.justus0405.ntmfluidconverters.tile.TileEntityHbmToForge;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * The HBM to Forge converter block.
 *
 * <p>
 * Connect HBM fluid pipes to any face to push fluid in.
 * Connect Forge fluid pipes/tanks to any face to pull fluid out.
 * The internal buffer holds 8,000 mB (8 buckets) of a single fluid at a time.
 */
public class BlockHbmToForge extends BlockContainer {

    public BlockHbmToForge() {
        super(Material.iron);
        setBlockName("hbm_to_forge");
        setCreativeTab(CreativeTabs.tabMisc);
        setHardness(3.5f);
        setResistance(10.0f);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityHbmToForge();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        blockIcon = register.registerIcon("ntmfluidconverters:hbm_to_forge");
    }
}
