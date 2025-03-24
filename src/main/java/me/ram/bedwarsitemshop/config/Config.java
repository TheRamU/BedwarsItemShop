package me.ram.bedwarsitemshop.config;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.ram.bedwarsitemshop.Main;
import me.ram.bedwarsitemshop.utils.ColorUtil;

public class Config {

	private static FileConfiguration language_config;
	public static int mode;
	public static String message_buy;
	public static List<String> item_frame;
	public static List<String> item_back;

	public static void loadConfig() {
		File folder = new File(Main.getInstance().getDataFolder(), "/");
		if (!folder.exists()) {
			folder.mkdirs();
		}
		Main.getInstance().getLocaleConfig().loadLocaleConfig();
		FileConfiguration config = getVerifiedConfig("config.yml");
		language_config = getVerifiedConfig("language.yml");
		mode = config.getInt("mode");
		message_buy = ColorUtil.color(config.getString("message.buy"));
		item_frame = ColorUtil.colorList(config.getStringList("item.frame"));
		item_back = ColorUtil.colorList(config.getStringList("item.back"));
	}

	private static FileConfiguration getVerifiedConfig(String fileName) {
		Map<String, String> configVersion = new HashMap<String, String>();
		configVersion.put("config.yml", "2");
		configVersion.put("language.yml", "1");
		File file = new File(Main.getInstance().getDataFolder(), "/" + fileName);
		if (!file.exists()) {
			Main.getInstance().getLocaleConfig().saveResource(fileName);
			return YamlConfiguration.loadConfiguration(file);
		}
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		if (!config.contains("version") || !config.getString("version").equals(configVersion.getOrDefault(fileName, ""))) {
			file.renameTo(new File(Main.getInstance().getDataFolder(), "/" + fileName.split("\\.")[0] + "_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + "_old.yml"));
			Main.getInstance().getLocaleConfig().saveResource(fileName);
			config = YamlConfiguration.loadConfiguration(file);
		}
		return config;
	}

	public static String getLanguage(String path) {
		return ColorUtil.color(language_config.getString(path, "null"));
	}

	public static List<String> getLanguageList(String path) {
		if (language_config.contains(path) && language_config.isList(path)) {
			return ColorUtil.colorList(language_config.getStringList(path));
		}
		return Arrays.asList("null");
	}
}
