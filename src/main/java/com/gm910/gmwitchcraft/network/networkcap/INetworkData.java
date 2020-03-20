package com.gm910.gmwitchcraft.network.networkcap;

import java.util.ArrayList;
import java.util.function.Predicate;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;

public interface INetworkData {
	/**
	 * adds message to queue
	 * @param message
	 */
	public void sendMessage(String message);
	/**
	 * reads oldest message and deletes from queue
	 * @return
	 */
	public String readMessage();
	/**
	 * Reads all messages on queue and deletes them
	 * @return
	 */
	public ArrayList<String> readMessages();
	/**
	 * Reads all messages on queue without deleting them
	 * @return
	 */
	public ArrayList<String> sneakReadMessages();
	/**
	 * Whether there are messages on queue
	 * @return
	 */
	public boolean hasMessage();
	
	public void copyTo(INetworkData other);
}
