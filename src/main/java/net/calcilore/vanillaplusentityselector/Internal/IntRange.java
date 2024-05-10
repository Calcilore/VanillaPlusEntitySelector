package net.calcilore.vanillaplusentityselector.Internal;

import net.calcilore.vanillaplusentityselector.EntitySelectException;

public class IntRange {
    public int min;
    public int max;

    public IntRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public static IntRange parseRange(String str) throws EntitySelectException {
        try {
            if (!str.contains("..")) {
                int num = Integer.parseInt(str);
                return new IntRange(num, num);
            }

            String[] lr = str.split("\\.\\.", -1);
            if (lr.length != 2) {
                throw new EntitySelectException("Range must contain 1 '..' separating the min from the max!");
            }

            IntRange range = new IntRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (!lr[0].isEmpty()) {
                range.min = Integer.parseInt(lr[0]);
            }

            if (!lr[1].isEmpty()) {
                range.max = Integer.parseInt(lr[1]);
            }

            return range;
        }
        catch (NumberFormatException e) {
            throw new EntitySelectException("Invalid range! Must be an integer!");
        }
    }

    public boolean within(int value) {
        return value >= min && value <= max;
    }
}

