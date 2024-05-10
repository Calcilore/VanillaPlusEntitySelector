package net.calcilore.vanillaplusentityselector.Parameters;

import net.calcilore.vanillaplusentityselector.EntitySelectException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class GamemodeParameter extends Parameter {
    @Override
    public void executeParameter(List<Entity> foundEntities, String value, ParamSettings settings) throws EntitySelectException {
        boolean invert = value.charAt(0) == '!';
        final String gamemode = (invert ? value.substring(1) : value).toUpperCase();

        switch (gamemode) {
            case "CREATIVE":
            case "SURVIVAL":
            case "ADVENTURE":
            case "SPECTATOR":
                break;

            default:
                throw new EntitySelectException("Invalid Gamemode!");
        }

        settings.restrictToPlayers(foundEntities);

        foundEntities.removeIf(entity -> ((Player) entity).getGameMode().name().equals(gamemode) == invert);
    }

    @Override
    public List<String> tabCompleteParameter(String arg) {
        return arg.startsWith("!") ?
                Arrays.asList("!creative", "!survival", "!adventure", "!spectator") :
                Arrays.asList("creative", "survival", "adventure", "spectator");
    }
}
