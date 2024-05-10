package net.calcilore.vanillaplusentityselector.Parameters;

import net.calcilore.vanillaplusentityselector.Internal.DoubleRange;
import net.calcilore.vanillaplusentityselector.EntitySelectException;
import org.bukkit.entity.Entity;

import java.util.List;

public class DistanceParameter extends Parameter {
    @Override
    public void executeParameter(List<Entity> foundEntities, String value, ParamSettings settings) throws EntitySelectException {
        if (settings.isOriginInvalid()) {
            throw new EntitySelectException(EntitySelectException.consoleLocationException);
        }

        settings.restrictWorld(foundEntities);

        DoubleRange range = DoubleRange.parseRange(value);
        foundEntities.removeIf(entity ->
                !range.within(entity.getLocation().distance(settings.origin)));
    }
}
