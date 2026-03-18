package me.pectics.kernelclaude.uuid;

import com.fasterxml.uuid.UUIDType;
import com.fasterxml.uuid.impl.NameBasedGenerator;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class UUIDGenerator {

    private static final UUID NAMESPACE = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private static final NameBasedGenerator NAME_BASED_GENERATOR;

    static {
        try {
            NAME_BASED_GENERATOR = new NameBasedGenerator(NAMESPACE, MessageDigest.getInstance("SHA-1"), UUIDType.NAME_BASED_SHA1);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static UUID generate(@NotNull String str) {
        return NAME_BASED_GENERATOR.generate(str);
    }

    public static UUID generate(byte[] bytes) {
        return NAME_BASED_GENERATOR.generate(bytes);
    }

}
