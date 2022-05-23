package gjavac.test.apicase;

import gjavac.lib.Component;
import gjavac.lib.UvmArray;
import gjavac.lib.UvmStringModule;

import static gjavac.lib.UvmCoreLibs.error;
import static gjavac.lib.UvmCoreLibs.importModule;

/**
 * Description: gjavac
 * Created by moloq on 2022/1/24 14:20
 */
@Component
public class Utils {

    public final UvmArray<String> parseArgs(String arg, int count, String errorMsg) {
        if (isBlank(arg)) {
            error(errorMsg);
            return UvmArray.create();
        }
        UvmStringModule stringModule = importModule(UvmStringModule.class, "string");
        UvmArray<String> parsed = stringModule.split(arg, ",");
        if (parsed != null && parsed.size() == count) {
            return parsed;
        } else {
            error(errorMsg);
            return UvmArray.create();
        }
    }


    public final boolean isBlank(String str) {
        return str == null || str.length() == 0;
    }

    public final boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
