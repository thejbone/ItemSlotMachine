    package com.darkblade12.itemslotmachine.settings;

    import com.darkblade12.itemslotmachine.ItemSlotMachine;
    import com.darkblade12.itemslotmachine.coin.CoinConfig;
    import com.darkblade12.itemslotmachine.item.ItemFactory;
    import com.darkblade12.itemslotmachine.reader.TemplateReader;
    import com.darkblade12.itemslotmachine.slotmachine.SlotMachineConfig;
    import org.bukkit.Material;
    import org.bukkit.configuration.Configuration;
    import org.bukkit.configuration.ConfigurationSection;
    import org.bukkit.inventory.ItemStack;

    import java.io.File;
    import java.io.IOException;
    import java.lang.reflect.Array;
    import java.nio.file.Files;
    import java.nio.file.Paths;
    import java.util.*;

    @SuppressWarnings("deprecation")
    public final class Settings {
        private static final SimpleSection GENERAL_SETTINGS = new SimpleSection("General_Settings");
        private static final HashMap<String,SlotMachineConfig> SLOT_MACHINES_CONFIGS = new HashMap<>();
        private static final HashMap<String, CoinConfig> COIN_CONFIGS = new HashMap<>();
        private static final HashMap<String,TemplateReader> TEMPLATE_CONFIGS = new HashMap<>();
        private static final SimpleSection DESIGN_SETTINGS = new SimpleSection("Design_Settings");
        private static boolean debugModeEnabled;
        private static String languageName;
        private static String defaultDesignName;
        private static String rawDesignName;
        private ItemSlotMachine plugin;

        public Settings(ItemSlotMachine plugin) {
            this.plugin = plugin;
        }

        public static HashMap<String,SlotMachineConfig> getSlotMachineConfigs(){
            return SLOT_MACHINES_CONFIGS;
        }
        public static HashMap<String,CoinConfig> getCoinConfigs(){
            return COIN_CONFIGS;
        }

        public static boolean isDebugModeEnabled() {
            return debugModeEnabled;
        }

        public static String getLanguageName() {
            return languageName;
        }

        public static String getRawDesignName() {
            return rawDesignName;
        }

        public static String getDefaultDesignName() {
            return defaultDesignName;
        }

        public static Map<String, TemplateReader> getTemplates() {return TEMPLATE_CONFIGS;}

        public void load() throws Exception {
            Configuration c = plugin.loadConfig();
            debugModeEnabled = GENERAL_SETTINGS.getBoolean(c, "Debug_Mode_Enabled");
            languageName = GENERAL_SETTINGS.getString(c, "Language_Name");
            if (languageName == null)
                throw new InvalidValueException("Language_Name", GENERAL_SETTINGS, "is null");

            for (String machine : c.getConfigurationSection("SlotMachines").getKeys(false)) {
                ConfigurationSection machineSection = c.getConfigurationSection("SlotMachines." + machine);
                File folder = new File(plugin.getDataFolder()+"/slot machines/"+machine);
                if(!folder.exists()){
                    folder.mkdirs();
                }
                SlotMachineConfig newMachine = new SlotMachineConfig(machine, machineSection.getString("Default_Name"));
                ConfigurationSection coinSection = c.getConfigurationSection("SlotMachines." + machine + ".Coin");
                ItemStack coinItem;
                String coinString = coinSection.getString("Item");
                if (coinString == null)
                    throw new Exception("Coin item for " + machine + " is null");
                try {
                    coinItem = ItemFactory.fromString(coinString);
                } catch (Exception e) {
                    throw new Exception("Coin item for " + machine + " is invalid" + e.getMessage());
                }

                CoinConfig newCoin = new CoinConfig(machine, coinSection.getString("Name"), coinItem,
                        coinSection.getInt("Price"), coinSection.getString("Lore"));

                newMachine.setCoinConfig(newCoin);
                newMachine.setLIMITED_USAGE(machineSection.getBoolean("Limited_Usage.Enabled"));
                newMachine.setMAX_USAGE(machineSection.getInt("Limited_Usage.Amount"));

                File templateFile = new File("plugins/ItemSlotMachine/"+newMachine.getSLOT_NAME()+".yml");
                if(!templateFile.exists()){
                    try {
                        Files.copy(Paths.get(plugin.getDataFolder()+"/template.yml"), Paths.get((plugin.getDataFolder()+ "/"+ newMachine.getSLOT_NAME() + ".yml")));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                TemplateReader templateNew = new TemplateReader(plugin,  newMachine.getSLOT_NAME()+".yml", "plugins/ItemSlotMachine/");
                if (!templateNew.readTemplate()) {
                    plugin.getLogger().warning("Failed to read " + newMachine.getSLOT_NAME() + ".yml, plugin will disable!");
                    plugin.getServer().getPluginManager().disablePlugin(plugin);
                    return;
                }
                plugin.getLogger().info("Loaded " + newMachine.getSLOT_NAME() + ".yml");
                TEMPLATE_CONFIGS.put(newMachine.getSLOT_NAME(),templateNew);

                COIN_CONFIGS.put(machine,newCoin);
                SLOT_MACHINES_CONFIGS.put(machine,newMachine);
            }

            defaultDesignName = DESIGN_SETTINGS.getString(c, "Default_Name");
            if (defaultDesignName == null)
                throw new InvalidValueException("Default_Name", DESIGN_SETTINGS, "is null");
            else if (defaultDesignName.matches(".+<num>.+"))
                throw new InvalidValueException("Default_Name", DESIGN_SETTINGS, "has <num> at an invalid position (middle)");
            else if (!defaultDesignName.contains("<num>"))
                defaultDesignName += "<num>";
            rawDesignName = defaultDesignName.replace("<num>", "");
        }

        public void reload() throws Exception {
            plugin.reloadConfig();
            load();
        }
    }