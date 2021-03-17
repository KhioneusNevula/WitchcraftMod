package com.gm910.goeturgy.entity;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.gm910.goeturgy.events.EntityStruckByCelestialBeamEvent;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityCelestialBeam extends EntityLightningBolt {

	private int livingTime;
	private boolean causesDestruction;
	
	public EntityCelestialBeam(World worldIn, double x, double y, double z, float strength, boolean causesDestruction) {
		super(worldIn, x, y, z, true);
		this.setLocationAndAngles(x, y, z, 0, 0);
		livingTime = rand.nextInt(100) + 1;
		this.causesDestruction = causesDestruction;
		for (int i = 0; i < 100; i++) {
			for (double y1 = this.posY; y1 < 255; y1++) {
				Supplier<Double> s = () -> rand.nextDouble() * 2 -1;
				world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, posX + 0.5 + s.get(), y1 + 0.5 + s.get(), posZ + 0.5 + s.get(), s.get(), s.get(), s.get());
			}
		}
		BlockPos blockpos = new BlockPos(this);
		world.setBlockState(blockpos.up().up(), Blocks.GLOWSTONE.getDefaultState());
		if (causesDestruction && !worldIn.isRemote && worldIn.getGameRules().getBoolean("doFireTick") && (worldIn.getDifficulty() == EnumDifficulty.NORMAL || worldIn.getDifficulty() == EnumDifficulty.HARD) && worldIn.isAreaLoaded(blockpos, 10))
        {
			for (int y1 = 255; y1 > blockpos.getY(); y1--) {
				BlockPos newPos = new BlockPos(blockpos.getX(), y1, blockpos.getZ());

            	if (worldIn.getBlockState(newPos).getBlock().getExplosionResistance(worldIn, blockpos, this, null) < strength*10 && !worldIn.getBlockState(newPos).getMaterial().isReplaceable()) {
            		worldIn.createExplosion(this, x, y1, z, strength, true);
            	} else {
            		continue;
            	}
            	
	            /*if (!worldIn.getBlockState(blockpos).getMaterial().isReplaceable()) // && Blocks.FIRE.canPlaceBlockAt(worldIn, blockpos))
	            {
	               this.setDead();
	               return;
	            }*/
			}

            for (int i = 0; i < 4; ++i)
            {
                BlockPos blockpos1 = blockpos.add(this.rand.nextInt(3) - 1, this.rand.nextInt(3) - 1, this.rand.nextInt(3) - 1);

                if (worldIn.getBlockState(blockpos1).getMaterial() == Material.AIR) // && Blocks.FIRE.canPlaceBlockAt(worldIn, blockpos1))
                {
                    //worldIn.setBlockState(blockpos1, Blocks.FIRE.getDefaultState());
                	worldIn.createExplosion(this, blockpos1.getX(), blockpos1.getY(), blockpos1.getZ(), strength / 2, true);
                }
            }
        }
	}
	
	@Override
	public void onUpdate() {
		// TODO Auto-generated method stub
		super.onUpdate();
		this.livingTime--;
		for (int i = 0; i < 100; i++) {
			for (double y = this.posY; y < 255; y++) {
				Supplier<Double> s = () -> rand.nextDouble() * 2 -1;
				world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, posX + 0.5 + s.get(), y + 0.5 + s.get(), posZ + 0.5 + s.get(), s.get(), s.get(), s.get());
			}
		}
		if (livingTime <= 0) {
			this.setDead();
			
			double d0 = 3.0D;
            List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(this.posX - 3.0D, this.posY - 3.0D, this.posZ - 3.0D, this.posX + 3.0D, 255, this.posZ + 3.0D));

            for (int i = 0; i < list.size(); ++i)
            {
                Entity entity = list.get(i);
                if (!MinecraftForge.EVENT_BUS.post(new EntityStruckByCelestialBeamEvent(entity, this))) {
                	if (entity instanceof ISpecialCelestialBeamEntity) {
                		((ISpecialCelestialBeamEntity) entity).onStruckByCelestialBeam(this);
                	} else {
                		 entity.attackEntityFrom(CelestialDamage.CELESTIAL, 5.0F);
                	     entity.setFire(9);
                	}
                }
            }
		}
	}
	
	public static void summonCelestialBeam(World world, double x, double y, double z, float strength, boolean doDamage) {
		world.spawnEntity(new EntityCelestialBeam(world, x, y, z, strength, doDamage));
		//if (world.isRemote) Minecraft.getMinecraft().getRenderManager().entityRenderMap.put(EntityCelestialBeam.class, new EntityCelestialBeam.RenderCelestialBeam(Minecraft.getMinecraft().getRenderManager()));
	}

	@Override
	public void entityInit() {
		
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
	}
	
	public static class CelestialDamage extends DamageSource {

		public static final DamageSource CELESTIAL = (new CelestialDamage("celestial")).setMagicDamage();
		
		public CelestialDamage(String damageTypeIn) {
			super(damageTypeIn);
		}
		
		@Override
		public ITextComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {
			
			return new TextComponentTranslation("death.celestialbeam", entityLivingBaseIn.getDisplayName());
		}
		
	}
	

	@SideOnly(Side.CLIENT)
	public static class RenderCelestialBeam extends Render<EntityCelestialBeam>
	{
	    public RenderCelestialBeam(RenderManager renderManagerIn)
	    {
	        super(renderManagerIn);
	    }

	    /**
	     * Renders the desired {@code T} type Entity.
	     */
	    public void doRender(EntityCelestialBeam entity, double x, double y, double z, float entityYaw, float partialTicks)
	    {
	    	System.out.println("Rendering celestial beam");
	        Tessellator tessellator = Tessellator.getInstance();
	        BufferBuilder bufferbuilder = tessellator.getBuffer();
	        GlStateManager.disableTexture2D();
	        GlStateManager.disableLighting();
	        GlStateManager.enableBlend();
	        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
	        double[] adouble = new double[8];
	        double[] adouble1 = new double[8];
	        double d0 = 0.0D;
	        double d1 = 0.0D;

	        for (int i = 7; i >= 0; --i)
	        {
	            adouble[i] = d0;
	            adouble1[i] = d1;
	            d0 += 1;//(double)(random.nextInt(11) - 5);
	            d1 += 1; //(double)(random.nextInt(11) - 5);
	        }

	        for (int k1 = 0; k1 < 4; ++k1)
	        {

	            for (int j = 0; j < 3; ++j)
	            {
	                int k = 7;
	                int l = 0;

	                if (j > 0)
	                {
	                    k = 7 - j;
	                }

	                if (j > 0)
	                {
	                    l = k - 2;
	                }

	                double d2 = adouble[k] - d0;
	                double d3 = adouble1[k] - d1;

	                for (int i1 = k; i1 >= l; --i1)
	                {
	                    double d4 = d2;
	                    double d5 = d3;

	                    if (j == 0)
	                    {
	                        d2 += 1;//(double)(random1.nextInt(11) - 5);
	                        d3 += 1;//(double)(random1.nextInt(11) - 5);
	                    }
	                    else
	                    {
	                        d2 += 1;//(double)(random1.nextInt(31) - 15);
	                        d3 += 1;//(double)(random1.nextInt(31) - 15);
	                    }

	                    bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);
	                    float f = 0.5F;
	                    float f1 = 0.45F;
	                    float f2 = 0.45F;
	                    float f3 = 0.5F;
	                    double d6 = 0.1D + (double)k1 * 0.2D;

	                    if (j == 0)
	                    {
	                        d6 *= (double)i1 * 0.1D + 1.0D;
	                    }

	                    double d7 = 0.1D + (double)k1 * 0.2D;

	                    if (j == 0)
	                    {
	                        d7 *= (double)(i1 - 1) * 0.1D + 1.0D;
	                    }

	                    for (int j1 = 0; j1 < 5; ++j1)
	                    {
	                        double d8 = x + 0.5D - d6;
	                        double d9 = z + 0.5D - d6;

	                        if (j1 == 1 || j1 == 2)
	                        {
	                            d8 += d6 * 2.0D;
	                        }

	                        if (j1 == 2 || j1 == 3)
	                        {
	                            d9 += d6 * 2.0D;
	                        }

	                        double d10 = x + 0.5D - d7;
	                        double d11 = z + 0.5D - d7;

	                        if (j1 == 1 || j1 == 2)
	                        {
	                            d10 += d7 * 2.0D;
	                        }

	                        if (j1 == 2 || j1 == 3)
	                        {
	                            d11 += d7 * 2.0D;
	                        }

	                        bufferbuilder.pos(d10 + d2, y + (double)(i1 * 16), d11 + d3).color(0.5F, 0.5F, 0.45F, 0.3F).endVertex();
	                        bufferbuilder.pos(d8 + d4, y + (double)((i1 + 1) * 16), d9 + d5).color(0.5F, 0.45F, 0.5F, 0.3F).endVertex();
	                    }

	                    tessellator.draw();
	                }
	            }
	        }

	        GlStateManager.disableBlend();
	        GlStateManager.enableLighting();
	        GlStateManager.enableTexture2D();
	    }

	    /**
	     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	     */
	    @Nullable
	    protected ResourceLocation getEntityTexture(EntityCelestialBeam entity)
	    {
	        return null;
	    }
	}

}
