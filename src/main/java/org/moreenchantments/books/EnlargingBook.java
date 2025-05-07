package org.moreenchantments.books;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.moreenchantments.ItemNBTUtils;
import org.moreenchantments.Main;

import java.util.List;

public class EnlargingBook {
    public static ItemStack item(){
        Main main = (Main)(Bukkit.getPluginManager().getPlugin("MoreEnchantments"));
        ItemStack result = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta itemMeta = result.getItemMeta();
        itemMeta.setLore(List.of(main.languageMapping.get("moreenchantments:enlarging")));
        result.setItemMeta(itemMeta);
        result = ItemNBTUtils.setCustomEnchantments(result, List.of("moreenchantments:enlarging"));
        return result;
    }
}
