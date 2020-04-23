package com.gm910.goeturgy.spells.util;

import java.util.List;
import java.util.function.BiFunction;

import com.gm910.goeturgy.spells.spellspaces.SpellSpace;
import com.gm910.goeturgy.spells.spellspaces.SpellSpaces;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ISpellComponent extends ISpellObject {

	/**
	 * Return true if the component can accept this compound
	 * @param facing
	 * @param comp
	 * @return whether the component can accept this compound
	 */
	public boolean accepts(EnumFacing facing, NBTTagCompound comp);
	
	/**
	 * Returns current input values
	 * @return
	 */
	public NonNullMap<EnumFacing, NBTTagCompound> getInputs();
	
	/**
	 * Puts an input into the spellcomponent <strong>from a static component</strong>, if not use activate
	 * @param inputs
	 * @return whether the input was accepted
	 */
	public boolean putInput(NonNullMap<EnumFacing, NBTTagCompound> inputs);
	
	public void resetInputs();
	
	public void resetOutputs();
	
	/**
	 * Activate this and return its output, or null if failed
	 * @param facing
	 * @param input
	 * @return
	 */
	public NonNullMap<EnumFacing, NBTTagCompound> activate(NonNullMap<EnumFacing, NBTTagCompound> inputs);
	
	/**
	 * Activate this and return its output, inputs are assumed to have been statically inserted (use for starter components)
	 * ##DO NOT OVERRIDE
	 * if input is incorrect return null
	 */
	public default NonNullMap<EnumFacing, NBTTagCompound> activate() {
		return activate(this.getInputs());
	}
	
	/**
	 * Returns the output if this component is static
	 * @return
	 */
	public default NonNullMap<EnumFacing, NBTTagCompound> getStaticOutput() {
		
		return new NonNullMap<EnumFacing, NBTTagCompound>(NBTTagCompound::new);
	}
	
	/**
	 * Returns the list of potential tags this can return from each face when given this list of potential tags as input through the given faces
	 * 
	 */
	public NonNullMap<EnumFacing, List<String>> getPossibleReturns(NonNullMap<EnumFacing, List<String>> input);
	
	/**
	 * Returns assumed required power based on the given stringlists from given sides
	 * @param tagsForSide
	 * @return
	 */
	public int getRequiredPower(NonNullMap<EnumFacing, List<String>> tagsForSide);
	
	/**
	 * Returns assumed required power based on the given compounds from given sides
	 * @param tagsForSide
	 * @return
	 */
	public int getRequiredPowerFromNBT(NonNullMap<EnumFacing, NBTTagCompound> tagsForSide);
	
	/**
	 * Returns assumed required power based on the inputs currently in the component
	 */
	public int getRequiredPower();
	
	/**
	 * Whether this spell's outputs do not change dynamically
	 * @return
	 */
	public default boolean isStatic() {
		return false;
	}
	
	/**
	 * Whether this spell acts as the point where the spellspace 'program' starts
	 * @return
	 */
	public default boolean isStarter() {
		return false;
	}
	
	public default void setSpellSpace(SpellSpace space) {
		
	}
	public default void setSpaceID(long id) {
		
	}
	public default SpellSpace getSpellSpace() {
		return SpellSpaces.get().getByPosition(this.getServerPos());
	}
	public default long getSpaceID() {
		return getSpellSpace() != null ? getSpellSpace().getID() : null;
	}
	
	public default String getString() {
		return this.getClass().getCanonicalName() + " at " + this.getPos();
	}
	

	

	
}
