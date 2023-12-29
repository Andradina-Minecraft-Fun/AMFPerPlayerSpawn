package br.com.centralandradina.amfperplayerspawn;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin implements Listener
{
	public Location spawnLocation = null;

	@Override
	public void onLoad() { }

	@Override
	public void onEnable()
	{
		getServer().getPluginManager().registerEvents(this, this);

		getLogger().info("AMFPerPlayerSpawn enabled");
	}

	@Override
	public void onDisable()
	{
		getLogger().info("AMFPerPlayerSpawn disabled");
	}


	@EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		event.setRespawnLocation(this.spawnLocation);
	}

	/**
	 * @param event
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{ 
		Player player = event.getPlayer();
		String playerName = player.getName();
		UUID uuid = player.getUniqueId();

		// verify if data player exists
		File configFile = new File(this.getDataFolder(), uuid.toString() + ".yml");
		if(!configFile.exists()) {
			

			// look for a new spawn area
			this.spawnLocation = newSpawnLocation(player.getWorld());
			getLogger().info("Found location for " + playerName + " at " + this.spawnLocation.getX() + ", " + this.spawnLocation.getY() + ", " + this.spawnLocation.getZ());

			// save the location for next 
			FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
			config.set("spawn.world", player.getWorld().getName());
			config.set("spawn.x", this.spawnLocation.getX());
			config.set("spawn.y", this.spawnLocation.getY());
			config.set("spawn.z", this.spawnLocation.getZ());
			try {
				config.save(configFile);
			}
			catch (IOException e) {
				e.printStackTrace();
			}

			// teleport user to the new spaw
			player.teleport(this.spawnLocation);
		}
		else {

			// load spawn location
			FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

			// get world
			World  world = getServer().getWorld(config.getString("spawn.world"));
			
			// get location
			this.spawnLocation = new Location(world, config.getInt("spawn.x"), config.getInt("spawn.y"), config.getInt("spawn.z"));

		}

	}

	public Location newSpawnLocation(World world)
	{
		getLogger().info("Finding new spawn location...");
		
		// world max size 
		// @todo add this size to a config file
		int maxX = 10000;
		int maxZ = 10000;

		// random location
		Random r = new Random();
		int posX = r.nextInt(maxX*2) - maxX;
		int posZ = r.nextInt(maxZ*2) - maxZ;

		// get highest block of this points, and go down while its air
		int posY = world.getHighestBlockYAt(posX, posZ);
		while(posY >= 0)
		{
			if(world.getBlockAt(posX, posY, posZ).getType() == Material.WATER) {
				return newSpawnLocation(world);
			}
			else if(world.getBlockAt(posX, posY, posZ).getType() == Material.LAVA) {
				return newSpawnLocation(world);
			}
			else if(world.getBlockAt(posX, posY, posZ).getType() != Material.AIR) {
				return new Location(world, posX, posY, posZ);
			}

			posY--;
		}

		return new Location(world, 0, 0, 0);
	}

}