package cc.reconnected;

import com.dieselpoint.norm.Database;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeEqualityPredicate;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.util.Tristate;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.equalTo;

@Table(name = "supporters")
public class Supporter {
    @Id
    @GeneratedValue
    public int id;
    public String checkoutid;
    public String email;
    public String uuid;
    public String discordid;
    public String username;
    public Date firsttime;
    public Date begincurrent;
    public int duration;
    public int totaldonated;

    @Transient
    public boolean isSupporter() {
        Calendar cal = new Calendar.Builder().setInstant(begincurrent).build();
        cal.add(Calendar.DAY_OF_YEAR, duration);
        return new Date().after(begincurrent) && new Date().before(cal.getTime());
    }

    @Transient
    public int getSupporterTier() {
        if (totaldonated >= 7500) {
            return 3;
        } else if (totaldonated >= 2500) {
            return 2;
        }
        return 1;
    }

    @Transient
    public static void reloadSupporters(Database db, UserManager userManager, GroupManager groupManager) {
        Main.LOGGER.info("Reloading Supporters");
        groupManager.modifyGroup("SupporterTier1", (Group group) -> {
            group.data().clear(NodeType.PREFIX::matches);
            group.data().add(PrefixNode.builder("[S1]", 1000).build());
            group.data().add(PermissionNode.builder("rcc.supporter.tier1").build());
        }).join();
        groupManager.modifyGroup("SupporterTier2", (Group group) -> {
            group.data().clear(NodeType.PREFIX::matches);
            group.data().add(PrefixNode.builder("[S2]", 1000).build());
            group.data().add(PermissionNode.builder("rcc.supporter.tier2").build());
        }).join();
        groupManager.modifyGroup("SupporterTier3", (Group group) -> {
            group.data().clear(NodeType.PREFIX::matches);
            group.data().add(PrefixNode.builder("[S3]", 1000).build());
            group.data().add(PermissionNode.builder("rcc.supporter.tier3").build());
        }).join();
        List<Supporter> SupporterList = db.table("supporters").results(Supporter.class);
        if (Main.cachedSupporters != null) {
            if (Main.cachedSupporters.equals(SupporterList)) {
                return;
            }
        }
        Main.LOGGER.info("Tables have been turned, processing " + SupporterList.size() + " supporters");
        Main.cachedSupporters = SupporterList;
        for (Supporter supporter : SupporterList) {
            Main.LOGGER.info("Processing supporter: " + supporter.username + " isSupporter: " + supporter.isSupporter() + " Tier: " + supporter.getSupporterTier());
            User user = userManager.loadUser(UUID.fromString(supporter.uuid.replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5" ))).join();
            if (user == null) {
                continue;
            }
            userManager.modifyUser(user.getUniqueId(), (User userMod) -> {
                if (!supporter.isSupporter()) {
                    if (userMod.data().contains(InheritanceNode.builder("SupporterTier3").build(), NodeEqualityPredicate.ONLY_KEY) == Tristate.TRUE) {
                        userMod.data().remove(InheritanceNode.builder("SupporterTier3").build());
                    }
                    if (userMod.data().contains(InheritanceNode.builder("SupporterTier2").build(), NodeEqualityPredicate.ONLY_KEY) == Tristate.TRUE) {
                        userMod.data().remove(InheritanceNode.builder("SupporterTier2").build());
                    }
                    if (userMod.data().contains(InheritanceNode.builder("SupporterTier1").build(), NodeEqualityPredicate.ONLY_KEY) == Tristate.TRUE) {
                        userMod.data().remove(InheritanceNode.builder("SupporterTier1").build());
                    }
                    return;
                }
                switch (supporter.getSupporterTier()) {
                    case 3:
                        Node node3 = InheritanceNode.builder("SupporterTier3").build();
                        userMod.data().add(node3);
                    case 2:
                        Node node2 = InheritanceNode.builder("SupporterTier2").build();
                        userMod.data().add(node2);
                    case 1:
                        Node node1 = InheritanceNode.builder("SupporterTier1").build();
                        userMod.data().add(node1);
                        break;
                }
            }).join();
        }
    }
}
