package com.gm910.goeturgy.spells.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import com.gm910.goeturgy.entity.EntityCelestialBeam;
import com.gm910.goeturgy.spells.ioflow.MagicIO;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.Spell;
import com.gm910.goeturgy.spells.util.ISpellComponent;
import com.gm910.goeturgy.tileentities.TileEntityBaseTickable;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;

import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

/**
 * Summons lightning at its position or the positions in the list or singular pos given to it
 * @author borah
 *
 */
public class LightningSummoner extends TileEntityBaseTickable implements ISpellComponent {

	
	@Override
	public void update() {
		//System.out.println("Lightning summoner existing");
		super.update();
	}

	@Override
	public boolean accepts(EnumFacing facing, NBTTagCompound comp) {
		
		return comp.hasNoTags() || comp.hasKey(MagicIO.POS) || comp.hasKey(MagicIO.toList(MagicIO.POS));
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


	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> activate(Spell sp, ServerPos modifiedPos, NonNullMap<EnumFacing, NBTTagCompound> inps) {
		List<ServerPos> ls = new ArrayList<>();
		for (EnumFacing f : EnumFacing.VALUES) {
			if (MagicIO.getPosList(inps.get(f)) != null) {
				ls.addAll(MagicIO.getPosList(inps.get(f)));
			}
			if (MagicIO.getPos(inps.get(f)) != null) {
				ls.add(MagicIO.getPos(inps.get(f)));
			}
		}
		if (ls.isEmpty()) {
			ls.add(modifiedPos);
		}
		for (ServerPos spos : ls) {
			BlockPos pos = spos.getPos();
			World world = DimensionManager.getWorld(spos.d);
			if (world != null) {
				System.out.println("Lightning at " + pos);
				//world.addWeatherEffect(new EntityLightningBolt(world, pos.getX(), pos.getY(), pos.getZ(), pos.equals(this.pos) ? true : false));
				EntityCelestialBeam.summonCelestialBeam(world, pos.getX(), pos.getY(), pos.getZ(), 4.0f, true);
				Supplier<Double> rand = () -> ((new Random()).nextDouble() * 2 - 1);
				
				for (int i = 0; i < 20; i++) sp.getSpellSpace().spawnParticles(EnumParticleTypes.SPELL_INSTANT, false, new Vec3d(pos.getX() + 0.5 + rand.get(), pos.getY()+ 0.5 + rand.get(), pos.getZ() + 0.5 + rand.get()), new Vec3d(rand.get(), rand.get(), rand.get()), spos.d, 4);
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

	

}
