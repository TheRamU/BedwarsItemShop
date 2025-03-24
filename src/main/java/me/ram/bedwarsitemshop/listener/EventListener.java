package me.ram.bedwarsitemshop.listener;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import me.ram.bedwarsitemshop.config.Config;
import me.ram.bedwarsitemshop.shop.GHDShop;
import me.ram.bedwarsitemshop.shop.NewShop;
import me.ram.bedwarsitemshop.shop.OldShop;
import me.ram.bedwarsitemshop.shop.Shop;

public class EventListener implements Listener {

	private final Map<Integer, Shop> shop_type_list;

	public EventListener() {
		shop_type_list = new HashMap<>();
		shop_type_list.put(1, new NewShop());
		shop_type_list.put(2, new OldShop());
		shop_type_list.put(3, new GHDShop());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onOpen(InventoryOpenEvent e) {
		if (!shop_type_list.containsKey(Config.mode)) {
			return;
		}
		Shop shop = shop_type_list.get(Config.mode);
		Player player = (Player) e.getPlayer();
		Inventory inventory = e.getInventory();
		Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
		if (game == null || !inventory.getTitle().equals(BedwarsRel._l(player, "ingame.shop.name"))) {
			return;
		}
		if (inventory.getSize() >= 54 && inventory.getItem(53) != null) {
			return;
		}
		e.setCancelled(true);
		shop.onOpen(game, player, inventory);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onClick(InventoryClickEvent e) {
		if (!shop_type_list.containsKey(Config.mode)) {
			return;
		}
		Shop shop = shop_type_list.get(Config.mode);
		Player player = (Player) e.getWhoClicked();
		Inventory inventory = e.getInventory();
		Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
		if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR || game == null || !inventory.getTitle().equals(BedwarsRel._l(player, "ingame.shop.name") + "§n§e§w")) {
			return;
		}
		e.setCancelled(true);
		if (!game.getState().equals(GameState.RUNNING)) {
			player.closeInventory();
			return;
		}
		shop.onClick(game, e);
	}
}
