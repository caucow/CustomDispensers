package com.caucraft.customdispensers;

import org.bukkit.block.BlockFace;

import java.util.Arrays;

public enum Direction {
    HORIZONTAL(new BlockFace[] {BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST}),
    VERTICAL(new BlockFace[] {BlockFace.UP, BlockFace.DOWN}),
    UP(new BlockFace[] {BlockFace.UP}),
    DOWN(new BlockFace[] {BlockFace.DOWN}),
    ALL(new BlockFace[] {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST});

    private final BlockFace[] faces;
    // lmao why
    // lmao why not
    private final boolean[] facemask;

    Direction(BlockFace[] faces) {
        this.faces = Arrays.copyOf(faces, faces.length);
        this.facemask = new boolean[BlockFace.values().length];
        for (BlockFace face : faces) {
            this.facemask[face.ordinal()] = true;
        }
    }

    public BlockFace[] getFaces() {
        return Arrays.copyOf(faces, faces.length);
    }

    public boolean matchesFace(BlockFace face) {
        return facemask[face.ordinal()];
    }
}
