package org.moreenchantments;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.itemutils.ItemUtils;
import org.moreenchantments.books.MoneyMendingBook;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

public final class MoreEnchantments extends JavaPlugin {
    public static MoreEnchantments getThis(){
        return (MoreEnchantments)(Bukkit.getPluginManager().getPlugin("MoreEnchantments"));
    }

    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list)
    {

        // Create a new ArrayList
        ArrayList<T> newList = new ArrayList<T>();

        // Traverse through the first list
        for (T element : list) {

            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {

                newList.add(element);
            }
        }

        // return the new list
        return newList;
    }

    public String pathToName(String path){
        String[] eventFullName = path.split("\\.");
        return eventFullName[eventFullName.length-1];
    }

    public Object getFieldFromInstance(String fieldName, Object instance) throws NoSuchFieldException, IllegalAccessException{
        return instance.getClass().getField(fieldName).get(instance);
    }

    public Object getStaticField(String fieldName, Class<?> clazz) throws NoSuchFieldException, IllegalAccessException{
        return clazz.getDeclaredField(fieldName).get(clazz);
    }

    public boolean hasEnchantment(ItemStack item, Class<?> enchantment){
        boolean result = false;
        try{
            String enchName = "moreenchantments:"+enchantment.getDeclaredField("name").get(String.class);
            ArrayList<String> enchantments = (ArrayList<String>) ItemUtils.itemGetNbtPath(item, "CustomEnchantments");
            for (String i : enchantments){
                if (enchName.equals(i)){
                    result = true;
                }
            }
        }
        catch (Exception ignored){}
        return result;
    }

    public boolean isEnchantmentBook(ItemStack item){
        return item.getType().equals(Material.ENCHANTED_BOOK);
    }

    public void removeVirtualEnchantment(ItemStack item){
        if (item.getEnchantments().get(Enchantment.OXYGEN) == null){
            return;
        }
        if (item.getEnchantments().get(Enchantment.OXYGEN) == 0) {
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.removeEnchant(Enchantment.OXYGEN);
            item.setItemMeta(itemMeta);
        }
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(itemMeta);
    }

    public void addVirtualEnchantment(ItemStack item){
        item.addUnsafeEnchantment(Enchantment.OXYGEN, 0);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(itemMeta);
    }

    public boolean hasVanillaEnchantment(ItemStack item){
        ItemStack checkItem = item.clone();
        removeVirtualEnchantment(checkItem);
        Map<Enchantment, Integer> enchantments = checkItem.getEnchantments();
//        enchantments.remove(Enchantment.OXYGEN, 0);
        if (enchantments.isEmpty()){
            return false;
        }
        return true;
    }

    public ArrayList mergeCustomEnchantments(ArrayList ce1, ArrayList ce2){
        if (ce1 == null){
            ce1 = new ArrayList();
        }
        if (ce2 == null){
            ce2 = new ArrayList();
        }
        ArrayList result = new ArrayList();
        result.addAll(ce1);
        result.addAll(ce2);
        if (result.isEmpty()){
            return null;
        }
        return result;
    }

    HashMap<String, Object> constructedEnchantments = new HashMap<>();
    HashMap<String, List<Consumer<Object[]>>> eventsFunctionMapping = new HashMap<>();
    HashMap<String, Class<?>[]> argumentTypeMapping = new HashMap<>();
    HashMap<String, String> languageMapping = new HashMap<>();

    @Override
    public void onEnable() {
        languageMapping.put("moreenchantments:money_mending","Money Mending");
        Reflections reflections = new Reflections("org.moreenchantments.enchantments" ,new SubTypesScanner(false));
        Set<Class<?>> enchantments = reflections.getSubTypesOf(Object.class);
        enchantments.addAll(reflections.getSubTypesOf(Object.class));
        for (Class<?> ench : enchantments){
            String enchantmentName = ench.getName();
            try{
                Bukkit.getLogger().info("Loading enchantment - "+pathToName(enchantmentName));
                Object constructedEnchantment = ench.getDeclaredConstructor().newInstance();
                String[] requiredPlugins = (String[]) (getFieldFromInstance("requiredPlugins", constructedEnchantment));
                for (String i : requiredPlugins){
                    if (Bukkit.getPluginManager().getPlugin(i) == null){
                        throw new Exception("Required plugin '"+i+"' not found!");
                    }
                }
                constructedEnchantments.put("moreenchantments:"+((String) getStaticField("name", ench)), ench.getDeclaredConstructor().newInstance());

            }
            catch (Exception exception){
                Bukkit.getLogger().warning("Error while loading enchantment - "+pathToName(enchantmentName));
                exception.printStackTrace();
            }
            for (Player i : Bukkit.getOnlinePlayers()){
                i.getInventory().addItem(MoneyMendingBook.item());
            }
        }

        Method[] events = Events.class.getMethods();
        for (Method i : events){
            String eventName = pathToName(i.getName());
            if (!eventName.contains("Event")){
                continue;
            }
            eventsFunctionMapping.put(eventName, new ArrayList<>());
            argumentTypeMapping.put(eventName, i.getParameterTypes());
        }

        for (Object i : constructedEnchantments.values()){
            for (String ii : eventsFunctionMapping.keySet()){
                eventsFunctionMapping.get(ii).add(
                    (args)->{
                        try{
                            i.getClass().getMethod(ii,argumentTypeMapping.get(ii)).invoke(i,args);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                );
            }
        }

        getServer().getPluginManager().registerEvents(new Events(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
