package com.gm910.goeturgy.tileentities;

import com.gm910.goeturgy.spells.ioflow.MagicIO;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace;
import com.gm910.goeturgy.spells.spellspaces.SpellSpaces;
import com.gm910.goeturgy.util.NonNullMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Includes useful input and output list for spellspace magics and stuff
 * @author borah
 *
 */
public abstract class TileEntityBase extends TileEntity {
	

	protected NonNullMap<EnumFacing, NBTTagCompound> inputs = new NonNullMap<EnumFacing, NBTTagCompound>(NBTTagCompound::new);
	protected NonNullMap<EnumFacing, NBTTagCompound> outputs = new NonNullMap<EnumFacing, NBTTagCompound>(NBTTagCompound::new);
	protected long figure;
	
	@Override
	public void onLoad() {
		// TODO Auto-generated method stub
		super.onLoad();
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
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, this.getUpdateTag());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		
		super.onDataPacket(net, pkt);
		this.handleUpdateTag(pkt.getNbtCompound());
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
		cmp.setLong("Figure", this.figure);
		return cmp;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		inputs = MagicIO.nonNullMapFromNBT(compound.getTagList("Inp", NBT.TAG_COMPOUND));
		outputs = MagicIO.nonNullMapFromNBT(compound.getTagList("Outp", NBT.TAG_COMPOUND));
		this.figure = compound.getLong("Figure");
		super.readFromNBT(compound);
	}
	
	public void setSpaceID(long id) {
		this.figure = id;
	}
	public SpellSpace getSpellSpace() {
		return SpellSpaces.get().getById(figure);
	}
	public long getSpaceID() {
		return figure;
	}
	
	
	
	public void resetInputs() {
		this.inputs.clear();
	}
	
	public void resetOutputs() {
		this.outputs.clear();
	}
	
}
