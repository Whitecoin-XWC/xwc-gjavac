package gjavac.test.ccsource;

import gjavac.lib.UvmContract;

import static gjavac.lib.UvmCoreLibs.print;

public class ContractEntrypoint {
    public UvmContract main() {
        print("hello java");
        SourceContract contract = new SourceContract();
//        contract.setStorage(new ERC721ForeverRewardStorage());
        print(contract);
//        contract.init();

        return contract;
    }
}
