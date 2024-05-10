package net.calcilore.vanillaplusentityselector.Internal;

import net.calcilore.vanillaplusentityselector.EntitySelectException;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Random;

public class Util {
    public static final Random random = new Random();

    public static int parseInt(String value) throws EntitySelectException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new EntitySelectException("Number must be an integer!");
        }
    }

    public static double parseDouble(String value) throws EntitySelectException {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new EntitySelectException("Number must be a number!");
        }
    }

    public static String parseString(String value) {
        if ((value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') ||
                (value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'')) {
            return value.substring(1, value.length() - 1);
        }

        return value;
    }

    public static Location getLocation(CommandSender sender) {
        if (sender instanceof Entity) {
            return ((Entity) sender).getLocation();
        } else if (sender instanceof BlockCommandSender) {
            return ((BlockCommandSender) sender).getBlock().getLocation();
        } else {
            return null;
        }
    }

    public static void sortByLocation(List<Entity> entities, Location location, boolean nearest) {
        entities.sort((a, b) -> {
            double distanceA = a.getLocation().distance(location);
            double distanceB = b.getLocation().distance(location);

            if (nearest) {
                return Double.compare(distanceA, distanceB);
            }

            return Double.compare(distanceB, distanceA);
        });
    }
}
