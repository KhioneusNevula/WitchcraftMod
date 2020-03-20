package com.gm910.gmwitchcraft.capabilities;

import com.gm910.gmwitchcraft.Reference;
import com.gm910.gmwitchcraft.network.networkcap.INetworkData;
import com.gm910.gmwitchcraft.network.networkcap.MessageProvider;
import com.gm910.gmwitchcraft.network.networkcap.NetworkData;
import com.gm910.gmwitchcraft.network.networkcap.NetworkDataStorage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class CapabilityHandler {

	public static void player(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof EntityPlayer) {
			event.addCapability(new ResourceLocation(Reference.MODID, "cap_networkdata"), new MessageProvider());
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void preInit() {
		CapabilityManager.INSTANCE.register(INetworkData.class, new NetworkDataStorage(), NetworkData.class);
	}
	
	@SubscribeEvent
	public static void capClone(PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			INetworkData cap = event.getOriginal().getCapability(MessageProvider.NET_CAP, null);
			INetworkData newCap = event.getEntityPlayer().getCapability(MessageProvider.NET_CAP, null);
			newCap.readMessages();
			cap.copyTo(newCap);
		}
	}
}
