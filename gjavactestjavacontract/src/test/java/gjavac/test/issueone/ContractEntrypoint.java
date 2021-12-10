package gjavac.test.issueone;

import gjavac.lib.UvmContract;
import gjavac.test.stableToken.StableTokenContract;

import static gjavac.lib.UvmCoreLibs.print;

/**
 * Description: gjavac
 * Created by moloq on 2021/12/10 15:56
 */
public class ContractEntrypoint {
    public UvmContract main() {
        print("hello java");
        IssueOneContract contract = new IssueOneContract();
//        contract.setStorage(new Storage());
        print(contract);
//        contract.init();

        return contract;
    }
}
