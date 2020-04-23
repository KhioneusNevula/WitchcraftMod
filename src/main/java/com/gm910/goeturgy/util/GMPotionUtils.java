package com.gm910.goeturgy.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class GMPotionUtils {

	public static boolean addEffectIfNotPresent(EntityLivingBase e, Potion p, int duration, int amplifier) {
		boolean b = e.getActivePotionEffect(p) == null;
		if (b) {
			e.addPotionEffect(new PotionEffect(p, duration, amplifier));
		}
		return !b;
	}
	
	public static boolean addEffectIfNotPresent(EntityLivingBase e, Potion p, int duration) {
		return addEffectIfNotPresent(e, p, duration, 0);
	}
	
	public static boolean addEffectIfNotPresent(EntityLivingBase e, String p, int duration) {
		return addEffectIfNotPresent(e, Potion.getPotionFromResourceLocation(p), duration, 0);
	}
	
	public static boolean addEffectIfNotPresent(EntityLivingBase e, String p, int duration, int amplifier) {
		return addEffectIfNotPresent(e, Potion.getPotionFromResourceLocation(p), duration, amplifier);
	}
}
