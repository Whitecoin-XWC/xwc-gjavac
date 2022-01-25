package gjavac.test.simple2;


import static gjavac.lib.UvmCoreLibs.importContractFromAddress;
import static gjavac.lib.UvmCoreLibs.print;

import gjavac.lib.Contract;
import gjavac.lib.Offline;
import gjavac.lib.UvmContract;


@Contract(storage = Storage.class)
class simpleContractTest extends UvmContract<Storage> {
    @Override
    public void init() {
        print("This is a simple contract testing init()...");
        this.getStorage().name = "A simple testing!";
    }

    @Offline
    public String test(String arg) {
        MultiOwnedContractSimpleInterface multiOwnedContractSimpleInterface = importContractFromAddress(MultiOwnedContractSimpleInterface.class, arg);
        return multiOwnedContractSimpleInterface.ping("");
    }

}