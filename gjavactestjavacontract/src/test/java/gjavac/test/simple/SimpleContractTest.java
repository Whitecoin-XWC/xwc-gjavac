package gjavac.test.simple;

import gjavac.lib.*;

import java.util.ArrayList;
import java.util.HashMap;

import static gjavac.lib.UvmCoreLibs.print;
import static gjavac.lib.UvmCoreLibs.tojsonstring;

/**
 * Description: gjavac
 * Created by moloq on 2021/12/24 17:34
 */

@Contract(
        storage = SimpleStorage.class
)
public class SimpleContractTest extends UvmContract<SimpleStorage> {
    @Override
    public void init() {
        print("This is a simple contract testing init()...");
        this.getStorage().name = "A simple testing!";
    }

//    @Offline
//    public String ping(String var1) {
//        UvmCoreLibs.print("Pong!");
//        return "Pong!";
//    }
//
//    @Offline
//    public String getName(String var1) {
//        UvmCoreLibs.print("Pong!");
//        return this.getStorage().name;
//    }

    @Offline
    public Object array(String arg) {
////        UvmStringModule stringModule = (UvmStringModule) UvmCoreLibs.importModule(UvmStringModule.class, "string");
////        UvmArray parsed = stringModule.split(arg, ",");
////        UvmMap map = UvmMap.create();
////        map.set("0", parsed.get(0));
////        map.set("1", parsed.get(1));
////        map.set("2", parsed.get(2));
//        UvmArray uvmArray = UvmArray.create();
//        uvmArray.set(100,100);
//        return uvmArray.get(100);
        ArrayList arrayList = new ArrayList();
        arrayList.add(1);
        return null;

    }


}

class SimpleStorage {
    public String name;
}

class simpleEntryPoint {
    public UvmContract main() {
        UvmCoreLibs.print("hello java");
        SimpleContractTest var1 = new SimpleContractTest();
        UvmCoreLibs.print(var1);
        return var1;
    }
}
