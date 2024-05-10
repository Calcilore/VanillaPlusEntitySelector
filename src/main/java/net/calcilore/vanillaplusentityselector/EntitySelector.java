package net.calcilore.vanillaplusentityselector;

import net.calcilore.vanillaplusentityselector.Internal.Util;
import net.calcilore.vanillaplusentityselector.Parameters.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;

public class EntitySelector {
    private static final HashMap<String, Parameter> parameters = new HashMap<>();
    private static final List<String> highPriorityParameters = Arrays.asList("x", "y", "z", "dx", "dy", "dz");

    static {
        registerParam("x", new PositionParameter(0));
        registerParam("y", new PositionParameter(1));
        registerParam("z", new PositionParameter(2));
        registerParam("dx", new VolumeParameter(0));
        registerParam("dy", new VolumeParameter(1));
        registerParam("dz", new VolumeParameter(2));
        registerParam("distance", new DistanceParameter());
        registerParam("gamemode", new GamemodeParameter());
        registerParam("level", new LevelParameter());
        registerParam("limit", new LimitParameter());
        registerParam("name", new NameParameter());
        registerParam("sort", new SortParameter());
        registerParam("type", new TypeParameter());
        registerParam("tag", new TagParameter());
        registerParam("team", new TeamParameter());
        registerParam("x_rotation", new RotationParameter(true));
        registerParam("y_rotation", new RotationParameter(false));
    }

    private static void registerParam(String name, Parameter param) {
        parameters.put(name, param);
    }

    private static void isValidSelector(String entityName) throws EntitySelectException {
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
    @SuppressWarnings("unused")
    public static String[] formatArgs(String[] args, int playerIndex) {
        // if index isn't in array, if it's the last argument, it doesn't need to do anything
        // and if it doesn't contain any parameters, the array won't be changed, so don't bother
        if (playerIndex+1 >= args.length || !args[playerIndex].contains("[")) {
            return args;
        }

        // all argument up to the selector argument are the same
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

    @SuppressWarnings("unused")
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

        Location location = Util.getLocation(sender);

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
        ParamSettings settings = new ParamSettings(location, limit);

        for (String[] strings : params) {
            String param = strings[0];
            String value = strings[1];

            Parameter parameter = parameters.get(param);
            if (parameter != null) {
                parameter.executeParameter(foundEntities, value, settings);
            } else {
                throw new EntitySelectException("Unknown parameter: '" + param + "'");
            }
        }

        // do the volume check
        if (settings.doVolumeCheck) {
            BoundingBox area = new BoundingBox(settings.origin.getX(), settings.origin.getY(), settings.origin.getZ(),
                    settings.volumeCheck.getX(), settings.volumeCheck.getY(), settings.volumeCheck.getZ());

            foundEntities.removeIf(entity -> !entity.getBoundingBox().overlaps(area));
        }

        // apply the limit
        if (settings.limit != -1 && foundEntities.size() > settings.limit) {
            foundEntities = foundEntities.subList(0, settings.limit);
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
                kv[0] = kv[0].trim();
                kv[1] = kv[1].trim();

                //                     allow trailing comma
                if (kv.length != 2 || (kv[1].isEmpty() && i + 1 == unsplitParams.length)) {
                    throw new EntitySelectException("Incorrectly formatted parameter");
                }

                if (usedParameters.contains(kv[0])) {
                    throw new EntitySelectException("Duplicate parameter: " + kv[0]);
                }

                usedParameters.add(kv[0]);

                // high priority params go to the start
                if (highPriorityParameters.contains(kv[0])) {
                    params.add(0, kv);
                } else {
                    params.add(kv);
                }
            }
        }

        return params;
    }

    private static NoParamResult selectEntityNoParams(CommandSender sender, String entityName, Location location) throws EntitySelectException {
        List<Entity> foundEntities = new ArrayList<>();

        if (entityName.isEmpty()) {
            throw new EntitySelectException("Entity name must not be empty");
        }

        if (entityName.charAt(0) == '@') {
            if (entityName.length() == 1) {
                throw new EntitySelectException("Selector type must be specified (@a, @s, @p, @e, @r)");
            }

            switch (entityName.charAt(1)) {
                case 's':
                    if (!(sender instanceof Entity)) {
                        return new NoParamResult(foundEntities);
                    }

                    foundEntities.add((Entity) sender);
                    return new NoParamResult(foundEntities);

                case 'p': {
                    if (location == null) {
                        throw new EntitySelectException(EntitySelectException.consoleLocationException);
                    }

                    Util.sortByLocation(foundEntities, location, true);

                    return new NoParamResult(foundEntities, 1);
                }

                case 'a': {
                    if (location == null) {
                        return new NoParamResult(foundEntities);
                    }

                    assert location.getWorld() != null;
                    foundEntities.addAll(location.getWorld().getPlayers());

                    return new NoParamResult(foundEntities);
                }

                case 'e': {
                    if (location == null) {
                        return new NoParamResult(foundEntities);
                    }

                    assert location.getWorld() != null;
                    foundEntities.addAll(location.getWorld().getEntities());

                    return new NoParamResult(foundEntities);
                }

                case 'r': {
                    if (location == null) {
                        return new NoParamResult(foundEntities);
                    }

                    assert location.getWorld() != null;
                    foundEntities.addAll(location.getWorld().getPlayers());
                    Collections.shuffle(foundEntities, Util.random);

                    return new NoParamResult(foundEntities, 1);
                }
            }

            throw new EntitySelectException("Unknown selector type '@" + entityName.charAt(1) + "'");
        }

        Entity e = Bukkit.getPlayer(entityName);
        if (e != null) {
            foundEntities.add(e);
        } else {
            e = Bukkit.getEntity(UUID.fromString(entityName));
            if (e != null) {
                foundEntities.add(e);
            }
        }

        return new NoParamResult(foundEntities);
    }

    @SuppressWarnings("unused")
    public static void tabCompleteSelection(List<String> tab, String arg, CommandSender sender) {
        doTabComplete(tab, arg, sender);

        final String argLower = arg.toLowerCase();
        tab.removeIf(ag -> !ag.toLowerCase().startsWith(argLower));
    }

    private static void doTabComplete(List<String> tab, String arg, CommandSender sender) {
        tab.add("@a");
        tab.add("@p");
        tab.add("@s");
        tab.add("@e");
        tab.add("@r");

        for (Player p : Bukkit.getOnlinePlayers()) {
            tab.add(p.getName());
        }

        if (sender instanceof Player) {
            Player p = (Player) sender;

            List<Entity> entities = p.getNearbyEntities(5, 5, 5);
            Entity entity = getTarget(p, entities);

            if (entity != null && !(entity instanceof Player)) {
                String eu = entity.getUniqueId().toString();
                tab.add(eu);
            }
        }

        if (!arg.contains("[")) {
            return;
        }

        // this is where paramStart is:
        // @e[type=player,lim     |  @e[ty
        //                ^ Here  |     ^ Here
        int paramStart = arg.lastIndexOf(',')+1;
        if (paramStart == 0) {
            paramStart = arg.indexOf('[')+1;
        }

        String postParamStart = arg.substring(paramStart);
        String preParamStart = arg.substring(0, paramStart);

        // if the user hasn't specified the param
        if (!postParamStart.contains("=")) {
            // add the parameter names
            for (String paramName : parameters.keySet()) {
                tab.add(preParamStart + paramName + "=");
            }
        } else {
            // add the parameter values
            String paramName = postParamStart.split("=", 2)[0]; // why is split so dumb in java?

            Parameter param = parameters.get(paramName);
            if (param == null) { // param doesn't exist so idk dont do anything?
                return;
            }

            final int indexOfEquals = arg.lastIndexOf('=')+1;
            final String preValue = arg.substring(0, indexOfEquals);
            final String postValue = arg.substring(indexOfEquals);

            List<String> completions = param.tabCompleteParameter(postValue);
            for (String completion : completions) {
                tab.add(preValue + completion);
            }
        }
    }

    public static Entity getTarget(final Entity entity, final Iterable<Entity> entities) {
        Entity target = null;
        final double threshold = 1;

        Location enLocation = entity.getLocation();
        Vector enDirNormalised = enLocation.getDirection().normalize();

        for (final Entity other : entities) {
            final Vector direction = other.getLocation().toVector().subtract(enLocation.toVector());

            if (enDirNormalised.crossProduct(direction).lengthSquared() < threshold &&
                    direction.normalize().dot(enDirNormalised) >= 0 && (
                        target == null || // don't compare to previous target if there is no previous target
                        target.getLocation().distanceSquared(enLocation) >
                        other.getLocation().distanceSquared(enLocation))) {

                target = other;
            }
        }
        return target;
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
