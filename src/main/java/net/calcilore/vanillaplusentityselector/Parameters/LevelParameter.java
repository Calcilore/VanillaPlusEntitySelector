package net.calcilore.vanillaplusentityselector.Parameters;

import net.calcilore.vanillaplusentityselector.EntitySelectException;
import net.calcilore.vanillaplusentityselector.Internal.IntRange;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class LevelParameter extends Parameter {
    @Override
    public void executeParameter(List<Entity> foundEntities, String value, ParamSettings settings) throws EntitySelectException {
        IntRange range = IntRange.parseRange(value);

        settings.restrictToPlayers(foundEntities);
        foundEntities.removeIf(entity -> !range.within(((Player) entity).getLevel()));
    }
}
