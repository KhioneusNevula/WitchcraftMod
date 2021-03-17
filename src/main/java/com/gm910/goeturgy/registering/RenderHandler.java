package com.gm910.goeturgy.registering;



import com.gm910.goeturgy.entity.EntityCelestialBeam;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@EventBusSubscriber
public class RenderHandler {
	
	@SideOnly(Side.CLIENT)
	public static void registerEntRenderers() {
		System.out.println("Registering entity renderers");
		RenderingRegistry.registerEntityRenderingHandler(EntityCelestialBeam.class, new IRenderFactory<EntityCelestialBeam> () {
			@Override
			public Render<? super EntityCelestialBeam> createRenderFor(RenderManager manager) {
				
				return new EntityCelestialBeam.RenderCelestialBeam(manager);
			}
		});
		
		
	}

}
