package com.gm910.goeturgy.tileentities;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ISyncableTile {
	public default void sync() {
		getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
		getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
		getWorld().scheduleBlockUpdate(getPos(), getBlockType(),0,0);
		updateContainingBlockInfo();
		
		markDirty();
	}

	public World getWorld();

	public Block getBlockType();

	public void updateContainingBlockInfo();

	public void markDirty();

	public default NBTTagCompound getUpdateTag() {
		// TODO Auto-generated method stub
		return writeToNBT(new NBTTagCompound());
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound);

	public default SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), 0, this.getUpdateTag());
	}
	
	public BlockPos getPos();

	public default void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	public default void handleUpdateTag(NBTTagCompound tag) {
		// TODO Auto-generated method stub
		readFromNBT(tag);
	}

	public void readFromNBT(NBTTagCompound tag);
}
