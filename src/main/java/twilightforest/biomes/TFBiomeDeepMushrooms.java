package twilightforest.biomes;


public class TFBiomeDeepMushrooms extends TFBiomeBase {

	public TFBiomeDeepMushrooms(BiomeProperties props) {
		super(props);

		this.rootHeight = 0.15F;
		this.heightVariation = 0.4F;
		
		getTFBiomeDecorator().setTreesPerChunk(1);
        
        getTFBiomeDecorator().setMushroomsPerChunk(12);
        getTFBiomeDecorator().setBigMushroomsPerChunk(8);
        
        getTFBiomeDecorator().myceliumPerChunk = 3;
        getTFBiomeDecorator().alternateCanopyChance = 0.9F;
        
	}
	
}
