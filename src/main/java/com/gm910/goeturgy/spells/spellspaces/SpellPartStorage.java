package com.gm910.goeturgy.spells.spellspaces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.gm910.goeturgy.spells.util.ISpellBorder;
import com.gm910.goeturgy.spells.util.ISpellChainListener;
import com.gm910.goeturgy.spells.util.ISpellComponent;
import com.gm910.goeturgy.spells.util.ISpellObject;
import com.google.common.base.Predicates;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class SpellPartStorage {

	private Map<BlockPos, ISpellObject> spellMap = new HashMap<>();
	
	private Map<BlockPos, SpellSpace> subSpaceMap = new HashMap<>();
	
	private SpellSpace space;
	
	public SpellPartStorage(SpellSpace space) {
		this.space = space;
		for (BlockPos pos : space.getInnerSpace()) {
			if (space.getWorld().getTileEntity(pos) instanceof ISpellObject && ((ISpellObject)space.getWorld().getTileEntity(pos)).getSpaceID() == space.id ) {
				spellMap.put(pos, ((ISpellObject)space.getWorld().getTileEntity(pos)));
			}
			if (space.getWorld().getTileEntity(pos) instanceof ISpellObject && ((ISpellObject)space.getWorld().getTileEntity(pos)).getSpaceID() != space.id) {
				for (SpellSpace mini : SpellSpaces.get().getInDimension(space.dimension)) {
					if (mini.getHeadPos().equals(pos)) {
						subSpaceMap.put(pos, mini);
					}
				}
			}
		}
	}
	
	public <T> T getInstance(Class<T> clazz, BlockPos pos) {
		return spellMap.get(pos) != null ? (clazz.isAssignableFrom(spellMap.get(pos).getClass()) ? (T) spellMap.get(pos) : null) : null;
	}
	
	public ISpellComponent getComponent(BlockPos pos) {
		return getInstance(ISpellComponent.class, pos);
	}
	
	public ISpellChainListener getChainListener(BlockPos pos) {
		return getInstance(ISpellChainListener.class, pos);
	}
	
	public ISpellBorder getBorderPart(BlockPos pos) {
		return getInstance(ISpellBorder.class, pos);
	}
	
	public TileEntity getTileEntity(BlockPos pos) {
		return getInstance(TileEntity.class, pos);
	}
	
	public ISpellObject getSpellObject(BlockPos pos) {
		return getInstance(ISpellObject.class, pos);
	}
	
	public <T> List<T> getInstancesInSpace(Class<T> clazz, Predicate<? super T> pred) {
		ArrayList<T> ls = new ArrayList<T>();
		for (BlockPos pos : this.spellMap.keySet()) {
			if (getInstance(clazz, pos) != null && pred.test(getInstance(clazz, pos))) {
				ls.add(getInstance(clazz, pos));
			}
		}
		for (BlockPos pos : this.subSpaceMap.keySet()) {
			ls.addAll(subSpaceMap.get(pos).getInstancesInSpace(clazz, pred));
		}
		return ls;
	}
	
	public <T> List<T> getInstancesInSpace(Class<T> clazz) {
		return getInstancesInSpace(clazz, Predicates.alwaysTrue());
	}
	
	public <T> List<BlockPos> getPositionsOfInstancesInSpace(Class<T> toCheck, Predicate<? super T> pred) {
		ArrayList<BlockPos> ls = new ArrayList<>();
		for (BlockPos pos : this.spellMap.keySet()) {
			if (getInstance(toCheck, pos) != null && pred.test(getInstance(toCheck, pos))) {
				ls.add(pos);
			}
		}
		for (BlockPos pos : this.subSpaceMap.keySet()) {
			ls.addAll(subSpaceMap.get(pos).getPositionsOfInstancesInSpace(toCheck, pred));
		}
		return ls;
	}

	public <T> List<BlockPos> getPositionsOfInstancesInSpace(Class<T> clazz) {
		return getPositionsOfInstancesInSpace(clazz, Predicates.alwaysTrue());
	}
	
}
