package managers;

import com.destroystokyo.paper.profile.PlayerProfile;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;

public class SkinTypePlaceholder extends PlaceholderExpansion {

    public static boolean isSlim(Player player) {
        PlayerProfile profile = player.getPlayerProfile();
        profile.complete();

        PlayerTextures textures = profile.getTextures();
        if (textures == null || textures.getSkin() == null) return false;

        return textures.getSkinModel() == PlayerTextures.SkinModel.SLIM;
    }


    @Override
    public String getIdentifier() {
        return "player";
    }

    @Override
    public String getAuthor() {
        return "SrvBits";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";

        if (identifier.equalsIgnoreCase("skin_type")) {
            return isSlim(player) ? "slim" : "wide";
        }

        return null;
    }

}
