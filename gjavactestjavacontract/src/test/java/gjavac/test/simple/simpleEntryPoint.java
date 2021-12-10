package gjavac.test.simple;

import gjavac.lib.UvmContract;

import static gjavac.lib.UvmCoreLibs.print;

public class simpleEntryPoint {
    public UvmContract main() {
        print("hello java");
        simpleContractTest contract = new simpleContractTest();
        contract.setStorage(new SimpleStorage());
        print(contract);
//        contract.init();

        return contract;
    }
}

