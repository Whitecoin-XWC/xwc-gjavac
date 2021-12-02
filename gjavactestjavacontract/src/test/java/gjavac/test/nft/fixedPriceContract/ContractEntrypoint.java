package gjavac.test.nft.fixedPriceContract;

import gjavac.lib.UvmContract;

import static gjavac.lib.UvmCoreLibs.print;

public class ContractEntrypoint {
    public UvmContract main() {
        print("hello java");
        FixedPriceContract contract = new FixedPriceContract();
        contract.setStorage(new Storage());
        print(contract);
//        contract.init();

        return contract;
    }
}
