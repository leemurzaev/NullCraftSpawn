package dev.nullcraft.spawn;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/** Random safe spawn around the world spawn, for new players and after death. */
public final class NullCraftSpawn extends JavaPlugin {

    private SpawnSettings settings;
    private SpotPool pool;
    private Messenger messenger;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messenger = new Messenger(this);
        reload();
        getServer().getPluginManager().registerEvents(new SpawnListener(this), this);
        PluginCommand command = getCommand("ncspawn");
        if (command != null) {
            SpawnCommand executor = new SpawnCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    @Override
    public void onDisable() {
        if (pool != null) {
            pool.close();
            pool = null;
        }
    }

    /** Reads config.yml again and rebuilds the spot pool. Returns false when the world is missing. */
    boolean reload() {
        reloadConfig();
        SpawnSettings next = SpawnSettings.from(getConfig());
        if (pool != null) {
            pool.close();
            pool = null;
        }
        settings = next;
        World world = getServer().getWorld(next.world());
        if (world == null) {
            getLogger().severe("World '" + next.world() + "' not found: random spawn is off");
            return false;
        }
        Location spawn = world.getSpawnLocation();
        int centerX = next.useWorldSpawn() ? spawn.getBlockX() : next.centerX();
        int centerZ = next.useWorldSpawn() ? spawn.getBlockZ() : next.centerZ();
        SpawnArea area = new SpawnArea(centerX, centerZ, next.radius());
        SafeSpotFinder finder = new SafeSpotFinder(world, area, new SafetyRule(next.unsafeBlocks()), next.maxAttempts());
        pool = new SpotPool(this, finder, next.poolSize());
        pool.refill();
        getLogger().info("Random spawn in " + world.getName() + ": X " + (centerX - next.radius()) + " to "
                + (centerX + next.radius()) + ", Z " + (centerZ - next.radius()) + " to " + (centerZ + next.radius()));
        return true;
    }

    SpawnSettings settings() {
        return settings;
    }

    SpotPool pool() {
        return pool;
    }

    Messenger messenger() {
        return messenger;
    }
}
