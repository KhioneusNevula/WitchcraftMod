package com.gm910.goeturgy.spells.events.space;

import com.gm910.goeturgy.spells.events.space.SpellSpaceEvent.SpellSpaceRunEvent;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.SpellInstance;

import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * Called before each tick of spellcasting of the spellspace
 *
 */
@Cancelable
public class SpellSpaceTickingEvent extends SpellSpaceRunEvent {

	private int currentTick;
	
	public SpellSpaceTickingEvent(SpellInstance sp, int tick) {
		super(sp);
		currentTick = tick;
	}
	
	public int getCurrentTick() {
		return currentTick;
	}

}
