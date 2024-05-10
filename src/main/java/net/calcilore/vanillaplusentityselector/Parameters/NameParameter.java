package net.calcilore.vanillaplusentityselector.Parameters;

import net.calcilore.vanillaplusentityselector.EntitySelectException;
import net.calcilore.vanillaplusentityselector.Internal.Util;
import org.bukkit.entity.Entity;

import java.util.List;

public class NameParameter extends Parameter {
    @Override
    public void executeParameter(List<Entity> foundEntities, String value, ParamSettings settings) throws EntitySelectException {
        value = Util.parseString(value);
        boolean invert = value.charAt(0) == '!';
        if (invert) {
            value = value.substring(1);
        }

        final String finalValue = value;
        foundEntities.removeIf(entity -> entity.getName().equals(finalValue) == invert);
    }
}
