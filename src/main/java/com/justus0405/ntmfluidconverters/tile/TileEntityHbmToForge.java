package com.justus0405.ntmfluidconverters.tile;

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
import com.justus0405.ntmfluidconverters.FluidConverter;

import api.hbm.fluidmk2.IFluidConnectorMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;

// Accepts fluid from HBM pipes and exposes it as a Forge IFluidHandler so Forge pipes can drain it.
public class TileEntityHbmToForge extends TileEntity implements IFluidStandardReceiverMK2, IFluidHandler {

    private final FluidTank hbmTank = new FluidTank(Fluids.NONE, 8000);

    // ILoadedTile
    @Override
    public boolean isLoaded() {
        return !isInvalid();
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
        if (hbmTank.getTankType() != type) {
            hbmTank.setTankType(type);
        }
        int toAdd = (int) Math.min(amount, hbmTank.getMaxFill() - hbmTank.getFill());
        hbmTank.setFill(hbmTank.getFill() + toAdd);
        return amount - toAdd;
    }

    // Tick
    @Override
    public void updateEntity() {
        if (worldObj.isRemote) return;

        // subscriptions expire so we re-subscribe every tick.
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            int nx = xCoord + dir.offsetX;
            int ny = yCoord + dir.offsetY;
            int nz = zCoord + dir.offsetZ;

            TileEntity neighbor = worldObj.getTileEntity(nx, ny, nz);
            if (!(neighbor instanceof IFluidConnectorMK2)) continue;

            if (hbmTank.getTankType() != Fluids.NONE && hbmTank.getFill() > 0) {
                trySubscribe(hbmTank.getTankType(), worldObj, nx, ny, nz, dir);
            } else {
                // empty tank, subscribe for everything so we accept whatever comes in.
                for (FluidType type : Fluids.getAll()) {
                    if (type == Fluids.NONE) continue;
                    trySubscribe(type, worldObj, nx, ny, nz, dir);
                }
            }
        }
    }

    // IFluidHandler drain only, Forge pipes can't push in.
    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource == null || hbmTank.getFill() <= 0) return null;
        Fluid forgeFluid = FluidConverter.getForgeFluid(hbmTank.getTankType());
        if (forgeFluid == null || !forgeFluid.equals(resource.getFluid())) return null;
        int amount = Math.min(resource.amount, hbmTank.getFill());
        if (doDrain) drainHbmTank(amount);
        return new FluidStack(forgeFluid, amount);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        if (hbmTank.getFill() <= 0) return null;
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
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        hbmTank.writeToNBT(nbt, "tank");
    }

    private void drainHbmTank(int amount) {
        hbmTank.setFill(hbmTank.getFill() - amount);
        if (hbmTank.getFill() == 0) {
            hbmTank.setTankType(Fluids.NONE);
        }
    }
}
