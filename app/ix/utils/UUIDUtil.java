package ix.utils;

import java.util.regex.Pattern;

/**
 * Created by katzelda on 9/7/16.
 */
public class UUIDUtil {
    private static final Pattern UUID_PATTERN  = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    public static boolean isUUID(String pid){
        return UUID_PATTERN.matcher(pid).find();
    }
}
