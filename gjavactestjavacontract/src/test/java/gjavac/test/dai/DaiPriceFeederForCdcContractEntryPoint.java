package gjavac.test.dai;

import gjavac.lib.UvmContract;

import static gjavac.lib.UvmCoreLibs.print;

public class DaiPriceFeederForCdcContractEntryPoint {
    public UvmContract main() {
        print("hello java");
        DaiPriceFeederForCdcContract contract = new DaiPriceFeederForCdcContract();
        contract.setStorage(new Storage());
        print(contract);
        //contract.init();

        return contract;
    }
}
