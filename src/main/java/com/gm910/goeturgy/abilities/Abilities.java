package com.gm910.goeturgy.abilities;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.gm910.goeturgy.abilities.abilitytypes.Ability;
import com.gm910.goeturgy.abilities.abilitytypes.GodModeAbility;
import com.gm910.goeturgy.util.GMReflection;
import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Abilities must be declared as fields and include the name 'ability' in their declaration.
 * @author borah
 *
 */
public class Abilities implements INBTSerializable<NBTTagCompound> {

	public Entity entity;
	
	public EntityAbilities storage;
	
	public MinecraftServer server;
	
	public List<Ability> abilities = new ArrayList<>();
	
	public GodModeAbility abilityGodMode = new GodModeAbility(this);
	
	public Abilities(Entity entity, EntityAbilities abilityInstance, MinecraftServer server) {
		this.entity = entity;
		this.server = server;
		this.storage = abilityInstance;
		MinecraftForge.EVENT_BUS.register(this);
		manageSpecializedAbilities();
	}
	
	public void manageSpecializedAbilities() {
		List<Field> allFields = Lists.newArrayList(Abilities.class.getDeclaredFields());
		allFields.removeIf((field) -> {
			return !Ability.class.isAssignableFrom(field.getType()) && !Modifier.isPublic(field.getModifiers());
		});
		allFields.forEach((field) -> {
			field.setAccessible(true);
		});
		//System.out.println("Found field list for Abilities of entity " + entity.getClass().getSimpleName() + " : " + allFields);
		List<Field> abilityFields = new ArrayList<>(allFields);
		
		for (Field ab : abilityFields) {
			Class<?> clazz = ab.getType();
			Field forTypes = null;
			boolean notForTypes = false;
			try {
				forTypes = clazz.getDeclaredField("forTypes");
				forTypes.setAccessible(true);
				//System.out.println("Found field forTypes in " + clazz);
			} catch (NoSuchFieldException | SecurityException e) {
				try {
					forTypes = clazz.getDeclaredField("notForTypes");
					notForTypes = true;
					forTypes.setAccessible(true);
					//System.out.println("Found field notForTypes in " + clazz);
				} catch (NoSuchFieldException | SecurityException e1) {

					//System.out.println("Didn't find field (not)forTypes in " + clazz);
				}
			}

			if (forTypes == null) continue;
			int mods = forTypes.getModifiers();
			if (Modifier.isStatic(mods) && Modifier.isPrivate(mods) && Modifier.isFinal(mods) && (forTypes.getType().isAssignableFrom(List.class) || List.class.isAssignableFrom(forTypes.getType()))) {
				List<Class<?>> entityClazzes;
				try {
					entityClazzes = (List<Class<?>>)forTypes.get(ab.get(this));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				for (Class<?> checker : entityClazzes) {
					Class<?> entClazz = getEntity().getClass();
					if (checker.isAssignableFrom(entClazz)) {
						if (notForTypes) {
							try {
								ab.set(this, null);
								//System.out.println("Set " + ab + " to null for " + entity.getClass().getSimpleName());
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
					} else {
						if (!notForTypes) {
							try {
								ab.set(this, null);
								//System.out.println("Set " + ab + " to null for " + entity.getClass().getSimpleName());
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
					}
				}
			} else {
				//System.out.println("Didn't find field (not)forTypes in " + clazz  + " because its modifiers were " + Modifier.toString(mods));
				continue;
			}
		}
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public MinecraftServer getServer() {
		return server;
	}
	
	public EntityAbilities getStorage() {
		return storage;
	}
	
	public List<Ability> getAbilityTypes() {
		return new ArrayList<>(abilities);
	}
	
	public Abilities beforeDelete() {
		for (Ability a : abilities) {
			a.deregisterFromEventBus();
		}
		return this;
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		
		NBTTagList ls = new NBTTagList();
		
		for (Ability a : abilities) {
			NBTTagCompound cmp = new NBTTagCompound();
			//cmp.setString("Class", a.getClass().getName());
			cmp.setTag("Data", a.serializeNBT());
		}
		
		nbt.setTag("List", ls);
		
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		NBTTagList list = nbt.getTagList("List", NBT.TAG_COMPOUND);
		
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound cmp = list.getCompoundTagAt(i);
			abilities.get(i).deserializeNBT(cmp.getCompoundTag("Data"));
			
		}
		
	}
	
	

}
