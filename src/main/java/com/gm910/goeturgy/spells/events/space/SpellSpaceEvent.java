package com.gm910.goeturgy.spells.events.space;

import com.gm910.goeturgy.spells.spellspaces.SpellSpace;

import net.minecraftforge.fml.common.eventhandler.Event;

public class SpellSpaceEvent extends Event {

	protected SpellSpace spellSpace;
	
	public SpellSpaceEvent(SpellSpace space) {
		this.spellSpace = space;
	}
	
	public SpellSpace getSpellSpace() {
		return spellSpace;
	}

}
