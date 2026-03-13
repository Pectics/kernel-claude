package me.pectics.kernelclaude.perms;

import me.pectics.kernelclaude.perms.context.ImmutableContextSet;
import me.pectics.kernelclaude.perms.context.MutableContextSet;
import me.pectics.kernelclaude.perms.model.Group;
import me.pectics.kernelclaude.perms.model.SimpleGroup;
import me.pectics.kernelclaude.perms.model.SimpleUser;
import me.pectics.kernelclaude.perms.model.User;
import me.pectics.kernelclaude.perms.node.types.InheritanceNode;
import me.pectics.kernelclaude.perms.node.types.PermissionNode;
import me.pectics.kernelclaude.perms.types.Tristate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for the permission system.
 */
@DisplayName("权限系统集成测试")
class PermissionIntegrationTest {

    private SimpleGroup defaultGroup;
    private SimpleGroup anotherGroup;
    private SimpleGroup adminGroup;

    @BeforeEach
    void setUpGroups() {
        // Create groups
        defaultGroup = new SimpleGroup("default");
        defaultGroup.setWeight(0);

        anotherGroup = new SimpleGroup("another");
        anotherGroup.setWeight(50);
        anotherGroup.data().add(PermissionNode.builder("another.test").value(true).build());
        anotherGroup.data().add(PermissionNode.builder("another.test.more").value(true).build());

        adminGroup = new SimpleGroup("admin");
        adminGroup.setWeight(100);
        adminGroup.data().add(PermissionNode.builder("*").value(true).build());
        adminGroup.data().add(InheritanceNode.builder("another").build());

        // Set up group resolver
        SimpleGroup.GroupResolver resolver = n -> switch (n) {
            case "default" -> defaultGroup;
            case "another" -> anotherGroup;
            case "admin" -> adminGroup;
            default -> null;
        };

        defaultGroup.setGroupResolver(resolver);
        anotherGroup.setGroupResolver(resolver);
        adminGroup.setGroupResolver(resolver);
    }

    @Nested
    @DisplayName("用户权限检查")
    class UserPermissionChecks {

        @Test
        @DisplayName("无权限用户应返回 UNDEFINED")
        void userWithNoPermissionsShouldReturnUndefined() {
            SimpleUser user = new SimpleUser("telegram", "12345");
            user.setGroupResolver(name -> switch (name) {
                case "default" -> defaultGroup;
                case "another" -> anotherGroup;
                case "admin" -> adminGroup;
                default -> null;
            });

            Tristate result = user.checkPermission("some.permission");
            assertThat(result).isEqualTo(Tristate.UNDEFINED);
        }

        @Test
        @DisplayName("直接赋予权限的用户应返回 TRUE")
        void userWithDirectPermissionShouldReturnTrue() {
            User user = new SimpleUser("telegram", "12345");
            user.data().add(PermissionNode.builder("my.permission").value(true).build());

            Tristate result = user.checkPermission("my.permission");
            assertThat(result).isEqualTo(Tristate.TRUE);
        }

        @Test
        @DisplayName("否定权限的用户应返回 FALSE")
        void userWithNegatedPermissionShouldReturnFalse() {
            User user = new SimpleUser("telegram", "12345");
            user.data().add(PermissionNode.builder("my.permission").value(false).build());

            Tristate result = user.checkPermission("my.permission");
            assertThat(result).isEqualTo(Tristate.FALSE);
        }

        @Test
        @DisplayName("组内用户应继承组权限")
        void userInGroupShouldInheritGroupPermissions() {
            SimpleUser user = new SimpleUser("telegram", "12345");
            user.setGroupResolver(name -> switch (name) {
                case "default" -> defaultGroup;
                case "another" -> anotherGroup;
                case "admin" -> adminGroup;
                default -> null;
            });

            // Add user to another group
            user.data().add(InheritanceNode.builder("another").build());

            Tristate result = user.checkPermission("another.test");
            assertThat(result).isEqualTo(Tristate.TRUE);
        }

        @Test
        @DisplayName("通配符权限应匹配所有权限")
        void adminWithWildcardShouldMatchAllPermissions() {
            SimpleUser user = new SimpleUser("telegram", "67890");
            user.setGroupResolver(name -> switch (name) {
                case "default" -> defaultGroup;
                case "another" -> anotherGroup;
                case "admin" -> adminGroup;
                default -> null;
            });

            // Add user to admin group
            user.data().add(InheritanceNode.builder("admin").build());

            Tristate result1 = user.checkPermission("any.random.permission");
            Tristate result2 = user.checkPermission("bot.command.ban");
            Tristate result3 = user.checkPermission("bot.feature.pin");

            assertThat(result1).isEqualTo(Tristate.TRUE);
            assertThat(result2).isEqualTo(Tristate.TRUE);
            assertThat(result3).isEqualTo(Tristate.TRUE);
        }
    }

    @Nested
    @DisplayName("组继承")
    class GroupInheritance {

        @Test
        @DisplayName("组应继承父组的权限")
        void groupShouldInheritFromParentGroups() {
            // admin inherits from another
            Group group = adminGroup;

            assertThat(group.getInheritedGroups(ImmutableContextSet.empty()))
                    .contains(adminGroup, anotherGroup);
        }

        @Test
        @DisplayName("继承的节点应包含父组节点")
        void inheritedNodesShouldIncludeParentGroupNodes() {
            Group group = adminGroup;

            var nodes = group.resolveInheritedNodes(ImmutableContextSet.empty());

            // Should contain admin's wildcard permission
            assertThat(nodes.stream().anyMatch(n -> n.getKey().equals("*"))).isTrue();
            // Should contain another's permission (inherited)
            assertThat(nodes.stream().anyMatch(n -> n.getKey().equals("another.test"))).isTrue();
        }
    }

    @Nested
    @DisplayName("基于上下文的权限")
    class ContextBasedPermissions {

        @Test
        @DisplayName("权限应仅在正确的上下文中生效")
        void permissionShouldOnlyWorkInCorrectContext() {
            MutableContextSet channelContext = MutableContextSet.create();
            channelContext.add("channel", "general");

            PermissionNode node = PermissionNode.builder("bot.feature.pin")
                    .value(true)
                    .withContext("channel", "general")
                    .build();

            SimpleUser user = new SimpleUser("telegram", "12345");
            user.data().add(node);

            // Should work in general channel context
            Tristate resultInGeneral = user.checkPermission("bot.feature.pin", channelContext.immutableCopy());

            // Should not work in admin channel context
            MutableContextSet adminContext = MutableContextSet.create();
            adminContext.add("channel", "admin");
            Tristate resultInAdmin = user.checkPermission("bot.feature.pin", adminContext.immutableCopy());

            assertThat(resultInGeneral).isEqualTo(Tristate.TRUE);
            assertThat(resultInAdmin).isEqualTo(Tristate.UNDEFINED);
        }

        @Test
        @DisplayName("全局权限应在任何上下文中生效")
        void globalPermissionShouldWorkInAnyContext() {
            PermissionNode node = PermissionNode.builder("bot.command.help")
                    .value(true)
                    .build(); // No context = global

            SimpleUser user = new SimpleUser("telegram", "12345");
            user.data().add(node);

            MutableContextSet anyContext = MutableContextSet.create();
            anyContext.add("platform", "discord");

            Tristate result = user.checkPermission("bot.command.help", anyContext.immutableCopy());

            assertThat(result).isEqualTo(Tristate.TRUE);
        }
    }

    @Nested
    @DisplayName("节点映射操作")
    class NodeMapOperations {

        @Test
        @DisplayName("应添加和移除节点")
        void shouldAddAndRemoveNodes() {
            SimpleUser user = new SimpleUser("telegram", "12345");
            PermissionNode node = PermissionNode.builder("test.perm").value(true).build();

            // Add
            var addResult = user.data().add(node);
            assertThat(addResult.isSuccess()).isTrue();
            assertThat(user.data().toCollection()).hasSize(1);

            // Remove
            var removeResult = user.data().remove(node);
            assertThat(removeResult.isSuccess()).isTrue();
            assertThat(user.data().toCollection()).isEmpty();
        }

        @Test
        @DisplayName("不应添加重复节点")
        void shouldNotAddDuplicateNodes() {
            SimpleUser user = new SimpleUser("telegram", "12345");
            PermissionNode node1 = PermissionNode.builder("test.perm").value(true).build();
            PermissionNode node2 = PermissionNode.builder("test.perm").value(true).build();

            user.data().add(node1);
            var result = user.data().add(node2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(user.data().toCollection()).hasSize(1);
        }

        @Test
        @DisplayName("应按条件清空节点")
        void shouldClearNodesByPredicate() {
            SimpleUser user = new SimpleUser("telegram", "12345");
            user.data().add(PermissionNode.builder("test.perm1").value(true).build());
            user.data().add(PermissionNode.builder("test.perm2").value(true).build());
            user.data().add(PermissionNode.builder("other.perm").value(true).build());

            user.data().clear(node -> node.getKey().startsWith("test."));

            assertThat(user.data().toCollection()).hasSize(1);
            assertThat(user.data().toCollection().iterator().next().getKey()).isEqualTo("other.perm");
        }
    }

    @Nested
    @DisplayName("权重系统")
    class WeightSystem {

        @Test
        @DisplayName("组应有正确的权重")
        void groupsShouldHaveCorrectWeight() {
            assertThat(defaultGroup.getWeight()).hasValue(0);
            assertThat(anotherGroup.getWeight()).hasValue(50);
            assertThat(adminGroup.getWeight()).hasValue(100);
        }

        @Test
        @DisplayName("无权重组应返回空 Optional")
        void groupWithoutWeightShouldReturnEmptyOptional() {
            Group newGroup = new SimpleGroup("new_group");
            assertThat(newGroup.getWeight()).isEmpty();
        }
    }
}
