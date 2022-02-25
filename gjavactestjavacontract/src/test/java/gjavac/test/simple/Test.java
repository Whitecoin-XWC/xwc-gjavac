package gjavac.test.simple;

import gjavac.lib.UvmArray;
import gjavac.lib.UvmCoreLibs;
import gjavac.lib.UvmStringModule;

/**
 * Description: gjavac
 * Created by moloq on 2022/2/16 14:08
 */
public class Test {
    public static void main(String[] args) {
        UvmStringModule stringModule = (UvmStringModule) UvmCoreLibs.importModule(UvmStringModule.class, "string");
        UvmArray parsed = stringModule.split("haha,hehe", ",");
        System.out.println(parsed.get(0));
    }
}
