package com.gm910.goeturgy.abilities.abilitytypes;

import java.util.List;

import com.gm910.goeturgy.abilities.Abilities;
import com.gm910.goeturgy.events.ServerSideKeyEvent;
import com.gm910.goeturgy.keyhandling.ModKey;
import com.gm910.goeturgy.messages.Messages;
import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GodModeAbility extends Ability {
	
	private final static List<Class<?>> forTypes = Lists.newArrayList(EntityPlayerMP.class);

	private boolean inGodMode = false;
	
	/**
	 * Used to store capabilities player previously had for switching out of godmode
	 * IsCreative, allowFlying, allowEdit, disableDamage, isFlying
	 */
	private boolean[] previousCapabilities = new boolean[] {false, false, true, false, false};
	
	/**
	 * Used to store the previous gamemode
	 */
	private GameType previousMode = GameType.SURVIVAL;
	
	/**
	 * The capabilities comprising god mode
	 * isCreative, allowFlying, allowEdit, disableDamage, isFlying
	 * @author borah
	 *
	 */
	public static final boolean[] godCapabilities = new boolean[] {true, true, true, true, true};
	
	/**
	 * The game mode for god mode
	 * @author borah
	 *
	 */
	public static final GameType godGameMode = GameType.SPECTATOR;
	
	public static enum Power {
		GOD_WATER,
		GOD_TIME_FORWARD,
		GOD_TIME_BACKWARD,
		GOD_INSTANT_BONEMEAL,
		GOD_CONTROL_MOB,
		GOD_SPAWN_DRAGON,
		GOD_SPAWN_WITHER,
		GOD_NBT_EDIT,
		GOD_LIGHTNING,
		GOD_EXPLOSION;
		public static Power cycleRight(Power other) {
			int index = other.ordinal();
			if (index + 1 >= values().length) {
				return values()[0];
			} else {
				return values()[index + 1];
			}
		}
		
		public static Power cycleLeft(Power other) {
			int index = other.ordinal();
			if (index - 1 < 0) {
				return values()[values().length - 1];
			} else {
				return values()[index - 1];
			}
		}
	}
	
	private Power power = Power.GOD_LIGHTNING;
	
	
	public GodModeAbility(Abilities parent) {
		super(parent);
		if (!(parent.getEntity() instanceof EntityPlayerMP)) {
			parent.abilityGodMode = null;
			MinecraftForge.EVENT_BUS.unregister(this);
		}
	}
	
	public void storeGameModeData(EntityPlayer player) {
		this.previousCapabilities = new boolean[] {
				player.capabilities.isCreativeMode,
				player.capabilities.allowFlying, 
				player.capabilities.allowEdit,
				player.capabilities.disableDamage,
				player.capabilities.isFlying
		};
	}
	
	public void setToPreviousGameMode(EntityPlayerMP player) {

		player.interactionManager.setGameType(previousMode);
		player.capabilities.isCreativeMode = previousCapabilities[0];
		player.capabilities.allowFlying = previousCapabilities[1];
		player.capabilities.allowEdit = previousCapabilities[2];
		player.capabilities.disableDamage = previousCapabilities[3];
		player.capabilities.isFlying = previousCapabilities[4];
	}
	
	public void setToGodMode(EntityPlayerMP player) {
		
		player.interactionManager.setGameType(godGameMode);
		player.capabilities.isCreativeMode = godCapabilities[0];
		player.capabilities.allowFlying = godCapabilities[1];
		player.capabilities.allowEdit = godCapabilities[2];
		player.capabilities.disableDamage = godCapabilities[3];
		player.capabilities.isFlying = godCapabilities[4];
	}
	
	public EntityPlayerMP getPlayer() {
		return (EntityPlayerMP) parent.getEntity();
	}
	
	public Power getPower() {
		return power;
	}
	
	public void cyclePowerRight() {
		this.power = Power.cycleRight(this.power);
	}
	
	public void cyclePowerLeft() {
		this.power = Power.cycleLeft(this.power);
	}
	
	public void setPower(Power power) {
		this.power = power;
	}
	
	@SubscribeEvent
	public void keyPress(ServerSideKeyEvent event) {
		
		if (event.player != getPlayer().getUniqueID()) {
			return;
		}
		if (inGodMode) {
			if (isPressed(event.key, ModKey.CYCLE_LEFT)) {
				cyclePowerLeft();
			}
			if (isPressed(event.key, ModKey.CYCLE_RIGHT)) {
				cyclePowerRight();
			}
			if (isPressed(event.key, ModKey.USE_GOD_POWER)) {
				usePower();
			}
			
			
			if (isPressed(event.key, ModKey.EXIT_GOD)) {
				this.setInGodMode(false);
				return;
			}
		} else {
			if (isPressed(event.key, ModKey.BECOME_GOD)) {
				System.out.println("changing to god mode");
				this.setInGodMode(true);
				return;
			}
		}
	}
	
	@SubscribeEvent
	public void tick(LivingUpdateEvent event) {
		if (!(parent.entity instanceof EntityPlayerMP)) {
			MinecraftForge.EVENT_BUS.unregister(this);
			return;
		}
		if (event.getEntity() != this.getPlayer()) {
			return;
		}
		if (inGodMode) {
			this.getPlayer().sendStatusMessage(new TextComponentTranslation("godpower." + this.power.name().toLowerCase()), true);
		}
	}
	
	public void usePower() {

		RayTraceResult result = getPlayer().rayTrace(getPlayer().getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue(), 1.0f);
		BlockPos to = result.getBlockPos();
		Entity toE = result.entityHit;
		if (power == Power.GOD_CONTROL_MOB && toE != null) {
			getPlayer().setSpectatingEntity(toE);
		} else if (power == Power.GOD_LIGHTNING && (to != null || toE != null)) {
			BlockPos hit = null;
			if (toE != null) {
				hit = new BlockPos(toE);
			} else {
				hit = to;
			}
			getPlayer().world.addWeatherEffect(new EntityLightningBolt(getPlayer().world, hit.getX(), hit.getY(), hit.getZ(), false));
		} else {
			this.getPlayer().sendStatusMessage(new TextComponentTranslation("godpower.failed", this.power.name()), true);
		}
	}

	public boolean isPressed(int given, ModKey key) {
		return given == ModKey.keys.get(key).getKeyCode();
	}
	
	public boolean isInGodMode() {
		return inGodMode;
	}
	
	public void setInGodMode(boolean inGodMode) {
		if (!this.inGodMode && inGodMode) {
			this.storeGameModeData(this.getPlayer());
			this.setToGodMode(this.getPlayer());
		} else if (this.inGodMode && !inGodMode) {
			this.setToPreviousGameMode(this.getPlayer());
		}
		this.inGodMode = inGodMode;
		if (inGodMode) {
			this.getPlayer().sendMessage(new TextComponentTranslation("ingodmode"));
		}
		Messages.changeGodMode(getPlayer(), inGodMode);
		
	}
	

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound cmp = new NBTTagCompound();
		cmp.setBoolean("InMode", inGodMode);
		byte[] bytes = new byte[previousCapabilities.length];
		for (int i = 0; i < previousCapabilities.length; i++) {
			bytes[i] = (byte) (previousCapabilities[i] ? 1 : 0);
		}
		NBTTagByteArray array = new NBTTagByteArray(bytes);
		cmp.setTag("PrevCaps", array);
		cmp.setInteger("PrevGameType", this.previousMode.getID());
		cmp.setString("Power", power.name());
		return cmp;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		inGodMode = nbt.getBoolean("InMode");
		byte[] bytes = nbt.getByteArray("PrevCaps");
		for (int i = 0; i < bytes.length; i++) {
			previousCapabilities[i] = bytes[i] != 0;
		}
		previousMode = GameType.getByID(nbt.getInteger("PrevGameType"));
		power = Power.valueOf(nbt.getString("Power"));
	}

}
