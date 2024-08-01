package org.moreenchantments.books;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.moreenchantments.ItemNBTUtils;
import org.moreenchantments.MoreEnchantments;
import java.util.List;

public class MoneyMendingBook {
    public static ItemStack item(){
        MoreEnchantments main = (MoreEnchantments)(Bukkit.getPluginManager().getPlugin("MoreEnchantments"));
        ItemStack result = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta itemMeta = result.getItemMeta();
        itemMeta.setLore(List.of(main.languageMapping.get("moreenchantments:money_mending")));
        result.setItemMeta(itemMeta);
        result = ItemNBTUtils.setCustomEnchantments(result, List.of("moreenchantments:money_mending"));
        return result;
    }
}