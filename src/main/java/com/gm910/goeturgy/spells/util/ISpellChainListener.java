package com.gm910.goeturgy.spells.util;

import com.gm910.goeturgy.spells.spellspaces.SpellSpace;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.SpellInstance;

import net.minecraft.util.math.BlockPos;

public interface ISpellChainListener {

	public void activated(SpellInstance space, BlockPos pos);
	
	public void finished(SpellInstance space, BlockPos pos, boolean success);
	
	public void addToChain(SpellInstance space, BlockPos pos);
	
	public boolean isPartOfChain(SpellInstance space, BlockPos pos);
}
