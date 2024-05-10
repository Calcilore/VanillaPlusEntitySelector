package net.calcilore.vanillaplusentityselector.Parameters;

import net.calcilore.vanillaplusentityselector.EntitySelectException;
import net.calcilore.vanillaplusentityselector.Internal.DoubleRange;
import org.bukkit.entity.Entity;

import java.util.List;

public class RotationParameter extends Parameter {
    final boolean isPitch;

    public RotationParameter(boolean isPitch) {
        super();
        this.isPitch = isPitch;
    }

    @Override
    public void executeParameter(List<Entity> foundEntities, String value, ParamSettings settings) throws EntitySelectException {
        DoubleRange range = DoubleRange.parseRange(value);

        if (isPitch) {
            foundEntities.removeIf(entity -> !range.within(entity.getLocation().getPitch()));
        } else {
            foundEntities.removeIf(entity -> !range.within(entity.getLocation().getYaw()));
        }
    }
}
