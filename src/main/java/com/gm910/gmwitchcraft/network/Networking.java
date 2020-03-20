package com.gm910.gmwitchcraft.network;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Networking {
	
	public static enum NetActions {
		CHANGEITEM,
		POSITION,
		DIMENSION,
		BLOCKCHANGE,
		SYNCENTITY
		;
		
		
		public boolean equals(String o) {
				return o.equalsIgnoreCase(this.toString());
		}
	}
	
	public static void sendMSG(Object...msg) {
		StringBuilder str = new StringBuilder();
		for (Object o : msg) {
			str.append(o + " ");
		}
		WitchNetworkHandler.INSTANCE.sendToServer(new WitchNetworkHandler.WitchMessage(str.toString().trim()));
	}
	
	public static void give(Entity entity, ItemStack stack) {
		changeItem(entity, stack, -1);
	}
	
	public static void changeItem(Entity entity, ItemStack stack, int slot) {
		sendMSG(NetActions.CHANGEITEM, entity.getUniqueID(), slot, stack.serializeNBT());
	}
	
	public static void moveTo(Entity entity, double x, double y, double z, float pitch, float yaw) {
		sendMSG(NetActions.POSITION, entity.getUniqueID(), x, y, z, pitch, yaw);
	}
	
	public static void moveTo(Entity entity, double x, double y, double z) {
		moveTo(entity, x, y, z, entity.rotationPitch, entity.rotationYaw);
	}
	
	public static void dimension(Entity entity, double x, double y, double z, int d) {
		sendMSG(NetActions.DIMENSION, entity.getUniqueID(), x, y, z, d);
	}
	
	public static void dimension(Entity entity, int d) {
		dimension(entity, entity.posX, entity.posY, entity.posZ, d);
	}

	public static void blockChange(int x, int y, int z, int d, IBlockState state, @Nullable TileEntity te) {
		sendMSG(x, y, z, d, state.getBlock().getRegistryName().toString(), state.getBlock().getMetaFromState(state), te != null ? te.serializeNBT().toString() : "none");
	}
	
	public static void syncEntity(Entity toSync) {
		sendMSG(toSync.getUniqueID(), toSync.serializeNBT().toString());
	}
	
}
