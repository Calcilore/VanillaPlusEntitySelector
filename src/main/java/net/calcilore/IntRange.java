package net.calcilore;

public class IntRange {
    public int min;
    public int max;

    public IntRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public static IntRange parseRange(String str) throws NumberFormatException {
        if (!str.contains("..")) {
            int num = Integer.parseInt(str);
            return new IntRange(num, num);
        }

        String[] lr = str.split("\\.\\.");
        if (lr.length != 2) {
            throw new NumberFormatException();
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

    public boolean within(int value) {
        return value >= min && value <= max;
    }
}

