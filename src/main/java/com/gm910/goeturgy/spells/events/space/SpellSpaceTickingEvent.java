package com.gm910.goeturgy.spells.events.space;

import com.gm910.goeturgy.spells.events.space.SpellSpaceEvent.SpellSpaceRunEvent;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.Spell;

import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * Called before each tick of spellcasting of the spellspace
 *
 */
@Cancelable
public class SpellSpaceTickingEvent extends SpellSpaceRunEvent {

	private int currentTick;
	
	public SpellSpaceTickingEvent(Spell sp, int tick) {
		super(sp);
		currentTick = tick;
	}
	
	public int getCurrentTick() {
		return currentTick;
	}

}
