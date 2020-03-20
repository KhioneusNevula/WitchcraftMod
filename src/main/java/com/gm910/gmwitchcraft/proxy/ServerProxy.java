package com.gm910.gmwitchcraft.proxy;

import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class ServerProxy implements IProxy {

	@Override
	public void registerItemRenderer(Item item, int meta, String id) {

	}

	@Override
	public void registerVariantRenderer(Item item, int meta, String filename, String id) {

	}

	@Override
	public Side getSide() {
		return Side.SERVER;
	}
	
	@Override
	public MinecraftServer getServer() {
		// TODO Auto-generated method stub
		return FMLCommonHandler.instance().getMinecraftServerInstance().getServer();
	}

}
