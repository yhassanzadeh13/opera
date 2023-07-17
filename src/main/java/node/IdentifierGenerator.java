package node;

import java.util.HashSet;
import java.util.Random;

/**
 * Generates unique identifiers.
 */
public class IdentifierGenerator {

    private static final Random random = new Random();
    /**
     * Keeps track of all generated identifiers to ensure uniqueness.
     */
    private static final HashSet<Identifier> generatedIdentifiers = new HashSet<>();

    /**
     * Generates a random and unique identifier.
     *
     * @return random and unique identifier.
     */
    public static Identifier newIdentifier() {
        Identifier identifier = null;

        do {
            identifier = generateIdentifier();
        } while (generatedIdentifiers.contains(identifier));

        generatedIdentifiers.add(identifier);
        return identifier;
    }

    /**
     * Generates a random identifier.
     *
     * @return random identifier.
     */
    private static Identifier generateIdentifier() {
        byte[] arr = new byte[Identifier.Size];
        random.nextBytes(arr);
        return new Identifier(arr);
    }
}
