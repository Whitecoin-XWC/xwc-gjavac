package gjavac.test.simple;


import gjavac.lib.Contract;
import gjavac.lib.Offline;
import gjavac.lib.UvmContract;

import static gjavac.lib.UvmCoreLibs.print;


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

    public String pingApi(String arg) {
        print("Pong!");
        return "Pong!";
    }

    @Offline
    public String getName(String arg) {
        print("getName()!");
        return this.getStorage().name;
    }

}