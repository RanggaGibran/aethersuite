package id.rnggagib.aethersuite.core.teleport;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.HashSet;
import java.util.Set;

public class SafeLocationFinder {
    private static final Set<Material> UNSAFE_MATERIALS = new HashSet<>();
    private static final Set<Material> NON_SOLID_MATERIALS = new HashSet<>();
    private static final int MAX_SEARCH_RADIUS = 5;
    
    static {
        // Unsafe materials to stand on
        UNSAFE_MATERIALS.add(Material.LAVA);
        UNSAFE_MATERIALS.add(Material.FIRE);
        UNSAFE_MATERIALS.add(Material.CAMPFIRE);
        UNSAFE_MATERIALS.add(Material.SOUL_CAMPFIRE);
        UNSAFE_MATERIALS.add(Material.MAGMA_BLOCK);
        UNSAFE_MATERIALS.add(Material.CACTUS);
        UNSAFE_MATERIALS.add(Material.SWEET_BERRY_BUSH);
        UNSAFE_MATERIALS.add(Material.WITHER_ROSE);
        UNSAFE_MATERIALS.add(Material.POWDER_SNOW);
        
        // Non-solid materials (can't stand on)
        NON_SOLID_MATERIALS.add(Material.AIR);
        NON_SOLID_MATERIALS.add(Material.CAVE_AIR);
        NON_SOLID_MATERIALS.add(Material.VOID_AIR);
        NON_SOLID_MATERIALS.add(Material.WATER);
        NON_SOLID_MATERIALS.add(Material.LAVA);
        NON_SOLID_MATERIALS.add(Material.FIRE);
        NON_SOLID_MATERIALS.add(Material.GRASS_BLOCK);
        NON_SOLID_MATERIALS.add(Material.TALL_GRASS);
        NON_SOLID_MATERIALS.add(Material.SEAGRASS);
        NON_SOLID_MATERIALS.add(Material.TALL_SEAGRASS);
    }
    
    /**
     * Finds a safe teleport location near the specified destination
     *
     * @param destination The target destination
     * @return A safe location, or null if none found
     */
    public static Location findSafeLocation(Location destination) {
        if (destination == null) {
            return null;
        }
        
        World world = destination.getWorld();
        if (world == null) {
            return null;
        }
        
        // Check if the original location is safe
        if (isSafeLocation(destination)) {
            return destination;
        }
        
        // Try to find a safe spot around the original location
        int x = destination.getBlockX();
        int y = destination.getBlockY();
        int z = destination.getBlockZ();
        
        // First, try to find a safe spot up or down
        for (int dy = 0; dy <= 5; dy++) {
            // Check above
            Location above = new Location(world, x + 0.5, y + dy + 0.1, z + 0.5, 
                                          destination.getYaw(), destination.getPitch());
            if (isSafeLocation(above)) {
                return above;
            }
            
            // Check below
            if (y - dy > 0) {  // Don't check below world bottom
                Location below = new Location(world, x + 0.5, y - dy + 0.1, z + 0.5, 
                                             destination.getYaw(), destination.getPitch());
                if (isSafeLocation(below)) {
                    return below;
                }
            }
        }
        
        // If we can't find a safe spot directly up or down, search in a spiral pattern
        for (int radius = 1; radius <= MAX_SEARCH_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    // Only check the perimeter of the current radius
                    if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
                        continue;
                    }
                    
                    // Check for a safe location at this x,z coordinate
                    Location location = findSafeYAt(world, x + dx, z + dz, y, destination.getYaw(), destination.getPitch());
                    if (location != null) {
                        return location;
                    }
                }
            }
        }
        
        // No safe location found
        return null;
    }
    
    /**
     * Finds a safe Y coordinate at the specified X,Z position
     */
    private static Location findSafeYAt(World world, int x, int z, int startY, float yaw, float pitch) {
        // First try at the same Y level
        Location loc = new Location(world, x + 0.5, startY + 0.1, z + 0.5, yaw, pitch);
        if (isSafeLocation(loc)) {
            return loc;
        }
        
        // Try searching up and down from the starting Y
        for (int dy = 1; dy <= 10; dy++) {
            // Check above
            Location above = new Location(world, x + 0.5, startY + dy + 0.1, z + 0.5, yaw, pitch);
            if (isSafeLocation(above)) {
                return above;
            }
            
            // Check below
            if (startY - dy > 0) {
                Location below = new Location(world, x + 0.5, startY - dy + 0.1, z + 0.5, yaw, pitch);
                if (isSafeLocation(below)) {
                    return below;
                }
            }
        }
        
        // If all else fails, try to find the highest block and stand on it
        int highestY = world.getHighestBlockYAt(x, z);
        Location highest = new Location(world, x + 0.5, highestY + 1.1, z + 0.5, yaw, pitch);
        if (isSafeLocation(highest)) {
            return highest;
        }
        
        return null;
    }
    
    /**
     * Checks if a location is safe for a player to teleport to
     */
    private static boolean isSafeLocation(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return false;
        }
        
        Block feet = location.getBlock();
        Block ground = feet.getRelative(BlockFace.DOWN);
        Block head = feet.getRelative(BlockFace.UP);
        
        // Make sure there's space for the player
        if (!isNonSolid(feet) || !isNonSolid(head)) {
            return false;
        }
        
        // Make sure the ground is solid and safe
        if (!ground.getType().isSolid() || UNSAFE_MATERIALS.contains(ground.getType())) {
            return false;
        }
        
        // Check for unsafe blocks at the feet position
        if (UNSAFE_MATERIALS.contains(feet.getType())) {
            return false;
        }
        
        // The location is safe!
        return true;
    }
    
    private static boolean isNonSolid(Block block) {
        Material material = block.getType();
        return NON_SOLID_MATERIALS.contains(material) || 
               !material.isSolid() || 
               material.isTransparent();
    }
}