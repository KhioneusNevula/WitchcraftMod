package com.gm910.goeturgy.spells.events.space;

import com.gm910.goeturgy.spells.events.space.SpellSpaceEvent.SpellSpaceRunEvent;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.SpellInstance;

import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class ActivateSpellSpaceEvent extends SpellSpaceRunEvent {

	public ActivateSpellSpaceEvent(SpellInstance space) {
		super(space);
	}

}
