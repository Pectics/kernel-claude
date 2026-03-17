/*
 * Full integration test for MyBatisPermsStorage
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.perms;

import me.pectics.kernelclaude.perms.model.DataType;
import me.pectics.kernelclaude.perms.model.Group;
import me.pectics.kernelclaude.perms.model.User;
import me.pectics.kernelclaude.perms.node.Node;
import me.pectics.kernelclaude.perms.node.types.InheritanceNode;
import me.pectics.kernelclaude.perms.node.types.MetaNode;
import me.pectics.kernelclaude.perms.node.types.PermissionNode;
import me.pectics.kernelclaude.perms.node.types.WeightNode;
import me.pectics.kernelclaude.perms.storage.Storage;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full integration test for MyBatisPermsStorage using SQLite.
 */
@SpringBootTest(classes = PermsTestApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PermsStorageFullIntegrationTest {

    @Autowired
    private Storage storage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        System.out.println("=== Starting test ===");
    }

    @AfterEach
    void cleanup() {
        System.out.println("=== Test completed ===\n");
    }

    @Test
    @Order(1)
    void testSchemaInitialized() {
        System.out.println("Testing schema initialization...");

        // Check tables exist
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name LIKE 'kc_%'",
                Integer.class);

        System.out.println("Tables found with 'kc_' prefix: " + count);
        assertTrue(count >= 4, "Should have at least 4 tables (users, user_nodes, groups, group_nodes)");

        // List tables
        jdbcTemplate.query(
                "SELECT name FROM sqlite_master WHERE type='table' AND name LIKE 'kc_%'",
                rs -> {
                    System.out.println("  - Table: " + rs.getString("name"));
                });
    }

    @Test
    @Order(2)
    void testCreateGroup() throws Exception {
        System.out.println("Testing group creation...");

        // Create a new group
        CompletableFuture<Group> future = storage.createGroup("admin");
        Group group = future.join();

        assertNotNull(group, "Group should not be null");
        assertEquals("admin", group.getGroupId(), "Group ID should be 'admin'");
        System.out.println("Created group: " + group.getGroupId());
    }

    @Test
    @Order(3)
    void testAddNodesToGroup() throws Exception {
        System.out.println("Testing adding nodes to group...");

        // Load the group
        Group group = storage.loadGroup("admin").join();
        assertNotNull(group, "Group should exist");

        // Add permission node
        Node permNode = PermissionNode.builder("minecraft.command.ban")
                .value(true)
                .build();
        group.getData(DataType.NORMAL).add(permNode);

        // Add weight node
        Node weightNode = WeightNode.builder(100)
                .value(true)
                .build();
        group.getData(DataType.NORMAL).add(weightNode);

        // Add meta node
        Node metaNode = MetaNode.builder("prefix", "&c[Admin]&f")
                .value(true)
                .build();
        group.getData(DataType.NORMAL).add(metaNode);

        // Save group
        storage.saveGroup(group).join();
        System.out.println("Saved group with 3 nodes");

        // Reload and verify
        Group reloaded = storage.loadGroup("admin").join();
        assertNotNull(reloaded, "Reloaded group should not be null");
        assertEquals(3, reloaded.getData(DataType.NORMAL).size(), "Group should have 3 nodes");
        System.out.println("Reloaded group has " + reloaded.getData(DataType.NORMAL).size() + " nodes");
    }

    @Test
    @Order(4)
    void testCreateDefaultGroup() throws Exception {
        System.out.println("Creating default group...");

        Group defaultGroup = storage.createGroup("default").join();
        assertNotNull(defaultGroup, "Default group should not be null");

        // Add some basic permissions
        defaultGroup.getData(DataType.NORMAL).add(
                PermissionNode.builder("minecraft.command.help").value(true).build());
        defaultGroup.getData(DataType.NORMAL).add(
                PermissionNode.builder("minecraft.command.spawn").value(true).build());
        defaultGroup.getData(DataType.NORMAL).add(
                WeightNode.builder(0).value(true).build());

        storage.saveGroup(defaultGroup).join();
        System.out.println("Created default group with basic permissions");
    }

    @Test
    @Order(5)
    void testCreateUser() throws Exception {
        System.out.println("Testing user creation...");

        // Create a new user using SimpleUser, then save
        me.pectics.kernelclaude.perms.model.SimpleUser newUser =
                new me.pectics.kernelclaude.perms.model.SimpleUser("minecraft", "test-player-uuid");
        newUser.setPrimaryGroup("default");

        // Save the new user
        storage.saveUser(newUser).join();
        System.out.println("Created and saved user: " + newUser.getUserId());

        // Now load it back
        User loaded = storage.loadUser("minecraft", "test-player-uuid").join();
        assertNotNull(loaded, "User should be loadable after save");
        assertEquals("minecraft", loaded.getPlatform());
        assertEquals("test-player-uuid", loaded.getNativeId());
        System.out.println("User creation test passed!");
    }

    @Test
    @Order(6)
    void testSaveAndLoadUser() throws Exception {
        System.out.println("Testing save and load user...");

        // Create a new user using SimpleUser directly (for testing)
        me.pectics.kernelclaude.perms.model.SimpleUser user =
                new me.pectics.kernelclaude.perms.model.SimpleUser("minecraft", "player-12345");
        user.setPrimaryGroup("default");

        // Add some nodes
        user.getData(DataType.NORMAL).add(
                PermissionNode.builder("minecraft.command.home").value(true).build());
        user.getData(DataType.NORMAL).add(
                PermissionNode.builder("minecraft.command.sethome").value(true).build());

        // Add group inheritance
        user.getData(DataType.NORMAL).add(
                InheritanceNode.builder("admin").value(true).build());

        // Save user
        storage.saveUser(user).join();
        System.out.println("Saved user: " + user.getUserId());

        // Load user back
        User loaded = storage.loadUser(user.getUserId()).join();
        assertNotNull(loaded, "Loaded user should not be null");
        assertEquals("minecraft", loaded.getPlatform(), "Platform should match");
        assertEquals("player-12345", loaded.getNativeId(), "Native ID should match");
        assertEquals("default", loaded.getPrimaryGroup(), "Primary group should match");
        assertEquals(3, loaded.getData(DataType.NORMAL).size(), "User should have 3 nodes");
        System.out.println("Loaded user has " + loaded.getData(DataType.NORMAL).size() + " nodes");
    }

    @Test
    @Order(7)
    void testGetAllGroupNames() throws Exception {
        System.out.println("Testing get all group names...");

        Set<String> names = storage.getAllGroupNames().join();
        System.out.println("All groups: " + names);

        assertTrue(names.contains("admin"), "Should contain 'admin' group");
        assertTrue(names.contains("default"), "Should contain 'default' group");
        assertEquals(2, names.size(), "Should have exactly 2 groups");
    }

    @Test
    @Order(8)
    void testGetAllUserIds() throws Exception {
        System.out.println("Testing get all user IDs...");

        Set<String> ids = storage.getAllUserIds().join();
        System.out.println("All user IDs: " + ids);

        assertFalse(ids.isEmpty(), "Should have at least one user");
    }

    @Test
    @Order(9)
    void testLoadAllGroups() throws Exception {
        System.out.println("Testing load all groups...");

        Collection<Group> groups = storage.loadAllGroups().join();
        System.out.println("Loaded " + groups.size() + " groups");

        for (Group g : groups) {
            System.out.println("  - " + g.getGroupId() + " (" + g.getData(DataType.NORMAL).size() + " nodes)");
        }

        assertEquals(2, groups.size(), "Should have 2 groups");
    }

    @Test
    @Order(10)
    void testDeleteUser() throws Exception {
        System.out.println("Testing delete user...");

        // First create a user to delete
        me.pectics.kernelclaude.perms.model.SimpleUser user =
                new me.pectics.kernelclaude.perms.model.SimpleUser("test", "to-delete");
        storage.saveUser(user).join();

        String userId = user.getUserId();
        System.out.println("Created user to delete: " + userId);

        // Verify it exists
        User loaded = storage.loadUser(userId).join();
        assertNotNull(loaded, "User should exist before deletion");

        // Delete
        boolean deleted = storage.deleteUser(userId).join();
        assertTrue(deleted, "Delete should return true");

        // Verify it's gone
        User gone = storage.loadUser(userId).join();
        assertNull(gone, "User should be null after deletion");
        System.out.println("User deleted successfully");
    }

    @Test
    @Order(11)
    void testDeleteGroup() throws Exception {
        System.out.println("Testing delete group...");

        // Create a temp group
        Group tempGroup = storage.createGroup("temp-to-delete").join();
        assertNotNull(tempGroup, "Temp group should be created");

        // Delete it
        boolean deleted = storage.deleteGroup("temp-to-delete").join();
        assertTrue(deleted, "Delete should return true");

        // Verify it's gone
        Group gone = storage.loadGroup("temp-to-delete").join();
        assertNull(gone, "Group should be null after deletion");
        System.out.println("Group deleted successfully");
    }
}
