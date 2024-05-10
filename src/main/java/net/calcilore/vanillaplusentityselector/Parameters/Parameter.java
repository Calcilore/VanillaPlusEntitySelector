package net.calcilore.vanillaplusentityselector.Parameters;

import net.calcilore.vanillaplusentityselector.EntitySelectException;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public abstract class Parameter {
    public abstract void executeParameter(List<Entity> foundEntities, String value, ParamSettings settings) throws EntitySelectException;
    public List<String> tabCompleteParameter(String arg) { return new ArrayList<>(); }
}
