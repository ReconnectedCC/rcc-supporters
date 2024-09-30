package cc.reconnected;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Objects;

import static net.minecraft.server.command.CommandManager.*;

public class SupporterCommand {
    public static void register1(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
                literal("supporter")
                        .requires(Permissions.require("rcc.supporter"))
                        .executes(ctx -> {
                            return 1;
                        })
                        .then(literal("optout")
                                .executes(context -> {
                                    UserManager userManager = LuckPermsProvider.get().getUserManager();
                                    userManager.modifyUser(Objects.requireNonNull(context.getSource().getPlayer()).getUuid(), user -> {
                                        user.data().clear(NodeType.PREFIX::matches);
                                        user.data().add(PrefixNode.builder("", 10000).build());
                                    });
                                    return 1;
                                })
                        )
                        .then(literal("optin")
                                .executes(context -> {
                                    UserManager userManager = LuckPermsProvider.get().getUserManager();
                                    userManager.modifyUser(Objects.requireNonNull(context.getSource().getPlayer()).getUuid(), user -> {
                                        user.data().clear(NodeType.PREFIX::matches);
                                        user.data().remove(PrefixNode.builder("", 10000).build());
                                    });
                                    return 1;
                                })
                        )
                        .then(literal("tier")
                                .then(argument("tier", IntegerArgumentType.integer(1, 3))
                                        .executes(context -> {
                                            if (context.getSource().getPlayer() == null) {
                                                context.getSource().sendFeedback(() -> Text.literal("You must be a player to use this command."), false);
                                                return 0;
                                            }
                                            UserManager userManager = LuckPermsProvider.get().getUserManager();
                                            switch (IntegerArgumentType.getInteger(context, "tier")) {
                                                case 1:
                                                    if (!Permissions.check(context.getSource().getPlayer(), "rcc.supporter.tier1")) {
                                                        context.getSource().sendFeedback(() -> Text.literal("You do not have permission to use this tier."), false);
                                                        return 0;
                                                    }
                                                    break;
                                                case 2:
                                                    if (!Permissions.check(context.getSource().getPlayer(), "rcc.supporter.tier2")) {
                                                        return 0;
                                                    }
                                                    break;
                                                case 3:
                                                    if (!Permissions.check(context.getSource().getPlayer(), "rcc.supporter.tier3")) {
                                                        context.getSource().sendFeedback(() -> Text.literal("You do not have permission to use this tier."), false);
                                                        return 0;
                                                    }
                                                    break;
                                            }
                                            userManager.modifyUser(Objects.requireNonNull(context.getSource().getPlayer()).getUuid(), user -> {
                                                user.data().clear(NodeType.PREFIX::matches);
                                                user.data().add(PrefixNode.builder("[S" + IntegerArgumentType.getInteger(context, "tier") + "]", 10000).build());
                                            });
                                            return 1;
                                        })
                                )
                        )
        );
    }
        public static void register2(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {

            dispatcher.register(
                literal("supporterreload") //TODO: Move this to the other command later
                        .requires(source -> source.hasPermissionLevel(4))
                                .executes(context -> {
                                    Supporter.reloadSupporters(Main.db, Main.userManager, Main.groupManager);
                                    return 1;
                                })
        );
    }
}
