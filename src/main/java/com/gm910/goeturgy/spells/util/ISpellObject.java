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
	
	/**
	 * If magic is offsetted
	 * @return
	 */
	public Vec3i getOffset();
	
	/**
	 * Set offset
	 */
	public void offset(Vec3i vec);
	
	/**
	 * if magic is cast to a diff location
	 * @return
	 */
	public ServerPos getDelegatedPos();
	
	/**
	 * Change the casting position
	 * @return
	 */
	public void setDelegatedPos(ServerPos pos);
		
	public default ServerPos getModifiedPos() {
		return getDelegatedPos().sAdd(getOffset());
	}
	
	public World getWorld();
	
	public default World getModifiedWorld() {
		return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(this.getModifiedPos().d);
	}
}
