package com.gm910.goeturgy.proxy;

import com.gm910.goeturgy.init.TileInit;
import com.gm910.goeturgy.messages.Messages;
import com.gm910.goeturgy.messages.Messages.ClientMessageHandler;
import com.gm910.goeturgy.messages.Messages.IRunnableTaskMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class ClientProxy implements IProxy {

	@Override
	public void registerItemRenderer(Item item, int meta, String id) {
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), id));
	}
	
	@Override
	public MinecraftServer getServer() {
		// TODO Auto-generated method stub
		return FMLCommonHandler.instance().getMinecraftServerInstance();
	}

	@Override
	public World getWorld(int d) {
		// TODO Auto-generated method stub
		return Minecraft.getMinecraft().world != null ? (Minecraft.getMinecraft().world.provider.getDimension() == d ? Minecraft.getMinecraft().world : null) : null;
	}
	
	@Override
	public void preInit() {
		// TODO Auto-generated method stub
		Messages.INSTANCE.registerMessage(ClientMessageHandler.class, IRunnableTaskMessage.class, Messages.returnNewID(), Side.CLIENT);
		TileInit.registerSpecialRenderers();
	}
	
}
