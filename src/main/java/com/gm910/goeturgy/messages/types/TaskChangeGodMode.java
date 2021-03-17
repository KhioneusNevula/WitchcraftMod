package com.gm910.goeturgy.messages.types;

import java.util.Collections;
import java.util.UUID;

import com.gm910.goeturgy.Goeturgy;
import com.gm910.goeturgy.events.ServerSideKeyEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;

/**
 * Use from server to client
 * @author borah
 *
 */
public class TaskChangeGodMode implements IRunnableTask {

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1321056234072889571L;
	public boolean in;
	
	public TaskChangeGodMode(boolean in) {
		this.in = in;
	}
	
	public TaskChangeGodMode() {
		in = false;
	}
	
	@Override
	public void run() {
		System.out.println((!in ? "Not i" : "I") + "n god mode");
		Goeturgy.proxy.getAsClientProxy().isGodMode = in;
	}
	

	@Override
	public void writeToNBT(NBTTagCompound comp) {
		comp.setBoolean("In", in);
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		this.in = comp.getBoolean("In");
	}

}
