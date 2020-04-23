package com.gm910.goeturgy.tileentities;

import com.gm910.goeturgy.spells.ioflow.MagicIO;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Includes useful input and output list for spellspace magics and stuff
 * @author borah
 *
 */
public abstract class TileEntityBase extends TileEntity {
	
	protected Vec3i offset = new Vec3i(0,0,0);
	protected ServerPos delegatePos = null;

	protected NonNullMap<EnumFacing, NBTTagCompound> inputs = new NonNullMap<EnumFacing, NBTTagCompound>(NBTTagCompound::new);
	protected NonNullMap<EnumFacing, NBTTagCompound> outputs = new NonNullMap<EnumFacing, NBTTagCompound>(NBTTagCompound::new);
	
	
	@Override
	public void onLoad() {
		// TODO Auto-generated method stub
		super.onLoad();
		delegatePos = new ServerPos(pos, world);
	}
	
	public void sync() {
		world.markBlockRangeForRenderUpdate(pos, pos);
		world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
		world.scheduleBlockUpdate(pos,this.getBlockType(),0,0);
		this.updateContainingBlockInfo();
		
		markDirty();
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		// TODO Auto-generated method stub
		return this.writeToNBT(new NBTTagCompound());
	}
	
	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		// TODO Auto-generated method stub
		super.handleUpdateTag(tag);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		
		NBTTagCompound cmp = super.writeToNBT(compound);
		cmp.setTag("Inp", MagicIO.nonNullMapToNBT(inputs));
		cmp.setTag("Outp", MagicIO.nonNullMapToNBT(outputs));
		return cmp;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		inputs = MagicIO.nonNullMapFromNBT(compound.getTagList("Inp", NBT.TAG_COMPOUND));
		outputs = MagicIO.nonNullMapFromNBT(compound.getTagList("Outp", NBT.TAG_COMPOUND));
		super.readFromNBT(compound);
	}
	
	public Vec3i getOffset() {
		return offset;
	}
	
	/**
	 * if magic is cast to a diff location
	 * @return
	 */
	public ServerPos getDelegatedPos() {
		return this.delegatePos;
	}
	
	public void offset(Vec3i offset) {
		this.offset = offset;
	}
	
	public void setDelegatedPos(ServerPos delegatePos) {
		this.delegatePos = delegatePos;
	}
	
	public void resetInputs() {
		this.inputs.clear();
	}
	
	public void resetOutputs() {
		this.outputs.clear();
	}
	
}
