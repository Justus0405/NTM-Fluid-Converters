package com.justus0405.ntmfluidconverters.tile;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.uninos.NodeNet;
import com.justus0405.ntmfluidconverters.FluidConverter;
import com.justus0405.ntmfluidconverters.FluidNetworkCompat;
import com.justus0405.ntmfluidconverters.ModConfig;

import api.hbm.fluidmk2.IFluidConnectorMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;

// Accepts fluid from HBM pipes and exposes it as a Forge IFluidHandler so Forge pipes can drain it.
public class TileEntityHbmToForge extends TileEntity implements IFluidStandardReceiverMK2, IFluidHandler {

    private final FluidTank hbmTank = new FluidTank(Fluids.NONE, ModConfig.hbmToForgeBufferSize);
    private final Set<NodeNet> subscribedNets = new HashSet<>();
    private FluidType subscribedType = Fluids.NONE;
    private boolean loaded = true;

    // ILoadedTile
    @Override
    public boolean isLoaded() {
        return loaded && !isInvalid();
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        loaded = false;
    }

    // IFluidUserMK2
    @Override
    public FluidTank[] getAllTanks() {
        return new FluidTank[] { hbmTank };
    }

    // IFluidStandardReceiverMK2
    @Override
    public FluidTank[] getReceivingTanks() {
        return new FluidTank[] { hbmTank };
    }

    // Accept any fluid type when the tank is empty, not just whatever is already stored.
    @Override
    public long getDemand(FluidType type, int pressure) {
        if (pressure != 0) return 0;
        if (hbmTank.getTankType() == type) {
            return hbmTank.getMaxFill() - hbmTank.getFill();
        }
        if (hbmTank.getTankType() == Fluids.NONE || hbmTank.getFill() == 0) {
            return hbmTank.getMaxFill();
        }
        return 0;
    }

    // Need to set the tank type on the first fill since the default impl expects them to already match.
    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        if (pressure != 0) return amount;
        if (hbmTank.getTankType() != Fluids.NONE && hbmTank.getTankType() != type) return amount;
        boolean changed = false;
        if (hbmTank.getTankType() != type) {
            hbmTank.setTankType(type);
            changed = true;
        }
        int toAdd = (int) Math.min(amount, hbmTank.getMaxFill() - hbmTank.getFill());
        if (toAdd > 0) {
            hbmTank.setFill(hbmTank.getFill() + toAdd);
            changed = true;
        }
        if (changed) markDirty();
        return amount - toAdd;
    }

    // Tick
    @Override
    public void updateEntity() {
        if (worldObj.isRemote) return;

        // X5808 times subscriptions out, X5842 does not, so drop ours ourselves when the type changes.
        if (hbmTank.getTankType() != subscribedType) {
            unsubscribeAll();
            subscribedType = hbmTank.getTankType();
        }

        // Nets are destroyed when their pipes break, so drop those references instead of keeping them.
        subscribedNets.removeIf(net -> !net.isValid());

        // While the tank is empty we accept any fluid, so scan the whole registry. Both the list
        // and the neighbour lookup are per tick, not per side, to keep the idle cost down.
        boolean acceptAny = hbmTank.getTankType() == Fluids.NONE || hbmTank.getFill() <= 0;
        FluidType[] allTypes = acceptAny ? Fluids.getAll() : null;

        // subscriptions expire or get dropped, so we re-subscribe every tick.
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            int nx = xCoord + dir.offsetX;
            int ny = yCoord + dir.offsetY;
            int nz = zCoord + dir.offsetZ;

            TileEntity neighbor = worldObj.getTileEntity(nx, ny, nz);
            if (!(neighbor instanceof IFluidConnectorMK2)) continue;

            // Same neighbour for every fluid type, so resolve it once through the HBM cache.
            TileEntity connector = TileAccessCache.getTileOrCache(worldObj, nx, ny, nz);

            if (acceptAny) {
                for (FluidType type : allTypes) {
                    if (type == Fluids.NONE) continue;
                    track(FluidNetworkCompat.subscribe(this, type, connector, worldObj, nx, ny, nz, dir));
                }
            } else {
                track(FluidNetworkCompat.subscribe(this, hbmTank.getTankType(), connector, worldObj, nx, ny, nz, dir));
            }
        }
    }

    private void track(NodeNet net) {
        if (net != null) subscribedNets.add(net);
    }

    @SuppressWarnings("unchecked")
    private void unsubscribeAll() {
        for (NodeNet net : subscribedNets) net.removeReceiver(this);
        subscribedNets.clear();
    }

    // IFluidHandler drain only, Forge pipes can't push in.
    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource == null || resource.getFluid() == null || resource.amount <= 0 || hbmTank.getFill() <= 0) {
            return null;
        }
        Fluid forgeFluid = FluidConverter.getForgeFluid(hbmTank.getTankType());
        if (forgeFluid == null || !forgeFluid.equals(resource.getFluid())) return null;
        int amount = Math.min(resource.amount, hbmTank.getFill());
        if (doDrain) drainHbmTank(amount);
        return new FluidStack(forgeFluid, amount);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        if (maxDrain <= 0 || hbmTank.getFill() <= 0) return null;
        Fluid forgeFluid = FluidConverter.getForgeFluid(hbmTank.getTankType());
        if (forgeFluid == null) return null;
        int amount = Math.min(maxDrain, hbmTank.getFill());
        if (doDrain) drainHbmTank(amount);
        return new FluidStack(forgeFluid, amount);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return false;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        if (hbmTank.getFill() <= 0) return false;
        Fluid forgeFluid = FluidConverter.getForgeFluid(hbmTank.getTankType());
        return forgeFluid != null && forgeFluid.equals(fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        net.minecraftforge.fluids.FluidTank forgeTank = new net.minecraftforge.fluids.FluidTank(hbmTank.getMaxFill());
        if (hbmTank.getFill() > 0 && hbmTank.getTankType() != Fluids.NONE) {
            Fluid forgeFluid = FluidConverter.getForgeFluid(hbmTank.getTankType());
            if (forgeFluid != null) {
                forgeTank.setFluid(new FluidStack(forgeFluid, hbmTank.getFill()));
            }
        }
        return new FluidTankInfo[] { forgeTank.getInfo() };
    }

    // NBT
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        hbmTank.readFromNBT(nbt, "tank");
        // HBM NBT includes maxFill, but config takes precedence.
        hbmTank.changeTankSize(ModConfig.hbmToForgeBufferSize);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        hbmTank.writeToNBT(nbt, "tank");
    }

    private void drainHbmTank(int amount) {
        if (amount <= 0) return;
        hbmTank.setFill(hbmTank.getFill() - amount);
        if (hbmTank.getFill() == 0) {
            hbmTank.setTankType(Fluids.NONE);
        }
        markDirty();
    }
}
