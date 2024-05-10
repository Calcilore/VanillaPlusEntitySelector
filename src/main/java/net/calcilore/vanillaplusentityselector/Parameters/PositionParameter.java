package net.calcilore.vanillaplusentityselector.Parameters;

import net.calcilore.vanillaplusentityselector.EntitySelectException;
import org.bukkit.entity.Entity;

import java.util.List;

public class PositionParameter extends Parameter {
    private final int type;

    public PositionParameter(int type) {
        super();
        this.type = type;
    }

    @Override
    public void executeParameter(List<Entity> foundEntities, String value, ParamSettings settings) throws EntitySelectException {
        settings.restrictWorld(foundEntities);

        double dValue = Double.parseDouble(value);

        switch (type) {
            case 0: settings.origin.setX(dValue); break;
            case 1: settings.origin.setY(dValue); break;
            case 2: settings.origin.setZ(dValue); break;
        }
    }
}
