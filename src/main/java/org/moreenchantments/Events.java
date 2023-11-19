package org.moreenchantments;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.itemutils.ItemUtils;
import org.moreenchantments.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Events implements Listener {
    MoreEnchantments main = MoreEnchantments.getThis();
    public void eventPass(String eventName, Object ... args){
        for (String i : main.eventsFunctionMapping.keySet()){
            if (i.equals(eventName)){
                for (Consumer<Object[]> ii : main.eventsFunctionMapping.get(i)){
                    ii.accept(args);
                }
            }
        }
    }

    @EventHandler
    public void PlayerItemDamageEvent(PlayerItemDamageEvent event){
        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);
    }

    @EventHandler
    public void EntitySpawnEvent(EntitySpawnEvent event){
        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);
    }

    @EventHandler
    public void PrepareAnvilEvent(PrepareAnvilEvent event){
        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);

        ItemStack aSlot = event.getInventory().getItem(0);
        ItemStack bSlot = event.getInventory().getItem(1);
        if (aSlot == null || bSlot == null){
            return;
        }
        ItemStack result = event.getResult();
        ArrayList aCustomEnchantments = (ArrayList) (ItemUtils.itemGetNbtPath(aSlot, "CustomEnchantments"));
        ArrayList bCustomEnchantments = (ArrayList) (ItemUtils.itemGetNbtPath(bSlot, "CustomEnchantments"));
        ArrayList customEnchantments = main.mergeCustomEnchantments(aCustomEnchantments,bCustomEnchantments);

        boolean validEnchant = false;

        if (aSlot.getType().equals(Material.ENCHANTED_BOOK) && bSlot.getType().equals(Material.ENCHANTED_BOOK)){
            validEnchant = true;
        }

        if (Enchantment.MENDING.canEnchantItem(new ItemStack(aSlot.getType(), 1)) && bSlot.getType().equals(Material.ENCHANTED_BOOK)){
            validEnchant = true;
        }

        if (aSlot.getType().equals(bSlot.getType()) && Enchantment.MENDING.canEnchantItem(new ItemStack(aSlot.getType(), 1))){
            validEnchant = true;
        }

        if (!validEnchant){
            return;
        }

        if (customEnchantments == null){
            return;
        }

        customEnchantments = ListUtils.removeDuplicates(customEnchantments);

        if (result.getType().equals(Material.AIR)){
            result = aSlot.clone();
        }

        result = ItemUtils.itemSetNbtPath(result, "CustomEnchantments", customEnchantments);

        int aRL = 0;
        int bRL = 0;

        if (aSlot.containsEnchantment(Enchantment.OXYGEN)){
            aRL = aSlot.getEnchantments().get(Enchantment.OXYGEN);
        }

        if (bSlot.containsEnchantment(Enchantment.OXYGEN)){
            bRL = aSlot.getEnchantments().get(Enchantment.OXYGEN);
        }


        int highestRespiration = Math.max(aRL,bRL);


        if (main.hasVanillaEnchantments(result)){
            main.removeVirtualEnchantment(result);
        }

        List<String> lores = new ArrayList<>();
        for (Object i : customEnchantments){
            lores.add("\u00a77"+main.languageMapping.get((String) i));
        }
        ItemMeta rItemMeta = result.getItemMeta();
        rItemMeta.setLore(lores);

        if (highestRespiration == 0){
            rItemMeta.removeEnchant(Enchantment.OXYGEN);
        }

        result.setItemMeta(rItemMeta);

        if (!main.hasVanillaEnchantments(result)){
            main.addVirtualEnchantment(result);
        }

        event.setResult(result);

        boolean enchantmentCompatible = true;
        try{
            for (Object i : customEnchantments) {
                Enchantment[] notCompatibleVanilla = ((Enchantment[]) (main.getFieldFromInstance("notCompatibleVanilla", main.constructedEnchantments.get((String) i))));
                for (Enchantment ii : notCompatibleVanilla) {
                    boolean containEnchantedBook = false;
                    if (main.isEnchantmentBook(result)){
                        containEnchantedBook = main.enchantmentBookContains(result, ii);
                    }
                    else {
                        containEnchantedBook = result.containsEnchantment(ii);
                    }

                    if (containEnchantedBook) {
                        enchantmentCompatible = false;
                        break;
                    }
                }

                String[] notCompatibleCustom = ((String[]) (main.getFieldFromInstance("notCompatibleCustom", main.constructedEnchantments.get((String) i))));
                for (String ii : notCompatibleCustom) {
                    if (customEnchantments.contains(ii)) {
                        enchantmentCompatible = false;
                        break;
                    }
                }
            }
        }
        catch (Exception ignored){}

        if (!enchantmentCompatible){
            event.setResult(new ItemStack(Material.AIR));
            return;
        }

    }

    @EventHandler
    public void InventoryClickEvent(InventoryClickEvent event){
        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);

        Inventory inventory = event.getInventory();
        HumanEntity player = event.getWhoClicked();
        if (!(inventory instanceof AnvilInventory)){
            return;
        }

        if (((AnvilInventory) inventory).getRepairCost() > player.getExpToLevel()){
            return;
        }
        if (!event.getSlotType().equals(InventoryType.SlotType.RESULT)){
            return;
        }

        if (event.getCurrentItem().getType().equals(Material.AIR)){
            return;
        }

        if (event.isShiftClick()){
            player.getInventory().addItem(event.getCurrentItem());
        }
        else{
            player.setItemOnCursor(event.getCurrentItem());
        }

        inventory.clear();
        Location inventoryLocation = inventory.getLocation();
        inventoryLocation.getWorld().playSound(inventoryLocation, Sound.BLOCK_ANVIL_USE, (float) 1, (float) (1.035-Math.random()*0.15));
    }
}
