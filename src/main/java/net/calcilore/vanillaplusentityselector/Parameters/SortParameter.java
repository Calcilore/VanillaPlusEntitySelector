package net.calcilore.vanillaplusentityselector.Parameters;

import net.calcilore.vanillaplusentityselector.EntitySelectException;
import net.calcilore.vanillaplusentityselector.Internal.Util;
import org.bukkit.entity.Entity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SortParameter extends Parameter {
    @Override
    public void executeParameter(List<Entity> foundEntities, String value, ParamSettings settings) throws EntitySelectException {
        switch (value) {
            case "arbitrary": // this is the default, so it is already sorted like this
                break;

            case "nearest":
            case "furthest": {
                boolean nearest = value.equals("nearest");

                Util.sortByLocation(foundEntities, settings.origin, nearest);
                break;
            }

            case "random":
                Collections.shuffle(foundEntities, Util.random);
                break;

            default:
                throw new EntitySelectException("Invalid sort type!");
        }
    }

    @Override
    public List<String> tabCompleteParameter(String arg) {
        return Arrays.asList("arbitrary", "nearest", "furthest", "random");
    }
}
