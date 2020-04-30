package com.gm910.goeturgy.spells.events.space;

import com.gm910.goeturgy.spells.events.space.SpellSpaceEvent.SpellSpaceRunEvent;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.Spell;

public class EndSpellSpaceMagicEvent extends SpellSpaceRunEvent {

	private boolean wasSuccessful;
	public EndSpellSpaceMagicEvent(Spell space, boolean success) {
		super(space);
		wasSuccessful = success;
	}
	
	public boolean wasSuccessful() {
		return wasSuccessful;
	}
	
}
