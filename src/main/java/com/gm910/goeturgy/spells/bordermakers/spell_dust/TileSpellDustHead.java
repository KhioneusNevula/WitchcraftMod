package com.gm910.goeturgy.spells.bordermakers.spell_dust;

import java.util.ArrayList;
import java.util.List;

import com.gm910.goeturgy.spells.spellspaces.SpellSpaces;
import com.gm910.goeturgy.util.IObjectMouseoverGui;
import com.gm910.goeturgy.util.ServerPos;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

public class TileSpellDustHead extends TileSpellDust implements IObjectMouseoverGui {

	public static int TOLERANCE = 7000;
	
	public static int HEIGHT = 10;

	private ShapeMaker shapeMaker = null;
	private DustPulse endPulse = null;
	public final DustPulse FAILED = new DustPulse(new BlockPos(0,0,0));
	
	@Override
	public void update() {
		if (!world.isRemote) {
			//System.out.println("Head " + this.hashCode() + " with id " + getSpaceID());
			if (this.getSpaceID() == -1) {
				System.out.println("Attempting generation of spellspace");
				SpellSpaces.get().forEach((d) -> {
					//System.out.println(d.getHeadPos());
				});
				if (shapeMaker == null) {
					shapeMaker = new ShapeMaker(TOLERANCE);
					shapeMaker.start();
				} else {
					endPulse = shapeMaker.tick();
				/*
				List<DustPulse> pulses = new ArrayList<>();
				DustPulse pulse1 = new DustPulse(pos);
				DustPulse endPulse = null;
				pulses.add(pulse1);
				for (int i = 0; i < TOLERANCE; i++) {
					List<DustPulse> ps = new ArrayList<>(pulses);
					for (DustPulse p : ps) {
						int end = p.update(pulses);
						if (end == 0) {
							pulses.remove(p);
							continue;
						} else if (end == 2) {
							endPulse = p;
							break;
						}
					}
					if (endPulse != null) {
						break;
					}
				}*/
				
					if (endPulse != null) {
						if (endPulse == FAILED) {

							System.out.println("Failed to generate spellspace");
						} else {
							SpellSpaces.get().createSpellSpace(world.provider.getDimension(), HEIGHT, this.pos, endPulse.visited, endPulse.visited);
						}

						shapeMaker = null;
					}
				}
			} else {
				//System.out.println("Hasspellspace");
				if (getSpellSpace() == null) {
					if (SpellSpaces.get().getByPosition(new ServerPos(pos, world)) != null) {
						this.figure = SpellSpaces.get().getByPosition(getServerPos()).getID();
					}
					System.out.println("Spellspace has not been created but it clearly exists...?");
					world.createExplosion(null, pos.getX()+0.5, pos.getY(), pos.getZ()+0.5, 0.2f, false);
					//this.setSpaceID(-1);
					return;
				}
				List<BlockPos> ol = getSpellSpace().getInnerSpace();
				for (BlockPos pos : ol) {
					//if (world.isAirBlock(pos) || world.getBlockState(pos).getMaterial() == Material.ROCK)
					//world.setBlockState(pos, Blocks.BARRIER.getDefaultState());
				}
			}
			
		} else {
			//not remote
			
			if (clientSpace != null && this.figure != -1) {
				
				for (BlockPos pos : clientSpace.getOutline()) {
					world.spawnParticle(EnumParticleTypes.TOWN_AURA, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 0, 0, 0);
				}
			} else {
				//System.out.println("No spellspace??");
			}
		}
		
		super.update();
	}
	
	public TileSpellDust getDust(BlockPos pos) {
		return world.getTileEntity(pos) instanceof TileSpellDust ? (TileSpellDust)world.getTileEntity(pos) : null;
	}
	
	public class ShapeMaker {
		
		public List<BlockPos> visited;
		public int tickNumber;
		List<DustPulse> pulses;
		DustPulse pulse1;
		DustPulse endPulse;
		int tolerance;
		TileSpellDustHead head;
		
		public ShapeMaker(int tolerance) {
			tickNumber = 0;
			visited = new ArrayList<BlockPos>();
			pulses = new ArrayList<>();
			head = TileSpellDustHead.this;
			pulse1 = new DustPulse(head.pos);
			endPulse = null;
			pulses.add(pulse1);
		}
		
		public void start() {
			tickNumber = 1;
		}
		
		/**
		 * Returns the pulse known as 'end', if this is not null we are successful
		 * @return
		 */
		public DustPulse tick() {
			if (tickNumber > TOLERANCE) {
				return FAILED;
			}
			List<DustPulse> ps = new ArrayList<>(pulses);
			for (DustPulse p : ps) {
				int end = p.update(pulses);
				if (end == 0) {
					pulses.remove(p);
					continue;
				} else if (end == 2) {
					endPulse = p;
					break;
				}
			}
			
			tickNumber++;
			return endPulse;
		}
	}
	
	public class DustPulse {
		
		
		
		BlockPos startPos;
		BlockPos curPos;
		List<BlockPos> visited = new ArrayList<BlockPos>();
		
		public DustPulse(BlockPos p) {
			this.startPos = p;
			curPos = p;
		}

		/**
		 * 0 means it failed
		 * 1 means it can continue
		 * 2 means it found the other sideeeee
		 * @param otherPulses
		 * @return
		 */
		public int update(List<DustPulse> otherPulses) {
			List<EnumFacing> faces = new ArrayList<>();
			for (EnumFacing f : EnumFacing.HORIZONTALS) {
				BlockPos neo = curPos.offset(f);
				if (getDust(neo) != null && !visited.contains(neo)) {
					if (getDust(neo) instanceof TileSpellDustHead) {
						if (getDust(neo) != TileSpellDustHead.this) {
							return 0;
						}
					}
					faces.add(f);
				} else if (neo.equals(startPos) && visited.size() > 2) {
					visited.add(curPos);
					return 2;
				}
			}
			if (faces.size() > 0) {
				BlockPos curprev = curPos;
				for (int i = 0; i < faces.size(); i++) {
					if (i == 0) {
						visited.add(this.curPos);
						this.curPos = this.curPos.offset(faces.get(i));
						
					} else {
						DustPulse pls2 = new DustPulse(this.startPos);
						pls2.visited.addAll(visited);
						pls2.curPos = curprev.offset(faces.get(i));
						//world.setBlockState(curPos.up(), Blocks.STONE.getDefaultState());
						otherPulses.add(pls2);
					}
				}
				return 1;
			} else {
				return 0;
			}
			
		}
	}

	@Override
	public void drawGuiOverlays(Pre event, Minecraft mc, Gui gui, TextureManager tex, ScaledResolution res,
			EnumFacing sideHit, IBlockState state, Vec3d hitVec, BlockPos position, TileEntity tile, Object object) {
		if (this.clientSpace == null || this.figure == -1) {
			//System.out.println("ugghghg");
			return;
		}
		//if (this.clientSpace.magickingPos == null) return;
		//BlockPos pos = clientSpace.magickingPos;
		//DrawEffects.drawBlock(event.getPartialTicks(), Blocks.FIRE.getDefaultState(), pos.up(), 1);
		
		
	}
	
	@Override
	public boolean isHeadPiece() {
		// TODO Auto-generated method stub
		return true;
	}
	
}
