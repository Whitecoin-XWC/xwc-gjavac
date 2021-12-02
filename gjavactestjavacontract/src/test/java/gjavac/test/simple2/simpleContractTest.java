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

    public String test(String arg) {
        MultiOwnedContractSimpleInterface multiOwnedContractSimpleInterface = importContractFromAddress(MultiOwnedContractSimpleInterface.class, arg);
        return multiOwnedContractSimpleInterface.ping("");
    }

    @Offline
    public String testOffline(String arg) {
        MultiOwnedContractSimpleInterface multiOwnedContractSimpleInterface = importContractFromAddress(MultiOwnedContractSimpleInterface.class, "XWCCTtDKzUKrx6Mr4fNRLdSkMtepRoBw95eQj");
        return multiOwnedContractSimpleInterface.ping("");
    }

    public String test1(String arg) {
        MultiOwnedContractSimpleInterface multiOwnedContractSimpleInterface = importContractFromAddress(MultiOwnedContractSimpleInterface.class, arg);
        return multiOwnedContractSimpleInterface.ping_api("");
    }

    @Offline
    public String test1Offline(String arg) {
        MultiOwnedContractSimpleInterface multiOwnedContractSimpleInterface = importContractFromAddress(MultiOwnedContractSimpleInterface.class, arg);
        return multiOwnedContractSimpleInterface.ping_api("");
    }

}