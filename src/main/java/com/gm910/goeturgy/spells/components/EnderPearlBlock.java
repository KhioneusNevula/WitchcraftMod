package com.gm910.goeturgy.spells.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import com.gm910.goeturgy.spells.ioflow.MagicIO;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.Spell;
import com.gm910.goeturgy.spells.util.ISpellComponent;
import com.gm910.goeturgy.tileentities.TileEntityBaseTickable;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;
import com.gm910.goeturgy.world.util.Teleport;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Summons lightning at its position or the positions in the list or singular pos given to it
 * @author borah
 *
 */
public class EnderPearlBlock extends TileEntityBaseTickable implements ISpellComponent {

	
	@Override
	public void update() {
		//System.out.println("Lightning summoner existing");
		super.update();
	}

	@Override
	public boolean accepts(EnumFacing facing, NBTTagCompound comp) {
		
		return comp.hasKey(MagicIO.ENTITY) || comp.hasKey(MagicIO.toList(MagicIO.ENTITY)) || comp.hasKey(MagicIO.POS) && !MagicIO.has(MagicIO.POS, inputs);
	}

	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> getInputs() {
		// TODO Auto-generated method stub
		return inputs;
	}

	@Override
	public boolean putInput(NonNullMap<EnumFacing, NBTTagCompound> inputs) {
		this.inputs = inputs;
		return true;
	}

	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> activate(Spell runner, Entity modifiedEntity,
			NonNullMap<EnumFacing, NBTTagCompound> inputs) {
		return privateActivate(runner, null, modifiedEntity, inputs);
	}
	

	private NonNullMap<EnumFacing, NBTTagCompound> privateActivate(Spell sp, ServerPos modifiedPos, Entity modifiedEntity, NonNullMap<EnumFacing, NBTTagCompound> inps) {
		List<Entity> ls = new ArrayList<>();
		for (EnumFacing f : EnumFacing.VALUES) {
			if (MagicIO.getPhysicalEntityList(inps.get(f)) != null) {
				ls.addAll(MagicIO.getPhysicalEntityList(inps.get(f)));
			}
			if (MagicIO.getPhysicalEntity(inps.get(f)) != null) {
				ls.add(MagicIO.getPhysicalEntity(inps.get(f)));
			}
		}
		if (ls.isEmpty()) {
			if (modifiedEntity != null) {
				ls.add(modifiedEntity);
			}
			else {
				return null;
			}
		}
		
		ServerPos toPos = null; 
		
		for (EnumFacing f : inps.keySet()) {
			toPos = MagicIO.getPos(inps.get(f)) != null ? MagicIO.getPos(inps.get(f)) : toPos;
		}
		
		for (Entity en : ls) {
			ServerPos formerPos = new ServerPos(en);
			if (toPos == null) {
				System.out.println("Teleporting entity forward");
				if (en == null) continue;
				if (en.getPositionVector() == null) continue;
				if (en.getLookVec() == null) continue;
				Vec3d ottopos = en.getPositionVector().add(en.getLookVec());
				toPos = new ServerPos(new BlockPos(ottopos), en.dimension);
				en.setPositionAndUpdate(ottopos.x, ottopos.y + 0.5, ottopos.z);
			} else {
				Teleport.teleportToDimension(en, toPos);
			}
			Supplier<Double> rand = () -> ((new Random()).nextDouble() * 2 - 1);
			
			for (int i = 0; i < 50; i++) {
				sp.getSpellSpace().spawnParticles(EnumParticleTypes.PORTAL, false, new Vec3d(toPos.getX() + 0.5 + rand.get(), toPos.getY()+ 0.5 + rand.get(), toPos.getZ() + 0.5 + rand.get()), new Vec3d(rand.get(), rand.get(), rand.get()), toPos.d, 4);
				sp.getSpellSpace().spawnParticles(EnumParticleTypes.PORTAL, false, new Vec3d(formerPos.getX() + 0.5 + rand.get(), formerPos.getY()+ 0.5 + rand.get(), formerPos.getZ() + 0.5 + rand.get()), new Vec3d(rand.get(), rand.get(), rand.get()), formerPos.d, 4);
				
			}
		}
		return new NonNullMap<EnumFacing, NBTTagCompound>(NBTTagCompound::new);
	}
	
	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> activate(Spell sp, ServerPos modifiedPos, NonNullMap<EnumFacing, NBTTagCompound> inps) {
		return privateActivate(sp, modifiedPos, null, inps);
	}

	@Override
	public NonNullMap<EnumFacing, List<String>> getPossibleReturns(NonNullMap<EnumFacing, List<String>> input) {
		return new NonNullMap<>(ArrayList<String>::new);
	}

	@Override
	public int getRequiredPower(NonNullMap<EnumFacing, List<String>> tagsForSide) {
		
		return 50;
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
		
		return 50 * ls.size();
	}

	@Override
	public int getRequiredPower() {
		// TODO Auto-generated method stub
		return 50;
	}

	public String toString() {
		return "Ender Pearl Block at " + pos;
	}

}
