package com.gm910.goeturgy.util;

import org.lwjgl.opengl.GL11;

import com.gm910.goeturgy.Goeturgy;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
	
	public static void drawEntity(float partialTicks, Entity en, double x, double y, double z, float yaw, float scale) {
		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
		
		double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
		double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
		double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
		
		Tessellator.getInstance().getBuffer().setTranslation(-d0, -d1, -d2);
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.translate(0.0F, 0.4F, 0.0F);
		GlStateManager.rotate(45f, 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(0.0F, -0.2F, 0.0F);
		GlStateManager.rotate(-30.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.scale(scale, scale, scale);

		//GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);

		
		BufferBuilder buf = Tessellator.getInstance().getBuffer();
		//buf.begin(7, DefaultVertexFormats.BLOCK);
		//BlockRendererDispatcher renderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		RenderManager ren = Minecraft.getMinecraft().getRenderManager();
		ren.renderEntity(en, x, y, z, yaw, partialTicks, false);
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
	

	public static class RenderBlockShape {
		public final World world;
		public final BlockPos pos;
		public final EntityPlayer player;
		public int red, green, blue;
		public String tex;
		
		public RenderBlockShape(World world, EntityPlayer player, BlockPos pos, int r, int g, int b, String tex) {
			this.world = world;
			this.player = player;
			this.pos = pos;
			this.red = r;
			this.green = g;
			this.blue = b;
			this.tex = tex;
		}
		
		public void render(float partialTicks) {
			render(partialTicks, EnumFacing.VALUES);
		}
		
		public void render(float partialTicks, EnumFacing...sides) {
			double offsetX = player.prevPosX + (player.posX - player.prevPosX) * (double) partialTicks;
		    double offsetY = player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks;
		    double offsetZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) partialTicks;
		    Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Goeturgy.MODID + ":textures/" + tex));
		    
		    IBlockState state = world.getBlockState(pos);
		    /*
		    GlStateManager.pushMatrix();
		    GlStateManager.translate(pos.getX() - offsetX, pos.getY() - offsetY, pos.getZ() - offsetZ);
		    GlStateManager.enableTexture2D();

		    GlStateManager.enableBlend();
		    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
		    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		    GlStateManager.color(red, green, blue, 0.7f);
		    GlStateManager.disableDepth();
		    
		    */
		    AxisAlignedBB box = (!state.getBlock().isAir(state, world, pos) ? state.getBlock().getBoundingBox(state, world, pos).grow(0, -0.5, 0).offset(0, -0.15, 0) : Block.FULL_BLOCK_AABB.grow(0, -0.5, 0).offset(0, -0.15, 0));
		    Tessellator tessellator = Tessellator.getInstance();
		    BufferBuilder buffer = tessellator.getBuffer();
		    
		    GlStateManager.pushMatrix();
	        GlStateManager.translate(pos.getX() - offsetX, pos.getY() - offsetY, pos.getZ() - offsetZ);

	        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

	        GlStateManager.enableBlend();
	        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

	        for (EnumFacing f : sides) {

		        this.addSideFullTexture(buffer, f.ordinal(), 1.1f, -0.05f);
	        }

	        tessellator.draw();
	        GlStateManager.popMatrix();

		    /*
		    r.begin(3, DefaultVertexFormats.POSITION); //3 //(int)(Math.random()*3)
		    
		    r.pos(box.minX, box.minY, box.minZ).tex(0, 0).endVertex();
		    r.pos(box.maxX, box.minY, box.minZ).tex(0.1, -0.05).endVertex();
		    r.pos(box.maxX, box.minY, box.maxZ).tex(0.1, 0).endVertex();
		    r.pos(box.minX, box.minY, box.maxZ).tex(0, -0.05).endVertex();
		    r.pos(box.minX, box.minY, box.minZ).endVertex();
		    tess.draw();
		    
		    r.begin(3, DefaultVertexFormats.POSITION); //3 //(int)(Math.random()*3)
		    r.pos(box.minX, box.maxY, box.minZ).endVertex();
		    r.pos(box.maxX, box.maxY, box.minZ).endVertex();
		    r.pos(box.maxX, box.maxY, box.maxZ).endVertex();
		    r.pos(box.minX, box.maxY, box.maxZ).endVertex();
		    r.pos(box.minX, box.maxY, box.minZ).endVertex();
		    tess.draw();
		    
		    r.begin(1, DefaultVertexFormats.POSITION); //1 //(int)(Math.random()*8)
		    r.pos(box.minX, box.minY, box.minZ).endVertex();
		    r.pos(box.minX, box.maxY, box.minZ).endVertex();
		    r.pos(box.maxX, box.minY, box.minZ).endVertex();
		    r.pos(box.maxX, box.maxY, box.minZ).endVertex();
		    r.pos(box.maxX, box.minY, box.maxZ).endVertex();
		    r.pos(box.maxX, box.maxY, box.maxZ).endVertex();
		    r.pos(box.minX, box.minY, box.maxZ).endVertex();
		    r.pos(box.minX, box.maxY, box.maxZ).endVertex();
		    tess.draw();

		    */
		    /*GlStateManager.enableDepth();
		    GlStateManager.disableBlend();
		    GlStateManager.enableTexture2D();
		    GlStateManager.popMatrix();*/
		    
	  }
		
		private static final Quad[] quads = new Quad[] {
	            new Quad(new Vt(0, 0, 0), new Vt(1, 0, 0), new Vt(1, 0, 1), new Vt(0, 0, 1)),       // DOWN
	            new Quad(new Vt(0, 1, 1), new Vt(1, 1, 1), new Vt(1, 1, 0), new Vt(0, 1, 0)),       // UP
	            new Quad(new Vt(1, 1, 0), new Vt(1, 0, 0), new Vt(0, 0, 0), new Vt(0, 1, 0)),       // NORTH
	            new Quad(new Vt(1, 0, 1), new Vt(1, 1, 1), new Vt(0, 1, 1), new Vt(0, 0, 1)),       // SOUTH
	            new Quad(new Vt(0, 0, 1), new Vt(0, 1, 1), new Vt(0, 1, 0), new Vt(0, 0, 0)),       // WEST
	            new Quad(new Vt(1, 0, 0), new Vt(1, 1, 0), new Vt(1, 1, 1), new Vt(1, 0, 1)),       // EAST
	    };

	    /*public void addSideFullTexture(BufferBuilder buffer, int side, float mult, float offset, Vec3d offs) {
	        int brightness = 240;
	        int b1 = brightness >> 16 & 65535;
	        int b2 = brightness & 65535;
	        Quad quad = quads[side];
	        buffer.pos(offs.x + quad.v1.x * mult + offset, offs.y + quad.v1.y * mult + offset, offs.z + quad.v1.z * mult + offset).tex(0, 0).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
	        buffer.pos(offs.x + quad.v2.x * mult + offset, offs.y + quad.v2.y * mult + offset, offs.z + quad.v2.z * mult + offset).tex(0, 1).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
	        buffer.pos(offs.x + quad.v3.x * mult + offset, offs.y + quad.v3.y * mult + offset, offs.z + quad.v3.z * mult + offset).tex(1, 1).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
	        buffer.pos(offs.x + quad.v4.x * mult + offset, offs.y + quad.v4.y * mult + offset, offs.z + quad.v4.z * mult + offset).tex(1, 0).lightmap(b1, b2).color(255, 255, 255, 128).endVertex();
	    }*/

	    public void addSideFullTexture(BufferBuilder buffer, int side, float mult, float offset) {
	        int brightness = 240;
	        int b1 = brightness >> 16 & 65535;
	        int b2 = brightness & 65535;
	        Quad quad = quads[side];
	        buffer.pos(quad.v1.x * mult + offset, quad.v1.y * mult + offset, quad.v1.z * mult + offset).tex(0, 0).lightmap(b1, b2).color(red/2, green/2, blue/2, 0.7f).endVertex();
	        buffer.pos(quad.v2.x * mult + offset, quad.v2.y * mult + offset, quad.v2.z * mult + offset).tex(0, 1).lightmap(b1, b2).color(red/2, green/2, blue/2, 0.7f).endVertex();
	        buffer.pos(quad.v3.x * mult + offset, quad.v3.y * mult + offset, quad.v3.z * mult + offset).tex(1, 1).lightmap(b1, b2).color(red/2, green/2, blue/2, 0.7f).endVertex();
	        buffer.pos(quad.v4.x * mult + offset, quad.v4.y * mult + offset, quad.v4.z * mult + offset).tex(1, 0).lightmap(b1, b2).color(red/2, green/2, blue/2, 0.7f).endVertex();
	    }

	    
	}

	private static class Vt {
        public final float x;
        public final float y;
        public final float z;

        public Vt(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static class Quad {
        public final Vt v1;
        public final Vt v2;
        public final Vt v3;
        public final Vt v4;

        public Quad(Vt v1, Vt v2, Vt v3, Vt v4) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
        }

        public Quad rotate(EnumFacing direction) {
            switch (direction) {
                case NORTH: return new Quad(v4, v1, v2, v3);
                case EAST: return new Quad(v3, v4, v1, v2);
                case SOUTH: return new Quad(v2, v3, v4, v1);
                case WEST: return this;
                default: return this;
            }
        }
    }
	
}
