package com.gm910.goeturgy.util;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class ServerPos extends BlockPos {
	
	public int d;
	public String name = "";
	
	public ServerPos() {
		this(0,0,0,0);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return (name.equals("") ? "" : (name + " -> ")) + "[" + this.getX() + ", " + this.getY() + ", " + this.getZ() + ", D=" + this.d + "]";
	}
	
	public ServerPos(Vec3i source, int dimension) {
		super(source);
		setDim(dimension);
	}
	
	private ServerPos from(BlockPos pos) {
		return new ServerPos(pos, d);
	}

	public ServerPos sAdd(double x, double y, double z) {
		// TODO Auto-generated method stub
		return from(super.add(x, y, z));
	}
	
	public ServerPos sAdd(int x, int y, int z) {
		// TODO Auto-generated method stub
		return from(super.add(x, y, z));
	}
	
	public ServerPos sAdd(Vec3i vec) {
		// TODO Auto-generated method stub
		return from(super.add(vec));
	}
	
	public ServerPos sDown(int n) {
		// TODO Auto-generated method stub
		return from(super.down(n));
	}
	
	public ServerPos sUp(int n) {
		// TODO Auto-generated method stub
		return from(super.up(n));
	}
	
	public ServerPos sNorth(int n) {
		// TODO Auto-generated method stub
		return from(super.north(n));
	}
	
	public ServerPos sSouth(int n) {
		// TODO Auto-generated method stub
		return from(super.south(n));
	}
	
	public ServerPos sEast(int n) {
		// TODO Auto-generated method stub
		return from(super.east(n));
	}
	
	public ServerPos sWest(int n) {
		// TODO Auto-generated method stub
		return from(super.west(n));
	}
	
	public ServerPos sDown() {
		// TODO Auto-generated method stub
		return from(super.down());
	}
	
	public ServerPos sUp() {
		// TODO Auto-generated method stub
		return from(super.up());
	}
	
	public ServerPos sNorth() {
		// TODO Auto-generated method stub
		return from(super.north());
	}
	
	public ServerPos sSouth() {
		// TODO Auto-generated method stub
		return from(super.south());
	}
	
	public ServerPos sEast() {
		// TODO Auto-generated method stub
		return from(super.east());
	}
	
	public ServerPos sWest() {
		// TODO Auto-generated method stub
		return from(super.west());
	}
	
	public ServerPos(ServerPos pos, String name) {
		this(pos, pos.getD());
		this.name = name;
	}
	
	public ServerPos setName(String name) {
		return new ServerPos(this, name);
	}
	
	public String getName() {
		return name;
	}
	
	public ServerPos(BlockPos pos, int dimension) {
		super(pos);
		setDim(dimension);
	}
	
	public ServerPos(BlockPos pos) {
		this(pos, pos instanceof ServerPos ? ((ServerPos)pos).d : 0);
	}
	
	public ServerPos(BlockPos pos, World dimension) {
		this(pos, dimension.provider.getDimension());
	}
	
	public ServerPos(int x, int y, int z, World dimension) {
		this(x, y, z, dimension.provider.getDimension());
	}
	
	private void setDim(int d) {
		this.d = d;
	}
	
	public World getWorld() {
		if (FMLCommonHandler.instance() == null) return null;
		if (FMLCommonHandler.instance().getMinecraftServerInstance() == null) return null;
		return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(d);
	}
	
	public ServerPos setD(int d) {
		return new ServerPos(this, d);
	}
	
	public int getD() {
		return d;
	}

	public ServerPos(Vec3d vec, int d) {
		super(vec);
		setDim(d);
	}

	public ServerPos(int x, int y, int z, int d) {
		super(x, y, z);
		setDim(d);
	}

	public ServerPos(Entity source) {
		super(source);
		setDim(source.dimension);
		
	}
	public ServerPos(double x, double y, double z, int d) {
		super(x, y, z);
		setDim(d);
	}
	
	public BlockPos getPos() {
		return new BlockPos(this);
	}
	
	@Override
	public boolean equals(Object p_equals_1_) {
		if (p_equals_1_ instanceof BlockPos) {
			return this.getPos().equals(p_equals_1_);
		} else if (p_equals_1_ instanceof ServerPos) {
			return this.getPos().equals(p_equals_1_) && this.d == ((ServerPos) p_equals_1_).getD();
		}
		return super.equals(p_equals_1_);
	}
	
	public boolean equalsWithName(Object p_equals_1_) {
		if (p_equals_1_ instanceof ServerPos) {
			return this.equals(p_equals_1_) && this.name.equals(((ServerPos) p_equals_1_).getName());
		} else {
			return this.equals(p_equals_1_);
		}
	}

	public NBTTagCompound toNBT(NBTTagCompound comp) {
		
		return ServerPos.toNBT(comp, this);
	}
	
	public NBTTagCompound toNBT() {
		return this.toNBT(new NBTTagCompound());
	}
	
	public static BlockPos bpFromNBT(NBTTagCompound comp) {
		if (!comp.hasKey("D")) {
			return new BlockPos(comp.getInteger("X"), 
				comp.getInteger("Y"), comp.getInteger("Z"));
		}
		return new ServerPos(new ServerPos(comp.getInteger("X"), 
				comp.getInteger("Y"), comp.getInteger("Z"), comp.getInteger("D")), comp.getString("Name"));
	}
	
	public static ServerPos fromNBT(NBTTagCompound comp) {
		return new ServerPos(new ServerPos(comp.getInteger("X"), 
				comp.getInteger("Y"), comp.getInteger("Z"), comp.getInteger("D")), comp.getString("Name"));
	}

	public static NBTTagCompound toNBT(NBTTagCompound comp, BlockPos pos) {
		comp.setInteger("X", pos.getX());
		comp.setInteger("Y", pos.getY());
		comp.setInteger("Z", pos.getZ());
		if (pos instanceof ServerPos) {
			 comp.setInteger("D", ((ServerPos) pos).getD());
			 comp.setString("Name", ((ServerPos) pos).getName());
		}
		return comp;
	}
	
	public static NBTTagCompound toNBT(BlockPos pos) {
		return toNBT(new NBTTagCompound(), pos);
	}
	
}
