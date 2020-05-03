package com.gm910.goeturgy.spells.util;

import net.minecraftforge.energy.IEnergyStorage;

public interface ISpellEnergyStorage extends ISpellObject {

	public AetherStorage getStorage();
	
	   /**
	    * 
	    * @param maxReceive
	    * @return quantity accepted
	    */
	public int addEnergy(int maxReceive);
	
	public int removeEnergy(int maxExtract);

	public int getEnergyStored();

}
