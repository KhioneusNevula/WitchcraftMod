package com.gm910.goeturgy.events;

import com.gm910.goeturgy.entity.EntityCelestialBeam;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class EntityStruckByCelestialBeamEvent extends EntityEvent {

	private final EntityCelestialBeam beam;
	
	public EntityStruckByCelestialBeamEvent(Entity entity, EntityCelestialBeam beam) {
		super(entity);
		this.beam = beam;
	}

	public EntityCelestialBeam getBeam() {
		return beam;
	}
	
}
