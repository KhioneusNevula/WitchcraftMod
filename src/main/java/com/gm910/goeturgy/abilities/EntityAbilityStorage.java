package com.gm910.goeturgy.abilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class EntityAbilityStorage implements IStorage<IEntityAbilities> {

	@Override
	public NBTBase writeNBT(Capability<IEntityAbilities> capability, IEntityAbilities instance, EnumFacing side) {
		
		return instance.writeToNBT(new NBTTagCompound());
	}

	@Override
	public void readNBT(Capability<IEntityAbilities> capability, IEntityAbilities instance, EnumFacing side,
			NBTBase nbt) {
		instance.readFromNBT((NBTTagCompound)nbt);
	}

}
