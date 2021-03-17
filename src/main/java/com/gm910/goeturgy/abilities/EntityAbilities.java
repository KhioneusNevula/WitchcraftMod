package com.gm910.goeturgy.abilities;

import com.gm910.goeturgy.capability.ICapabilityWithOwner;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;

public class EntityAbilities implements IEntityAbilities, ICapabilityWithOwner<Entity> {

	public MinecraftServer server;
	
	public Entity entity;
	
	public Abilities abilities;
	
	public EntityAbilities(MinecraftServer server) {
		this.server = server;
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void setOwner(Entity newe) {
		this.entity = newe;
		this.abilities = new Abilities(entity, this, server);
	}
	
	public Entity getOwner() {
		return entity;
	}
	
	public void onFinish() {
		MinecraftForge.EVENT_BUS.unregister(this);
	
	}
	
	public void setAbilities(Abilities a) {
		this.abilities = a;
	}
	
	public Abilities getAbilities() {
		return abilities;
	}
	
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		
			System.out.println("Reading ability " + nbt + " for " + this.entity);
			Abilities a = new Abilities(entity, this, server);
			a.deserializeNBT(nbt.getCompoundTag("Data"));
			this.abilities = a;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		
		compound.setTag("Data", abilities.serializeNBT());
		return compound;
	}
	
	
}
