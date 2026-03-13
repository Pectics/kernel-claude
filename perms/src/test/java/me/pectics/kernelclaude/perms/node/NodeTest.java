package me.pectics.kernelclaude.perms.node;

import me.pectics.kernelclaude.perms.node.types.InheritanceNode;
import me.pectics.kernelclaude.perms.node.types.MetaNode;
import me.pectics.kernelclaude.perms.node.types.PermissionNode;
import me.pectics.kernelclaude.perms.node.types.WeightNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for Node implementations.
 */
@DisplayName("节点测试")
class NodeTest {

    @Nested
    @DisplayName("权限节点")
    class PermissionNodeTests {

        @Test
        @DisplayName("应创建值为 true 的权限节点")
        void shouldCreatePermissionNodeTrue() {
            PermissionNode node = PermissionNode.builder("admin.test")
                    .value(true)
                    .build();

            assertThat(node.getKey()).isEqualTo("admin.test");
            assertThat(node.getValue()).isTrue();
            assertThat(node.getType()).isEqualTo(NodeType.PERMISSION);
            assertThat(node.hasExpiry()).isFalse();
            assertThat(node.getContexts().isEmpty()).isTrue();
        }

        @Test
        @DisplayName("应创建值为 false 的权限节点（否定权限）")
        void shouldCreatePermissionNodeFalse() {
            PermissionNode node = PermissionNode.builder("admin.test")
                    .value(false)
                    .build();

            assertThat(node.getKey()).isEqualTo("admin.test");
            assertThat(node.getValue()).isFalse();
        }

        @Test
        @DisplayName("应创建临时权限节点")
        void shouldCreateTemporaryPermissionNode() {
            Instant expiry = Instant.now().plus(Duration.ofHours(1));
            PermissionNode node = PermissionNode.builder("admin.test")
                    .value(true)
                    .expiry(expiry)
                    .build();

            assertThat(node.hasExpiry()).isTrue();
            assertThat(node.getExpiry()).isNotNull();
            assertThat(node.getExpiry()).isAfter(Instant.now());
        }

        @Test
        @DisplayName("应创建带上下文的权限节点")
        void shouldCreatePermissionNodeWithContext() {
            PermissionNode node = PermissionNode.builder("admin.test")
                    .value(true)
                    .withContext("foo", "bar")
                    .build();

            assertThat(node.getContexts().contains("foo", "bar")).isTrue();
        }

        @Test
        @DisplayName("应匹配非递归通配符权限")
        void shouldMatchWildcardKeys() {
            PermissionNode node = PermissionNode.builder("admin.*")
                    .value(true)
                    .build();

            assertThat(node.matchesKey("admin.test")).isTrue();
            assertThat(node.matchesKey("admin.test.more")).isFalse();
        }

        @Test
        @DisplayName("应匹配递归通配符权限")
        void shouldMatchRecursiveWildcardKeys() {
            PermissionNode node = PermissionNode.builder("admin.**")
                    .value(true)
                    .build();

            assertThat(node.matchesKey("admin.test")).isTrue();
            assertThat(node.matchesKey("admin.test.more")).isTrue();
        }
    }

    @Nested
    @DisplayName("继承节点")
    class InheritanceNodeTests {

        @Test
        @DisplayName("应创建继承节点")
        void shouldCreateInheritanceNode() {
            InheritanceNode node = InheritanceNode.builder("admin")
                    .build();

            assertThat(node.getGroupName()).isEqualTo("admin");
            assertThat(node.getType()).isEqualTo(NodeType.INHERITANCE);
            assertThat(node.getValue()).isTrue();
            assertThat(node.getKey()).isEqualTo("group.admin");
        }

        @Test
        @DisplayName("应创建临时继承")
        void shouldCreateTemporaryInheritance() {
            Instant expiry = Instant.now().plus(Duration.ofDays(7))
                    .truncatedTo(ChronoUnit.SECONDS);
            InheritanceNode node = InheritanceNode.builder("helper")
                    .expiry(expiry)
                    .build();

            assertThat(node.hasExpiry()).isTrue();
            assertThat(node.getExpiry()).isEqualTo(expiry);
        }

        @Test
        @DisplayName("应创建带上下文的继承节点")
        void shouldCreateInheritanceWithContext() {
            InheritanceNode node = InheritanceNode.builder("helper")
                    .withContext("platform", "qq")
                    .build();

            assertThat(node.getContexts().contains("platform", "wechat")).isFalse();
            assertThat(node.getContexts().contains("platform", "qq")).isTrue();
        }
    }

    @Nested
    @DisplayName("权重节点")
    class WeightNodeTests {

        @Test
        @DisplayName("应创建权重节点")
        void shouldCreateWeightNode() {
            WeightNode node = WeightNode.builder(100).build();

            assertThat(node.getWeight()).isEqualTo(100);
            assertThat(node.getType()).isEqualTo(NodeType.WEIGHT);
            assertThat(node.getKey()).isEqualTo("weight.100");
            assertThat(node.getValue()).isTrue();
        }

        @Test
        @DisplayName("不同权重应有不同的 key")
        void differentWeightsShouldHaveDifferentKeys() {
            WeightNode node50 = WeightNode.builder(50).build();
            WeightNode node100 = WeightNode.builder(100).build();

            assertThat(node50.getKey()).isNotEqualTo(node100.getKey());
        }
    }

    @Nested
    @DisplayName("元数据节点")
    class MetaNodeTests {

        @Test
        @DisplayName("应创建元数据节点")
        void shouldCreateMetaNode() {
            MetaNode node = MetaNode.builder("tag", "[Developer]")
                    .build();

            assertThat(node.getMetaKey()).isEqualTo("tag");
            assertThat(node.getMetaValue()).isEqualTo("[Developer]");
            assertThat(node.getMetaValue()).isNotEqualTo("[DEVELOPER]");
            assertThat(node.getType()).isEqualTo(NodeType.META);
            assertThat(node.getKey()).isEqualTo("meta.tag.[developer]");
            assertThat(node.getValue()).isTrue();
        }

        @Test
        @DisplayName("应创建带上下文的元数据节点")
        void shouldCreateMetaNodeWithContext() {
            MetaNode node = MetaNode.builder("flag", "★")
                    .withContext("platform", "qq")
                    .build();

            assertThat(node.getContexts().contains("platform", "wechat")).isFalse();
            assertThat(node.getContexts().contains("platform", "qq")).isTrue();
        }
    }
}
