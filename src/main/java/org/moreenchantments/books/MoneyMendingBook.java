package org.moreenchantments.books;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.itemutils.ItemUtils;
import org.moreenchantments.MoreEnchantments;
import org.moreenchantments.enchantments.MoneyMendingEnchantment;

import java.util.ArrayList;
import java.util.List;

public class MoneyMendingBook {
    MoreEnchantments main = (MoreEnchantments)(Bukkit.getPluginManager().getPlugin("MoreEnchantments"));

    public static ItemStack item(){
        ItemStack result = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta itemMeta = result.getItemMeta();
        itemMeta.setLore(List.of("\u00a77Money Mending"));
        result.setItemMeta(itemMeta);
        result = ItemUtils.itemSetNbtPath(result, "CustomEnchantments", List.of("moreenchantments:money_mending"));
        return result;
    }

//    public void PrepareAnvilEvent(PrepareAnvilEvent event){
//        ItemStack result = event.getResult();
//        String rename = event.getResult().getItemMeta().getDisplayName();
//        ItemStack toolSlot = event.getInventory().getItem(0);
//        ItemStack enchanceBookSlot = event.getInventory().getItem(1);
//
//
//
//
//
//
//        if (!main.hasEnchantment(enchanceBook, MoneyMendingEnchantment.class)){
//            return;
//        }
//
//        if (main.hasEnchantment(tool, MoneyMendingEnchantment.class)){
//            return;
//        }
//
//        if (!event.getResult().getType().equals(Material.AIR)){
//            tool = event.getResult();
//        }
//
//        if (ItemUtils.itemGetNbtPath(tool, "CustomEnchantments") == null){
//            tool = ItemUtils.itemSetNbtPath(tool, "CustomEnchantments", new ArrayList<>());
//        }
//        ArrayList customEnchantments = ((ArrayList)(ItemUtils.itemGetNbtPath(tool,"CustomEnchantments")));
//        customEnchantments.add("moreenchantments:money_mending");
//        tool = ItemUtils.itemSetNbtPath(tool, "CustomEnchantments", customEnchantments);
//
//        if (tool.getItemMeta().getEnchants().size() == 0){
//            tool.addUnsafeEnchantment(Enchantment.OXYGEN, 0);
//            ItemMeta itemMeta = tool.getItemMeta();
//            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
//            tool.setItemMeta(itemMeta);
//        }
//
//        ItemMeta itemMeta = tool.getItemMeta();
//        itemMeta.setLore(List.of("\u00a77Money Mending"));
//        tool.setItemMeta(itemMeta);
//
//        event.setResult(tool);
//    }
}