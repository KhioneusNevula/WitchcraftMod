package com.gm910.goeturgy.messages.types;

import java.io.Serializable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraftforge.common.util.INBTSerializable;

public interface IRunnableTask extends Serializable, Runnable {

	public void run();
	
}
