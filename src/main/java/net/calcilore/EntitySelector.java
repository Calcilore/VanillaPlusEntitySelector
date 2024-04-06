package net.calcilore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;

// for later:
// String[] result = input.split("\\s+(?![^\\[]*\\])");

public class EntitySelector {
    private static final Random random = new Random();
    private static final String consoleLocationException = "You cannot use @p from command console!";

    public static void isValidSelector(String entityName) throws EntitySelectException {
        entityName = entityName.trim();

        if (entityName.isEmpty()) {
            throw new EntitySelectException("Cannot select nothing!");
        }

        // is player name
        if (entityName.charAt(0) != '@') {
            return;
        }

        // "@"
        if (entityName.length() == 1) {
            throw new EntitySelectException("Invalid selector!");
        }

        //  "@[s|p|a|e|r]"
        switch (entityName.charAt(1)) {
            case 's':
            case 'p':
            case 'a':
            case 'e':
            case 'r':
                break;

            default:
                throw new EntitySelectException("Invalid selector '" + entityName.charAt(1) + "'!");
        }

        if (entityName.length() == 2) {
            return;
        }

        // @.[*]
        if (entityName.charAt(2) != '[' || entityName.charAt(entityName.length() - 1) != ']') {
            throw new EntitySelectException("Parameters must be surrounded by []!");
        }
    }

    // player selector can have spaces in it, this makes things a lot harder because command arguments are split by spaces
    public static String[] formatArgs(String[] args, int playerIndex) {
        // if index isn't in array, if it's the last argument, it doesn't need to do anything
        // and if it doesn't contain any parameters, the array won't be changed, so don't bother
        if (playerIndex+1 >= args.length || !args[playerIndex].contains("[")) {
            return args;
        }

        // all argument up to the argument are the same
        List<String> newArgs = new ArrayList<>(Arrays.asList(args).subList(0, playerIndex));

        String playerArg = "";
        List<Character> levels = new ArrayList<>();
        boolean end = false;

        for (int i = playerIndex; i < args.length; i++) {
            if (end) {
                newArgs.add(args[i]);
                continue;
            }

            String arg = args[i] + ' ';

            for (int j = 0; j < arg.length() && !end; j++) {
                char ch = arg.charAt(j);
                playerArg += ch;

                switch (ch) {
                    case ']':
                        Bukkit.getLogger().info("], " + levels.size() + " : " + levels.isEmpty());
                        if (levels.isEmpty()) {
                            end = true;
                            newArgs.add(playerArg);
                            Bukkit.getLogger().info('"' + playerArg + '"');
                        }
                        break;

                    case '\'':
                    case '"':
                        Bukkit.getLogger().info("\", " + levels.size());
                        if (!levels.isEmpty() && levels.get(levels.size() - 1) == ch) {
                            levels.remove(levels.size() - 1);
                            break;
                        }

                        levels.add(ch);
                        break;
                }
            }
        }

        return newArgs.toArray(new String[0]);
    }

    public static List<Entity> selectEntitiesPrint(CommandSender sender, String entityName) {
        try {
            return selectEntities(sender, entityName);
        } catch (EntitySelectException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }

        return null;
    }

    public static List<Entity> selectEntities(CommandSender sender, String entityName) throws EntitySelectException {
        entityName = entityName.trim();
        isValidSelector(entityName);

        Location location = getLocation(sender);

        List<Entity> foundEntities;
        int limit;
        {
            NoParamResult result = selectEntityNoParams(sender, entityName, location);
            foundEntities = result.entities;
            limit = result.limit;
        }

        // if it didn't find anything, or if there are no parameters
        if (foundEntities.isEmpty() || entityName.charAt(0) != '@' || entityName.length() == 2) {
            return foundEntities;
        }

        // find the parameters and extract the keys and values
        List<String[]> params = findParams(entityName);

        // try to catch all NumberFormatExceptions, bc im lazy
        try {
            // if else is used to make origin final for the removeIf() after the high priority parameters
            final Location origin;
            if (location != null) {
                origin = location.clone();
            } else {
                origin = null;
            }

            boolean currentWorldExclusive = false;

            // volumeCheck if for the dx, dy and dz params
            // in vanilla, this value defaults to 0, so if you only specify dx as 5, the volume to check
            // will have a volume of 5, 0, 0. Since this is a check if the hitbox overlaps, It's not too bad though.
            Vector volumeCheck = new Vector(0, 0, 0);
            boolean doVolumeCheck = false;

            // high priority parameters
            for (String[] strings : params) {
                String param = strings[0];
                String value = strings[1];

                switch (param) {
                    case "x":
                        if (origin == null) {
                            throw new EntitySelectException(consoleLocationException);
                        }

                        origin.setX(Double.parseDouble(value));
                        currentWorldExclusive = true;
                        break;

                    case "y":
                        if (origin == null) {
                            throw new EntitySelectException(consoleLocationException);
                        }

                        origin.setY(Double.parseDouble(value));
                        currentWorldExclusive = true;
                        break;

                    case "z":
                        if (origin == null) {
                            throw new EntitySelectException(consoleLocationException);
                        }

                        origin.setZ(Double.parseDouble(value));
                        currentWorldExclusive = true;
                        break;

                    // dx, dy, and dz don't need to be in high priority, but the code looks cleaner this way.
                    case "dx": {
                        if (origin == null) {
                            throw new EntitySelectException(consoleLocationException);
                        }

                        volumeCheck.setX(Double.parseDouble(value));
                        doVolumeCheck = true;
                        break;
                    }

                    case "dy": {
                        if (origin == null) {
                            throw new EntitySelectException(consoleLocationException);
                        }

                        volumeCheck.setY(Double.parseDouble(value));
                        doVolumeCheck = true;
                        break;
                    }

                    case "dz": {
                        if (origin == null) {
                            throw new EntitySelectException(consoleLocationException);
                        }

                        volumeCheck.setZ(Double.parseDouble(value));
                        doVolumeCheck = true;
                        break;
                    }
                }
            }

            if (currentWorldExclusive) {
                foundEntities.removeIf(entity ->
                        !entity.getWorld().getUID().equals(origin.getWorld().getUID()));
            }

            if (doVolumeCheck) {
                BoundingBox area = new BoundingBox(origin.getX(), origin.getY(), origin.getZ(),
                        volumeCheck.getX(), volumeCheck.getY(), volumeCheck.getZ());

                foundEntities.removeIf(entity -> !entity.getBoundingBox().overlaps(area));
            }

            // normal priority parameters
            for (String[] strings : params) {
                String param = strings[0];
                String value = strings[1];

                switch (param) {
                    case "distance": {
                        if (origin == null) {
                            throw new EntitySelectException(consoleLocationException);
                        }

                        DoubleRange range = DoubleRange.parseRange(value);

                        foundEntities.removeIf(entity ->
                                !range.within(entity.getLocation().distance(origin)));
                        break;
                    }

                    case "gamemode": {
                        boolean invert = value.charAt(0) == '!';
                        final String gamemode = (invert ? value.substring(1) : value).toUpperCase();

                        switch (gamemode) {
                            case "CREATIVE":
                            case "SURVIVAL":
                            case "ADVENTURE":
                            case "SPECTATOR":
                                break;

                            default:
                                throw new EntitySelectException("Invalid Gamemode!");
                        }

                        foundEntities.removeIf(entity -> {
                            if (entity instanceof Player) {
                                return ((Player) entity).getGameMode().name().equals(gamemode) == invert;
                            }

                            return true;
                        });
                        break;
                    }

                    case "level": {
                        IntRange range = IntRange.parseRange(value);
                        Bukkit.getLogger().info("Range is: " + range.min + ", " + range.max);

                        foundEntities.removeIf(entity -> {
                            if (entity instanceof Player) {
                                return !range.within(((Player) entity).getLevel());
                            }

                            return true;
                        });
                        break;
                    }

                    case "limit": {
                        int newLimit = Integer.parseInt(value);
                        if (newLimit < 1) {
                            throw new EntitySelectException("Limit must be 1 or more!");
                        }

                        limit = newLimit;
                        break;
                    }

                    case "name": {
                        value = parseString(value);

                        boolean invert = value.charAt(0) == '!';
                        if (invert) {
                            value = value.substring(1);
                        }

                        final String finalValue = value;

                        foundEntities.removeIf(entity ->
                                entity.getName().equals(finalValue) == invert);
                        break;
                    }

                    case "sort": {
                        switch (value) {
                            case "arbitrary":
                                break;

                            case "nearest":
                            case "furthest": {
                                boolean nearest = value.equals("nearest");

                                sortByLocation(foundEntities, origin, nearest);
                                break;
                            }

                            case "random":
                                Collections.shuffle(foundEntities, random);
                                break;

                            default:
                                throw new EntitySelectException("Invalid sort type!");
                        }
                        break;
                    }

                    case "tag": {
                        boolean invert = value.charAt(0) == '!';
                        if (invert) {
                            value = value.substring(1);
                        }

                        final String finalValue = value;
                        foundEntities.removeIf(entity ->
                                entity.getScoreboardTags().contains(finalValue) == invert);
                        break;
                    }

                    case "team": {
                        boolean invert = value.charAt(0) == '!';
                        if (invert) {
                            value = value.substring(1);
                        }

                        // get Team from value
                        ScoreboardManager scb = Bukkit.getScoreboardManager();
                        if (scb == null) {
                            break;
                        }

                        Team team = scb.getMainScoreboard().getTeam(value);
                        if (team == null) {
                            if (!invert) {
                                foundEntities.clear();
                            }

                            break;
                        }

                        foundEntities.removeIf(entity -> {
                            if (entity instanceof Player) {
                                return (team.hasEntry(entity.getName()) ||
                                        team.hasEntry(entity.getUniqueId().toString())) == invert;
                            }

                            return team.hasEntry(entity.getUniqueId().toString()) == invert;
                        });
                        break;
                    }

                    case "type": {
                        boolean invert = value.charAt(0) == '!';
                        if (invert) {
                            value = value.substring(1);
                        }

                        final String finalValue = value;

                        foundEntities.removeIf(entity ->
                                entity.getType().getKey().getKey().equalsIgnoreCase(finalValue) == invert);
                        break;
                    }

                    // THIS ISN'T THE VANILLA ONE, IT'S JUST A SIMPLER VERSION.
                    case "advancement": {
                        value = parseString(value);

                        boolean invert = value.charAt(0) == '!';
                        if (invert) {
                            value = value.substring(1);
                        }

                        Advancement advancement = Bukkit.getServer().getAdvancement(NamespacedKey.fromString(value));

                        foundEntities.removeIf(entity -> {
                            if (entity instanceof Player) {
                                return !((Player)entity).getAdvancementProgress(advancement).isDone();
                            }

                            return true;
                        });
                    }

                    case "x_rotation": {
                        DoubleRange range = DoubleRange.parseRange(value);
                        foundEntities.removeIf(entity -> !range.within(entity.getLocation().getPitch()));
                    }

                    case "y_rotation": {
                        DoubleRange range = DoubleRange.parseRange(value);
                        foundEntities.removeIf(entity -> !range.within(entity.getLocation().getYaw()));
                    }

                    // these are defined in high-priority
                    case "x":
                    case "y":
                    case "z":
                    case "dx":
                    case "dy":
                    case "dz":
                        break;

                    default:
                        throw new EntitySelectException("Unknown parameter: '" + param + "'");
                }
            }
        }
        catch (NumberFormatException e) {
            throw new EntitySelectException("Failed to parse number!");
        }

        if (limit != -1 && foundEntities.size() > limit) {
            foundEntities = foundEntities.subList(0, limit);
        }

        return foundEntities;
    }

    private static List<String[]> findParams(String entityName) throws EntitySelectException {
        List<String[]> params = new ArrayList<>();

        {
            String[] unsplitParams = entityName.substring(3, entityName.length() - 1).split(",");
            List<String> usedParameters = new ArrayList<>();

            for (int i = 0; i < unsplitParams.length; i++) {
                String[] kv = unsplitParams[i].trim().split("=");

                //                     allow trailing comma
                if (kv.length != 2 || (kv[1].isEmpty() && i + 1 == unsplitParams.length)) {
                    throw new EntitySelectException("Incorrectly formatted parameter");
                }

                if (usedParameters.contains(kv[0])) {
                    throw new EntitySelectException("Duplicate parameter: " + kv[0]);
                }

                usedParameters.add(kv[0]);
                params.add(kv);
            }
        }

        return params;
    }

    private static NoParamResult selectEntityNoParams(CommandSender sender, String entityName, Location location) throws EntitySelectException {
        List<Entity> foundEntities = new ArrayList<>();

        if (entityName.charAt(0) == '@') {
            switch (entityName.charAt(1)) {
                case 's':
                    if (!(sender instanceof Entity)) {
                        return new NoParamResult(foundEntities);
                    }

                    foundEntities.add((Entity) sender);
                    return new NoParamResult(foundEntities);

                case 'p': {
                    if (location == null) {
                        throw new EntitySelectException(consoleLocationException);
                    }

                    sortByLocation(foundEntities, location, true);

                    return new NoParamResult(foundEntities, 1);
                }

                case 'a': {
                    if (location == null) {
                        return new NoParamResult(foundEntities);
                    }

                    foundEntities.addAll(location.getWorld().getPlayers());

                    return new NoParamResult(foundEntities);
                }

                case 'e': {
                    if (location == null) {
                        return new NoParamResult(foundEntities);
                    }

                    foundEntities.addAll(location.getWorld().getEntities());

                    return new NoParamResult(foundEntities);
                }

                case 'r': {
                    if (location == null) {
                        return new NoParamResult(foundEntities);
                    }

                    foundEntities.addAll(location.getWorld().getPlayers());
                    Collections.shuffle(foundEntities, random);

                    return new NoParamResult(foundEntities, 1);
                }
            }

            throw new EntitySelectException("Unknown selector type '@" + entityName.charAt(1) + "'");
        }

        Player p = Bukkit.getPlayer(entityName);
        if (p != null) {
            foundEntities.add(p);
        }

        return new NoParamResult(foundEntities);
    }

    private static Location getLocation(CommandSender sender) {
        if (sender instanceof Entity) {
            return ((Entity) sender).getLocation();
        } else if (sender instanceof BlockCommandSender) {
            return ((BlockCommandSender) sender).getBlock().getLocation();
        } else {
            return null;
        }
    }

    private static void sortByLocation(List<Entity> entities, Location location, boolean nearest) {
        entities.sort((a, b) -> {
            double distanceA = a.getLocation().distance(location);
            double distanceB = b.getLocation().distance(location);

            if (nearest) {
                return Double.compare(distanceA, distanceB);
            }

            return Double.compare(distanceB, distanceA);
        });
    }

    private static String parseString(String value) {
        if ((value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') ||
                (value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'')) {
            return value.substring(1, value.length() - 1);
        }

        return value;
    }

    private static class NoParamResult {
        public List<Entity> entities;
        public int limit;

        public NoParamResult(List<Entity> entities, int limit) {
            this.entities = entities;
            this.limit = limit;
        }

        public NoParamResult(List<Entity> entities) {
            this(entities, -1);
        }
    }
}
