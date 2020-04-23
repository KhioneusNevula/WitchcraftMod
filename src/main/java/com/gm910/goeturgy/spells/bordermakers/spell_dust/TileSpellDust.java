package com.gm910.goeturgy.spells.bordermakers.spell_dust;

import java.io.Serializable;

import com.gm910.goeturgy.messages.Messages;
import com.gm910.goeturgy.spells.spellspaces.ClientSpellSpace;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace;
import com.gm910.goeturgy.spells.spellspaces.SpellSpaces;
import com.gm910.goeturgy.spells.util.ISpellBorder;
import com.gm910.goeturgy.tileentities.TileEntityBaseTickable;
import com.gm910.goeturgy.util.GMNBT;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

public class TileSpellDust extends TileEntityBaseTickable implements ISpellBorder {

	protected boolean isSolid;
	
	protected long figure = -1;
	protected BlockPos head = null;
	
	protected ClientSpellSpace clientSpace = null;
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound update = super.getUpdateTag();
		update.setTag("Spsp", GMNBT.makeList(SpellSpace.runClients, (e) -> new NBTTagString(Messages.serialize((Runnable & Serializable)e))));
		if (getSpellSpace() != null) {
			update.setTag("SpellSpace", this.getSpellSpace().serializeNBT());
		} else {
			System.out.println("No spellspace");
		}
		System.out.println("Sendin' updates yo");
		return update;
	}
	
	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		super.handleUpdateTag(tag);
		if (tag.hasKey("SpellSpace")) {
			this.clientSpace = new ClientSpellSpace(tag.getCompoundTag("SpellSpace"));
		} else {
			this.clientSpace = null;
		}
		
	}
	
	public SpellSpace getClientSpace() {
		return clientSpace;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		// TODO Auto-generated method stub
		NBTTagCompound cmp = super.writeToNBT(compound);
		cmp.setLong("Figure", figure);
		if (head != null) cmp.setTag("Head", NBTUtil.createPosTag(head));
		return cmp;
	}
	
	public SpellSpace getSpellSpace() {
		if (world.isRemote) return clientSpace;
		return SpellSpaces.get().getById(figure);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		// TODO Auto-generated method stub
		super.readFromNBT(compound);
		if (compound.hasKey("Head")) this.head = NBTUtil.getPosFromTag(compound.getCompoundTag("Head"));
		this.figure = compound.getLong("Figure");
	}
	
	public BlockPos getHead() {
		return head;
	}
	
	public boolean hasHead() {
		return head != null;
	}

	@Override
	public long getSpaceID() {
		// TODO Auto-generated method stub
		return figure;
	}

	@Override
	public void setSpellSpace(SpellSpace space) {
		this.figure = space.getID();
		this.head = space.getHeadPos();
		System.out.println("Set spell space to " + space.getID());
	}

	@Override
	public void setSpaceID(long id) {
		this.figure = id;
		System.out.println("Set spell space to " + id);
	}

	@Override
	public void setSolid(boolean solid) {
		this.isSolid = solid;
	}

	@Override
	public boolean isSolid() {
		// TODO Auto-generated method stub
		return isSolid;
	}
	
	@Override
	public void update() {
		
		super.update();
	}
	
}
