package com.gm910.goeturgy.util;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@EventBusSubscriber
public class DrawEffects {

	/**
	 * Finds the block, tile, or entity player is looking at and checks if it is instanceof IObjectMouseoverGui and then draws the necessary overlays
	 * Order: Check if entity, check if tile entity, check if block
	 * @param event
	 */
	@SubscribeEvent
	public static void render(RenderGameOverlayEvent.Pre event) {
		Minecraft mc = Minecraft.getMinecraft();
		
		EntityPlayerSP play = Minecraft.getMinecraft().player;
		RayTraceResult trace = Minecraft.getMinecraft().objectMouseOver;//play.world.rayTraceBlocks(play.getPositionVector().addVector(0, play.eyeHeight, 0), play.getLookVec(), false);
		
		Gui gui = mc.ingameGUI;
		TextureManager tex = mc.getTextureManager();
		
		ScaledResolution res = new ScaledResolution(mc);
		
		if (trace != null) {
			if (trace.typeOfHit == RayTraceResult.Type.ENTITY) {
				
				Entity entity = trace.entityHit;
				
				if (entity instanceof IObjectMouseoverGui) {
					((IObjectMouseoverGui)entity).drawGuiOverlays(event, mc, gui, tex, res, trace.sideHit, null, null, null, null, entity);
				} /*else if (MouseoverEntry.ENTITIES.get(entity.getClass()) != null) {
					MouseoverEntry.ENTITIES.get(entity.getClass()).drawGuiOverlays(event, mc, gui, tex, res, trace.sideHit, null, null, null, null, entity);
				}*/
			}
			else if (trace.typeOfHit == RayTraceResult.Type.BLOCK) {
				BlockPos position = trace.getBlockPos();
				World world = mc.world;
				
				//PlayerControllerMP controller = Minecraft.getMinecraft().playerController;
				
				
				IBlockState state = world.getBlockState(position);
				TileEntity tile = world.getTileEntity(position);
				
				if (tile != null) {
					if (tile instanceof IObjectMouseoverGui) {
						
						((IObjectMouseoverGui)tile).drawGuiOverlays(event, mc, gui, tex, res, trace.sideHit, state, trace.hitVec, position, tile, null);
						
					} /* else if (MouseoverEntry.TILEENTITIES.get(tile.getClass()) != null) {
						MouseoverEntry.TILEENTITIES.get(tile.getClass()).drawGuiOverlays(event, mc, gui, tex, res, trace.sideHit, state, trace.hitVec, position, tile, null);
					}*/
				}
				if (state.getBlock() instanceof IObjectMouseoverGui) {
					((IObjectMouseoverGui)state.getBlock()).drawGuiOverlays(event, mc, gui, tex, res, trace.sideHit, state, trace.hitVec, position, world.getTileEntity(position), null);
				} /*else if (MouseoverEntry.BLOCKS.get(state.getBlock()) != null) {
					MouseoverEntry.BLOCKS.get(state.getBlock()).drawGuiOverlays(event, mc, gui, tex, res, trace.sideHit, state, trace.hitVec, position, tile, null);
				}*/
			}
		}
	}
	
	public static void drawBlock(float partialTicks, IBlockState state, BlockPos pos, float alpha) {
		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
		
		double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
		double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
		double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
		
		Tessellator.getInstance().getBuffer().setTranslation(-d0, -d1, -d2);
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		

		//GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);

		GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);
		
		BufferBuilder buf = Tessellator.getInstance().getBuffer();
		buf.begin(7, DefaultVertexFormats.BLOCK);
		BlockRendererDispatcher renderer = Minecraft.getMinecraft().getBlockRendererDispatcher();

		renderer.getBlockModelRenderer().renderModel(Minecraft.getMinecraft().player.world, renderer.getModelForState(state), state, pos, buf, false);
				
		Tessellator.getInstance().draw();
		
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		

		//GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
		
		GlStateManager.enableLighting();
		GlStateManager.disableAlpha();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		Tessellator.getInstance().getBuffer().setTranslation(0, 0, 0);
		
	}
	
	public static void drawEntity(float partialTicks, Entity en, float scale) {
		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
		
		double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
		double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
		double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
		
		Tessellator.getInstance().getBuffer().setTranslation(-d0, -d1, -d2);
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.scale(scale, scale, scale);

		//GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);

		
		BufferBuilder buf = Tessellator.getInstance().getBuffer();
		buf.begin(7, DefaultVertexFormats.BLOCK);
		//BlockRendererDispatcher renderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		RenderManager ren = Minecraft.getMinecraft().getRenderManager();
		ren.renderEntity(en, en.posX, en.posY, en.posZ, en.rotationYaw, partialTicks, false);
		//renderer.getBlockModelRenderer().renderModel(Minecraft.getMinecraft().player.world, renderer.getModelForState(state), state, pos, buf, false);type name = new type();
		
		Tessellator.getInstance().draw();
		
		
		GlStateManager.scale(1/scale, 1/scale, 1/scale);
		//GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
		
		GlStateManager.enableLighting();
		GlStateManager.disableAlpha();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		Tessellator.getInstance().getBuffer().setTranslation(0, 0, 0);
		
	}
	
	public static void drawItem(Minecraft minecraft, ItemStack stack, int x, int y, float width, float height) { 
		//minecraft.getRenderItem().renderItemAndEffectIntoGUI(stack, xPosition, yPosition);
		RenderItem itemRender = minecraft.getRenderItem();
		RenderHelper.disableStandardItemLighting();
		RenderHelper.enableGUIStandardItemLighting();
        //GlStateManager.translate(0.0F, 0.0F, 32.0F);
		//GlStateManager.scale(4.0f, 4, 1);
		float sf = width/16f;
		float sfy = height/16f;
        GL11.glScalef(sf, sfy, 1.0F);
        
        //itemRender.zLevel = 200.0F;
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = minecraft.fontRenderer;
        
		
        itemRender.renderItemIntoGUI(stack, (int)(x*(1.0/sf)), (int)(y*(1.0/sfy)));
        itemRender.renderItemOverlayIntoGUI(font, stack, (int)(x*(1.0/sf)), (int)(y*(1.0/sfy)), "");
        GlStateManager.disableLighting();
        //GlStateManager.scale(1.0f, 1, 1);
        GL11.glScalef(1.0f/sf, 1.0f/sfy, 1.0F);
        //GlStateManager.translate(0.0f, 0.0f, 0.0f);
        //itemRender.zLevel = 0.0F;
		
        RenderHelper.enableStandardItemLighting();
        
        GlStateManager.enableLighting();
		RenderHelper.enableGUIStandardItemLighting();
	}
	
	
}
