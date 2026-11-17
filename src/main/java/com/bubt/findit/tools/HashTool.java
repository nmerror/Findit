package com.bubt.findit.tools;

import com.bubt.findit.core.util.PasswordHasher;

/**
 * Small helper that prints the hash + salt for a password.
 *
 * <p>Use it when you want to put a ready-made account into
 * {@code sql/02_seed_data.sql}:
 *
 * <pre>
 *   java -cp target/classes com.bubt.findit.tools.HashTool Admin@1234
 * </pre>
 */
public final class HashTool {

    private HashTool() {
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("usage: HashTool <password>");
            return;
        }
        PasswordHasher.HashedPassword hashed = PasswordHasher.hash(args[0].toCharArray());
        System.out.println("hash = " + hashed.getHash());
        System.out.println("salt = " + hashed.getSalt());
    }
}
