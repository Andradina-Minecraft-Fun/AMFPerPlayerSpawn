package br.com.centralandradina.amfperplayerspawn;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin implements Listener
{
	Map<String, Location> playerLocations = new HashMap<>();

	FileConfiguration config = getConfig();

	@Override
	public void onLoad() { }

	@Override
	public void onEnable()
	{
		getServer().getPluginManager().registerEvents(this, this);

		getLogger().info("Reading config");
		this.config.addDefault("maxX", 6000);
		this.config.addDefault("maxZ", 6000);
		this.config.options().copyDefaults(true);
		saveConfig();

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
		Player player = event.getPlayer();
		String uuid = player.getUniqueId().toString();

		if(playerLocations.containsKey(uuid.toString())) {
			Location location = playerLocations.get(uuid);
			event.setRespawnLocation(location);
		}
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
		Location spawnLocation;

		// verify if data player exists
		File configFile = new File(this.getDataFolder(), uuid.toString() + ".yml");
		if(!configFile.exists()) {
			
			// look for a new spawn area
			spawnLocation = newSpawnLocation(player.getWorld());
			getLogger().info("Found location for " + playerName + " at " + spawnLocation.getX() + ", " + spawnLocation.getY() + ", " + spawnLocation.getZ());

			// save the location for next login
			saveConfigFile(uuid, spawnLocation);

			// teleport user to the new spaw
			player.teleport(spawnLocation);
		}
		else {

			// load spawn location
			FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

			// get world
			World  world = getServer().getWorld(config.getString("spawn.world"));
			
			// get location
			spawnLocation = new Location(world, config.getInt("spawn.x")+0.5, config.getInt("spawn.y"), config.getInt("spawn.z")+0.5);

		}

		// save the player location on array
		playerLocations.put(uuid.toString(), spawnLocation);

	}

	/**
	 * save config file
	 * @return
	 */
	public void saveConfigFile(UUID uuid, Location location)
	{
		File configFile = new File(this.getDataFolder(), uuid.toString() + ".yml");

		// save the location for next 
		FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		config.set("spawn.world", location.getWorld().getName());
		config.set("spawn.x", location.getX());
		config.set("spawn.y", location.getY());
		config.set("spawn.z", location.getZ());
		try {
			config.save(configFile);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * find new spawn location
	 * @param world
	 * @return
	 */
	public Location newSpawnLocation(World world)
	{
		getLogger().info("Finding new spawn location...");
		
		// world max size 
		// @todo add this size to a config file
		int maxX = this.config.getInt("maxX");
		int maxZ = this.config.getInt("maxZ");;

		// random location
		Random r = new Random();
		int posX = r.nextInt(maxX*2) - maxX;
		int posZ = r.nextInt(maxZ*2) - maxZ;

		// get highest block of this points, and go down while its air
		int posY = world.getHighestBlockYAt(posX, posZ);
		while(posY >= 0)
		{
			// @todo verify if this place has protection (redprotection and griefprevent)
			if(world.getBlockAt(posX, posY, posZ).isLiquid()) {
				return newSpawnLocation(world);
			}
			else if(world.getBlockAt(posX, posY, posZ).getType() != Material.AIR) {
				posY++; // back to the previous block position
				return new Location(world, posX+0.5, posY, posZ+0.5);
			}

			posY--;
		}

		return new Location(world, 0, 0, 0);
	}

	/**
	 * commands
	 * 
	 * @todo translate
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{

		String commandName = cmd.getName().toLowerCase();

		// verify if the command was executed by a player
		if (!(sender instanceof Player)) {
			sender.sendMessage("Execute this command as a player");
			return false;
		}
		Player player = (Player)sender;

		// /spawn command
		if(commandName.equals("spawn")) {

			// if the command has no arguments, send player to their spawn
			if(args.length == 0) {
    			
				// verify if has permission
				if(!sender.hasPermission("amf.perplayerspawn.spawn")) {
					sender.sendMessage("You cannot to that!");
    				return false;
				}

				
				// teleport player
				String uuid = player.getUniqueId().toString();
				if(playerLocations.containsKey(uuid.toString())) {
					Location location = playerLocations.get(uuid);
					player.teleport(location);
				}
				else {
					sender.sendMessage("I cannot find your spawn point!");
    				return false;
				}
				
    			return false;
    		}
			else {
				// verify if arg[0] is set
				if(args[0].equals("set")) {
					// verify permission
					if(!sender.hasPermission("amf.perplayerspawn.spawn.set")) {
						sender.sendMessage("You cannot to that!");
						return false;
					}
					else {
						setSpawn(player);
					}
				}

				// verify if arg[0] is find
				else if(args[0].equals("new")) {
					// verify permission
					if(!sender.hasPermission("amf.perplayerspawn.spawn.new")) {
						sender.sendMessage("You cannot to that!");
						return false;
					}
					else {
						newSpawn(player);
					}
				}
			}

		}

		return false;
	}

	/**
	 * set new spawn point to the player
	 */
	public boolean newSpawn(Player player)
	{
		player.sendMessage("Finding new spawn location...");
		
		// get player location
		Location newLocation = newSpawnLocation(player.getWorld());
		getLogger().info("Found location for " + player.getName() + " at " + newLocation.getX() + ", " + newLocation.getY() + ", " + newLocation.getZ());
		
		// save config file with the new location
		saveConfigFile(player.getUniqueId(), newLocation);

		// add the new location to map
		String uuid = player.getUniqueId().toString();
		playerLocations.put(uuid, newLocation);

		// teleport player
		player.teleport(newLocation);
		
		// warning player
		player.sendMessage("New spawn defined");

		return true;
	}

	/**
	 * set new spawn point to the player
	 */
	public boolean setSpawn(Player player)
	{
		// get player location
		Location location = player.getLocation();
		
		Block playerblock = location.getBlock(); 
		if(playerblock.isLiquid()) {
			player.sendMessage("You need a safe place to spawn");
			return false;
		}

		Block underblock = playerblock.getRelative(0, -1, 0);
		if(underblock.isLiquid()) {
			player.sendMessage("You need a safe place to spawn");
			return false;
		}

		// rewrite location to the middle of block
		Location newLocation = new Location(location.getWorld(), ((int)location.getX())+0.5, ((int)location.getY()), ((int)location.getZ())+0.5);

		// save config file with the new location
		saveConfigFile(player.getUniqueId(), newLocation);

		// add the new location to map
		String uuid = player.getUniqueId().toString();
		playerLocations.put(uuid, newLocation);

		// warning player
		player.sendMessage("New spawn defined");

		return true;
	}

}