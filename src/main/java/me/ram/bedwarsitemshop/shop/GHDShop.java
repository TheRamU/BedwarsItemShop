package me.ram.bedwarsitemshop.shop;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.villager.MerchantCategory;
import me.ram.bedwarsitemshop.utils.ColorUtil;
import me.ram.bedwarsitemshop.utils.ItemShopUtils;
import me.ram.bedwarsitemshop.xpshop.ItemShop;
import me.ram.bedwarsitemshop.xpshop.XPItemShop;

public class GHDShop implements Shop {

	public void onOpen(Game game, Player player, Inventory shop) {
		List<ItemStack> shops = ItemShopUtils.getShops(shop);
		List<ItemStack> shop_items = ItemShopUtils.getShopItems(shop);
		Map<String, ItemStack> resource_list = ItemShopUtils.getResourceList();
		int line1 = shops.size() / 9;
		if (line1 * 9 < shops.size()) {
			line1++;
		}
		int line2 = shop_items.size() / 9;
		if (line2 * 9 < shop_items.size()) {
			line2++;
		}
		if (line2 == 0) {
			line2++;
		}
		Inventory inventory = Bukkit.createInventory(null, (line1 + line2) * 18, BedwarsRel._l(player, "ingame.shop.name") + "§n§e§w");
		int slot = 0;
		for (ItemStack item : shops) {
			inventory.setItem(slot, item);
			slot++;
		}
		int line = shops.size() / 9;
		if (line * 9 < shops.size()) {
			line++;
		}
		slot = line * 9 + 18;
        for (ItemStack shopItem : shop_items) {
            if (slot == 36) {
                slot = 45;
            }
            if (slot > inventory.getSize()) {
                break;
            }
            inventory.setItem(slot, shopItem);
            String lore = shopItem.getItemMeta().getLore().get(shopItem.getItemMeta().getLore().size() - 1);
            String[] args = lore.split(" ");
            ItemStack resitem = resource_list.getOrDefault(lore.substring(args[0].length() + 1), new ItemStack(Material.AIR));
            resitem.setAmount(Integer.parseInt(ColorUtil.removeColor(args[0])));
            ItemMeta resitemMeat = resitem.getItemMeta();
            resitemMeat.setDisplayName(lore + "§s§h§o§p§r§e§s");
            resitem.setItemMeta(resitemMeat);
            inventory.setItem(slot - 9, resitem);
            slot++;
        }
		ItemStack frame = ItemShopUtils.getFrameItem(7);
		for (int i = line * 9; i < 9 + (line * 9); i++) {
			inventory.setItem(i, frame);
		}
		if (shop_items.isEmpty() && !shops.isEmpty()) {
			if (Bukkit.getPluginManager().isPluginEnabled("BedwarsXP")) {
				XPItemShop itemShop = new XPItemShop(game.getNewItemShop(player).getCategories(), game);
				MerchantCategory clickedCategory = itemShop.getCategoryByMaterial(shops.get(0).getType());
				if (clickedCategory != null) {
					itemShop.openBuyInventory(clickedCategory, player, game);
					return;
				}
			} else {
				ItemShop itemShop = new ItemShop(game.getNewItemShop(player).getCategories());
				MerchantCategory clickedCategory = itemShop.getCategoryByMaterial(shops.get(0).getType());
				if (clickedCategory != null) {
					itemShop.openBuyInventory(clickedCategory, player, game);
					return;
				}
			}
		}
		player.openInventory(inventory);
	}

	public void onClick(Game game, InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		if (e.getCurrentItem().getItemMeta().getItemFlags().contains(ItemFlag.HIDE_ENCHANTS)) {
			return;
		}
		Map<String, ItemStack> resname = ItemShopUtils.getResourceList();
		if (e.getCurrentItem().getItemMeta().getDisplayName() != null && e.getCurrentItem().getItemMeta().getDisplayName().contains("§s§h§o§p§r§e§s")) {
			return;
		}
		if (ItemShopUtils.isShopItem(e.getCurrentItem())) {
			if (e.isShiftClick()) {
				int ba = 64 / e.getCurrentItem().getAmount();
				ItemShopUtils.buyItem(game, player, e.getCurrentItem(), resname, ba);
			} else {
				ItemShopUtils.buyItem(game, player, e.getCurrentItem(), resname, 1);
			}
		} else if (!e.getCurrentItem().isSimilar(ItemShopUtils.getFrameItem(7)) && !e.getCurrentItem().isSimilar(ItemShopUtils.getFrameItem(5))) {
			game.getNewItemShop(player).handleInventoryClick(e, game, player);
		}
	}
}
