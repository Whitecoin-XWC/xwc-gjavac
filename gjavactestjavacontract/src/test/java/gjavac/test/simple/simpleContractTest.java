package gjavac.test.simple;


import static gjavac.lib.UvmCoreLibs.print;

import gjavac.lib.Contract;
import gjavac.lib.Offline;
import gjavac.lib.UvmContract;
import gjavac.test.dai.DaiPriceFeederForCdcContract;

class Storage {
    public String name; // both field and property supported
}


@Contract(storage = Storage.class)
class simpleContractTest extends UvmContract<Storage> {
    @Override
    public void init() {
        print("This is a simple contract testing init()...");
        this.getStorage().name = "A simple testing!";
    }

    @Offline
    public String ping(String arg) {
        print("Pong!");
        return "Pong!";
    }

    @Offline
    public String getName(String arg) {
        print("getName()!");
        return this.getStorage().name;
    }

}