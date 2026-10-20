package dev.nullcraft.spawn;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageTemplate {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([a-z_]+)}");

    private MessageTemplate() {
    }

    /** Replaces {name} placeholders in one pass, so a value is never itself filled in. */
    public static String fill(String template, Map<String, String> values) {
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            String value = values.getOrDefault(matcher.group(1), matcher.group());
            matcher.appendReplacement(out, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(out);
        return out.toString();
    }
}
