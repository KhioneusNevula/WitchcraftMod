package com.gm910.goeturgy.spells.events.space;

import com.gm910.goeturgy.spells.spellspaces.SpellSpace;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.SpellInstance;

import net.minecraftforge.fml.common.eventhandler.Event;

public class SpellSpaceEvent extends Event {

	protected SpellSpace spellSpace;
	
	public SpellSpaceEvent(SpellSpace space) {
		this.spellSpace = space;
	}
	
	public SpellSpace getSpellSpace() {
		return spellSpace;
	}
	
	public static class SpellSpaceRunEvent extends SpellSpaceEvent {

		protected SpellInstance runner;
		
		public SpellSpaceRunEvent(SpellInstance runner) {
			super(runner.getSpellSpace());
			this.runner = runner;
			// TODO Auto-generated constructor stub
		}
		
		public SpellInstance getRunner() {
			return runner;
		}
		
	}

}
