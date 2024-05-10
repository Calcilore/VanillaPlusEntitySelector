package net.calcilore.vanillaplusentityselector.Parameters;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class ParamSettings {
    private boolean currentWorldExclusive = false;
    private boolean playerExclusive = false;

    public Location origin;
    public int limit;

    // volumeCheck if for the dx, dy and dz params
    // in vanilla, this value defaults to 0, so if you only specify dx as 5, the volume to check
    // will have a volume of 5, 0, 0. Since this is a check if the hitbox overlaps, It's not too bad though.
    public Vector volumeCheck = new Vector(0, 0, 0);
    public boolean doVolumeCheck = false;

    public ParamSettings(Location location, int limit) {
        if (location != null) {
            origin = location.clone();
        } else {
            origin = new Location(Bukkit.getWorlds().get(0), Double.NaN, Double.NaN, Double.NaN);
        }

        this.limit = limit;
    }

    public boolean isOriginInvalid() {
        return Double.isNaN(origin.getX()) || Double.isNaN(origin.getY()) || Double.isNaN(origin.getZ());
    }

    public void restrictWorld(List<Entity> foundEntities) {
        if (currentWorldExclusive || origin.getWorld() == null) {
            return;
        }

        currentWorldExclusive = true;
        foundEntities.removeIf(entity ->
                !entity.getWorld().getUID().equals(origin.getWorld().getUID()));
    }

    public void restrictToPlayers(List<Entity> foundEntities) {
        if (playerExclusive) {
            return;
        }

        playerExclusive = true;
        foundEntities.removeIf(entity -> !(entity instanceof Player));
    }
}
