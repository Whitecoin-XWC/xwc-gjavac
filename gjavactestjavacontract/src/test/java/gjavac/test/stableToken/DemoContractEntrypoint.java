package gjavac.test.stableToken;

import gjavac.lib.UvmContract;

import static gjavac.lib.UvmCoreLibs.print;

public class DemoContractEntrypoint {
    public UvmContract main() {
        print("hello java");
        StableTokenContract contract = new StableTokenContract();
        contract.setStorage(new Storage());
        print(contract);
//        contract.init();

        return contract;
    }
}
