package com.hw.validator.simple;

/**
 * 描述信息
 *
 * @author chengwei11
 * @since 2019/1/24
 */

public class CommonRegex {
    public static String email() {
        return "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
    }

    public static String mobile() {
        return "^(1[3-9])\\d{9}$";
    }

    public static String identityCard() {
        return "^\\d{15}|\\d{18}$";
    }

    public static String limitLengthString(int min, int max) {
        return "^.{" + min + "," + max + "}$";
    }

    public static String limitLengthNumber(int min, int max) {
        return "^\\d{" + min + "," + max + "}$";
    }
}
