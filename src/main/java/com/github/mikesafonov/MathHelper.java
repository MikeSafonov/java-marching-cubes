package com.github.mikesafonov;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MathHelper {
    public static double log2(int n) {
        return (Math.log(n) / Math.log(2));
    }
}
