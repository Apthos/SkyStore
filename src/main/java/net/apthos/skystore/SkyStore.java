package net.apthos.skystore;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class SkyStore extends JavaPlugin {

    public static Economy econ = null;

    private static SkyStore SKYSTORE;

    private List<Shop> Shops = new ArrayList<>();

    @Override
    public void onEnable() {
        SKYSTORE = this;
        getCommand("Shop").setExecutor(new Command());
        getServer().getPluginManager().registerEvents(new Listeners(), this);

        if (! setupEconomy()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager()
                .getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static SkyStore getInstance() {
        return SKYSTORE;
    }

    public void addShop(Shop shop){
        this.Shops.add(shop);
    }

    public void removeShop(Shop shop){
        this.Shops.remove(shop);
    }

    public Shop getShop(Location location) {
        for (Shop shop : this.Shops) {
            if (shop.getFrame().getLocation().getBlock().getLocation().equals(location
                    .getBlock().getLocation())){
                return shop;
            }
        }
        if (!Utils.getFileLocation(location).exists()){
            return null;
        }
        Shop shop = new Shop(location);
        addShop(shop);
        return shop;
    }

}
