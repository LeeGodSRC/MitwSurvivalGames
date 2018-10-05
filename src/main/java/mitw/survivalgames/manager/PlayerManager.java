package mitw.survivalgames.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mitw.survivalgames.GameStatus;
import mitw.survivalgames.Lang;
import mitw.survivalgames.SurvivalGames;
import mitw.survivalgames.ratings.PlayerCache;
import mitw.survivalgames.ratings.RatingManager;

public class PlayerManager {

	public static PlayerManager ins;
	public static int max = 0;
	public static Location spawnLocation;
	public static List<UUID> players = new ArrayList<>();
	public static List<UUID> oringalPlayers = new ArrayList<>();
	public static List<UUID> builders = new ArrayList<>();
	public static Map<UUID, String> uuidDB = new HashMap<>();
	public static Map<Location, Player> spawns = new HashMap<>();
	public static Map<UUID, Integer> kills = new HashMap<>();

	private static Map<UUID, PlayerCache> playerCaches = new HashMap<>();

	public List<UUID> inCooldown = new ArrayList<>();

	public PlayerManager() {
		ins = this;
	}

	public PlayerCache createCache(final Player player) {
		return playerCaches.put(player.getUniqueId(), RatingManager.getInstance().getDatabase().createCache(player));
	}

	public boolean hasCache(UUID uuid) {
		return playerCaches.containsKey(uuid);
	}

	public void saveCache(final UUID uuid) {

		if (!playerCaches.containsKey(uuid)) {
			return;
		}

		if (!oringalPlayers.contains(uuid)) {
			return;
		}

		final PlayerCache playerCache = playerCaches.get(uuid);

		if (GameStatus.isGaming(true)) {
			if (kills.containsKey(uuid)) {
				playerCache.setKills(playerCache.getKills() + kills.get(uuid));
			}
			if (!players.contains(uuid)) {
				playerCache.setDeaths(playerCache.getDeaths() + 1);
			}
		} else if (GameStatus.isFinished()) {
			if (kills.containsKey(uuid)) {
				playerCache.setKills(playerCache.getKills() + kills.get(uuid));
			}
			if (players.get(0).equals(uuid)) {
				playerCache.setWins(playerCache.getWins() + 1);
			} else {
				playerCache.setDeaths(playerCache.getDeaths() + 1);
			}
		}

		RatingManager.getInstance().getDatabase().savePlayerCache(playerCache);

	}

	public void removeCache(UUID uuid) {
		playerCaches.remove(uuid);
	}

	public void saveAllCache() {
		new ArrayList<>(playerCaches.keySet()).forEach(this::saveCache);
	}

	public PlayerCache getCache(final UUID uuid) {
		return playerCaches.containsKey(uuid) ? playerCaches.get(uuid) : null;
	}

	public void setSpec(final Player p) {
		hidePlayer(p);
		p.setHealth(20.0);
		p.setGameMode(GameMode.SURVIVAL);
		clearInventory(p);
		p.setAllowFlight(true);
		p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999, 0));
		giveSpecItem(p);
	}

	public void tpToSpawn(final Player p) {
		if (spawnLocation != null) {
			p.teleport(spawnLocation);
		}
	}

	public void clearInventory(final Player p) {
		final PlayerInventory i = p.getInventory();
		p.setHealth(20.0);
		p.setFoodLevel(20);
		i.clear();
		i.setArmorContents(null);
		for (final PotionEffect potion : p.getActivePotionEffects()) {
			p.removePotionEffect(potion.getType());
		}

	}

	public void hidePlayer(final Player p) {
		for (final Player p2 : Bukkit.getOnlinePlayers()) {
			p2.hidePlayer(p);
		}
	}

	public void giveWaitingItem(final Player p) {
		final PlayerInventory i = p.getInventory();
		i.setItem(0, Lang.iVoteMap);
		i.setItem(1, Lang.arrowTrails);
		i.setItem(2, Lang.statsItem);
		i.setItem(8, Lang.returnToLobby);
	}

	private void giveSpecItem(final Player p) {
		Bukkit.getScheduler().runTaskLater(SurvivalGames.getInstance(), new Runnable() {
			PlayerInventory inv = p.getInventory();

			@Override
			public void run() {
				inv.setItem(0, Lang.specTp);
				inv.setItem(7, Lang.playAnotherGame);
				inv.setItem(8, Lang.returnToLobby);
			}
		}, 2L);
	}

	public void randomTeleportPlayer(final Player p) {
		final UUID u = p.getUniqueId();
		if (inCooldown.contains(u))
			return;
		p.teleport(Bukkit.getPlayer(players.get(SurvivalGames.getRandom().nextInt(players.size()))));
		inCooldown.add(u);
		Bukkit.getScheduler().runTaskLater(SurvivalGames.getInstance(), () -> inCooldown.remove(u), 20);
	}

	public String getNameByUUID(final UUID u) {
		return uuidDB.get(u);
	}

	public void putUuidDb(final Player p) {
		uuidDB.put(p.getUniqueId(), p.getName());
	}

	public boolean isBuilder(final Player p) {
		if (builders.contains(p.getUniqueId()))
			return true;
		return false;
	}

	public boolean isGameingPlayer(final Player p) {
		if (players.contains(p.getUniqueId()))
			return true;
		return false;
	}

	public void giveSetChestItem(final Player p, final String name) {
		final ItemStack i = new ItemStack(Material.BLAZE_ROD);
		final ItemMeta m = i.getItemMeta();
		m.setDisplayName(name);
		i.setItemMeta(m);
		p.getInventory().addItem(i);

	}

	public void addKills(final Player p) {
		final UUID u = p.getUniqueId();
		kills.put(u, kills.get(u) + 1);
	}

	public int getKills(final Player p) {
		return getKills(p.getUniqueId());
	}

	public int getKills(final UUID uuid) {
		if (kills.containsKey(uuid))
			return kills.get(uuid);
		return 0;
	}

}
