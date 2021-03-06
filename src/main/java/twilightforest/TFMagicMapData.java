package twilightforest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TFMagicMapData extends MapData {

	public final Set<TFMapDecoration> tfDecorations = new HashSet<>();

	public TFMagicMapData(String name) {
		super(name);
	}

	@Override
	public void read(CompoundNBT cmp) {
		super.read(cmp);

		byte[] featureStorage = cmp.getByteArray("features");
		if (featureStorage.length > 0) {
			this.deserializeFeatures(featureStorage);
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT cmp) {
		cmp = super.write(cmp);

		if (this.tfDecorations.size() > 0) {
			cmp.putByteArray("features", serializeFeatures());
		}

		return cmp;
	}

	/**
	 * Checks existing features against the feature cache changes wrong ones
	 */
	public void checkExistingFeatures(World world) {
		List<TFMapDecoration> toRemove = new ArrayList<>();
		List<TFMapDecoration> toAdd = new ArrayList<>();

		for (TFMapDecoration coord : tfDecorations) {
			int worldX = (coord.getX() << this.scale - 1) + this.xCenter;
			int worldZ = (coord.getY() << this.scale - 1) + this.zCenter;

			int trueId = TFFeature.getFeatureID(worldX, worldZ, world);
			if (coord.featureId != trueId) {
				toRemove.add(coord);
				toAdd.add(new TFMapDecoration(trueId, coord.getX(), coord.getY(), coord.getRotation()));
			}
		}

		tfDecorations.removeAll(toRemove);
		tfDecorations.addAll(toAdd);
	}

	public void deserializeFeatures(byte[] arr) {
		this.tfDecorations.clear();

		for (int i = 0; i < arr.length / 3; ++i) {
			byte featureId = arr[i * 3];
			byte mapX = arr[i * 3 + 1];
			byte mapZ = arr[i * 3 + 2];
			byte mapRotation = 8;
			this.tfDecorations.add(new TFMapDecoration(featureId, mapX, mapZ, mapRotation));
		}
	}

	public byte[] serializeFeatures() {
		byte[] storage = new byte[this.tfDecorations.size() * 3];

		int i = 0;
		for (TFMapDecoration featureCoord : tfDecorations) {
			storage[i * 3] = (byte) featureCoord.featureId;
			storage[i * 3 + 1] = featureCoord.getX();
			storage[i * 3 + 2] = featureCoord.getY();
			i++;
		}

		return storage;
	}

	// VanillaCopy of super, but adjust origin
	@Override
	public void calculateMapCenter(double x, double z, int mapScale) {
		// magic maps are offset by 1024 from normal maps so that 0,0 is in the middle of the map containing those coords
		int mapSize = 128 * (1 << mapScale);
		int roundX = (int) Math.round(x / mapSize);
		int roundZ = (int) Math.round(z / mapSize);
		this.xCenter = roundX * mapSize;
		this.zCenter = roundZ * mapSize;
	}

	public static class TFMapDecoration extends MapDecoration {

		private static final ResourceLocation MAP_ICONS = TwilightForestMod.prefix("textures/gui/mapicons.png");

		final int featureId;

		public TFMapDecoration(int featureId, byte xIn, byte yIn, byte rotationIn) {
			super(Type.TARGET_X, xIn, yIn, rotationIn);
			this.featureId = featureId;
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public boolean render(int idx) {
			if (TFFeature.getFeatureByID(featureId).isStructureEnabled) {
				Minecraft.getInstance().textureManager.bindTexture(MAP_ICONS);
				GlStateManager.pushMatrix();
				GlStateManager.translatef(0.0F + getX() / 2.0F + 64.0F, 0.0F + getY() / 2.0F + 64.0F, -0.02F);
				GlStateManager.rotatef((float) (getRotation() * 360) / 16.0F, 0.0F, 0.0F, 1.0F);
				GlStateManager.scalef(4.0F, 4.0F, 3.0F);
				GlStateManager.translatef(-0.125F, 0.125F, 0.0F);
				float f1 = (float) (featureId % 8) / 8.0F;
				float f2 = (float) (featureId / 8) / 8.0F;
				float f3 = (float) (featureId % 8 + 1) / 8.0F;
				float f4 = (float) (featureId / 8 + 1) / 8.0F;
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferbuilder = tessellator.getBuffer();
				bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
				bufferbuilder.pos(-1.0D, 1.0D, (float) idx * -0.001F).tex((double) f1, (double) f2).endVertex();
				bufferbuilder.pos(1.0D, 1.0D, (float) idx * -0.001F).tex((double) f3, (double) f2).endVertex();
				bufferbuilder.pos(1.0D, -1.0D, (float) idx * -0.001F).tex((double) f3, (double) f4).endVertex();
				bufferbuilder.pos(-1.0D, -1.0D, (float) idx * -0.001F).tex((double) f1, (double) f4).endVertex();
				tessellator.draw();
				GlStateManager.popMatrix();
			}
			return true;
		}

		@Override
		public boolean equals(Object o) {
			if (super.equals(o) && o instanceof TFMapDecoration) {
				TFMapDecoration other = (TFMapDecoration) o;
				return this.featureId == other.featureId;
			}

			return false;
		}

		@Override
		public int hashCode() {
			return super.hashCode() * 31 + featureId;
		}
	}
}
