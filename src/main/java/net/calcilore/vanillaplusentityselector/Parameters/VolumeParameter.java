package net.calcilore.vanillaplusentityselector.Parameters;

import net.calcilore.vanillaplusentityselector.EntitySelectException;
import org.bukkit.entity.Entity;

import java.util.List;

public class VolumeParameter extends Parameter {
    private final int type;

    public VolumeParameter(int type) {
        super();
        this.type = type;
    }

    @Override
    public void executeParameter(List<Entity> foundEntities, String value, ParamSettings settings) throws EntitySelectException {
        settings.restrictWorld(foundEntities);

        double dValue = Double.parseDouble(value);

        settings.doVolumeCheck = true;
        switch (type) {
            case 0: settings.volumeCheck.setX(dValue); break;
            case 1: settings.volumeCheck.setY(dValue); break;
            case 2: settings.volumeCheck.setZ(dValue); break;
        }
    }
}
