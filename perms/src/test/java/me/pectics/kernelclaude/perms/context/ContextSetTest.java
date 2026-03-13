package me.pectics.kernelclaude.perms.context;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ContextSet implementations.
 */
@DisplayName("上下文集合测试")
class ContextSetTest {

    @Nested
    @DisplayName("不可变上下文集合")
    class ImmutableContextSetTests {

        @Test
        @DisplayName("空集合应为单例")
        void emptyShouldBeSingleton() {
            ImmutableContextSet empty1 = ImmutableContextSet.empty();
            ImmutableContextSet empty2 = ImmutableContextSet.empty();

            assertThat(empty1).isSameAs(empty2);
            assertThat(empty1.isEmpty()).isTrue();
            assertThat(empty1.size()).isZero();
        }

        @Test
        @DisplayName("应包含添加的上下文")
        void shouldContainAddedContexts() {
            MutableContextSet mutable = MutableContextSet.create();
            mutable.add("platform", "telegram");
            mutable.add("channel", "general");

            ImmutableContextSet immutable = mutable.immutableCopy();

            assertThat(immutable.size()).isEqualTo(2);
            assertThat(immutable.contains("platform", "telegram")).isTrue();
            assertThat(immutable.contains("channel", "general")).isTrue();
            assertThat(immutable.contains("platform", "discord")).isFalse();
        }

        @Test
        @DisplayName("应迭代所有上下文")
        void shouldIterateOverAllContexts() {
            MutableContextSet mutable = MutableContextSet.create();
            mutable.add("platform", "telegram");
            mutable.add("platform", "discord");
            mutable.add("channel", "general");

            ImmutableContextSet immutable = mutable.immutableCopy();

            int count = 0;
            for (Context ctx : immutable) {
                count++;
                assertThat(ctx.getKey()).isIn("platform", "channel");
            }
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("键应忽略大小写")
        void keyShouldBeCaseInsensitive() {
            MutableContextSet mutable = MutableContextSet.create();
            mutable.add("Platform", "Telegram");
            mutable.add("PLATFORM", "TELEGRAM");

            assertThat(mutable.size()).isEqualTo(1);
            assertThat(mutable.contains("platform", "telegram")).isTrue();
            assertThat(mutable.contains("PLATFORM", "TELEGRAM")).isTrue();
        }

        @Test
        @DisplayName("值应忽略大小写")
        void valueShouldBeCaseInsensitive() {
            MutableContextSet mutable = MutableContextSet.create();
            mutable.add("platform", "Telegram");

            assertThat(mutable.contains("platform", "TELEGRAM")).isTrue();
            assertThat(mutable.contains("platform", "telegram")).isTrue();
        }

        @Test
        @DisplayName("不可变副本应返回自身")
        void immutableCopyShouldReturnSelf() {
            ImmutableContextSet original = ImmutableContextSet.empty();
            ImmutableContextSet copy = original.immutableCopy();

            assertThat(copy).isSameAs(original);
        }

        @Test
        @DisplayName("应正确获取值集合")
        void shouldGetValuesForKey() {
            MutableContextSet mutable = MutableContextSet.create();
            mutable.add("platform", "telegram");
            mutable.add("platform", "discord");
            mutable.add("channel", "general");

            ImmutableContextSet immutable = mutable.immutableCopy();

            assertThat(immutable.getValues("platform")).containsExactlyInAnyOrder("telegram", "discord");
            assertThat(immutable.getValues("channel")).containsExactly("general");
            assertThat(immutable.getValues("nonexistent")).isEmpty();
        }

        @Test
        @DisplayName("应正确检测键存在")
        void shouldDetectKeyPresence() {
            MutableContextSet mutable = MutableContextSet.create();
            mutable.add("platform", "telegram");

            ImmutableContextSet immutable = mutable.immutableCopy();

            assertThat(immutable.containsKey("platform")).isTrue();
            assertThat(immutable.containsKey("PLATFORM")).isTrue();
            assertThat(immutable.containsKey("nonexistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("可变上下文集合")
    class MutableContextSetTests {

        @Test
        @DisplayName("应添加和移除上下文")
        void shouldAddAndRemoveContexts() {
            MutableContextSet contexts = MutableContextSet.create();

            contexts.add("platform", "telegram");
            assertThat(contexts.contains("platform", "telegram")).isTrue();

            contexts.remove("platform", "telegram");
            assertThat(contexts.contains("platform", "telegram")).isFalse();
        }

        @Test
        @DisplayName("应支持多值键")
        void shouldSupportMultiValueKeys() {
            MutableContextSet contexts = MutableContextSet.create();
            contexts.add("platform", "telegram");
            contexts.add("platform", "discord");

            assertThat(contexts.size()).isEqualTo(2);
            assertThat(contexts.contains("platform", "telegram")).isTrue();
            assertThat(contexts.contains("platform", "discord")).isTrue();
        }

        @Test
        @DisplayName("应清空所有上下文")
        void shouldClearAllContexts() {
            MutableContextSet contexts = MutableContextSet.create();
            contexts.add("platform", "telegram");
            contexts.add("channel", "general");

            contexts.clear();

            assertThat(contexts.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("应移除键的所有值")
        void shouldRemoveAllValuesForKey() {
            MutableContextSet contexts = MutableContextSet.create();
            contexts.add("platform", "telegram");
            contexts.add("platform", "discord");
            contexts.add("channel", "general");

            boolean removed = contexts.removeAll("platform");

            assertThat(removed).isTrue();
            assertThat(contexts.size()).isEqualTo(1);
            assertThat(contexts.containsKey("platform")).isFalse();
        }

        @Test
        @DisplayName("应正确添加另一个集合的所有上下文")
        void shouldAddAllFromOtherSet() {
            MutableContextSet source = MutableContextSet.create();
            source.add("platform", "telegram");
            source.add("channel", "general");

            MutableContextSet target = MutableContextSet.create();
            target.addAll(source);

            assertThat(target.size()).isEqualTo(2);
            assertThat(target.contains("platform", "telegram")).isTrue();
            assertThat(target.contains("channel", "general")).isTrue();
        }

        @Test
        @DisplayName("应创建不可变副本")
        void shouldCreateImmutableCopy() {
            MutableContextSet mutable = MutableContextSet.create();
            mutable.add("platform", "telegram");

            ImmutableContextSet immutable = mutable.immutableCopy();

            // Modify original should not affect copy
            mutable.add("platform", "discord");

            assertThat(immutable.size()).isEqualTo(1);
            assertThat(immutable.contains("platform", "discord")).isFalse();
        }

        @Test
        @DisplayName("应创建可变副本")
        void shouldCreateMutableCopy() {
            MutableContextSet original = MutableContextSet.create();
            original.add("platform", "telegram");

            MutableContextSet copy = original.mutableCopy();

            // Modify copy should not affect original
            copy.add("platform", "discord");

            assertThat(original.size()).isEqualTo(1);
            assertThat(original.contains("platform", "discord")).isFalse();
        }
    }

    @Nested
    @DisplayName("上下文匹配")
    class ContextSatisfactionTests {

        @Test
        @DisplayName("空集合互相满足")
        void emptySetsShouldSatisfyEachOther() {
            ImmutableContextSet empty1 = ImmutableContextSet.empty();
            ImmutableContextSet empty2 = ImmutableContextSet.empty();

            assertThat(empty1.satisfies(empty2, ContextSatisfyMode.ANY_VALUE_MATCH_PER_KEY)).isTrue();
            assertThat(empty1.satisfies(empty2, ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY)).isTrue();
            assertThat(empty1.isSatisfiedBy(empty2, ContextSatisfyMode.ANY_VALUE_MATCH_PER_KEY)).isTrue();
            assertThat(empty1.isSatisfiedBy(empty2, ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY)).isTrue();
        }

        @Test
        @DisplayName("全局权限（空上下文）语义：查询上下文满足空节点上下文")
        void globalPermissionSemantics() {
            // 节点上下文（空）= 全局权限
            ImmutableContextSet nodeContext = ImmutableContextSet.empty();

            // 查询上下文（有内容）
            MutableContextSet queryContext = MutableContextSet.create();
            queryContext.add("platform", "discord");
            queryContext.add("channel", "general");

            // 正确语义：queryContext.satisfies(nodeContext)
            // = "查询上下文是否满足节点上下文的要求"
            // 节点上下文为空（无要求），所以任何查询都满足
            assertThat(queryContext.immutableCopy().satisfies(nodeContext, ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY)).isTrue();
            assertThat(queryContext.immutableCopy().satisfies(nodeContext, ContextSatisfyMode.ANY_VALUE_MATCH_PER_KEY)).isTrue();

            // 也等于：nodeContext.isSatisfiedBy(queryContext)
            assertThat(nodeContext.isSatisfiedBy(queryContext.immutableCopy(), ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY)).isTrue();
            assertThat(nodeContext.isSatisfiedBy(queryContext.immutableCopy(), ContextSatisfyMode.ANY_VALUE_MATCH_PER_KEY)).isTrue();
        }

        @Test
        @DisplayName("ANY_VALUE_MATCH_PER_KEY 语义")
        void anyValueMatchPerKeySemantics() {
            // 节点上下文: platform=discord
            MutableContextSet nodeContext = MutableContextSet.create();
            nodeContext.add("platform", "discord");

            // 查询上下文匹配（包含 discord）
            MutableContextSet query1 = MutableContextSet.create();
            query1.add("platform", "discord");

            // 查询上下文匹配（包含多个值其中有 discord）
            MutableContextSet query2 = MutableContextSet.create();
            query2.add("platform", "telegram");
            query2.add("platform", "discord");

            // 查询上下文不匹配（不包含 discord）
            MutableContextSet query3 = MutableContextSet.create();
            query3.add("platform", "telegram");

            // 查询上下文不匹配（缺少 platform 键）
            MutableContextSet query4 = MutableContextSet.create();
            query4.add("channel", "general");

            assertThat(query1.immutableCopy().satisfies(nodeContext.immutableCopy(), ContextSatisfyMode.ANY_VALUE_MATCH_PER_KEY)).isTrue();
            assertThat(query2.immutableCopy().satisfies(nodeContext.immutableCopy(), ContextSatisfyMode.ANY_VALUE_MATCH_PER_KEY)).isTrue();
            assertThat(query3.immutableCopy().satisfies(nodeContext.immutableCopy(), ContextSatisfyMode.ANY_VALUE_MATCH_PER_KEY)).isFalse();
            assertThat(query4.immutableCopy().satisfies(nodeContext.immutableCopy(), ContextSatisfyMode.ANY_VALUE_MATCH_PER_KEY)).isFalse();
        }

        @Test
        @DisplayName("ALL_VALUE_MATCH_PER_KEY 语义")
        void allValueMatchPerKeySemantics() {
            // 节点上下文: platform=discord, platform=telegram
            MutableContextSet nodeContext = MutableContextSet.create();
            nodeContext.add("platform", "discord");
            nodeContext.add("platform", "telegram");

            // 查询上下文匹配（包含所有值）
            MutableContextSet query1 = MutableContextSet.create();
            query1.add("platform", "discord");
            query1.add("platform", "telegram");

            // 查询上下文匹配（包含所有值 + 额外值）
            MutableContextSet query2 = MutableContextSet.create();
            query2.add("platform", "discord");
            query2.add("platform", "telegram");
            query2.add("platform", "slack");

            // 查询上下文不匹配（缺少 telegram）
            MutableContextSet query3 = MutableContextSet.create();
            query3.add("platform", "discord");

            // 查询上下文不匹配（缺少 discord）
            MutableContextSet query4 = MutableContextSet.create();
            query4.add("platform", "telegram");

            assertThat(query1.immutableCopy().satisfies(nodeContext.immutableCopy(), ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY)).isTrue();
            assertThat(query2.immutableCopy().satisfies(nodeContext.immutableCopy(), ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY)).isTrue();
            assertThat(query3.immutableCopy().satisfies(nodeContext.immutableCopy(), ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY)).isFalse();
            assertThat(query4.immutableCopy().satisfies(nodeContext.immutableCopy(), ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY)).isFalse();
        }

        @Test
        @DisplayName("多键情况下应正确处理")
        void shouldHandleMultipleKeysCorrectly() {
            // 节点上下文: platform=discord, channel=general
            MutableContextSet nodeContext = MutableContextSet.create();
            nodeContext.add("platform", "discord");
            nodeContext.add("channel", "general");

            // 查询上下文匹配（包含所有键值对）
            MutableContextSet query1 = MutableContextSet.create();
            query1.add("platform", "discord");
            query1.add("channel", "general");
            query1.add("server", "prod"); // 额外值不影响

            // 查询上下文不匹配（缺少 channel）
            MutableContextSet query2 = MutableContextSet.create();
            query2.add("platform", "discord");
            query2.add("server", "prod");

            // 查询上下文不匹配（channel 值不匹配）
            MutableContextSet query3 = MutableContextSet.create();
            query3.add("platform", "discord");
            query3.add("channel", "random");

            assertThat(query1.immutableCopy().satisfies(nodeContext.immutableCopy(), ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY)).isTrue();
            assertThat(query2.immutableCopy().satisfies(nodeContext.immutableCopy(), ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY)).isFalse();
            assertThat(query3.immutableCopy().satisfies(nodeContext.immutableCopy(), ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY)).isFalse();
        }

        @Test
        @DisplayName("相同实例应返回 true")
        void sameInstanceShouldReturnTrue() {
            MutableContextSet data = MutableContextSet.create();
            data.add("platform", "discord");

            assertThat(data.satisfies(data, ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY)).isTrue();
            assertThat(data.satisfies(data, ContextSatisfyMode.ANY_VALUE_MATCH_PER_KEY)).isTrue();
        }
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("应构建非空集合（空集合用 empty() 获取）")
        void shouldBuildNonEmptySet() {
            ImmutableContextSet set = ImmutableContextSet.builder()
                    .add("platform", "discord")
                    .add("channel", "general")
                    .build();

            assertThat(set.isEmpty()).isFalse();
            assertThat(set.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("应构建包含多个上下文的集合")
        void shouldBuildSetWithMultipleContexts() {
            ImmutableContextSet set = ImmutableContextSet.builder()
                    .add("platform", "discord")
                    .add("platform", "telegram")
                    .add("channel", "general")
                    .build();

            assertThat(set.size()).isEqualTo(3);
            assertThat(set.contains("platform", "discord")).isTrue();
            assertThat(set.contains("platform", "telegram")).isTrue();
            assertThat(set.contains("channel", "general")).isTrue();
        }

        @Test
        @DisplayName("应从另一个集合添加所有上下文")
        void shouldAddAllFromOtherSet() {
            MutableContextSet source = MutableContextSet.create();
            source.add("platform", "discord");
            source.add("channel", "general");

            ImmutableContextSet set = ImmutableContextSet.builder()
                    .addAll(source)
                    .build();

            assertThat(set.size()).isEqualTo(2);
            assertThat(set.contains("platform", "discord")).isTrue();
            assertThat(set.contains("channel", "general")).isTrue();
        }

        @Test
        @DisplayName("空 Builder 应构建空集合")
        void emptyBuilderShouldBuildEmptySet() {
            ImmutableContextSet set = ImmutableContextSet.builder().build();

            assertThat(set.isEmpty()).isTrue();
            // Note: 可能不是同一个实例，但语义上等价
        }
    }

    @Nested
    @DisplayName("equals 和 hashCode 测试")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("相同内容的集合应相等")
        void setsWithSameContentShouldBeEqual() {
            MutableContextSet set1 = MutableContextSet.create();
            set1.add("platform", "discord");
            set1.add("channel", "general");

            MutableContextSet set2 = MutableContextSet.create();
            set2.add("platform", "discord");
            set2.add("channel", "general");

            assertThat(set1).isEqualTo(set2);
            assertThat(set1.hashCode()).isEqualTo(set2.hashCode());
        }

        @Test
        @DisplayName("不同内容的集合应不相等")
        void setsWithDifferentContentShouldNotBeEqual() {
            MutableContextSet set1 = MutableContextSet.create();
            set1.add("platform", "discord");

            MutableContextSet set2 = MutableContextSet.create();
            set2.add("platform", "telegram");

            assertThat(set1).isNotEqualTo(set2);
        }

        @Test
        @DisplayName("空集合应相等")
        void emptySetsShouldBeEqual() {
            MutableContextSet set1 = MutableContextSet.create();
            MutableContextSet set2 = MutableContextSet.create();

            assertThat(set1).isEqualTo(set2);
        }
    }

    @Nested
    @DisplayName("containsAny 测试")
    class ContainsAnyTests {

        @Test
        @DisplayName("应正确检测多个值中是否有任一匹配")
        void shouldDetectAnyMatch() {
            MutableContextSet contexts = MutableContextSet.create();
            contexts.add("platform", "discord");
            contexts.add("platform", "telegram");

            assertThat(contexts.containsAny("platform", java.util.List.of("discord", "slack"))).isTrue();
            assertThat(contexts.containsAny("platform", java.util.List.of("telegram", "slack"))).isTrue();
            assertThat(contexts.containsAny("platform", java.util.List.of("slack", "irc"))).isFalse();
            assertThat(contexts.containsAny("nonexistent", java.util.List.of("discord"))).isFalse();
        }
    }
}
