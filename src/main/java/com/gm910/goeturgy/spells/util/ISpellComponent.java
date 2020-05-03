package com.gm910.goeturgy.spells.util;

import java.util.List;

import com.gm910.goeturgy.spells.spellspaces.SpellSpace;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.Spell;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

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
	
	
	/**
	 * Activate this and return its output, or null if failed
	 * @param facing
	 * @param input
	 * @return
	 */
	public NonNullMap<EnumFacing, NBTTagCompound> activate(Spell spell, ServerPos modifiedPos, NonNullMap<EnumFacing, NBTTagCompound> inputs);
	
	/**
	 * Activate this and return its output, inputs are assumed to have been statically inserted (use for starter components)
	 * ##DO NOT OVERRIDE
	 * if input is incorrect return null
	 */
	public default NonNullMap<EnumFacing, NBTTagCompound> activate(Spell spell, ServerPos modifiedPos) {
		return activate(spell, modifiedPos, this.getInputs());
	}
	
	public default NonNullMap<EnumFacing, NBTTagCompound> activate(Spell spell, Entity modifiedEntity, NonNullMap<EnumFacing, NBTTagCompound> inputs) {
		return activate(spell, new ServerPos(modifiedEntity), inputs);
	}
	
	/**
	 * Returns the output if this component is static
	 * @return
	 */
	public default NonNullMap<EnumFacing, NBTTagCompound> getStaticOutput(ServerPos modifiedPos) {
		
		return new NonNullMap<EnumFacing, NBTTagCompound>(NBTTagCompound::new);
	}
	
	public default NonNullMap<EnumFacing, NBTTagCompound> getStaticOutput(Entity modEntity, ServerPos modifiedPos) {
		return getStaticOutput(modifiedPos);
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
	 * @param modifiedPos TODO
	 * @return
	 */
	public int getRequiredPowerFromNBT(NonNullMap<EnumFacing, NBTTagCompound> tagsForSide, ServerPos modifiedPos);
	
	public default int getRequiredPowerFromNBT(NonNullMap<EnumFacing, NBTTagCompound> tagsForSide, Entity modifiedEntity) {
		return this.getRequiredPowerFromNBT(tagsForSide, new ServerPos(modifiedEntity));
	}
	
	
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
	
	/**
	 * Whether this component accepts an empty NBTTagCompound through this face
	 * @return
	 */
	public default boolean acceptsEmpty(EnumFacing face) {
		return false;
	}
	
	/**
	 * Whether this face is an output
	 * @param face
	 * @return
	 */
	public default boolean isOutput(EnumFacing face) {
		return false;
	}
	
	public default void setSpellSpace(SpellSpace space) {
	}
	
	public default String getString() {
		return this.getClass().getCanonicalName() + " at " + this.getPos();
	}
	
	
	public default void end(Spell runner, boolean success, BlockPos pos) {
		
	}
	

	public void resetInputs();
	
	public void resetOutputs();
	
}
