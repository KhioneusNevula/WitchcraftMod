package com.gm910.goeturgy.spells.util;

import com.gm910.goeturgy.spells.spellspaces.SpellSpace;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.Spell;

import net.minecraft.util.math.BlockPos;

public interface ISpellChainListener {

	public void activated(Spell space, BlockPos pos);
	
	public void finished(Spell space, BlockPos pos, boolean success);
	
	public void addToChain(Spell space, BlockPos pos);
	
	public boolean isPartOfChain(Spell space, BlockPos pos);
}
