package gjavac.test.apicase;

import gjavac.lib.UvmContract;

import static gjavac.lib.UvmCoreLibs.print;

public class ContractEntrypoint {
    public UvmContract main() {
        print("hello java");
        ApiCase contract = new ApiCase();
//        contract.setStorage(new ERC721ForeverRewardStorage());
        print(contract);
//        contract.init();

        return contract;
    }
}
