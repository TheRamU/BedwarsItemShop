package me.ram.bedwarsitemshop.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.utils.SoundMachine;
import ldcr.BedwarsXP.api.XPManager;
import me.ram.bedwarsitemshop.Main;
import me.ram.bedwarsitemshop.config.Config;

public class ItemShopUtils {

	public static boolean isResources(String line) {
		String[] args = line.split(" ");
		return args.length > 1 && ColorUtil.removeColor(args[0].replaceAll("\\d+", "")).isEmpty() && getResourceNameList().contains(line.substring(args[0].length() + 1));
	}

	public static boolean isShopItem(ItemStack itemStack) {
		return isResources(itemStack, 1);
	}

	private static boolean isResources(ItemStack itemStack, int line) {
		if (itemStack != null && itemStack.getItemMeta().hasLore() && itemStack.getItemMeta().getLore().size() >= line) {
			String lore = itemStack.getItemMeta().getLore().get(itemStack.getItemMeta().getLore().size() - line);
			return isResources(lore);
		}
		return false;
	}

	public static Map<String, ItemStack> getResourceList() {
		Map<String, ItemStack> resourceList = new HashMap<>();
		for (ItemStack res : getResource()) {
			if (res.getItemMeta().getDisplayName() != null) {
				resourceList.put(res.getItemMeta().getDisplayName(), res);
			}
		}
		resourceList.put("经验", new ItemStack(Material.EXP_BOTTLE));
		return resourceList;
	}

	public static List<String> getResourceNameList() {
		List<String> resourceList = new ArrayList<>();
		for (ItemStack res : getResource()) {
			if (res.getItemMeta().getDisplayName() == null) {
				resourceList.add("null");
			} else {
				resourceList.add(res.getItemMeta().getDisplayName());
			}
		}
		resourceList.add("经验");
		return resourceList;
	}

	public static List<ItemStack> getShops(Inventory shop) {
		List<ItemStack> shops = new ArrayList<>();
		for (int i = 0; i < shop.getSize(); i++) {
			ItemStack itemStack = shop.getItem(i);
			if (itemStack != null && !isOptionItem(itemStack) && !isShopItem(itemStack)) {
				shops.add(shop.getItem(i));
			}
		}
		return shops;
	}

	public static List<ItemStack> getShopItems(Inventory shop) {
		List<ItemStack> shopItems = new ArrayList<>();
		for (int i = 0; i < shop.getSize(); i++) {
			ItemStack itemStack = shop.getItem(i);
			if (isShopItem(itemStack)) {
				shopItems.add(itemStack);
			}
		}
		return shopItems;
	}

	public static ItemStack getFrameItem(int damage) {
		ItemStack itemStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) damage);
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(getItemName(Config.item_frame) + "§f§r§a§m§e");
		itemMeta.setLore(getItemLore(Config.item_frame));
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	public static ItemStack getBackItem() {
		ItemStack itemStack = new ItemStack(Material.ARROW);
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(getItemName(Config.item_back) + "§b§a§c§k");
		itemMeta.setLore(getItemLore(Config.item_back));
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	public static void buyItem(Game game, Player player, ItemStack itemStack, Map<String, ItemStack> resname, int a) {
		String lore = itemStack.getItemMeta().getLore().get(itemStack.getItemMeta().getLore().size() - 1);
		String lore_2 = null;
		if (isResources(itemStack, 2)) {
			lore_2 = itemStack.getItemMeta().getLore().get(itemStack.getItemMeta().getLore().size() - 2);
		}
		for (int i = 0; i < a; i++) {
			if ((lore_2 == null || isEnough(game, player, lore_2, resname)) && isEnough(game, player, lore, resname)) {
				takeItem(game, player, lore, resname);
				if (lore_2 != null) {
					takeItem(game, player, lore_2, resname);
				}
				ItemStack item = itemStack.clone();
				List<String> lores = item.getItemMeta().getLore();
				lores.remove(lores.size() - 1);
				ItemMeta meta = item.getItemMeta();
				meta.setLore(lores);
				item.setItemMeta(meta);
				player.getInventory().addItem(item);
				if (i < 1) {
					player.playSound(player.getLocation(), SoundMachine.get("ITEM_PICKUP", "ENTITY_ITEM_PICKUP"), Float.parseFloat("1.0"), Float.parseFloat("1.0"));
				}
				if (i < 1 && !Config.message_buy.isEmpty()) {
					String name;
					if (item.getItemMeta().getDisplayName() == null) {
						if (Main.getInstance().getLocaleConfig().getPluginLocale().name().startsWith("ZH_")) {
							name = ItemUtil.getRealName(item);
						} else {
							name = item.getType().name().replace("_", " ");
						}
					} else {
						name = item.getItemMeta().getDisplayName();
					}
					player.sendMessage(Config.message_buy.replace("{item}", name));
				}
			} else if (i < 1) {
				player.sendMessage("§c" + ColorUtil.color(BedwarsRel._l(player, "errors.notenoughress")));
			}
		}
	}

	public static boolean isEnough(Game game, Player player, String line, Map<String, ItemStack> resname) {
		String[] args = line.split(" ");
		String type = line.substring(args[0].length() + 1);
		int amount = Integer.parseInt(ColorUtil.removeColor(args[0]));
		if (type.equals("经验") && Bukkit.getPluginManager().isPluginEnabled("BedwarsXP")) {
            return XPManager.getXPManager(game.getName()).getXP(player) >= amount;
		} else {
			int k = 0;
			int i = player.getInventory().getContents().length;
			ItemStack[] stacks = player.getInventory().getContents();
			for (int j = 0; j < i; j++) {
				ItemStack stack = stacks[j];
				if (stack != null) {
					if (stack.getType().equals(resname.get(type).getType())) {
						k = k + stack.getAmount();
					}
				}
			}
            return k >= amount;
		}
    }

	public static void takeItem(Game game, Player player, String line, Map<String, ItemStack> resname) {
		String[] args = line.split(" ");
		String type = line.substring(args[0].length() + 1);
		int amount = Integer.parseInt(ColorUtil.removeColor(args[0]));
		if (type.equals("经验")) {
			XPManager.getXPManager(game.getName()).takeXP(player, amount);
		} else {
			int ta = amount;
			int i = player.getInventory().getContents().length;
			ItemStack[] stacks = player.getInventory().getContents();
			for (int j = 0; j < i; j++) {
				ItemStack stack = stacks[j];
				if (stack != null) {
					if (stack.getType().equals(resname.get(type).getType()) && ta > 0) {
						if (stack.getAmount() >= ta) {
							stack.setAmount(stack.getAmount() - ta);
							ta = 0;
						} else if (stack.getAmount() < ta) {
							ta = ta - stack.getAmount();
							stack.setAmount(0);
						}
						player.getInventory().setItem(j, stack);
					}
				}
			}
		}
	}

	private static String getItemName(List<String> list) {
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return "§f";
	}

	private static List<String> getItemLore(List<String> list) {
		List<String> lore = new ArrayList<>();
		if (list.size() > 1) {
			lore.addAll(list);
			lore.remove(0);
		}
		return lore;
	}

	private static Boolean isOptionItem(ItemStack item) {
		ItemStack slime = new ItemStack(Material.SLIME_BALL, 1);
		ItemMeta slimeMeta = slime.getItemMeta();
		slimeMeta.setDisplayName(BedwarsRel._l(Bukkit.getConsoleSender(), "ingame.shop.oldshop"));
		slimeMeta.setLore(new ArrayList<>());
		slime.setItemMeta(slimeMeta);
		if (item.isSimilar(slime)) {
			return true;
		}
		ItemStack snow = new ItemStack(Material.SNOW_BALL, 1);
		ItemMeta snowMeta = snow.getItemMeta();
		snowMeta.setDisplayName(BedwarsRel._l(Bukkit.getConsoleSender(), "ingame.shop.newshop"));
		snowMeta.setLore(new ArrayList<>());
		snow.setItemMeta(snowMeta);
		if (item.isSimilar(snow)) {
			return true;
		}
		ItemStack bucket = new ItemStack(Material.BUCKET, 1);
		final ItemMeta bucketMeta = bucket.getItemMeta();
		bucketMeta.setDisplayName(ChatColor.AQUA + BedwarsRel._l(Bukkit.getConsoleSender(), "default.currently") + ": " + ChatColor.WHITE + BedwarsRel._l(Bukkit.getConsoleSender(), "ingame.shop.onestackpershift"));
		bucketMeta.setLore(new ArrayList<>());
		bucket.setItemMeta(bucketMeta);
		if (item.isSimilar(bucket)) {
			return true;
		}
		ItemStack lavaBucket = new ItemStack(Material.LAVA_BUCKET, 1);
		final ItemMeta lavaBucketMeta = lavaBucket.getItemMeta();
		lavaBucketMeta.setDisplayName(ChatColor.AQUA + BedwarsRel._l(Bukkit.getConsoleSender(), "default.currently") + ": " + ChatColor.WHITE + BedwarsRel._l(Bukkit.getConsoleSender(), "ingame.shop.fullstackpershift"));
		lavaBucketMeta.setLore(new ArrayList<>());
		lavaBucket.setItemMeta(lavaBucketMeta);
        return item.isSimilar(lavaBucket);
    }

	private static List<ItemStack> getResource() {
		List<ItemStack> items = new ArrayList<>();
		ConfigurationSection config = BedwarsRel.getInstance().getConfig().getConfigurationSection("resource");
		for (String res : config.getKeys(false)) {
			List<Map<String, Object>> list = (List<Map<String, Object>>) BedwarsRel.getInstance().getConfig().getList("resource." + res + ".item");
			for (Map<String, Object> resource : list) {
				ItemStack itemStack = ItemStack.deserialize(resource);
				items.add(itemStack);
			}
		}
		return items;
	}
}
