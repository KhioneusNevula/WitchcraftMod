package com.gm910.goeturgy.spells.events.space;

import com.gm910.goeturgy.spells.spellspaces.SpellSpace;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.Spell;

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

		protected Spell runner;
		
		public SpellSpaceRunEvent(Spell runner) {
			super(runner.getSpellSpace());
			this.runner = runner;
			// TODO Auto-generated constructor stub
		}
		
		public Spell getRunner() {
			return runner;
		}
		
	}

}
