package com.gm910.goeturgy.spells.events.space;

import com.gm910.goeturgy.spells.spellspaces.SpellSpace;

import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class ForceLoadSpellSpace extends SpellSpaceEvent {

	public enum Type {
		JUST_HEAD,
		WHOLE_SPACE
	}
	
	private Type type;
	
	public ForceLoadSpellSpace(SpellSpace sp, boolean justHead) {
		super(sp);
		this.type = justHead ? Type.JUST_HEAD : Type.WHOLE_SPACE;
	}
	
	public Type getType() {
		return type;
	}
	
	public boolean justLoadedHead() {
		return type == Type.JUST_HEAD;
	}
	
	public boolean loadedWholeSpace() {
		return type == Type.WHOLE_SPACE;
	}

}
