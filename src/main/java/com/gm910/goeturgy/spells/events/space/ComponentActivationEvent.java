package com.gm910.goeturgy.spells.events.space;

import com.gm910.goeturgy.spells.events.space.SpellSpaceEvent.SpellSpaceRunEvent;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.Spell;
import com.gm910.goeturgy.spells.util.ISpellComponent;

import net.minecraft.tileentity.TileEntity;

public class ComponentActivationEvent<T extends TileEntity> extends SpellSpaceRunEvent {

	private T asTileEntity;
	private ISpellComponent component;
	
	public ComponentActivationEvent(Spell space, T component) {
		super(space);
		this.asTileEntity = component;
		this.component = (ISpellComponent) component;
		// TODO Auto-generated constructor stub
	}
	
	public ComponentActivationEvent(Spell space, ISpellComponent component) {
		super(space);
		try {
			this.asTileEntity = (T) component;
		} catch (ClassCastException e) {
			System.err.println("Difficulty creating component activation event--type inference mismatch");
		}
		this.component = component;
		// TODO Auto-generated constructor stub
	}

	public T getAsTileEntity() {
		return asTileEntity;
	}
	
	public ISpellComponent getComponent() {
		return component;
	}

}
