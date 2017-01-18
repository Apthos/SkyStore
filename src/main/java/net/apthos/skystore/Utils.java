package net.apthos.skystore;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utils {

    public static Entity[] getNearbyEntities(Location l, int radius) {
        int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16)) / 16;
        HashSet<Entity> radiusEntities = new HashSet<Entity>();
        for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++) {
            for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++) {
                int x = (int) l.getX(), y = (int) l.getY(), z = (int) l.getZ();
                for (Entity e : new Location(l.getWorld(), x + (chX * 16), y, z + (chZ *
                        16)).getChunk().getEntities()) {
                    if (e.getLocation().distance(l) <= radius && e.getLocation().getBlock
                            () != l.getBlock())
                        radiusEntities.add(e);
                }
            }
        }
        return radiusEntities.toArray(new Entity[radiusEntities.size()]);
    }

    public static ItemFrame getDatFrame(Player player) {
        Block LastBlock = player.getTargetBlock((Set<Material>) null, 7);
        List<Block> lastBlocks = player.getLastTwoTargetBlocks((Set<Material>) null, 7);
        lastBlocks.remove(LastBlock);

        Block framelocation = lastBlocks.get(0);

        final Location EntityLocation = lastBlocks.get(0).getLocation();

        for (Entity entity : getNearbyEntities(EntityLocation, 2)) {
            if (entity instanceof ItemFrame &&
                    entity.getLocation().getBlock().equals(framelocation))
                return (ItemFrame) entity;
        }
        return null;
    }

    public static boolean isNumeric(String str) {
        try {
            @SuppressWarnings("unused")
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static File getFileLocation(Location location) {
        String fileName = "{" + location.getBlockX() + "," + location.getBlockY()
                + "," + location.getBlockZ() + "}";
        File file = new File(SkyStore.getInstance().getDataFolder() + "/Worlds/" +
                location.getWorld().getName() + "/" + fileName + ".yml");
        return file;
    }

    // w,x,y,z
    public static Location getLocationFromString(String S) {
        String[] Ls = S.split(",");
        World w = Bukkit.getWorld(Ls[0]);
        int x = Integer.parseInt(Ls[1]);
        int y = Integer.parseInt(Ls[2]);
        int z = Integer.parseInt(Ls[3]);

        Location loc = new Location(w, x, y, z);
        return loc;
    }

    public static String getStringLocation(Location Loc) {
        String S = "";
        String W = Loc.getWorld().getName();
        int x = (int) Loc.getX();
        int y = (int) Loc.getY();
        int z = (int) Loc.getZ();
        S = W + "," + x + "," + y + "," + z;
        return S;
    }


}
