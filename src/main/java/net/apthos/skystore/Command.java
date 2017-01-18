package net.apthos.skystore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Command implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command
            command, String s, String[] strings) {

        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (strings.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "" +
                    "&c<==== &aShop Commands &c=============>\n" +
                    " &e&lSub Commands: \n" +
                    "   &6&lCreate \n" +
                    "   &6&lRemove \n" +
                    "   &6&lEdit \n" +
                    "&c<==== &aShop Commands &c=============>"
            ));
            return true;
        }

        if (strings[0].equalsIgnoreCase("create") && sender.hasPermission("SkyStore" +
                ".Create")) {
            ItemFrame frame = Utils.getDatFrame(player);
            if (frame == null){
                player.sendMessage(ChatColor.RED + "Error: cannot locate frame");
                return true;
            }

            if (!(SkyStore.getInstance().getShop(frame.getLocation()) == null)){
                player.sendMessage(ChatColor.RED + "Error: Shop already exists here!");
                return true;
            }

            if (strings.length == 1){
                player.sendMessage(ChatColor.RED + "Usage: /Shop create [price]");
                return true;
            }

            if (frame == null){
                player.sendMessage(ChatColor.RED + "SkyStore: Couldn't find frame!");
                return true;
            }

            if (!Utils.isNumeric(strings[1])){
                player.sendMessage(ChatColor.RED + "Error: Price has to be a number!");
                return true;
            }

            Double price = Double.parseDouble(strings[1]);
            SkyStore.getInstance().addShop(new Shop(frame, price));
            player.sendMessage(ChatColor.GREEN + "Successfully created frame shop!");

        }

        if (strings[0].equalsIgnoreCase("remove") &&
                sender.hasPermission("SkyStore.Remove")){
            ItemFrame frame = Utils.getDatFrame(player);
            if (frame == null){
                player.sendMessage(ChatColor.RED + "Error: cannot locate frame");
                return true;
            }
            Shop shop = SkyStore.getInstance().getShop(frame.getLocation());
            if (shop == null){
                player.sendMessage(ChatColor.RED + "Error: frame doesn't belong to a shop!");
                return true;
            }
            shop.destroy();
        }

        return true;
    }
}
