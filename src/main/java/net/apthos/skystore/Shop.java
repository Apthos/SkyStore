package net.apthos.skystore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Shop {

    private Location location;
    private double price;
    private ItemFrame frame = null;
    private ItemStack item;

    public Shop(Location location) {
        File file = Utils.getFileLocation(location);
        if (! file.exists()) {
            Bukkit.broadcastMessage(ChatColor.RED + "SkyShop: Couldn't find shop data!");
            return;
        }
        this.location = location;
        location.getChunk().load();

        for (Entity entity : Utils.getNearbyEntities(location, 5)) {
            if (entity instanceof ItemFrame && entity.getLocation().getBlock().equals(
                    location.getBlock())) {
                frame = (ItemFrame) entity;
            }
        }

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
        price = conf.getDouble("price");
        item = getItemFromSerial(conf.getString("item"));

        if (frame == null) {
            recreate();
        }
    }

    public Shop(ItemFrame frame, Double price) {
        location = frame.getLocation();
        this.price = price;
        this.frame = frame;
        saveFrameFile();
        ItemMeta meta = frame.getItem().getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Price: " + ChatColor.RED + "$" + this.price);
        ItemStack item = frame.getItem();
        item.setItemMeta(meta);
        frame.setItem(item);
    }

    public void recreate() {
        ItemFrame frame = (ItemFrame) location.getWorld().spawnEntity(location, EntityType
                .ITEM_FRAME);
        frame.setItem(item);
        frame.setCustomName("Price: " + this.price + "$");
    }

    public void destroy(){
        frame.remove();
        File file = Utils.getFileLocation(location);
        file.delete();
    }

    // MATERIAL:DURABILITY:DISPLAY_NAME:LORE:ENCHANTS
    public String serializeItemStack(ItemStack item) {
        String serialized = "";
        serialized = serialized + item.getType().name().toUpperCase() + ":";
        serialized = serialized + item.getDurability() + ":";
        if (item.hasItemMeta()) {
            if (item.getItemMeta().hasDisplayName()) {
                serialized += item.getItemMeta().getDisplayName() + ":";
            } else {
                serialized += "NULL:";
            }
            if (item.getItemMeta().hasLore()) {
                for (int x = 0; x < item.getItemMeta().getLore().size(); x++) {
                    serialized += item.getItemMeta().getLore().get(x);
                    if (x < item.getItemMeta().getLore().size() - 1) {
                        serialized += ";";
                    }
                }
            } else {
                serialized += "NULL:";
            }
            if (item.getItemMeta().hasEnchants()) {
                int c = 0, c_max = item.getItemMeta().getEnchants().keySet().size() - 1;
                for (Enchantment enchantment : item.getItemMeta().getEnchants().keySet()) {
                    serialized += enchantment.getName() + "-" + item.getItemMeta()
                            .getEnchants().get(enchantment);
                    if (c <= c_max) {
                        serialized += ";";
                    }
                    c++;
                }
            } else {
                serialized += "NULL";
            }
            return serialized;
        }
        serialized += "NULL:NULL:NULL";
        return serialized;
    }

    public ItemStack getItemFromSerial(String string) {
        ItemStack item = new ItemStack(Material.getMaterial(string.split(":")[0]));
        item.setDurability(Short.parseShort(string.split(":")[1]));
        if (string.split(":")[2].equalsIgnoreCase("NULL") && string.split(":")[3]
                .equalsIgnoreCase("NULL") && string.split(":")[4].equalsIgnoreCase("NULL")) {
            return item;
        }
        ItemMeta meta = item.getItemMeta();

        if (! string.split(":")[2].equalsIgnoreCase("NULL")) {
            meta.setDisplayName(string.split(":")[2]);
        }

        if (! string.split(":")[3].equalsIgnoreCase("NULL")) {
            meta.setLore(Arrays.asList(string.split(":")[3].split(";")));
        }

        if (! string.split(":")[4].equalsIgnoreCase("NULL")) {
            for (String enchant : string.split(":")[4].split(";")) {
                int level = Integer.parseInt(enchant.split("-")[1]);
                Enchantment enchantment = Enchantment.getByName(enchant.split("-")[0]);
                meta.addEnchant(enchantment, level, true);
            }
        }

        item.setItemMeta(meta);
        return item;
    }

    public void saveFrameFile() {
        YamlConfiguration conf = new YamlConfiguration();
        conf.createSection("location");
        conf.set("location", Utils.getStringLocation(location));
        conf.createSection("price"); conf.set("price", price);
        conf.createSection("item"); conf.set("item", serializeItemStack(frame.getItem()));

        try {
            conf.save(Utils.getFileLocation(this.location));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getPrice() {
        return price;
    }

    public ItemFrame getFrame() {
        return frame;
    }

    public ItemStack getItem() {
        return item;
    }
}
