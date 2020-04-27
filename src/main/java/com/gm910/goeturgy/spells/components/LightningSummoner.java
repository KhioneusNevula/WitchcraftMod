package com.gm910.goeturgy.spells.components;

import java.util.ArrayList;
import java.util.List;

import com.gm910.goeturgy.spells.ioflow.MagicIO;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.SpellInstance;
import com.gm910.goeturgy.spells.util.ISpellComponent;
import com.gm910.goeturgy.tileentities.TileEntityBaseTickable;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;

import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class LightningSummoner extends TileEntityBaseTickable implements ISpellComponent {

	
	@Override
	public void update() {
		//System.out.println("Lightning summoner existing");
		super.update();
	}

	@Override
	public boolean accepts(EnumFacing facing, NBTTagCompound comp) {
		
		return comp.hasKey(MagicIO.POS) || comp.hasKey(MagicIO.toList(MagicIO.POS));
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
	public NonNullMap<EnumFacing, NBTTagCompound> activate(SpellInstance sp, ServerPos modifiedPos, NonNullMap<EnumFacing, NBTTagCompound> inps) {
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
				world.addWeatherEffect(new EntityLightningBolt(world, pos.getX(), pos.getY(), pos.getZ(), pos.equals(this.pos) ? true : false));
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
