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
    private double sell, buy;
    private ItemFrame frame = null;
    private ItemStack item;
    boolean buying, selling;

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
        sell = conf.getDouble("sell_price");
        buy = conf.getDouble("buy_price");
        if (sell == -1){ selling = false; } else selling = true;
        if (buy == -1){ buying = false; } else buying = true;
        item = getItemFromSerial(conf.getString("item"));

        if (frame == null) {
            recreate();
        }
    }

    public Shop(ItemFrame frame, Double buy, Double sell) {
        location = frame.getLocation();
        this.sell = sell;
        this.buy = buy;
        this.frame = frame;
        this.item = frame.getItem();
        if (sell == -1){ selling = false; } else selling = true;
        if (buy == -1){ buying = false; } else buying = true;
        saveFrameFile();
        ItemMeta meta = frame.getItem().getItemMeta();
        meta.setDisplayName(getTitle());
        ItemStack item = frame.getItem();
        item.setItemMeta(meta);
        frame.setItem(item);
    }

    public void recreate() {
        ItemFrame frame = (ItemFrame) location.getWorld().spawnEntity(location, EntityType
                .ITEM_FRAME);
        ItemStack stack = item.clone();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(getTitle());
        stack.setItemMeta(meta);
        frame.setItem(stack);
    }

    public void destroy(){
        frame.remove();
        File file = Utils.getFileLocation(location);
        file.delete();
        SkyStore.getInstance().removeShop(this);
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
        conf.createSection("selling"); conf.set("selling", selling);
        conf.createSection("buying"); conf.set("buying", buying);
        conf.createSection("sell_price"); conf.set("sell_price", sell);
        conf.createSection("buy_price"); conf.set("buy_price", buy);
        conf.createSection("item"); conf.set("item", serializeItemStack(frame.getItem()));

        try {
            conf.save(Utils.getFileLocation(this.location));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getTitle(){
        if (selling && buying){
            return (ChatColor.GREEN + "B: " + ChatColor.RED + "$" + buy + ChatColor.GREEN
            + " S: " + ChatColor.RED + "$" + sell);
        } else if (selling) {
            return (ChatColor.GREEN + "Sell: " + ChatColor.RED + "$" + sell);
        } else if (buying){
            return (ChatColor.GREEN + "Buy: " + ChatColor.RED + "$" + buy);
        }
        return null;
    }

    public double getSellPrice() {
        return sell;
    }

    public double getBuyPrice(){
        return buy;
    }

    public ItemFrame getFrame() {
        return frame;
    }

    public ItemStack getItem() {
        return item;
    }
}
