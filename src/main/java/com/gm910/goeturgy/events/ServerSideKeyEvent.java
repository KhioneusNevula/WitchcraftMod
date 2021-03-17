package com.gm910.goeturgy.events;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ServerSideKeyEvent extends Event {

	public final int key;
	
	public final UUID player;
	
	public ServerSideKeyEvent(int key, UUID player) {
		this.key = key;
		this.player = player;
	}
	
	public EntityPlayer getEntity() {
		return (EntityPlayer) FMLCommonHandler.instance().getMinecraftServerInstance().getEntityFromUuid(player);
	}

}
