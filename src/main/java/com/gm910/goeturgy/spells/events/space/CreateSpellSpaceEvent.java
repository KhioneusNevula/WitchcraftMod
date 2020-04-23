package com.gm910.goeturgy.spells.events.space;

import com.gm910.goeturgy.spells.spellspaces.SpellSpace;

import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class CreateSpellSpaceEvent extends SpellSpaceEvent {

	
	public CreateSpellSpaceEvent(SpellSpace space) {
		super(space);
	}
	
	public long getID() {
		return super.spellSpace.getID();
	}

}
