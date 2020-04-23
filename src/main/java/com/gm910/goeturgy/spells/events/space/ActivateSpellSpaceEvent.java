package com.gm910.goeturgy.spells.events.space;

import com.gm910.goeturgy.spells.spellspaces.SpellSpace;

import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class ActivateSpellSpaceEvent extends SpellSpaceEvent {

	public ActivateSpellSpaceEvent(SpellSpace space) {
		super(space);
	}

}
