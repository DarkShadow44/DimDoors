package org.dimdev.dimdoors.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;

public class LimboChunkGenerator extends ChunkGenerator {
	public static final Codec<LimboChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
	).apply(instance, instance.stable(LimboChunkGenerator::new)));
	public static final StructuresConfig EMPTY = new StructuresConfig(Optional.empty(), Collections.emptyMap());

	private LimboChunkGenerator(BiomeSource biomeSource) {
		this(biomeSource, ThreadLocalRandom.current().nextLong());
	}

	private LimboChunkGenerator(BiomeSource biomeSource, long worldSeed) {
		super(biomeSource, biomeSource, EMPTY, worldSeed);
	}

	@Override
	protected Codec<? extends ChunkGenerator> getCodec() {
		return CODEC;
	}

	@Override
	public ChunkGenerator withSeed(long seed) {
		return new LimboChunkGenerator(this.getBiomeSource(), seed);
	}

	@Override
	public MultiNoiseUtil.MultiNoiseSampler getMultiNoiseSampler() {
		// TODO
		return null;
	}

	@Override
	public void carve(ChunkRegion chunkRegion, long seed, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver generationStep) {
		// TODO
	}

	@Override
	public void buildSurface(ChunkRegion region, StructureAccessor structures, Chunk chunk) {
		// TODO
	}

	@Override
	public void populateEntities(ChunkRegion region) {
		// TODO
	}

	@Override
	public int getWorldHeight() {
		return 176;
	}

	@Override
	public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, StructureAccessor structureAccessor, Chunk chunk) {
		// TODO
		return CompletableFuture.completedFuture(chunk);
	}

	@Override
	public int getSeaLevel() {
		return 32;
	}

	@Override
	public int getMinimumY() {
		return 0;
	}

	@Override
	public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world) {
		// TODO
		return 0;
	}

	@Override
	public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world) {
		// TODO
		return null;
	}
}
