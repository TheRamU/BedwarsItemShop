package me.ram.bedwarsitemshop.shop;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import io.github.bedwarsrel.game.Game;

public interface Shop {

	void onOpen(Game game, Player player, Inventory shop);

	void onClick(Game game, InventoryClickEvent e);
}
