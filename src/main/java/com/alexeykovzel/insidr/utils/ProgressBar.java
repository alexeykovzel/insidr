package com.alexeykovzel.insidr.utils;

public class ProgressBar {
    private static final int TEXT_SIZE = 40;
    private static final int BAR_SIZE = 20;
    private final String text;
    private final double total;

    public ProgressBar(String text, double total) {
        this.text = text + " ".repeat(TEXT_SIZE - text.length());
        this.total = total;
        update(0);
    }

    public void update(double current) {
        double share = (total != 0) ? current / total : 1;
        int nums = (int) Math.round(BAR_SIZE * share);
        String signs = "#".repeat(nums) + " ".repeat(BAR_SIZE - nums);
        System.out.printf(text + "[%s] %d%%\r", signs, Math.round(share * 100));
        if (current == total) System.out.println();
    }
}
