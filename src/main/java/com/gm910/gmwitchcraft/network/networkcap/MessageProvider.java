package com.gm910.gmwitchcraft.network.networkcap;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class MessageProvider implements ICapabilitySerializable<NBTBase> {

	@CapabilityInject(INetworkData.class)
	public static final Capability<INetworkData> NET_CAP = null;
	
	private INetworkData instance = NET_CAP.getDefaultInstance();
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		// TODO Auto-generated method stub
		return capability == NET_CAP;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		// TODO Auto-generated method stub
		return capability == NET_CAP ? NET_CAP.<T> cast(this.instance) : null;
	}

	@Override
	public NBTBase serializeNBT() {
		// TODO Auto-generated method stub
		return NET_CAP.getStorage().writeNBT(NET_CAP, this.instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		// TODO Auto-generated method stub
		NET_CAP.getStorage().readNBT(NET_CAP, this.instance, null, nbt);
	}
	
}
