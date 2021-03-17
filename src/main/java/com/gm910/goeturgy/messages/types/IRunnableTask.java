package com.gm910.goeturgy.messages.types;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.gm910.goeturgy.util.GMReflection;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface IRunnableTask extends Serializable, INBTSerializable<NBTTagCompound> {

	public void run();
	
	public default NBTTagCompound serializeNBT() {
		NBTTagCompound comp = new NBTTagCompound();
		comp.setString("Class", this.getClass().getName());
		NBTTagCompound cmp = new NBTTagCompound();
		writeToNBT(cmp);
		comp.setTag("Data", cmp);
		return comp;
	}
	
	public default void deserializeNBT(NBTTagCompound nbt) {
		this.readFromNBT(nbt.getCompoundTag("Data"));
	}
	
	public static IRunnableTask getFromNBT(NBTTagCompound comp) {
		Class<? extends IRunnableTask> clazz = null;
		try {
			clazz = (Class<? extends IRunnableTask>) Class.forName(comp.getString("Class"));
		} catch (ClassNotFoundException | ClassCastException e) {
			System.out.println("Problem getting message threw " + e + ": " + e.getMessage());
			return null;
		}
		IRunnableTask tasque = GMReflection.construct(clazz);
		if (tasque == null) {
			System.out.println("Problem constructing message from class " + clazz);
			return null;
		}
		tasque.deserializeNBT(comp);
		return tasque;
	}
	
	public void writeToNBT(NBTTagCompound comp);
	public void readFromNBT(NBTTagCompound comp);
	
}
