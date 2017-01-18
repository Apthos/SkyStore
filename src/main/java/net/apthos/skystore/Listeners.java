package net.apthos.skystore;

import org.bukkit.*;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public class Listeners implements Listener {

    @EventHandler
    public void PlayerInteractWithEntityEvent(PlayerInteractEntityEvent e) {
        if (! (e.getRightClicked().getType().toString().equals("ITEM_FRAME"))) { return; }
        ItemFrame Frame = (ItemFrame) e.getRightClicked();
        Shop shop = SkyStore.getInstance().getShop(Frame.getLocation().getBlock()
                .getLocation());
        if (shop == null) { return; }
        e.setCancelled(true);

        int amount = 1;
        Double playerBalance = SkyStore.econ.getBalance(Bukkit.getOfflinePlayer(e.getPlayer
                ().getUniqueId()));

        if (e.getPlayer().isSneaking()) {
            amount = shop.getItem().getMaxStackSize();
        }

        if (playerBalance < shop.getPrice() * amount) {
            e.getPlayer().sendMessage(ChatColor.RED + "Insufficient funds! You need about "
                    + ChatColor.GREEN + "$" + String.valueOf((shop.getPrice() * amount) -
                    playerBalance)
                    + ChatColor.RED + " more to afford this!");
            e.getPlayer().getWorld().playSound(shop.getFrame().getLocation(),
                    Sound.UI_BUTTON_CLICK, 10, 3);
            e.getPlayer().getWorld().playEffect(shop.getFrame().getLocation(),
                    Effect.MOBSPAWNER_FLAMES, 1);
            return;
        } else {
            if (e.getPlayer().getInventory().firstEmpty() == - 1) {
                e.getPlayer().sendMessage(ChatColor.RED + "You cannot buy, what you cannot"
                        + " carry!");
                e.getPlayer().getWorld().playSound(shop.getFrame().getLocation(),
                        Sound.UI_BUTTON_CLICK, 10, 3);
                e.getPlayer().getWorld().playEffect(shop.getFrame().getLocation(),
                        Effect.MOBSPAWNER_FLAMES, 1);
                return;
            }
            e.getPlayer().getWorld().playSound(shop.getFrame().getLocation(),
                    Sound.UI_BUTTON_CLICK, 10, 3);
            for (int c = 0; c < 15; c++) {
                e.getPlayer().getWorld().playEffect(shop.getFrame().getLocation()
                        , Effect.INSTANT_SPELL, 1);
            }
            e.getPlayer().sendMessage(ChatColor.GREEN + "You have purchased " + ChatColor
                    .RED + amount + " " + shop.getItem().getType().name().replace('_', ' ')
                    .toLowerCase() + ChatColor.GREEN + " for " + ChatColor.RED + "$"
                    + String.valueOf(shop.getPrice() * amount) + ChatColor.GREEN + ".");
            ItemStack item = shop.getItem().clone();
            item.setAmount(amount);
            e.getPlayer().getInventory().addItem(item);
            SkyStore.econ.withdrawPlayer(Bukkit.getOfflinePlayer(e.getPlayer
                    ().getUniqueId()), shop.getPrice() * amount);
        }
        return;
    }

    @EventHandler
    public void itemFrameItemRemoval(EntityDamageEvent e) {
        if (! (e.getEntity() instanceof ItemFrame)) { return; }
        ItemFrame Frame = (ItemFrame) e.getEntity();
        if (SkyStore.getInstance().getShop(Frame.getLocation()) == null) {
            return;
        }
        e.setCancelled(true);
        return;
    }

}
