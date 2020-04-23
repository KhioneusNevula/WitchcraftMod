package com.gm910.goeturgy.spells.components;

import java.util.ArrayList;
import java.util.List;

import com.gm910.goeturgy.spells.ioflow.BlockStack;
import com.gm910.goeturgy.spells.ioflow.MagicIO;
import com.gm910.goeturgy.spells.util.ISpellComponent;
import com.gm910.goeturgy.tileentities.TileEntityBaseTickable;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;
import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class PhilosophicCrystal extends TileEntityBaseTickable implements ISpellComponent {

	
	@Override
	public void update() {
		//System.out.println("Lightning summoner existing");
		super.update();
	}

	@Override
	public boolean accepts(EnumFacing facing, NBTTagCompound comp) {
		
		return comp.hasKey(MagicIO.POS) || comp.hasKey(MagicIO.toList(MagicIO.POS)) || comp.hasKey(MagicIO.BLOCK);
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
	public NonNullMap<EnumFacing, NBTTagCompound> activate(NonNullMap<EnumFacing, NBTTagCompound> inps) {
		NonNullMap<EnumFacing, NBTTagCompound> out = new NonNullMap<>(NBTTagCompound::new);
		List<ServerPos> ls = new ArrayList<>();
		BlockStack block = null;
		for (EnumFacing f : EnumFacing.VALUES) {
			if (MagicIO.getPosList(inps.get(f)) != null) {
				ls.addAll(MagicIO.getPosList(inps.get(f)));
			}
			if (MagicIO.getPos(inps.get(f)) != null) {
				ls.add(MagicIO.getPos(inps.get(f)));
			}
			if (MagicIO.getBlockStack(inps.get(f)) != null) {
				block = MagicIO.getBlockStack(inps.get(f));
			}
		}
		if (ls.isEmpty()) {
			if (!this.getModifiedPos().equals(this.getServerPos())) {
				ls.add(this.getModifiedPos());
			} else {
				ls.add(this.getServerPos().sUp());
			}
		}
		
		if (block == null) {
			return null;
		}
		List<BlockStack> blocksRet = new ArrayList<>();
		List<ItemStack> itemRet = new ArrayList<>();
		for (ServerPos spos : ls) {
			BlockPos pos = spos.getPos();
			World world = DimensionManager.getWorld(spos.d);
			if (world != null && world.getBlockState(pos).getBlockHardness(world, pos) < 5) {
				System.out.println("Transformation at " + pos);
				BlockStack prevStack = new BlockStack(spos);
				List<ItemStack> drops = world.getBlockState(pos).getBlock().getDrops(world, pos, world.getBlockState(pos), 1);
				block.setInWorld(spos);
				blocksRet.add(prevStack);
				itemRet.addAll(drops);
			}
		}
		for (EnumFacing f : EnumFacing.VALUES) {
			
			if (!blocksRet.isEmpty()) {
				MagicIO.writeBlockListToCompound(blocksRet, out.get(f));
				MagicIO.writeBlockToCompound(blocksRet.get(0), out.get(f));
			}
			if (!itemRet.isEmpty()) {
				MagicIO.writeItemListToCompound(itemRet, out.get(f));
				MagicIO.writeItemToCompound(itemRet.get(0), out.get(f));
			}
			
		}
		return out;
	}

	@Override
	public NonNullMap<EnumFacing, List<String>> getPossibleReturns(NonNullMap<EnumFacing, List<String>> input) {
		// TODO Auto-generated method stub
		return new NonNullMap<EnumFacing, List<String>>(() -> {
			return Lists.newArrayList(MagicIO.BLOCK, MagicIO.toList(MagicIO.BLOCK), MagicIO.ITEM, MagicIO.toList(MagicIO.ITEM));
		});
	}

	@Override
	public int getRequiredPower(NonNullMap<EnumFacing, List<String>> tagsForSide) {
		
		return 30;
	}

	@Override
	public int getRequiredPowerFromNBT(NonNullMap<EnumFacing, NBTTagCompound> tagsForSide) {
		List<ServerPos> ls = new ArrayList<>();
		int val = 10;
		BlockStack block = null;
		for (EnumFacing f : EnumFacing.VALUES) {
			if (MagicIO.getPosList(tagsForSide.get(f)) != null) {
				ls.addAll(MagicIO.getPosList(tagsForSide.get(f)));
			}
			if (MagicIO.getPos(tagsForSide.get(f)) != null) {
				ls.add(MagicIO.getPos(tagsForSide.get(f)));
			}
			if (MagicIO.getBlockStack(tagsForSide.get(f)) != null) {
				block = MagicIO.getBlockStack(tagsForSide.get(f));
			}
		}
		if (block != null) {
			val += 4*block.getBlock().getBlockHardness(getModifiedWorld(), getModifiedPos());
		}
		val += 2*ls.size();
		return val;
	}

	@Override
	public int getRequiredPower() {
		// TODO Auto-generated method stub
		return 30;
	}

	

}
