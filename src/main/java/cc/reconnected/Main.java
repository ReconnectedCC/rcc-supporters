package cc.reconnected;

import com.dieselpoint.norm.Database;
import io.github.blumbo.blfscheduler.BlfRunnable;
import io.github.blumbo.blfscheduler.BlfScheduler;
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


public class Main implements ModInitializer {
    static Logger LOGGER = LoggerFactory.getLogger("rcc-supporters");
    public LuckPerms luckPerms;
    public static UserManager userManager;
    public static GroupManager groupManager;
    public static Database db;
    public static List<Supporter> cachedSupporters;
    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> onStartServer());
        CommandRegistrationCallback.EVENT.register(SupporterCommand::register1);
        CommandRegistrationCallback.EVENT.register(SupporterCommand::register2);
    }
    public void onStartServer() {
        luckPerms = LuckPermsProvider.get();
        userManager = luckPerms.getUserManager();
        groupManager = luckPerms.getGroupManager();
        db = new Database();
        final cc.reconnected.RccSupporterConfig config = cc.reconnected.RccSupporterConfig.createAndLoad();
        db.setJdbcUrl(config.jdbcUrl());
        BlfScheduler.repeat(0, 20 * 60 * 5, new BlfRunnable() {
            @Override
            public void run() {
                Supporter.reloadSupporters(db, userManager, groupManager);
            }
        });
    }
}
