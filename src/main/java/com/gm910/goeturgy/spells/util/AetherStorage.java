package com.gm910.goeturgy.spells.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;

public class AetherStorage extends EnergyStorage implements IEnergyStorage, ICapabilitySerializable<NBTTagCompound> {

	
	public AetherStorage() {
		this(0);
		
	}
	
	public AetherStorage(NBTTagCompound comp) {
		this();
		this.deserializeNBT(comp);
	}
	
	
	public AetherStorage(int capacity, int maxReceive, int maxExtract, int energy) {
		super(capacity, maxReceive, maxExtract, energy);
		
	}
	
	public AetherStorage(int capacity, int maxTransfer, int energy) {
		this(capacity, maxTransfer, maxTransfer, energy);
	}
	
	public AetherStorage(int capacity, int maxTransfer) {
		this(capacity, maxTransfer, maxTransfer, 0);
	}
	
	public AetherStorage(int capacity) {
		this(capacity, capacity);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		
		return capability == CapabilityEnergy.ENERGY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		
		return hasCapability(capability, facing) ? (T) this : null;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound cmp = new NBTTagCompound();
		cmp.setInteger("Power", this.energy);
		return cmp;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		this.energy = nbt.getInteger("Power");
	}
	
}
