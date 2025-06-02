package adralik.srvBits;

import managers.AfkManager;
import managers.JoinProtectionManager;
import managers.LinkPlayerManager;
import managers.SpawnProtection;

import static adralik.srvBits.Main.javaPlugin;
import static adralik.srvBits.Main.pluginManager;

public class Features {

    public static void init() {
        pluginManager.registerEvents(new JoinProtectionManager(), javaPlugin);
        pluginManager.registerEvents(new LinkPlayerManager(), javaPlugin);
        pluginManager.registerEvents(new AfkManager(), javaPlugin);
        pluginManager.registerEvents(new SpawnProtection(), javaPlugin);
    }

}
