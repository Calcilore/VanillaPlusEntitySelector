package net.calcilore.vanillaplusentityselector;

public class EntitySelectException extends Exception {
    public static final String consoleLocationException = "You cannot use @p from command console!";

    public EntitySelectException(String errorMessage) {
        super(errorMessage);
    }
}
