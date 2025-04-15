package org.moreenchantments;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.apache.commons.io.FileUtils;
import org.moreenchantments.enchantments.AbstractCustomEnchantment;
import org.moreenchantments.enchantments.MoneyMendingEnchantment;
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

public final class Main extends JavaPlugin {
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

    public static Main getThis(){
        return (Main)(Bukkit.getPluginManager().getPlugin("MoreEnchantments"));
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


    public HashMap<String, AbstractCustomEnchantment> constructedEnchantments = new HashMap<>();
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

        AbstractCustomEnchantment.registerCustomEnchantment(MoneyMendingEnchantment.class);

        Set<Class<? extends AbstractCustomEnchantment>> enchantments = AbstractCustomEnchantment.customEnchantments;
        for (Class<?> ench : enchantments){
            String enchantmentName = ench.getName();
            try{
                Bukkit.getLogger().info("Loading enchantment - "+pathToName(enchantmentName));
                AbstractCustomEnchantment constructedEnchantment = (AbstractCustomEnchantment) ench.getDeclaredConstructor().newInstance();
                String[] requiredPlugins = constructedEnchantment.requiredPlugins;
                String[] requiredConfigs = constructedEnchantment.requiredConfigs;
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
                constructedEnchantments.put("moreenchantments:"+constructedEnchantment.name, constructedEnchantment);
            }
            catch (Exception exception){
                Bukkit.getLogger().warning("Error while loading enchantment - "+pathToName(enchantmentName));
                exception.printStackTrace();
            }
        }

        getServer().getPluginManager().registerEvents(new Events(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
