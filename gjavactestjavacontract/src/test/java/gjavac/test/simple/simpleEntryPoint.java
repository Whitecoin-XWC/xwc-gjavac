package gjavac.test.simple;

import gjavac.lib.UvmContract;
import gjavac.test.simple.simpleContractTest;

import static gjavac.lib.UvmCoreLibs.print;

public class simpleEntryPoint {
    public UvmContract main() {
        print("hello java");
        simpleContractTest contract = new simpleContractTest();
        contract.setStorage(new Storage());
        print(contract);
//        contract.init();

        return contract;
    }
}

