package com.gm910.goeturgy.spells.util;

import com.gm910.goeturgy.spells.spellspaces.SpellSpace;
import com.gm910.goeturgy.util.ServerPos;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public interface ISpellObject {

	public SpellSpace getSpellSpace();
	public long getSpaceID();
	public void setSpellSpace(SpellSpace space);
	public void setSpaceID(long id);
	public BlockPos getPos();
	public default ServerPos getServerPos() {
		return new ServerPos(getPos(), getWorld());
	}
	
	public World getWorld();
}
