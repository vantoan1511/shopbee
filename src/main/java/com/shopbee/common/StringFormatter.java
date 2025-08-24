/*
 * StringFormatter.java
 *
 * Copyright by shopbee-service, all rights reserved.
 * MIT License: https://mit-license.org
 */

package com.shopbee.common;

/**
 * A utility class for formatting strings with placeholders.
 * <p>
 * This class provides a static method to replace occurrences of {@code {}} in a string
 * with the string representation of provided objects. It is a simple, dependency-free
 * alternative to more complex formatting libraries.
 * </p>
 */
public final class StringFormatter {

    private StringFormatter() {
    }

    /**
     * Replaces placeholders {@code {}} in a message with the string representation of the given parameters.
     * <p>
     * For example, {@code StringFormatter.format("Hello {}, today is {}", "World", new Date())}
     * will produce a string like "Hello World, today is Tue Aug 26 12:00:00 2025".
     * </p>
     * <p>
     * If a parameter is {@code null}, it will be replaced by the string "null".
     * If there are more placeholders than parameters, the remaining placeholders will not be replaced.
     * If there are more parameters than placeholders, the excess parameters will be ignored.
     * </p>
     *
     * @param message    the message template containing {@code {}} placeholders.
     * @param parameters the objects to be inserted into the message.
     * @return the formatted string, or the original message if it's null or no parameters are provided.
     */
    public static String format(String message, Object... parameters) {
        if (message == null || parameters == null) {
            return message;
        }
        StringBuilder sb = new StringBuilder(message);
        for (Object o : parameters) {
            int index = sb.indexOf("{}");
            if (index == -1) {
                break;
            }
            sb.replace(index, index + 2, String.valueOf(o));
        }
        return sb.toString();
    }
}
