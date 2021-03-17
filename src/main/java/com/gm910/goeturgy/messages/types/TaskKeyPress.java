package com.gm910.goeturgy.messages.types;

import java.util.Collections;
import java.util.UUID;

import org.lwjgl.input.Keyboard;

import com.gm910.goeturgy.Goeturgy;
import com.gm910.goeturgy.events.ServerSideKeyEvent;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;

/**
 * Use from client to server
 * @author borah
 *
 */
public class TaskKeyPress implements IRunnableTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4055757593825299620L;
	
	protected int key;
	
	protected UUID player;
	
	public TaskKeyPress(UUID player, int key) {
		this.key = key;
		this.player = player;
	}
	
	public TaskKeyPress() {}

	public int getKey() {
		return key;
	}
	
	public UUID getPlayer() {
		return player;
	}
	
	@Override
	public void run() {
		System.out.println("Key " + Keyboard.getKeyName(key) + " pressed");
		MinecraftForge.EVENT_BUS.post(new ServerSideKeyEvent(key, player));
	}
	

	@Override
	public void writeToNBT(NBTTagCompound comp) {
		comp.setInteger("Key", this.key);
		comp.setUniqueId("Player", this.player);
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		this.key = comp.getInteger("Key");
		this.player = comp.getUniqueId("Player");
	}

}
