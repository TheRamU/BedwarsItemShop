package me.ram.bedwarsitemshop;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import me.ram.bedwarsitemshop.commands.Commands;
import me.ram.bedwarsitemshop.config.Config;
import me.ram.bedwarsitemshop.config.LocaleConfig;
import me.ram.bedwarsitemshop.listener.EventListener;

public class Main extends JavaPlugin {

    @Getter
    private static Main instance;
    @Getter
    private LocaleConfig localeConfig;

    public String getVersion() {
        return "1.1";
    }

    public void onEnable() {
        if (!getDescription().getName().equals("BedwarsItemShop") || !getDescription().getVersion().equals(getVersion()) || !getDescription().getAuthors().contains("TheRamU")) {
            try {
                new Exception("Please don't edit plugin.yml!").printStackTrace();
            } catch (Exception e) {
            }
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        instance = this;
        localeConfig = new LocaleConfig();
        localeConfig.loadLocaleConfig();
        Bukkit.getConsoleSender().sendMessage("§f=========================================");
        Bukkit.getConsoleSender().sendMessage("§7");
        Bukkit.getConsoleSender().sendMessage("             §bBedwarsItemShop");
        Bukkit.getConsoleSender().sendMessage("§7");
        Bukkit.getConsoleSender().sendMessage(" §a" + localeConfig.getLanguage("version") + ": " + getVersion());
        Bukkit.getConsoleSender().sendMessage("§7");
        Bukkit.getConsoleSender().sendMessage(" §a" + localeConfig.getLanguage("author") + ": Ram");
        Bukkit.getConsoleSender().sendMessage("§7");
        Bukkit.getConsoleSender().sendMessage("§f=========================================");
        Config.loadConfig();
        Bukkit.getPluginCommand("bedwarsitemshop").setExecutor(new Commands());
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        try {
            Metrics metrics = new Metrics(this, 12105);
            metrics.addCustomChart(new SimplePie("language", () -> localeConfig.getPluginLocale().getName()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
