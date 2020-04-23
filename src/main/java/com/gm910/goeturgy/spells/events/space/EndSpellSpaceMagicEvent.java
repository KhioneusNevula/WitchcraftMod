package com.gm910.goeturgy.spells.events.space;

import com.gm910.goeturgy.spells.spellspaces.SpellSpace;

public class EndSpellSpaceMagicEvent extends SpellSpaceEvent {

	private boolean wasSuccessful;
	public EndSpellSpaceMagicEvent(SpellSpace space, boolean success) {
		super(space);
		wasSuccessful = success;
	}
	
	public boolean wasSuccessful() {
		return wasSuccessful;
	}
	
}
