package org.moreenchantments.utils;

import org.bukkit.inventory.ItemStack;
import org.moreenchantments.ItemNBTUtils;

import java.util.UUID;

public class UUIDUtils {

    public static ItemStack itemUpdateUUID(ItemStack item){
        return ItemNBTUtils.setUUID(item, UUID.randomUUID());
    }

    public static boolean itemHasUUID(ItemStack item){
        return ItemNBTUtils.containsUUID(item);
    }
    public static boolean itemSameUUID(ItemStack itemA, ItemStack itemB){
        if (!itemHasUUID(itemA) && !itemHasUUID(itemB)) return true;
        if (!itemHasUUID(itemA) || !itemHasUUID(itemB)) return false;

        UUID A = ItemNBTUtils.getUUID(itemA);
        UUID B = ItemNBTUtils.getUUID(itemB);

        return A.equals(B);
    }
}
