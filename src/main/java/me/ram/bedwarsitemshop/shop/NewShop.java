package me.ram.bedwarsitemshop.shop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.utils.Utils;
import io.github.bedwarsrel.villager.MerchantCategory;
import io.github.bedwarsrel.villager.VillagerTrade;
import me.ram.bedwarsitemshop.utils.ItemShopUtils;
import me.ram.bedwarsitemshop.xpshop.ItemShop;
import me.ram.bedwarsitemshop.xpshop.XPItemShop;

public class NewShop implements Shop {

	public void onOpen(Game game, Player player, Inventory shop) {
		List<ItemStack> shops = ItemShopUtils.getShops(shop);
		List<ItemStack> shop_items = ItemShopUtils.getShopItems(shop);
		Inventory inventory = Bukkit.createInventory(null, 54, BedwarsRel._l(player, "ingame.shop.name") + "§n§e§w");
		int slot = 0;
		for (ItemStack item : shops) {
			inventory.setItem(slot, item);
			slot++;
		}
		int line = shops.size() / 9;
		if (line * 9 < shops.size()) {
			line++;
		}
		slot = line * 9 + 10;
        for (ItemStack shopItem : shop_items) {
            if (slot == 26 || slot == 27) {
                slot = 28;
            }
            if (slot == 35 || slot == 36) {
                slot = 37;
            }
            if (slot == 44 || slot == 45) {
                slot = 46;
            }
            if (slot == 53) {
                break;
            }
            inventory.setItem(slot, shopItem);
            slot++;
        }
		ItemStack frame = ItemShopUtils.getFrameItem(7);
		for (int i = line * 9; i < 9 + (line * 9); i++) {
			for (int l = 1; l < line + 1; l++) {
				if (inventory.getItem(i - l * 9) != null && inventory.getItem(i - l * 9).getEnchantments().containsKey(Enchantment.DAMAGE_ALL)) {
					frame.setDurability((short) 5);
					inventory.getItem(i - l * 9).removeEnchantment(Enchantment.DAMAGE_ALL);
				}
			}
			inventory.setItem(i, frame);
			frame.setDurability((short) 7);
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
		Map<String, ItemStack> resname = ItemShopUtils.getResourceList();
		if (ItemShopUtils.isShopItem(e.getCurrentItem())) {
			if (e.isShiftClick()) {
				int ba = 64 / e.getCurrentItem().getAmount();
				ItemShopUtils.buyItem(game, player, e.getCurrentItem(), resname, ba);
			} else {
				ItemShopUtils.buyItem(game, player, e.getCurrentItem(), resname, 1);
			}
		} else if (!e.getCurrentItem().isSimilar(ItemShopUtils.getFrameItem(7)) && !e.getCurrentItem().isSimilar(ItemShopUtils.getFrameItem(5))) {
			if (Bukkit.getPluginManager().isPluginEnabled("BedwarsXP")) {
				new XPItemShop(game.getNewItemShop(player).getCategories(), game).handleInventoryClick(e, game, player);
			} else {
				new ItemShop(game.getNewItemShop(player).getCategories()).handleInventoryClick(e, game, player);
			}
		}
	}

	private VillagerTrade getTradingItem(MerchantCategory category, ItemStack stack, Game game, Player player) {
		for (VillagerTrade trade : category.getOffers()) {
			if (trade.getItem1().getType() == Material.AIR && trade.getRewardItem().getType() == Material.AIR) {
				continue;
			}
			ItemStack iStack = this.toItemStack(trade, player, game);
			if (iStack.getType() == Material.ENDER_CHEST && stack.getType() == Material.ENDER_CHEST) {
				return trade;
			}
			if (iStack.getType() == Material.POTION || (!BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_8") && (iStack.getType().equals(Material.valueOf("TIPPED_ARROW")) || iStack.getType().equals(Material.valueOf("LINGERING_POTION")) || iStack.getType().equals(Material.valueOf("SPLASH_POTION"))))) {
				if (BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_8")) {
					if (iStack.getItemMeta().equals(stack.getItemMeta())) {
						return trade;
					}
                } else {
					PotionMeta iStackMeta = (PotionMeta) iStack.getItemMeta();
					PotionMeta stackMeta = (PotionMeta) stack.getItemMeta();
					if (iStackMeta.getBasePotionData().equals(stackMeta.getBasePotionData()) && iStackMeta.getCustomEffects().equals(stackMeta.getCustomEffects())) {
						return trade;
					}
                }
			} else {
				if (iStack.equals(stack)) {
					return trade;
				}
            }
		}
		return null;
	}

	private ItemStack toItemStack(VillagerTrade trade, Player player, Game game) {
		ItemStack tradeStack = trade.getRewardItem().clone();
		Method colorable = Utils.getColorableMethod(tradeStack.getType());
		ItemMeta meta = tradeStack.getItemMeta();
		ItemStack item1 = trade.getItem1();
		ItemStack item2 = trade.getItem2();
		if (Utils.isColorable(tradeStack)) {
			tradeStack.setDurability(game.getPlayerTeam(player).getColor().getDyeColor().getWoolData());
		} else if (colorable != null) {
			colorable.setAccessible(true);
			try {
				colorable.invoke(meta, game.getPlayerTeam(player).getColor().getColor());
			} catch (Exception e) {
				BedwarsRel.getInstance().getBugsnag().notify(e);
				e.printStackTrace();
			}
		}
		List<String> lores = meta.getLore();
		if (lores == null) {
			lores = new ArrayList<>();
		}
		lores.add(ChatColor.WHITE + String.valueOf(item1.getAmount()) + " " + item1.getItemMeta().getDisplayName());
		if (item2 != null) {
			lores.add(ChatColor.WHITE + String.valueOf(item2.getAmount()) + " " + item2.getItemMeta().getDisplayName());
		}
		meta.setLore(lores);
		tradeStack.setItemMeta(meta);
		return tradeStack;
	}
}
