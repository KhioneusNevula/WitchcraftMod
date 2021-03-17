package com.gm910.goeturgy.keyhandling.conflict;

import com.gm910.goeturgy.Goeturgy;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;

public class GodModeKeyConflictContext implements IKeyConflictContext {

	@Override
	public boolean isActive() {
		
		return godModeActive();
	}
	
	public static boolean godModeActive() {
		return Goeturgy.proxy.getAsClientProxy().isGodMode;
	}

	@Override
	public boolean conflicts(IKeyConflictContext other) {
		
		return this == other;
	}

	public static class GodModeConsiderateUniversal implements IKeyConflictContext {

		@Override
		public boolean isActive() {
			// TODO Auto-generated method stub
			return !godModeActive();
		}

		@Override
		public boolean conflicts(IKeyConflictContext other) {
			// TODO Auto-generated method stub
			return !(other instanceof GodModeKeyConflictContext);
		}
		
	}
}
