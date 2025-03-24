package me.ram.bedwarsitemshop.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.ram.bedwarsitemshop.Main;
import me.ram.bedwarsitemshop.config.Config;

public class Commands implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("bedwarsitemshop")) {
			if (args.length == 0) {
				sender.sendMessage("§f==========================================================");
				sender.sendMessage("");
				sender.sendMessage("§b                     BedwarsItemShop");
				sender.sendMessage("");
				sender.sendMessage(" §a" + Main.getInstance().getLocaleConfig().getLanguage("version") + ": " + Main.getInstance().getVersion());
				sender.sendMessage("§7");
				sender.sendMessage(" §a" + Main.getInstance().getLocaleConfig().getLanguage("author") + ": Ram");
				sender.sendMessage("");
				sender.sendMessage("§f==========================================================");
				return true;
			}
			if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
				if (sender.hasPermission("bedwarsitemshop.reload")) {
					Config.loadConfig();
					sender.sendMessage(Config.getLanguage("commands.message.prefix") + Config.getLanguage("commands.message.reloaded"));
					return true;
				} else {
					sender.sendMessage(Config.getLanguage("commands.message.prefix") + Config.getLanguage("commands.message.no_permission"));
					return true;
				}
			}
		}
		return false;
	}
}
