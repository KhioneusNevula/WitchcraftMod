package com.gm910.goeturgy.spells.misc;

import com.gm910.goeturgy.spells.spellspaces.SpellSpace;
import com.gm910.goeturgy.spells.spellspaces.SpellSpaces;
import com.gm910.goeturgy.spells.util.ISpellObject;
import com.gm910.goeturgy.tileentities.TileEntityBaseTickable;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;

public class TileMagicBarrier extends TileEntityBaseTickable implements ISpellObject {

	private long space;
	
	@Override
	public void update() {
		if (world.isRemote) return;
		if (this.getSpaceID() == -1) {
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
		} else {
			SpellSpace sp = this.getSpellSpace();
			if (!sp.areBordersSolid()) {
				world.setBlockState(pos, Blocks.AIR.getDefaultState());
			}
		}
		super.update();
	}

	@Override
	public SpellSpace getSpellSpace() {
		// TODO Auto-generated method stub
		return SpellSpaces.get().getById(space);
	}

	@Override
	public long getSpaceID() {
		// TODO Auto-generated method stub
		return space;
	}

	@Override
	public void setSpellSpace(SpellSpace space) {
		this.space = space.getID();
	}

	@Override
	public void setSpaceID(long id) {
		this.space = id;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound cmp = super.writeToNBT(compound);
		cmp.setLong("SPSP", space);
		return cmp;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.space = compound.getLong("SPSP");
		super.readFromNBT(compound);
	}
}
