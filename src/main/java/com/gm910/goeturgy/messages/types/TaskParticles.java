package com.gm910.goeturgy.messages.types;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;

public class TaskParticles implements IRunnableTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2606648558480222345L;
	
	private EnumParticleTypes type;
	private Vec3d pos;
	private Vec3d speed;
	private int d;
	private int count;
	private int[] args;

	private boolean thatDistanceThing;
	
	public TaskParticles(EnumParticleTypes type, boolean thatDistanceThing, Vec3d pos, Vec3d speed, int d, int count, int...args) {
		this.type = type;
		this.pos = pos;
		this.speed = speed;
		this.d = d;
		this.count = count;
		this.args = args;
		this.thatDistanceThing = thatDistanceThing;
	}
	
	@Override
	public void run() {
		if (Minecraft.getMinecraft().world == null) return;
		if (Minecraft.getMinecraft().world.provider.getDimension() != d) return;
		for (int i = 0; i < count; i++) {
			Minecraft.getMinecraft().world.spawnParticle(type, thatDistanceThing, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z, args);
		}
	}

}
