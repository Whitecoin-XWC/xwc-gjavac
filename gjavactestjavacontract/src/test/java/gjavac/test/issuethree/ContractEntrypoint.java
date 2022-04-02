package gjavac.test.issuethree;

import gjavac.lib.UvmContract;

import static gjavac.lib.UvmCoreLibs.print;

/**
 * Description: gjavac
 * Created by moloq on 2021/12/10 15:56
 */
public class ContractEntrypoint {
    public UvmContract main() {
        print("hello java");
        IssueThreeContract contract = new IssueThreeContract();
//        contract.setStorage(new Storage());
        print(contract);
//        contract.init();

        return contract;
    }
}
