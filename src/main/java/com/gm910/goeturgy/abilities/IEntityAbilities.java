package com.gm910.goeturgy.abilities;

import net.minecraft.nbt.NBTTagCompound;

public interface IEntityAbilities {

	public Abilities getAbilities();
	
	public void setAbilities(Abilities ab);
	
	public NBTTagCompound writeToNBT(NBTTagCompound comp);
	
	public void readFromNBT(NBTTagCompound nbt);
}
