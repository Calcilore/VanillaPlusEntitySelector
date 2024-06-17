package net.calcilore.vanillaplusentityselector.Parameters;

import net.calcilore.vanillaplusentityselector.EntitySelectException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class TypeParameter extends Parameter {
    private final List<String> tabComplete;
    private final List<String> tabCompleteInvert;

    public TypeParameter() {
        super();

        tabComplete = new ArrayList<>(EntityType.values().length-1);
        tabCompleteInvert = new ArrayList<>(EntityType.values().length-1);
        for (EntityType type : EntityType.values()) {
            if (type == EntityType.UNKNOWN) {
                continue;
            }

            String key = type.getKey().getKey();
            tabComplete.add(key);
            tabCompleteInvert.add("!" + key);
        }
    }

    @Override
    public void executeParameter(List<Entity> foundEntities, String value, ParamSettings settings) throws EntitySelectException {
        // if restricting to players, mark it as players only (this is before invert check so !players does normal behaviour)
        if (value.equalsIgnoreCase("player")) {
            settings.restrictToPlayers(foundEntities);
            return;
        }

        boolean invert = value.charAt(0) == '!';
        if (invert) {
            value = value.substring(1);
        }

        final String finalValue = value;
        foundEntities.removeIf(entity -> entity.getType().getKey().getKey().equalsIgnoreCase(finalValue) == invert);
    }

    @Override
    public List<String> tabCompleteParameter(String arg) {
        return arg.startsWith("!") ? tabCompleteInvert : tabComplete;
    }
}
