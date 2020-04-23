package com.gm910.goeturgy.world.dimensions;

import net.minecraft.client.audio.MusicTicker.MusicType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;

public class SpiritWorldProvider extends WorldProvider {

	@Override
	public DimensionType getDimensionType() {
		// TODO Auto-generated method stub
		return DimensionManager.getProviderType(this.getDimension());
	}
	
	/*@Override
	public BiomeProvider getBiomeProvider() {
		
		return new BiomeProviderSingle(BiomeInit.heavenly);
	}*/
	
	@Override
	public Vec3d getCloudColor(float partialTicks) {
	// TODO Auto-generated method stub
		return new Vec3d(1.0,0.5,0);
	}
	
	/*@Override
	public IChunkGenerator createChunkGenerator() {
		String prov = GodMod.proxy.getServer().getWorld(0).getWorldInfo().getGeneratorOptions();
		return new ChunkGeneratorHeavenly(this.world, null, this.getSeed(), false, prov);
	}*/
	
	@Override
	public Vec3d getSkyColor(Entity cameraEntity, float partialTicks) {
		/*if (cameraEntity instanceof EntityPlayer) {
			return new Vec3d(1,1,0);
		}
		return new Vec3d(0,0,0);*/
		return super.getSkyColor(cameraEntity, partialTicks);
	}
	
	@Override
	public boolean isSurfaceWorld() {
	// TODO Auto-generated method stub
		return false;
	}
	
	/*@Override
	public void onWorldUpdateEntities() {
		Deity d = Deities.get().getDeity(this.getDimensionType().getName());
		if (d == null) return;
	}*/
	
	/*public Deity getDeity() { 
		return Deities.get().getDeity(this.getDimensionType().getName());
	}*/
	
	@Override
	public MusicType getMusicType() {
		return super.getMusicType();
		//return EnumHelper.addEnum(MusicType.class, "HEAVENLY", new Class<?>[] {SoundEvent.class, int.class, int.class}, SoundEvents.AMBIENT_CAVE, 20, 600);
	}
	
	@Override
	public String getSaveFolder() {
		// TODO Auto-generated method stub
		return "DIM_" + this.getDimensionType().getName().toUpperCase();
	}
	
}
