package net.calcilore.vanillaplusentityselector.Parameters;

import net.calcilore.vanillaplusentityselector.EntitySelectException;
import org.bukkit.entity.Entity;

import java.util.Arrays;
import java.util.List;

public class SortParameter extends Parameter {
    @Override
    public void executeParameter(List<Entity> foundEntities, String value, ParamSettings settings) throws EntitySelectException {
        value = value.toLowerCase();
        switch (value) {
            case "arbitrary":
            case "nearest":
            case "furthest":
            case "random":
                settings.sortType = value;
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
