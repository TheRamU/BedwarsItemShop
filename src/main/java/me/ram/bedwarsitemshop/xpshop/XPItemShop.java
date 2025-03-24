package me.ram.bedwarsitemshop.xpshop;

import io.github.bedwarsrel.game.*;
import io.github.bedwarsrel.shop.NewItemShop;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.*;
import io.github.bedwarsrel.*;
import org.bukkit.*;
import org.bukkit.enchantments.*;
import org.bukkit.event.inventory.*;
import io.github.bedwarsrel.villager.*;
import io.github.bedwarsrel.utils.*;
import java.lang.reflect.*;

import ldcr.BedwarsXP.XPShop.XPVillagerTrade;
import ldcr.BedwarsXP.api.*;
import org.bukkit.inventory.*;
import java.util.*;
import org.bukkit.inventory.meta.*;

public class XPItemShop extends NewItemShop {
	private final Game bedwars;
	@Getter
    private List<MerchantCategory> categories;
	@Setter
    private MerchantCategory currentCategory;

	public XPItemShop(List<MerchantCategory> cate, Game bw) {
		super(cate);
		this.currentCategory = null;
		this.categories = cate;
		this.bedwars = bw;
	}

    public boolean hasOpenCategory() {
		return this.currentCategory != null;
	}

	public boolean hasOpenCategory(MerchantCategory category) {
		return this.currentCategory != null && this.currentCategory.equals(category);
	}

	private int getCategoriesSize(Player player) {
		int size = 0;
		for (MerchantCategory cat : this.categories) {
			if (cat.getMaterial() != null && (player == null || player.hasPermission(cat.getPermission()))) {
				++size;
			}
		}
		return size;
	}

	public void openCategoryInventory(Player player) {
		int catSize = this.getCategoriesSize(player);
		int nom = (catSize % 9 == 0) ? 9 : (catSize % 9);
		int size = catSize + (9 - nom) + 9;
		Inventory inventory = Bukkit.createInventory(player, size, BedwarsRel._l("ingame.shop.name"));
		this.addCategoriesToInventory(inventory, player);
		Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
		ItemStack stack;
		if (game != null) {
			if (game.getPlayerSettings(player).oneStackPerShift()) {
				stack = new ItemStack(Material.BUCKET, 1);
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(ChatColor.AQUA + BedwarsRel._l("default.currently") + ": " + ChatColor.WHITE + BedwarsRel._l("ingame.shop.onestackpershift"));
				meta.setLore(new ArrayList<>());
				stack.setItemMeta(meta);
			} else {
				stack = new ItemStack(Material.LAVA_BUCKET, 1);
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(ChatColor.AQUA + BedwarsRel._l("default.currently") + ": " + ChatColor.WHITE + BedwarsRel._l("ingame.shop.fullstackpershift"));
				meta.setLore(new ArrayList<>());
				stack.setItemMeta(meta);
			}
            inventory.setItem(size - 4, stack);
        }
		player.openInventory(inventory);
	}

	private void addCategoriesToInventory(Inventory inventory, Player player) {
		for (MerchantCategory category : this.categories) {
			if (category.getMaterial() == null) {
				BedwarsRel.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "Careful: Not supported material in shop category '" + category.getName() + "'"));
			} else {
				if (player != null && !player.hasPermission(category.getPermission())) {
					continue;
				}
				ItemStack is = new ItemStack(category.getMaterial(), 1);
				ItemMeta im = is.getItemMeta();
				if (this.currentCategory != null && this.currentCategory.equals(category)) {
					im.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
				}
				im.setDisplayName(category.getName());
				im.setLore(category.getLores());
				is.setItemMeta(im);
				inventory.addItem(is);
			}
		}
	}

	private int getInventorySize(int itemAmount) {
		int nom = (itemAmount % 9 == 0) ? 9 : (itemAmount % 9);
		return itemAmount + (9 - nom);
	}

	public void handleInventoryClick(InventoryClickEvent ice, Game game, Player player) {
		if (!this.hasOpenCategory()) {
			this.handleCategoryInventoryClick(ice, game, player);
		} else {
			this.handleBuyInventoryClick(ice, game, player);
		}
	}

	private void handleCategoryInventoryClick(InventoryClickEvent ice, Game game, Player player) {
		int catSize = this.getCategoriesSize(player);
		int sizeCategories = this.getInventorySize(catSize) + 9;
		int rawSlot = ice.getRawSlot();
		if (rawSlot >= this.getInventorySize(catSize) && rawSlot < sizeCategories) {
			ice.setCancelled(true);
			if (ice.getCurrentItem().getType() == Material.BUCKET) {
				game.getPlayerSettings(player).setOneStackPerShift(false);
				player.playSound(player.getLocation(), SoundMachine.get("CLICK", "UI_BUTTON_CLICK"), 10.0f, 1.0f);
				this.openCategoryInventory(player);
				return;
			}
			if (ice.getCurrentItem().getType() == Material.LAVA_BUCKET) {
				game.getPlayerSettings(player).setOneStackPerShift(true);
				player.playSound(player.getLocation(), SoundMachine.get("CLICK", "UI_BUTTON_CLICK"), 10.0f, 1.0f);
				this.openCategoryInventory(player);
				return;
			}
		}
		MerchantCategory clickedCategory = this.getCategoryByMaterial(ice.getCurrentItem().getType());
		if (clickedCategory != null) {
			this.openBuyInventory(clickedCategory, player, game);
			player.playSound(player.getLocation(), SoundMachine.get("CLICK", "UI_BUTTON_CLICK"), 10.0f, 1.0f);
			return;
		}
		if (ice.isShiftClick()) {
			ice.setCancelled(true);
        }
	}

	public void openBuyInventory(MerchantCategory category, Player player, Game game) {
		ArrayList<VillagerTrade> offers = category.getOffers();
		int sizeCategories = this.getCategoriesSize(player);
		int sizeItems = offers.size();
		int invSize = this.getBuyInventorySize(sizeCategories, sizeItems);
		this.currentCategory = category;
		Inventory buyInventory = Bukkit.createInventory(player, invSize, BedwarsRel._l("ingame.shop.name"));
		this.addCategoriesToInventory(buyInventory, player);
		for (int i = 0; i < offers.size(); ++i) {
			VillagerTrade trade = offers.get(i);
			if (trade.getItem1().getType() != Material.AIR || trade.getRewardItem().getType() != Material.AIR) {
				int slot = this.getInventorySize(sizeCategories) + i;
				ItemStack tradeStack = this.toItemStack(trade, player, game);
				buyInventory.setItem(slot, tradeStack);
			}
		}
		player.openInventory(buyInventory);
	}

	private int getBuyInventorySize(int sizeCategories, int sizeOffers) {
		return this.getInventorySize(sizeCategories) + this.getInventorySize(sizeOffers);
	}

	private ItemStack toItemStack(VillagerTrade trade, Player player, Game game) {
		ItemStack tradeStack = trade.getRewardItem().clone();
		Method colorable = Utils.getColorableMethod(tradeStack.getType());
		ItemMeta meta = tradeStack.getItemMeta();
		ItemStack item1 = trade.getItem1();
		ItemStack item2 = trade.getItem2();
		if (tradeStack.getType().equals(Material.STAINED_GLASS) || tradeStack.getType().equals(Material.WOOL) || tradeStack.getType().equals(Material.STAINED_CLAY)) {
			tradeStack.setDurability(game.getPlayerTeam(player).getColor().getDyeColor().getWoolData());
		} else if (colorable != null) {
			colorable.setAccessible(true);
			try {
				colorable.invoke(meta, game.getPlayerTeam(player).getColor().getColor());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		List<String> lores = meta.getLore();
		if (lores == null) {
			lores = new ArrayList<>();
		}
		if (trade instanceof XPVillagerTrade) {
			XPVillagerTrade xpTrade = ((XPVillagerTrade) trade);
			int xp = xpTrade.getXp();
			lores.add("§a" + xp + " 经验");
		} else {
			lores.add(ChatColor.WHITE + String.valueOf(item1.getAmount()) + " " + item1.getItemMeta().getDisplayName());
			if (item2 != null) {
				lores.add(ChatColor.WHITE + String.valueOf(item2.getAmount()) + " " + item2.getItemMeta().getDisplayName());
			}
		}
		meta.setLore(lores);
		tradeStack.setItemMeta(meta);
		return tradeStack;
	}

	public void handleBuyInventoryClick(InventoryClickEvent ice, Game game, Player player) {
		int sizeCategories = this.getCategoriesSize(player);
		ArrayList<VillagerTrade> offers = this.currentCategory.getOffers();
		int sizeItems = offers.size();
		int totalSize = this.getBuyInventorySize(sizeCategories, sizeItems);
		ItemStack item = ice.getCurrentItem();
		boolean cancel = false;
		int bought = 0;
		boolean oneStackPerShift = game.getPlayerSettings(player).oneStackPerShift();
		if (this.currentCategory == null) {
			player.closeInventory();
			return;
		}
		if (ice.getRawSlot() < sizeCategories) {
			ice.setCancelled(true);
			if (item == null) {
				return;
			}
			if (item.getType().equals(this.currentCategory.getMaterial())) {
				this.currentCategory = null;
				this.openCategoryInventory(player);
			} else {
				this.handleCategoryInventoryClick(ice, game, player);
			}
		} else {
			if (ice.getRawSlot() >= totalSize) {
                ice.setCancelled(ice.isShiftClick());
				return;
			}
			ice.setCancelled(true);
			if (item == null || item.getType() == Material.AIR) {
				return;
			}
			MerchantCategory category = this.currentCategory;
			VillagerTrade trade = this.getTradingItem(category, item, game, player);
			if (trade == null) {
				return;
			}
			player.playSound(player.getLocation(), SoundMachine.get("ITEM_PICKUP", "ENTITY_ITEM_PICKUP"), 10.0f, 1.0f);
			if (!this.hasEnoughRessource(player, trade)) {
				player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel._l("errors.notenoughress")));
				return;
			}
			if (ice.isShiftClick()) {
				while (this.hasEnoughRessource(player, trade) && !cancel) {
					cancel = !this.buyItem(trade, ice.getCurrentItem(), player);
					if (!cancel && oneStackPerShift) {
						bought += item.getAmount();
						cancel = (bought + item.getAmount() > 64);
					}
				}
				bought = 0;
			} else {
				this.buyItem(trade, ice.getCurrentItem(), player);
			}
		}
	}

	private boolean buyItem(VillagerTrade trade, ItemStack item, Player player) {
		PlayerInventory inventory = player.getInventory();
		boolean success = true;
		if (!(trade instanceof XPVillagerTrade)) {
			int item1ToPay = trade.getItem1().getAmount();
			Iterator<?> stackIterator = inventory.all(trade.getItem1().getType()).entrySet().iterator();
			int firstItem1 = inventory.first(trade.getItem1());
			if (firstItem1 > -1) {
				inventory.clear(firstItem1);
			} else {
				while (stackIterator.hasNext()) {
					Map.Entry<Integer, ItemStack> entry = (Map.Entry<Integer, ItemStack>) stackIterator.next();
					ItemStack stack = entry.getValue();
					int endAmount = stack.getAmount() - item1ToPay;
					if (endAmount < 0) {
						endAmount = 0;
					}
					item1ToPay -= stack.getAmount();
					stack.setAmount(endAmount);
					inventory.setItem(entry.getKey(), stack);
					if (item1ToPay <= 0) {
						break;
					}
				}
			}
			if (trade.getItem2() != null) {
				int item2ToPay = trade.getItem2().getAmount();
				stackIterator = inventory.all(trade.getItem2().getType()).entrySet().iterator();
				int firstItem2 = inventory.first(trade.getItem2());
				if (firstItem2 > -1) {
					inventory.clear(firstItem2);
				} else {
					while (stackIterator.hasNext()) {
						Map.Entry<Integer, ItemStack> entry2 = (Map.Entry<Integer, ItemStack>) stackIterator.next();
						ItemStack stack2 = entry2.getValue();
						int endAmount2 = stack2.getAmount() - item2ToPay;
						if (endAmount2 < 0) {
							endAmount2 = 0;
						}
						item2ToPay -= stack2.getAmount();
						stack2.setAmount(endAmount2);
						inventory.setItem(entry2.getKey(), stack2);
						if (item2ToPay <= 0) {
							break;
						}
					}
				}
			}
		} else {
			XPManager.getXPManager(this.bedwars.getName()).takeXP(player, ((XPVillagerTrade) trade).getXp());
		}
		ItemStack addingItem = item.clone();
		ItemMeta meta = addingItem.getItemMeta();
		List<String> lore = meta.getLore();
		if (!lore.isEmpty()) {
			lore.remove(lore.size() - 1);
			if (trade.getItem2() != null && !(trade instanceof XPVillagerTrade)) {
				lore.remove(lore.size() - 1);
			}
		}
		meta.setLore(lore);
		addingItem.setItemMeta(meta);
		HashMap<Integer, ? extends ItemStack> notStored = inventory.addItem(addingItem);
		if (!notStored.isEmpty()) {
			ItemStack notAddedItem = notStored.get(0);
			int removingAmount = addingItem.getAmount() - notAddedItem.getAmount();
			addingItem.setAmount(removingAmount);
			inventory.removeItem(addingItem);
			inventory.addItem(trade.getItem1());
			if (trade.getItem2() != null) {
				inventory.addItem(trade.getItem2());
			}
			success = false;
		}
		player.updateInventory();
		return success;
	}

	private boolean hasEnoughRessource(Player player, VillagerTrade trade) {
		if (trade instanceof XPVillagerTrade) {
			return XPManager.getXPManager(this.bedwars.getName()).hasEnoughXP(player, ((XPVillagerTrade) trade).getXp());
		}
		ItemStack item1 = trade.getItem1();
		ItemStack item2 = trade.getItem2();
		PlayerInventory inventory = player.getInventory();
		if (item2 != null) {
            return inventory.contains(item1.getType(), item1.getAmount()) && inventory.contains(item2.getType(), item2.getAmount());
		} else return inventory.contains(item1.getType(), item1.getAmount());
    }

	private VillagerTrade getTradingItem(MerchantCategory category, ItemStack stack, Game game, Player player) {
		for (VillagerTrade trade : category.getOffers()) {
			if (trade.getItem1().getType() != Material.AIR || trade.getRewardItem().getType() != Material.AIR) {
				ItemStack iStack = this.toItemStack(trade, player, game);
				if (iStack.getType() == Material.ENDER_CHEST && stack.getType() == Material.ENDER_CHEST) {
					return trade;
				}
				if ((iStack.getType() == Material.POTION || (BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_9") && (iStack.getType().equals(Material.valueOf("TIPPED_ARROW")) || iStack.getType().equals(Material.valueOf("LINGERING_POTION")) || iStack.getType().equals(Material.valueOf("SPLASH_POTION"))))) && ((PotionMeta) iStack.getItemMeta()).getCustomEffects().equals(((PotionMeta) stack.getItemMeta()).getCustomEffects())) {
					return trade;
				}
				if (iStack.equals(stack)) {
					return trade;
				}
            }
		}
		return null;
	}

	public MerchantCategory getCategoryByMaterial(Material material) {
		for (MerchantCategory category : this.categories) {
			if (category.getMaterial() == material) {
				return category;
			}
		}
		return null;
	}
}
