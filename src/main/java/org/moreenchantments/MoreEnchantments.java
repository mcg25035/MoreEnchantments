package org.moreenchantments;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.itemutils.ItemUtils;
import org.apache.commons.io.FileUtils;
import org.moreenchantments.books.MoneyMendingBook;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

public final class MoreEnchantments extends JavaPlugin {
    public File pluginDir = getDataFolder();
    public void checkPluginFile() throws IOException {

        if (!pluginDir.exists()){
            pluginDir.mkdirs();
        }
        if (!pluginDir.isDirectory()){
            pluginDir.delete();
            pluginDir.mkdirs();
        }
        File configFileReal = pluginDir.toPath().resolve("config.yml").toFile();
        if (!configFileReal.exists()){
            URL configFileTemplate = getClass().getResource("/config.yml");
            assert configFileTemplate != null;
            FileUtils.copyURLToFile(configFileTemplate,configFileReal);
        }
        if (!configFileReal.isFile()){
            boolean ignored = configFileReal.delete();
            URL configFileTemplate = getClass().getResource("/config.yml");
            assert configFileTemplate != null;
            FileUtils.copyURLToFile(configFileTemplate,configFileReal);
        }
    }

    public static MoreEnchantments getThis(){
        return (MoreEnchantments)(Bukkit.getPluginManager().getPlugin("MoreEnchantments"));
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

    public boolean hasCustomEnchantment(ItemStack item, Class<?> enchantment){
        boolean result = false;
        try{
            String enchName = "moreenchantments:"+enchantment.getDeclaredField("name").get(String.class);
            ArrayList<String> enchantments = (ArrayList<String>) ItemUtils.itemGetNbtPath(item, "CustomEnchantments");
            enchantments.contains(enchName);
        }
        catch (Exception ignored){}
        return result;
    }

    public boolean isEnchantmentBook(ItemStack item){
        return item.getType().equals(Material.ENCHANTED_BOOK);
    }

    public boolean enchantmentBookContains(ItemStack item, Enchantment enchantment){
//        System.out.println(item);
//        System.out.println((EnchantmentStorageMeta)(item.getItemMeta()));
//        System.out.println(((EnchantmentStorageMeta)(item.getItemMeta())).getStoredEnchants());
//        System.out.println(((EnchantmentStorageMeta)(item.getItemMeta())).hasStoredEnchant(enchantment));
        return ((EnchantmentStorageMeta)(item.getItemMeta())).hasStoredEnchant(enchantment);
    }

    public void removeVirtualEnchantment(ItemStack item){
        if (item.getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS)){
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(itemMeta);
        }
        if (item.getEnchantments().get(Enchantment.OXYGEN) == null){
            return;
        }
        if (item.getEnchantments().get(Enchantment.OXYGEN) == 0) {
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.removeEnchant(Enchantment.OXYGEN);
            item.setItemMeta(itemMeta);
        }
    }

    public void addVirtualEnchantment(ItemStack item){
        item.addUnsafeEnchantment(Enchantment.OXYGEN, 0);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(itemMeta);
    }

    public boolean hasVanillaEnchantments(ItemStack item){
        ItemStack checkItem = item.clone();
        removeVirtualEnchantment(checkItem);
        Map<Enchantment, Integer> enchantments = checkItem.getEnchantments();
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

    public HashMap<String, Object> constructedEnchantments = new HashMap<>();
    public HashMap<String, List<Consumer<Object[]>>> eventsFunctionMapping = new HashMap<>();
    public HashMap<String, Class<?>[]> argumentTypeMapping = new HashMap<>();
    public HashMap<String, String> languageMapping = new HashMap<>();
    public String messagePrefix = "\u00a7f[\u00a7aMoreEnchantments\u00a7f] ";
    public HashMap<String, Object> config = new HashMap<>();

    @Override
    public void onEnable() {
        try{
            checkPluginFile();
        }
        catch (Exception ignored){}

        File configFile = pluginDir.toPath().resolve("config.yml").toFile();
        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream(configFile);
        } catch (Exception e) {}
        this.config = (new Yaml()).load(inputStream);

        try {
            InputStream textSource = this.getClass().getClassLoader().getResourceAsStream("langs/"+config.get("lang")+".json");
            String fileData = new String(textSource.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject langJson = new Gson().fromJson(fileData , JsonObject.class);
            Map<String, JsonElement> langJsonElement = langJson.asMap();
            for (String key : langJsonElement.keySet()){
                String value = langJsonElement.get(key).getAsString();
                if (value == null) continue;
                languageMapping.put(key, value);
            }
        } catch (Exception ignored) {}

        Reflections reflections = new Reflections("org.moreenchantments.enchantments" ,new SubTypesScanner(false));
        Set<Class<?>> enchantments = reflections.getSubTypesOf(Object.class);
        enchantments.addAll(reflections.getSubTypesOf(Object.class));
        for (Class<?> ench : enchantments){
            String enchantmentName = ench.getName();
            try{
                Bukkit.getLogger().info("Loading enchantment - "+pathToName(enchantmentName));
                Object constructedEnchantment = ench.getDeclaredConstructor().newInstance();
                String[] requiredPlugins = (String[]) (getFieldFromInstance("requiredPlugins", constructedEnchantment));
                String[] requiredConfigs = (String[]) (getFieldFromInstance("requiredConfigs", constructedEnchantment));
                for (String i : requiredPlugins){
                    if (Bukkit.getPluginManager().getPlugin(i) == null){
                        throw new Exception("Required plugin '"+i+"' not found!");
                    }
                }
                for (String i : requiredConfigs){
                    if (config.get(i) == null){
                        throw new Exception("Required config '"+i+"' not found!");
                    }
                }
                constructedEnchantments.put("moreenchantments:"+((String) getStaticField("name", ench)), ench.getDeclaredConstructor().newInstance());

            }
            catch (Exception exception){
                Bukkit.getLogger().warning("Error while loading enchantment - "+pathToName(enchantmentName));
                exception.printStackTrace();
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
