package net.apthos.skystore;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.xml.soap.Text;

public class Command implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command
            command, String s, String[] strings) {

        if (! (sender instanceof Player)) return true;
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
            if (frame == null) {
                player.sendMessage(ChatColor.RED + "Error: cannot locate frame");
                return true;
            }

            if (! (SkyStore.getInstance().getShop(frame.getLocation()) == null)) {
                player.sendMessage(ChatColor.RED + "Error: Shop already exists here!");
                return true;
            }

            if (strings.length <= 2) {
                player.sendMessage(ChatColor.RED + "Usage: /Shop create [Buy] [Sell]");
                return true;
            }

            if (frame == null) {
                player.sendMessage(ChatColor.RED + "SkyStore: Couldn't find frame!");
                return true;
            }

            if (! Utils.isNumeric(strings[1]) && ! Utils.isNumeric(strings[2])) {
                player.sendMessage(ChatColor.RED + "Error: Prices have to be a number!");
                return true;
            }

            Double buy = Double.parseDouble(strings[1]),
                    sell = Double.parseDouble(strings[2]);
            SkyStore.getInstance().addShop(new Shop(frame, buy, sell));
            player.sendMessage(ChatColor.GREEN + "Successfully created frame shop!");

        }

        if (strings[0].equalsIgnoreCase("remove") &&
                sender.hasPermission("SkyStore.Remove")) {
            ItemFrame frame = Utils.getDatFrame(player);
            if (frame == null) {
                player.sendMessage(ChatColor.RED + "Error: cannot locate frame");
                return true;
            }
            Shop shop = SkyStore.getInstance().getShop(frame.getLocation());
            if (shop == null) {
                player.sendMessage(ChatColor.RED + "Error: frame doesn't belong to a shop!");
                return true;
            }
            shop.destroy();
            player.sendMessage(ChatColor.GREEN + "Shop has successfully been removed!");
            return true;
        }

        if (strings[0].equalsIgnoreCase("preview") && sender.hasPermission("SkyStore" +
                ".Preview") && (sender instanceof Player)) {

            ItemFrame frame = Utils.getDatFrame(player);
            if (frame == null) {
                player.sendMessage(ChatColor.RED + "Error: cannot locate frame");
                return true;
            }
            Shop shop = SkyStore.getInstance().getShop(frame.getLocation());
            if (shop == null) {
                player.sendMessage(ChatColor.RED + "Error: frame doesn't belong to a shop!");
                return true;
            }

            TextComponent component = new TextComponent("SkyStore Preview : ");
            component.setColor(net.md_5.bungee.api.ChatColor.GREEN);
            component.setBold(true);

            TextComponent Item = new TextComponent(shop.getItem().getType().name().replace
                    ('_', ' ').toLowerCase());
            Item.setColor(net.md_5.bungee.api.ChatColor.AQUA);

            component.addExtra(Item);

            BaseComponent[] hoverEventComponents = new BaseComponent[]{
                    new TextComponent(convertItemStackToJsonRegular(shop.getItem()))
            };

            TextComponent hover = new TextComponent(" [?]");
            hover.setColor(net.md_5.bungee.api.ChatColor.DARK_PURPLE);
            hover.setBold(true);
            hover.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM,
                    hoverEventComponents));
            component.addExtra(hover);

            player.spigot().sendMessage(component);
            return true;
        }

        return true;
    }

    public String convertItemStackToJsonRegular(ItemStack itemStack) {
        net.minecraft.server.v1_11_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy
                (itemStack);
        net.minecraft.server.v1_11_R1.NBTTagCompound compound = new NBTTagCompound();
        compound = nmsItemStack.save(compound);

        return compound.toString();
    }
}
