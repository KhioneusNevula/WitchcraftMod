package com.gm910.goeturgy.abilities.abilitytypes;

import com.gm910.goeturgy.abilities.Abilities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * MUST HAVE A CONSTRUCTOR ACCEPTING A SINGLE ABILITIES INSTANCE
 * A private final static field of type List<Class> called 'forTypes' will ensure the ability instances are set to null for entities extending classes not in the list
 * A private final static field of type List<Class> called 'notForTypes' will ensure the ability instances are set to null for entities extending classes in the list
 * @author borah
 *
 */
public abstract class Ability implements INBTSerializable<NBTTagCompound> {

	protected Abilities parent;
	
	public Ability(Abilities parent) {
		this.parent = parent;
		parent.abilities.add(this);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public Ability deregisterFromEventBus() {
		MinecraftForge.EVENT_BUS.unregister(this);
		return this;
	}
	

}
