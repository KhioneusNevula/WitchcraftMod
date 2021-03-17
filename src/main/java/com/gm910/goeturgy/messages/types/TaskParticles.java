package com.gm910.goeturgy.messages.types;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
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
	
	public TaskParticles() {}
	
	@Override
	public void run() {
		if (Minecraft.getMinecraft().world == null) return;
		if (Minecraft.getMinecraft().world.provider.getDimension() != d) return;
		for (int i = 0; i < count; i++) {
			Minecraft.getMinecraft().world.spawnParticle(type, thatDistanceThing, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z, args);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound comp) {
		comp.setInteger("ID", type.getParticleID());
		NBTTagCompound vec = new NBTTagCompound();
		vec.setDouble("X", pos.x);vec.setDouble("Y", pos.y);vec.setDouble("Z", pos.z);
		comp.setTag("PosVec", vec.copy());
		vec.setDouble("X", speed.x);vec.setDouble("Y", speed.y);vec.setDouble("Z", speed.z);
		comp.setTag("SpeedVec", vec);
		comp.setInteger("Dimension", d);
		comp.setInteger("Count", count);
		comp.setIntArray("Args", args);
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {

		type = EnumParticleTypes.getParticleFromId(comp.getInteger("ID"));
		NBTTagCompound posVec = comp.getCompoundTag("PosVec");
		pos = new Vec3d(posVec.getDouble("X"), posVec.getDouble("Y"), posVec.getDouble("Z"));
		posVec = comp.getCompoundTag("SpeedVec");
		speed = new Vec3d(posVec.getDouble("X"), posVec.getDouble("Y"), posVec.getDouble("Z"));
		d = comp.getInteger("Dimension");
		count = comp.getInteger("Count");
		int[] args = comp.getIntArray("Args");
	}

}
