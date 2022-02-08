package gjavac.test.simple;

import gjavac.lib.Contract;
import gjavac.lib.Offline;
import gjavac.lib.UvmContract;
import gjavac.lib.UvmCoreLibs;

import static gjavac.lib.UvmCoreLibs.print;

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

    @Offline
    public String ping(String var1) {
        UvmCoreLibs.print("Pong!");
        return "Pong!";
    }

    @Offline
    public String getName(String var1) {
        UvmCoreLibs.print("Pong!");
        return this.getStorage().name;
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
