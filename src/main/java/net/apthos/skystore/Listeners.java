package net.apthos.skystore;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class Listeners implements Listener {

    @EventHandler
    public void PlayerInteractWithEntityEvent(PlayerInteractEntityEvent e) {
        if (! (e.getRightClicked() instanceof ItemFrame)) { return; }
        ItemFrame Frame = (ItemFrame) e.getRightClicked();
        Shop shop = SkyStore.getInstance().getShop(Frame.getLocation().getBlock()
                .getLocation());
        if (shop == null) { return; } else e.setCancelled(true);
        if (!shop.selling) return;
        if (e.getPlayer().getItemInHand() == null) return;
        if (! shop.getItem().isSimilar(e.getPlayer().getItemInHand())) {
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "" +
                    "&a&lSkyStore: &cSorry, this item doesn't match the stores."));
            e.getPlayer().getWorld().playSound(shop.getFrame().getLocation(),
                    Sound.UI_BUTTON_CLICK, 10, 3);
            e.getPlayer().getWorld().playEffect(shop.getFrame().getLocation(),
                    Effect.MOBSPAWNER_FLAMES, 1);
            return;
        }
        int amount = 1;
        if (e.getPlayer().isSneaking()) {
            amount = e.getPlayer().getItemInHand().getAmount();
        }
        double sale = shop.getSellPrice() * amount;

        SkyStore.econ.depositPlayer(Bukkit.getOfflinePlayer
                (e.getPlayer().getUniqueId()), sale);

        e.getPlayer().sendMessage(ChatColor.GREEN + "You have sold " + ChatColor
                .RED + amount + " " + shop.getItem().getType().name().replace('_', ' ')
                .toLowerCase() + ChatColor.GREEN + " for " + ChatColor.RED + "$"
                + sale + ChatColor.GREEN + ".");

        if (amount == e.getPlayer().getItemInHand().getAmount()) {
            e.getPlayer().setItemInHand(new ItemStack(Material.AIR));
        } else {
            e.getPlayer().getItemInHand().setAmount(e.getPlayer().getItemInHand()
                    .getAmount() - 1);
        }

        e.getPlayer().getWorld().playSound(shop.getFrame().getLocation(),
                Sound.UI_BUTTON_CLICK, 10, 3);
        for (int c = 0; c < 15; c++) {
            e.getPlayer().getWorld().playEffect(shop.getFrame().getLocation()
                    , Effect.INSTANT_SPELL, 1);
        }
        return;
    }

    @EventHandler
    public void EntityDamageEvent(EntityDamageByEntityEvent e) {
        if (! (e.getEntity() instanceof ItemFrame)) { return; }
        if (! (e.getDamager() instanceof Player)) { return; }

        ItemFrame Frame = (ItemFrame) e.getEntity();
        Player player = (Player) e.getDamager();

        if (SkyStore.getInstance().getShop(Frame.getLocation()) == null) {
            return;
        }
        e.setCancelled(true);

        Shop shop = SkyStore.getInstance().getShop(Frame.getLocation().getBlock()
                .getLocation());
        if (shop == null) { return; }
        if (!shop.buying) return;
        e.setCancelled(true);

        int amount = 1;
        Double playerBalance = SkyStore.econ.getBalance(Bukkit.getOfflinePlayer(
                player.getUniqueId()));

        if (player.isSneaking()) {
            amount = shop.getItem().getMaxStackSize();
        }

        if (playerBalance < shop.getBuyPrice() * amount) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "" +
                    "&a&lSkyStore: &cYou need about &a$" +
                    String.valueOf((shop.getBuyPrice() * amount) - playerBalance)
                    + "&c more to afford this!"));
            player.getWorld().playSound(shop.getFrame().getLocation(),
                    Sound.UI_BUTTON_CLICK, 10, 3);
            player.getWorld().playEffect(shop.getFrame().getLocation(),
                    Effect.MOBSPAWNER_FLAMES, 1);
            return;
        } else {
            if (player.getInventory().firstEmpty() == - 1) {
                player.sendMessage(ChatColor.RED + "You cannot buy, what you cannot"
                        + " carry!");
                player.getWorld().playSound(shop.getFrame().getLocation(),
                        Sound.UI_BUTTON_CLICK, 10, 3);
                player.getWorld().playEffect(shop.getFrame().getLocation(),
                        Effect.MOBSPAWNER_FLAMES, 1);
                return;
            }
            player.getWorld().playSound(shop.getFrame().getLocation(),
                    Sound.UI_BUTTON_CLICK, 10, 3);
            for (int c = 0; c < 15; c++) {
                player.getWorld().playEffect(shop.getFrame().getLocation()
                        , Effect.INSTANT_SPELL, 1);
            }
            player.sendMessage(ChatColor.GREEN + "You have purchased " + ChatColor
                    .RED + amount + " " + shop.getItem().getType().name().replace('_', ' ')
                    .toLowerCase() + ChatColor.GREEN + " for " + ChatColor.RED + "$"
                    + String.valueOf(shop.getBuyPrice() * amount) + ChatColor.GREEN + ".");
            ItemStack item = shop.getItem().clone();
            item.setAmount(amount);
            player.getInventory().addItem(item);
            SkyStore.econ.withdrawPlayer(Bukkit.getOfflinePlayer(
                    player.getUniqueId()), shop.getBuyPrice() * amount);
        }

        return;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        List<Entity> Entities = Arrays.asList(Utils.getNearbyEntities
                (e.getBlock().getLocation(), 2));
        for (Entity entity : Entities) {
            if (entity.getType().equals(EntityType.ITEM_FRAME)) {
                ItemFrame frame = (ItemFrame) entity;
                if (! (SkyStore.getInstance().getShop(entity.getLocation()) == null)
                        && frame.getLocation().getBlock().getRelative(
                        frame.getAttachedFace()).equals(e.getBlock())) {
                    e.setCancelled(true);
                }
            }
        }

    }

}
