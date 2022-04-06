package gjavac.test.cctarget;

import gjavac.lib.UvmContract;

import static gjavac.lib.UvmCoreLibs.print;

public class ContractEntrypoint {
    public UvmContract main() {
        print("hello java");
        TargetContract contract = new TargetContract();
//        contract.setStorage(new Storage());
        print(contract);
//        contract.init();

        return contract;
    }
}
