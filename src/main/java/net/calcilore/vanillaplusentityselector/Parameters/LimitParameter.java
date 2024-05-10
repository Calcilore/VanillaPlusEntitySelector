package net.calcilore.vanillaplusentityselector.Parameters;

import net.calcilore.vanillaplusentityselector.EntitySelectException;
import net.calcilore.vanillaplusentityselector.Internal.Util;
import org.bukkit.entity.Entity;

import java.util.List;

public class LimitParameter extends Parameter {
    @Override
    public void executeParameter(List<Entity> foundEntities, String value, ParamSettings settings) throws EntitySelectException {
        int limit = Util.parseInt(value);
        if (limit < 1) {
            throw new EntitySelectException("Limit must be 1 or more!");
        }

        settings.limit = limit;
    }
}
