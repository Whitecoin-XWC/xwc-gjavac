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
        this.getStorage().totalReward = new UvmMap<>();
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

    public void set_test(){
        this.getStorage().totalReward.set("XWC",10L);
    }

    @Offline
    public Long get_test(){
        return this.getStorage().totalReward.get("XWC");
    }

}

class SimpleStorage {
    public String name;
    public UvmMap<Long> totalReward;
}

class simpleEntryPoint {
    public UvmContract main() {
        UvmCoreLibs.print("hello java");
        SimpleContractTest var1 = new SimpleContractTest();
        UvmCoreLibs.print(var1);
        return var1;
    }
}
