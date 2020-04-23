package com.gm910.goeturgy.spells.spellspaces;

import java.util.List;

import com.gm910.goeturgy.spells.util.AetherStorage;
import com.gm910.goeturgy.util.GMNBT;
import com.gm910.goeturgy.util.NonNullMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

public class ClientSpellSpace extends SpellSpace {

	public ClientSpellSpace(int dimension) {
		super(dimension);
		// TODO Auto-generated constructor stub
	}
	
	public ClientSpellSpace(NBTTagCompound comp) {
		super(comp);
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		this.dimension = nbt.getInteger("Dimension");
		this.shape = GMNBT.createList(nbt.getTagList("Shape", NBT.TAG_COMPOUND), (b) ->  NBTUtil.getPosFromTag((NBTTagCompound)b) );
		this.edges = GMNBT.createList(nbt.getTagList("Edges", NBT.TAG_COMPOUND), (b) ->  NBTUtil.getPosFromTag((NBTTagCompound)b) );
		this.id = nbt.getLong("Id");
		this.headPos = NBTUtil.getPosFromTag(nbt.getCompoundTag("Head"));
		this.ticks = nbt.getInteger("Ticks");
		this.height = nbt.getInteger("Height");
		
		(nbt.getTagList("SigPoints", NBT.TAG_COMPOUND)).forEach((b) -> {
			NBTTagCompound cmp = (NBTTagCompound)b;
			sigPoints.put(NBTUtil.getPosFromTag(cmp.getCompoundTag("Pos")), cmp.getCompoundTag("State"));
		});
	}
	
	@Override
	public void activateAndDisseminateOutput(BlockPos pos, boolean starter, NonNullMap<EnumFacing, NBTTagCompound> inputs, List<BlockPos> pp) {}
	@Override
	public void decrementPower(int amount) {}
	@Override
	public void disseminateStaticOutput(BlockPos pos, boolean vertical) {}
	@Override
	public void end(boolean s, BlockPos p) {}
	
	@Override
	public int extractPower(int power) {return 0;}
	
	@Override
	public MinecraftServer getServer() { return null;}
	
	@Override
	public int getPower() { return 0;}
	
	@Override
	public void init(long id, int height, BlockPos head, List<BlockPos> edges, List<BlockPos> sigPoints) {}
	
}
