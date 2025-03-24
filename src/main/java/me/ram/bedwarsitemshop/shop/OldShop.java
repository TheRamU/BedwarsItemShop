package me.ram.bedwarsitemshop.shop;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import me.ram.bedwarsitemshop.utils.ItemShopUtils;
import me.ram.bedwarsitemshop.xpshop.ItemShop;
import me.ram.bedwarsitemshop.xpshop.XPItemShop;

public class OldShop implements Shop {

	public void onOpen(Game game, Player player, Inventory shop) {
		List<ItemStack> shops = ItemShopUtils.getShops(shop);
		List<ItemStack> shop_items = ItemShopUtils.getShopItems(shop);
		int line;
		if (shop_items.isEmpty()) {
			line = shops.size() / 5;
			if (line * 5 < shops.size()) {
				line++;
			}
			line--;
		} else {
			line = shop_items.size() / 7;
			if (line * 7 < shop_items.size()) {
				line++;
			}
		}
		Inventory inventory = Bukkit.createInventory(null, line * 9 + 27, BedwarsRel._l(player, "ingame.shop.name") + "§n§e§w");
		int slot = 11;
		if (shop_items.isEmpty()) {
			for (ItemStack item : shops) {
				if (slot == 16) {
					slot = 20;
				} else if (slot == 25) {
					slot = 29;
				} else if (slot == 34) {
					slot = 38;
				} else if (slot == 43) {
					slot = 47;
				} else if (slot == 52) {
					break;
				}
				inventory.setItem(slot, item);
				slot++;
			}
		} else {
			slot = 10;
            for (ItemStack shopItem : shop_items) {
                if (slot == 17 || slot == 18) {
                    slot = 19;
                } else if (slot == 26 || slot == 27) {
                    slot = 28;
                } else if (slot == 35 || slot == 36) {
                    slot = 37;
                } else if (slot == 44 || slot == 45) {
                    slot = 46;
                } else if (slot == 53) {
                    break;
                }
                inventory.setItem(slot, shopItem);
                slot++;
            }
			inventory.setItem(inventory.getSize() - 5, ItemShopUtils.getBackItem());
		}
		player.openInventory(inventory);
	}

	public void onClick(Game game, InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		if (ItemShopUtils.getBackItem().isSimilar(e.getCurrentItem())) {
			game.getNewItemShop(player).openCategoryInventory(player);
			return;
		}
		Map<String, ItemStack> resname = ItemShopUtils.getResourceList();
		if (ItemShopUtils.isShopItem(e.getCurrentItem())) {
			if (e.isShiftClick()) {
				int ba = 64 / e.getCurrentItem().getAmount();
				ItemShopUtils.buyItem(game, player, e.getCurrentItem(), resname, ba);
			} else {
				ItemShopUtils.buyItem(game, player, e.getCurrentItem(), resname, 1);
			}
		} else {
			if (Bukkit.getPluginManager().isPluginEnabled("BedwarsXP")) {
				new XPItemShop(game.getNewItemShop(player).getCategories(), game).handleInventoryClick(e, game, player);
			} else {
				new ItemShop(game.getNewItemShop(player).getCategories()).handleInventoryClick(e, game, player);
			}
		}
	}
}
