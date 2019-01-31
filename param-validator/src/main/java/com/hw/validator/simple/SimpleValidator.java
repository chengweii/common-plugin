package com.hw.validator.simple;

import com.sun.istack.internal.Nullable;

/**
 * 简单参数验证器
 *
 * @author chengwei11
 * @date 2019/1/24
 */
public class SimpleValidator {
    private static final String DEFAULT_ERROR_MSG = "非法输入参数";

    public static void checkArgument(boolean expression, String errorMsgTemplate, Object... args) {
        if (!expression) {
            String errorMsg = errorMsgTemplate;
            if (args != null) {
                errorMsg = format(errorMsgTemplate, args);
            }
            throw new SimpleIllegalArgumentException(errorMsg);
        }
    }

    public static void checkArgument(boolean expression) {
        if (!expression) {
            throw new SimpleIllegalArgumentException(DEFAULT_ERROR_MSG);
        }
    }

    public static void checkArgument(Checker checker, String errorMsgTemplate, Object... args) {
        if (!checker.check()) {
            String errorMsg = errorMsgTemplate;
            if (args != null) {
                errorMsg = format(errorMsgTemplate, args);
            }
            throw new SimpleIllegalArgumentException(errorMsg);
        }
    }

    public static void checkArgument(Checker checker) {
        if (!checker.check()) {
            throw new SimpleIllegalArgumentException(DEFAULT_ERROR_MSG);
        }
    }

    public static <T> void checkArgument(String value, String regex, String errorMsgTemplate, Object... args) {
        if (!value.matches(regex)) {
            String errorMsg = errorMsgTemplate;
            if (args != null) {
                errorMsg = format(errorMsgTemplate, args);
            }
            throw new SimpleIllegalArgumentException(errorMsg);
        }
    }

    public static <T> void checkArgument(T value, String regex) {
        if (!value.toString().matches(regex)) {
            throw new SimpleIllegalArgumentException(DEFAULT_ERROR_MSG);
        }
    }

    @FunctionalInterface
    public interface Checker {
        boolean check();
    }

    private static String format(String template, @Nullable Object... args) {
        template = String.valueOf(template);
        StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
        int templateStart = 0;

        int i;
        int placeholderStart;
        for (i = 0; i < args.length; templateStart = placeholderStart + 2) {
            placeholderStart = template.indexOf("%s", templateStart);
            if (placeholderStart == -1) {
                break;
            }

            builder.append(template.substring(templateStart, placeholderStart));
            builder.append(args[i++]);
        }

        builder.append(template.substring(templateStart));
        if (i < args.length) {
            builder.append(" [");
            builder.append(args[i++]);

            while (i < args.length) {
                builder.append(", ");
                builder.append(args[i++]);
            }

            builder.append(']');
        }

        return builder.toString();
    }
}
