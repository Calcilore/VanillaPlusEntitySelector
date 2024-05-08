package net.calcilore.vanillaplusentityselector;

public class DoubleRange {
    public double min;
    public double max;

    public DoubleRange(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public static DoubleRange parseRange(String str) throws NumberFormatException {
        if (!str.contains("..")) {
            double num = Double.parseDouble(str);
            return new DoubleRange(num, num);
        }

        String[] lr = str.split("\\.\\.");
        if (lr.length != 2) {
            throw new NumberFormatException();
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

    public boolean within(double value) {
        return value >= min && value <= max;
    }
}
