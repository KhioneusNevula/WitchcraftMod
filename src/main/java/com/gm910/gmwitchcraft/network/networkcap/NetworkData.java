package com.gm910.gmwitchcraft.network.networkcap;

import java.util.ArrayList;
import java.util.function.Predicate;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;

public class NetworkData implements INetworkData {
	
	public ArrayList<String> messages = new ArrayList<String>();
	
	@Override
	public void sendMessage(String message) {
		messages.add(message);
	}

	@Override
	public ArrayList<String> readMessages() {
		ArrayList<String> messes = new ArrayList<String>(messages);
		messages.clear();
		return messes;
	}

	@Override
	public boolean hasMessage() {
		
		return messages.size() > 0;
	}

	@Override
	public String readMessage() {
		if (messages.size() == 0) return "";
		return messages.remove(0);
	}

	@Override
	public ArrayList<String> sneakReadMessages() {
		
		return new ArrayList<String>(messages);
	}

	@Override
	public void copyTo(INetworkData other) {
		if (hasMessage()) {
			for (String mes : this.messages) {
				other.sendMessage(mes);
			}
		}
	}

	
}
