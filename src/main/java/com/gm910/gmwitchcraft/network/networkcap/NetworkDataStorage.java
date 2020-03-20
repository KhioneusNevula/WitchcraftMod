package com.gm910.gmwitchcraft.network.networkcap;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.util.Constants.NBT;

public class NetworkDataStorage implements IStorage<INetworkData> {

	@Override
	public NBTBase writeNBT(Capability<INetworkData> capability, INetworkData instance, EnumFacing side) {
		NBTTagCompound sp = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		if (instance.hasMessage()) {
			for (String message : instance.sneakReadMessages()) {
				list.appendTag(new NBTTagString(message));
			}
		}
		sp.setTag("List", list);
		return sp;
	}

	@Override
	public void readNBT(Capability<INetworkData> capability, INetworkData instance, EnumFacing side, NBTBase nbt) {
		NBTTagCompound tag = (NBTTagCompound)nbt;
		NBTTagList list = tag.getTagList("List", NBT.TAG_COMPOUND);
		for (NBTBase t : list) {
			instance.sendMessage(((NBTTagString)t).getString());
		}
	}

}
