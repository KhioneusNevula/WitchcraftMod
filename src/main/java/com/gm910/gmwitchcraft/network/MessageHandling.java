package com.gm910.gmwitchcraft.network;

import java.util.UUID;

import com.gm910.gmwitchcraft.network.Networking.NetActions;
import com.gm910.gmwitchcraft.network.networkcap.MessageProvider;
import com.gm910.gmwitchcraft.teleport.Teleport;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
@EventBusSubscriber
public class MessageHandling {

	@SubscribeEvent
	public static void getMessage(LivingUpdateEvent event) {
		if (event.getEntity() instanceof EntityPlayerMP) {
			boolean hm = event.getEntityLiving().getCapability(MessageProvider.NET_CAP, null).hasMessage();
			if (hm) {
				String message = event.getEntityLiving().getCapability(MessageProvider.NET_CAP, null).readMessage();
				execMSG((EntityPlayerMP)event.getEntityLiving(), message);
			}
		}
	}
	
	
	public static MinecraftServer server() {
		return FMLCommonHandler.instance().getMinecraftServerInstance();
	}

	public static void execMSG(EntityPlayerMP player, String message) {
		if (message.length() == 0) return;
		String[] parts = message.split(" ");
		String key = parts[0];
		if (NetActions.CHANGEITEM.equals(key)) {
			give(parts);
		} 
		else if (NetActions.POSITION.equals(key)) {
			position(parts);
		} 
		else if (NetActions.DIMENSION.equals(key)) {
			dimension(parts);
		}
		else if (NetActions.BLOCKCHANGE.equals(key)) {
			blockchange(parts);
		}
		else if (NetActions.SYNCENTITY.equals(key)) {
			syncEntity(parts);
		}
	}
	
	public static void give(String[] parts) {
		Entity en = server().getEntityFromUuid(UUID.fromString(parts[1]));
		int slot = Integer.parseInt(parts[2]);
		NBTTagCompound stacknbt = new NBTTagCompound();
		if (!parts[6].equalsIgnoreCase("none")) {
			try {
				stacknbt = JsonToNBT.getTagFromJson(parts[6]);
			} catch (NBTException e) {
				System.out.println("Give message with faulty NBT");
			}
		}
		ItemStack stack = new ItemStack(stacknbt);
		
		if (slot == -1) {
			en.world.spawnEntity(new EntityItem(en.world, en.posX, en.posY, en.posZ, stack));
		} else {
			en.replaceItemInInventory(slot, stack);
		}
	}
	
	public static void position(String[] parts) {
		Entity en = server().getEntityFromUuid(UUID.fromString(parts[1]));
		double x = Double.parseDouble(parts[2]);
		double y = Double.parseDouble(parts[3]);
		double z = Double.parseDouble(parts[4]);
		float pitch = Float.parseFloat(parts[5]);
		float yaw = Float.parseFloat(parts[6]);
		en.setPositionAndRotation(x, y, z, pitch, yaw);
	}
	
	public static void dimension(String[] parts) {
		Entity en = server().getEntityFromUuid(UUID.fromString(parts[1]));
		double x = Double.parseDouble(parts[2]);
		double y = Double.parseDouble(parts[3]);
		double z = Double.parseDouble(parts[4]);
		int d = Integer.parseInt(parts[5]);
		
		Teleport.teleportToDimension(en, d, x, y, z);
	}

	public static void blockchange(String[] parts) {
		double x = Integer.parseInt(parts[1]);
		double y = Integer.parseInt(parts[2]);
		double z = Integer.parseInt(parts[3]);
		int d = Integer.parseInt(parts[4]);
		Block block = Block.getBlockFromName(parts[5]);
		int meta = Integer.parseInt(parts[5]);
		WorldServer world = server().getWorld(d);
		IBlockState state = block.getStateFromMeta(meta);
		world.setBlockState(new BlockPos(x, y, z), state);
		if (!parts[6].equalsIgnoreCase("none")) {
			try {
				world.setTileEntity(new BlockPos(x, y, z), TileEntity.create(world, JsonToNBT.getTagFromJson(parts[6])));
			} catch (NBTException e) {
				// TODO Auto-generated catch block
				System.out.println("Malformed blockchange message");
			}
		}
	}
	
	public static void syncEntity(String[] parts) {
		Entity en = server().getEntityFromUuid(UUID.fromString(parts[1]));
		try {
			en.deserializeNBT(JsonToNBT.getTagFromJson(parts[2]));
		} catch (NBTException e) {
			System.out.println("Malformed syncentity message");
		}
	}
	
}
