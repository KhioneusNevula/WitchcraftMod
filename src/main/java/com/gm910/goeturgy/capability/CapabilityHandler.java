package com.gm910.goeturgy.capability;

import com.gm910.goeturgy.Goeturgy;
import com.gm910.goeturgy.abilities.EntityAbilities;
import com.gm910.goeturgy.abilities.EntityAbilityStorage;
import com.gm910.goeturgy.abilities.IEntityAbilities;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class CapabilityHandler {
	
	public static final ResourceLocation EN_CAP = new ResourceLocation(Goeturgy.MODID, "_entityabilities");
	
	public static void preInit() {
		CapabilityManager.INSTANCE.register(IEntityAbilities.class, new EntityAbilityStorage(), () -> {
			
			return new EntityAbilities(FMLCommonHandler.instance().getMinecraftServerInstance());
		});
	}
	
	@SubscribeEvent
	public static void attachEntity(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject().world.isRemote) return;
		event.addCapability(EN_CAP, new GoeturgyCaps<>(GoeturgyCaps.EN_CAP, event.getObject()));
	}
	
	@SubscribeEvent
	public static void onClone(Clone event) {
		if (event.getEntity().world.isRemote) return;
		IEntityAbilities instance1 = event.getOriginal().getCapability(GoeturgyCaps.EN_CAP, null);
		IEntityAbilities instance2 = event.getEntityPlayer().getCapability(GoeturgyCaps.EN_CAP, null);
		instance2.readFromNBT(instance1.writeToNBT(new NBTTagCompound()));
	}
}
