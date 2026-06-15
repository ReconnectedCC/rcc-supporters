package cc.reconnected;

import com.dieselpoint.norm.Database;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main implements ModInitializer {
    static Logger LOGGER = LoggerFactory.getLogger("rcc-supporters");
    public LuckPerms luckPerms;
    public static UserManager userManager;
    public static GroupManager groupManager;
    public static Database db;
    public static List<Supporter> cachedSupporters;
    private ScheduledExecutorService scheduler;
    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> onStartServer());
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        });
        CommandRegistrationCallback.EVENT.register(SupporterCommand::register);
    }
    public void onStartServer() {
        luckPerms = LuckPermsProvider.get();
        userManager = luckPerms.getUserManager();
        groupManager = luckPerms.getGroupManager();
        db = new Database();
        final cc.reconnected.RccSupporterConfig config = cc.reconnected.RccSupporterConfig.createAndLoad();
        db.setJdbcUrl(config.jdbcUrl());
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            Supporter.reloadSupporters(db, userManager, groupManager);
            },0, 5, TimeUnit.MINUTES);
    }
}
