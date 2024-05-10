package net.calcilore.vanillaplusentityselector.Internal;

import net.calcilore.vanillaplusentityselector.EntitySelectException;

public class DoubleRange {
    public double min;
    public double max;

    public DoubleRange(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public static DoubleRange parseRange(String str) throws EntitySelectException {
        try {
            if (!str.contains("..")) {
                double num = Double.parseDouble(str);
                return new DoubleRange(num, num);
            }

            String[] lr = str.split("\\.\\.", -1);
            if (lr.length != 2) {
                throw new EntitySelectException("Range must contain 1 '..' separating the min from the max!");
            }

            // Double.MIN_VALUE and Double.MAX_VALUE doesn't work for some reason, so biiig numbers
            DoubleRange range = new DoubleRange(-6000000000d, 6000000000d);
            if (!lr[0].isEmpty()) {
                range.min = Double.parseDouble(lr[0]);
            }

            if (!lr[1].isEmpty()) {
                range.max = Double.parseDouble(lr[1]);
            }

            return range;
        }
        catch (NumberFormatException e) {
            throw new EntitySelectException("Invalid range! Must be a number!");
        }
    }

    public boolean within(double value) {
        return value >= min && value <= max;
    }
}
