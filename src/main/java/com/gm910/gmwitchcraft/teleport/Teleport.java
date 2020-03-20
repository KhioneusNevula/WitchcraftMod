package com.gm910.gmwitchcraft.teleport;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class Teleport extends Teleporter {

	private final WorldServer world;
	private double x,y,z;
	
	public Teleport(WorldServer worldIn, double x, double y, double z) {
		super(worldIn);
		world = worldIn;
		
		this.x = x;
		this.y = y;
		this.z = z;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void placeInPortal(Entity entityIn, float rotationYaw) {
		// TODO Auto-generated method stub
		this.world.getBlockState(new BlockPos((int)this.x, (int)this.y, (int)this.z));
		entityIn.setPosition(x, y, z);
		entityIn.motionX = 0f;
		entityIn.motionY = 0f;
		entityIn.motionZ = 0f;
	}
	
	public static void teleportToDimension(Entity teleportee, int dimension, double x, double y, double z) {
		int oldDimension = teleportee.getEntityWorld().provider.getDimension();
		if (teleportee instanceof EntityPlayer) {
			EntityPlayerMP player = (EntityPlayerMP)teleportee;
			MinecraftServer server = teleportee.getEntityWorld().getMinecraftServer();
			WorldServer server2 = server.getWorld(dimension);
			if (server2 == null || server == null) throw new IllegalArgumentException("Dimension: " + dimension + " doesn't exist");
			//server2.getPlayerChunkMap().addPlayer(player);
			server2.getMinecraftServer().getPlayerList().transferPlayerToDimension(player, dimension, new Teleport(server2, x, y, z));
			//if (teleportee.isEntityEqual(Minecraft.getMinecraft().player)) Minecraft.getMinecraft().setRenderViewEntity(teleportee);
			teleportee.setPositionAndUpdate(x, y, z);
		} else if (!teleportee.world.isRemote) {
			Entity tele = teleportee.changeDimension(oldDimension);
			tele.setPositionAndUpdate(x, y, z);
		}
		
	}
	
}
