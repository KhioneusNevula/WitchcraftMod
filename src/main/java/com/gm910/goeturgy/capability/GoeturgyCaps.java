package com.gm910.goeturgy.capability;

import com.gm910.goeturgy.abilities.Abilities;
import com.gm910.goeturgy.abilities.IEntityAbilities;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class GoeturgyCaps<T, K> implements ICapabilitySerializable<NBTBase> {

	@CapabilityInject(IEntityAbilities.class)
	public static final Capability<IEntityAbilities> EN_CAP = null;
	
	private T instance;
	
	private Capability<T> capability;
	
	private K owner;
	
	@SuppressWarnings("unchecked")
	public GoeturgyCaps(Capability<T> cap, K owner) {
		capability = cap;
		instance = cap.getDefaultInstance();
		if (instance instanceof ICapabilityWithOwner) {
			((ICapabilityWithOwner<K>)instance).setOwner(owner);
		}
		this.owner = owner;
	}
	
	public K getOwner() {
		return owner;
	}
	
	public T getInstance() {
		return instance;
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		// TODO Auto-generated method stub
		return capability == this.capability;
	}

	@Override
	public <M> M getCapability(Capability<M> capability, EnumFacing facing) {
		// TODO Auto-generated method stub
		return capability == this.capability ? this.capability.<M> cast(this.instance) : null;
	}

	@Override
	public NBTBase serializeNBT() {
		// TODO Auto-generated method stub
		return this.capability.getStorage().writeNBT(this.capability, this.instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		// TODO Auto-generated method stub
		this.capability.getStorage().readNBT(this.capability, this.instance, null, nbt);
	}
	
	public static Abilities getAbilities(Entity e) {
		return e.getCapability(EN_CAP, null).getAbilities();
	}
	
}
