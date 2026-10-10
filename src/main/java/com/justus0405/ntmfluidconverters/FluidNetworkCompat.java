package com.justus0405.ntmfluidconverters;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.NodeNet;
import com.hbm.uninos.UniNodespace;

import api.hbm.fluidmk2.IFluidConnectorMK2;
import api.hbm.fluidmk2.IFluidProviderMK2;
import api.hbm.fluidmk2.IFluidReceiverMK2;
import api.hbm.tile.ILoadedTile.TileAccessCache;

// HBM removed the trySubscribe and tryProvide defaults in X5842. These helpers
// reimplement their network subscription with API that exists in both versions,
// so one jar keeps working on both.
@SuppressWarnings("unchecked")
public final class FluidNetworkCompat {

    private FluidNetworkCompat() {}

    // The caller already resolved the neighbor, so the tile cache is only read once per type scan.
    public static NodeNet subscribe(IFluidReceiverMK2 receiver, FluidType type, TileEntity te, World world, int x,
        int y, int z, ForgeDirection dir) {
        if (!(te instanceof IFluidConnectorMK2)) return null;

        IFluidConnectorMK2 connector = (IFluidConnectorMK2) te;
        if (!connector.canConnect(type, dir.getOpposite())) return null;

        GenNode node = UniNodespace.getNode(world, x, y, z, type.getNetworkProvider());
        if (node == null || node.net == null) return null;

        node.net.addReceiver(receiver);
        return node.net;
    }

    public static NodeNet provide(IFluidProviderMK2 provider, FluidTank tank, World world, int x, int y, int z,
        ForgeDirection dir) {
        TileEntity te = TileAccessCache.getTileOrCache(world, x, y, z);
        FluidType type = tank.getTankType();
        int pressure = tank.getPressure();
        NodeNet net = null;

        if (te instanceof IFluidConnectorMK2) {
            IFluidConnectorMK2 connector = (IFluidConnectorMK2) te;
            if (connector.canConnect(type, dir.getOpposite())) {
                GenNode node = UniNodespace.getNode(world, x, y, z, type.getNetworkProvider());
                if (node != null && node.net != null) {
                    node.net.addProvider(provider);
                    net = node.net;
                }
            }
        }

        // X5808's tryProvide also pushed straight into a directly adjacent receiver.
        // X5842 only moves fluid through networks, so keep the old behavior here.
        if (te != provider && te instanceof IFluidReceiverMK2) {
            IFluidReceiverMK2 receiver = (IFluidReceiverMK2) te;
            if (receiver.canConnect(type, dir.getOpposite())) {
                long provides = Math
                    .min(provider.getFluidAvailable(type, pressure), provider.getProviderSpeed(type, pressure));
                long receives = Math.min(receiver.getDemand(type, pressure), receiver.getReceiverSpeed(type, pressure));
                long toTransfer = Math.min(provides, receives);
                if (toTransfer > 0) {
                    long moved = toTransfer - receiver.transferFluid(type, pressure, toTransfer);
                    if (moved > 0) provider.useUpFluid(type, pressure, moved);
                }
            }
        }

        return net;
    }
}
