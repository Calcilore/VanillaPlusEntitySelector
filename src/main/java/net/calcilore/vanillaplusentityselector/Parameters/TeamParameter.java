package net.calcilore.vanillaplusentityselector.Parameters;

import net.calcilore.vanillaplusentityselector.EntitySelectException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TeamParameter extends Parameter {
    @Override
    public void executeParameter(List<Entity> foundEntities, String value, ParamSettings settings) throws EntitySelectException {
        boolean invert = value.charAt(0) == '!';
        if (invert) {
            value = value.substring(1);
        }

        // get Team from value
        ScoreboardManager scb = Bukkit.getScoreboardManager();
        if (scb == null) {
            Bukkit.getLogger().info("Selection: Failed to get scoreboard manager");
            return;
        }

        Team team = scb.getMainScoreboard().getTeam(value);
        if (team == null) {
            if (!invert) {
                foundEntities.clear();
            }

            return;
        }

        foundEntities.removeIf(entity -> {
            if (entity instanceof Player) {
                return (team.hasEntry(entity.getName()) ||
                        team.hasEntry(entity.getUniqueId().toString())) == invert;
            }

            return team.hasEntry(entity.getUniqueId().toString()) == invert;
        });
    }

    @Override
    public List<String> tabCompleteParameter(String arg) {
        ScoreboardManager scb = Bukkit.getScoreboardManager();
        if (scb == null) {
            Bukkit.getLogger().info("Selection autocomplete: Failed to get scoreboard manager");
            return new ArrayList<>();
        }

        boolean invert = arg.startsWith("!");

        Set<Team> teams = scb.getMainScoreboard().getTeams();
        List<String> tab = new ArrayList<>(teams.size());
        for (Team team : teams) {
            tab.add(invert ? "!" + team.getName() : team.getName());
        }

        return tab;
    }
}
