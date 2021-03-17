package com.gm910.goeturgy.spells.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

import com.gm910.goeturgy.spells.ioflow.MagicIO;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.Spell;
import com.gm910.goeturgy.spells.util.ISpellComponent;
import com.gm910.goeturgy.tileentities.TileEntityBaseTickable;
import com.gm910.goeturgy.util.IObjectMouseoverGui;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemLingeringPotion;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSpectralArrow;
import net.minecraft.item.ItemSplashPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.DimensionManager;

/**
 * Summons lightning at its position or the positions in the list or singular pos given to it
 * @author borah
 *
 */
public class PotionDisperser extends TileEntityBaseTickable implements ISpellComponent, IObjectMouseoverGui {

	private ItemStack potion = ItemStack.EMPTY;
	
	@Override
	public void update() {
		//System.out.println("Lightning summoner existing");
		super.update();
	}

	@Override
	public boolean accepts(EnumFacing facing, NBTTagCompound comp) {
		if (comp.hasKey(MagicIO.ITEM)) {
			if ((MagicIO.getItemStack(comp).getItem() instanceof ItemPotion || MagicIO.getItemStack(comp).getItem() instanceof ItemSpectralArrow )&& potion.isEmpty()) {
				return true;
			}
		}
		return comp.hasNoTags() || comp.hasKey(MagicIO.POS) || comp.hasKey(MagicIO.toList(MagicIO.POS)) || comp.hasKey(MagicIO.ENTITY) || comp.hasKey(MagicIO.toList(MagicIO.ENTITY));
	}

	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> getInputs() {
		// TODO Auto-generated method stub
		return inputs;
	}
	
	@Override
	public boolean acceptsEmpty(EnumFacing face) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public boolean isOutput(EnumFacing face) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean putInput(NonNullMap<EnumFacing, NBTTagCompound> inputs) {
		this.inputs = inputs;
		return true;
	}

	public void setPotion(ItemStack potion) {
		this.potion = potion;
	}
	
	public ItemStack getPotion() {
		return potion;
	}

	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> activate(Spell sp, ServerPos modifiedPos, NonNullMap<EnumFacing, NBTTagCompound> inps) {
		ItemStack stack = potion;
		
		for (EnumFacing f : EnumFacing.VALUES) {
			if (inputs.get(f).hasKey(MagicIO.ITEM)) {
				ItemStack ch = MagicIO.getItemStack(inputs.get(f));
				
				if (ch.getItem() instanceof ItemPotion || ch.getItem() instanceof ItemSpectralArrow) {
					if (stack != null) {

						System.out.println("Too many potion item stacks passed in");
						return null;
					}
					stack = ch;
				}
			}
		}
		if (stack == null) {
			System.out.println("No potion item stack");
			return null;
		}
		
		List<ServerPos> posList = new ArrayList<>();
		List<UUID> entityList = new ArrayList<>();
		for (EnumFacing f : EnumFacing.VALUES) {
			if (MagicIO.getPosList(inps.get(f)) != null) {
				posList.addAll(MagicIO.getPosList(inps.get(f)));
			}
			if (MagicIO.getPos(inps.get(f)) != null) {
				posList.add(MagicIO.getPos(inps.get(f)));
			}
		}
		if (posList.isEmpty()) {
			posList.add(modifiedPos.sUp());
		}
		for (EnumFacing f : EnumFacing.VALUES) {
			if (MagicIO.getEntityList(inps.get(f)) != null) {
				entityList.addAll(MagicIO.getEntityList(inps.get(f)));
			}
			if (MagicIO.getEntity(inps.get(f)) != null) {
				entityList.add(MagicIO.getEntity(inps.get(f)));
			}
		}
		if (stack.getItem() instanceof ItemSpectralArrow) {
			for (ServerPos spos : posList) {
				BlockPos pos = spos.getPos();
				World world = DimensionManager.getWorld(spos.d);
				EntitySpectralArrow en = new EntitySpectralArrow(world, spos.getX(), spos.getY(), spos.getZ());
				world.spawnEntity(en);
				System.out.println("Glowing arrow at " + pos);
				Supplier<Double> rand = () -> ((new Random()).nextDouble() * 2 - 1);
				
				for (int i = 0; i < 20; i++) sp.getSpellSpace().spawnParticles(EnumParticleTypes.SPELL_INSTANT, false, new Vec3d(pos.getX() + 0.5 + rand.get(), pos.getY()+ 0.5 + rand.get(), pos.getZ() + 0.5 + rand.get()), new Vec3d(rand.get(), rand.get(), rand.get()), spos.d, 4);
			}
			for (UUID uu : entityList) {
				Entity e = MagicIO.entityfromid(uu);
				if (!(e instanceof EntityLivingBase)) {
					continue;
				}
				EntityLivingBase entity = (EntityLivingBase)e;

				World world = DimensionManager.getWorld(entity.dimension);
				if (world != null) {
					System.out.println("Glowing arrow at " + entity);
					entity.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 200));
					Supplier<Double> rand = () -> ((new Random()).nextDouble() * 2 - 1);
					
					for (int i = 0; i < 20; i++) sp.getSpellSpace().spawnParticles(EnumParticleTypes.SPELL_INSTANT, false, new Vec3d(entity.posX + 0.5 + rand.get(), entity.posY+ 0.5 + rand.get(), entity.posZ + 0.5 + rand.get()), new Vec3d(rand.get(), rand.get(), rand.get()), entity.dimension, 4);
				}
			}
		} else 
		if (stack.getItem() instanceof ItemLingeringPotion || stack.getItem() instanceof ItemSplashPotion) {
			
			boolean lingering = stack.getItem() instanceof ItemLingeringPotion;
			for (ServerPos spos : posList) {
				BlockPos pos = spos.getPos();
				World world = DimensionManager.getWorld(spos.d);
				if (world != null) {
					System.out.println("Potion thrown at " + pos);
					EntityPotion en = new EntityPotion(world, spos.getX() + 0.5, spos.getY() + 1, spos.getZ() + 0.5, stack);
					world.spawnEntity(en);
					Supplier<Double> rand = () -> ((new Random()).nextDouble() * 2 - 1);
					
					for (int i = 0; i < 20; i++) sp.getSpellSpace().spawnParticles(EnumParticleTypes.SPELL_INSTANT, false, new Vec3d(pos.getX() + 0.5 + rand.get(), pos.getY()+ 0.5 + rand.get(), pos.getZ() + 0.5 + rand.get()), new Vec3d(rand.get(), rand.get(), rand.get()), spos.d, 4);
				}
			}
		} else {
			
			for (UUID uu : entityList) {
				Entity e = MagicIO.entityfromid(uu);
				if (!(e instanceof EntityLivingBase)) {
					continue;
				}
				EntityLivingBase entity = (EntityLivingBase)e;
				World world = DimensionManager.getWorld(entity.dimension);
				if (world != null) {
					System.out.println("Potion applied to " + entity);
					for (PotionEffect potioneffect : PotionUtils.getEffectsFromStack(stack))
		            {
		                if (potioneffect.getPotion().isInstant())
		                {
		                    potioneffect.getPotion().affectEntity(null, null, entity, potioneffect.getAmplifier(), 1.0D);
		                }
		                else
		                {
		                    entity.addPotionEffect(new PotionEffect(potioneffect));
		                }
		            }
					Supplier<Double> rand = () -> ((new Random()).nextDouble() * 2 - 1);
					
					for (int i = 0; i < 20; i++) sp.getSpellSpace().spawnParticles(EnumParticleTypes.SPELL_INSTANT, false, new Vec3d(entity.posX + 0.5 + rand.get(), entity.posY+ 0.5 + rand.get(), entity.posZ + 0.5 + rand.get()), new Vec3d(rand.get(), rand.get(), rand.get()), entity.dimension, 4);
				}
			}
		}
		
		return new NonNullMap<EnumFacing, NBTTagCompound>(NBTTagCompound::new);
	}

	@Override
	public NonNullMap<EnumFacing, List<String>> getPossibleReturns(NonNullMap<EnumFacing, List<String>> input) {
		return new NonNullMap<>(ArrayList<String>::new);
	}

	@Override
	public int getRequiredPower(NonNullMap<EnumFacing, List<String>> tagsForSide) {
		
		return 20;
	}

	@Override
	public int getRequiredPowerFromNBT(NonNullMap<EnumFacing, NBTTagCompound> tagsForSide, ServerPos modifiedPos) {
		List<ServerPos> ls = new ArrayList<>();
		for (EnumFacing f : EnumFacing.VALUES) {
			List<ServerPos> lse =MagicIO.getPosList(tagsForSide.get(f));
			if (lse != null) {
				ls.addAll(lse);
			}
			ServerPos ps = MagicIO.getPos(tagsForSide.get(f));
			if (ps != null) {
				ls.add(ps);
			}
		}
		
		return 20 * ls.size();
	}

	@Override
	public int getRequiredPower() {
		// TODO Auto-generated method stub
		return 20;
	}

	@Override
	public void drawGuiOverlays(Pre event, Minecraft mc, Gui gui, TextureManager tex, ScaledResolution res,
			EnumFacing sideHit, IBlockState state, Vec3d hitVec, BlockPos position, TileEntity tile, Object object) {
		// TODO Auto-generated method stub
		gui.drawCenteredString(mc.fontRenderer, "" + this.getPotion().getDisplayName(), res.getScaledWidth()/ 2, res.getScaledHeight() / 2, 0xFFFFFF);
	}


}
