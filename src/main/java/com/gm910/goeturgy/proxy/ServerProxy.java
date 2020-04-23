package com.gm910.goeturgy.proxy;

import com.gm910.goeturgy.messages.Messages;
import com.gm910.goeturgy.messages.Messages.StringMessage;
import com.gm910.goeturgy.messages.Messages.ServerMessageHandler;

import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class ServerProxy implements IProxy {

	public void registerItemRenderer(Item item, int meta, String id) {}
	
	public MinecraftServer getServer() {
		return FMLCommonHandler.instance().getMinecraftServerInstance();
	}
	
	@Override
	public World getWorld(int d) {
		// TODO Auto-generated method stub
		return this.getServer().getWorld(d);
	}
	
	@Override
	public void preInit() {
		Messages.INSTANCE.registerMessage(ServerMessageHandler.class, StringMessage.class, Messages.returnNewID(), Side.SERVER);
	}
}
