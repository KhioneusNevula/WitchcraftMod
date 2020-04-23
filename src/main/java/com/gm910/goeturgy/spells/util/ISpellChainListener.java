package com.gm910.goeturgy.spells.util;

import com.gm910.goeturgy.spells.spellspaces.SpellSpace;

import net.minecraft.util.math.BlockPos;

public interface ISpellChainListener {

	public void activated(SpellSpace space, BlockPos pos);
	
	public void finished(SpellSpace space, BlockPos pos, boolean success);
	
	public void addToChain(SpellSpace space, BlockPos pos);
	
	public boolean isPartOfChain(SpellSpace space, BlockPos pos);
}
