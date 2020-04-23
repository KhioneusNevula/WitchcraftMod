package com.gm910.goeturgy.spells.ioflow;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.gm910.goeturgy.util.ServerPos;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Immutable
public class BlockStack {
	private IBlockState block;
	private NBTTagCompound tileData = new NBTTagCompound();
	public BlockStack(IBlockState state, @Nullable NBTTagCompound tile) {
		this.block = state;
		this.tileData = tile == null ? tileData : tile;
	}
	
	public BlockStack(IBlockState state, TileEntity te) {
		this(state, te != null ? te.serializeNBT() : null);
	}
	
	
	public BlockStack(IBlockState state, BlockPos pos, World world) {
		this(state,world.getTileEntity(pos));
	}
	
	public BlockStack(IBlockState state, ServerPos pos) {
		this(state, pos.getWorld().getTileEntity(pos));
	}
	
	public BlockStack(IBlockState state) {
		this(state, (NBTTagCompound) null);
	}
	
	public BlockStack(BlockPos pos, World world) {
		this(world.getBlockState(pos), world.getTileEntity(pos));
	}
	
	public BlockStack(ServerPos pos) {
		this(pos.getPos(), pos.getWorld());
	}
	
	public IBlockState getBlock() {
		return block;
	}
	
	
	public TileEntity getGeneratedTile(World w) {
		return getGeneratedTile(w, null);
	}
	
	public TileEntity getGeneratedTile(World w, BlockPos pos) {
		TileEntity te;
		if (this.tileData.hasNoTags()) {
			te = this.block.getBlock().createTileEntity(w, block);
		} else {
			te = TileEntity.create(w, this.tileData);
		}
		
		if (pos != null && te != null) {
			te.setPos(pos);
		}
		return te;
	}
	
	public TileEntity getGeneratedTile(ServerPos pos) {
		return getGeneratedTile(pos.getWorld(), pos.getPos());
	}
	
	public NBTTagCompound getTileData() {
		return tileData;
	}
	
	public BlockStack setBlock(IBlockState block) {
		return new BlockStack(block, this.tileData);
	}
	
	public void setInWorld(ServerPos pos) {
		setInWorld(pos.getPos(), pos.getWorld());
	}
	
	public void setInWorld(BlockPos pos, World world) {
		world.setBlockState(pos, this.block);
		world.setTileEntity(pos, this.getGeneratedTile(world, pos));
	}
	
	
	public BlockStack setTileData(NBTTagCompound tileData) {
		return new BlockStack(this.block, tileData);
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound com) {
		com.setTag("State", NBTUtil.writeBlockState(new NBTTagCompound(), block));
		com.setTag("Data", this.tileData);
		
		return com;
	}
	
	public static BlockStack fromNBT(NBTTagCompound cmp) {
		if (cmp == null) return null;
		if (!cmp.hasKey("State")) return null;
		return new BlockStack(NBTUtil.readBlockState(cmp.getCompoundTag("State")), cmp.getCompoundTag("Data"));
	}
	
}