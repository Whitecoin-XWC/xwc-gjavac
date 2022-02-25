package gjavac.test.erc20;

import gjavac.lib.UvmContract;

import static gjavac.lib.UvmCoreLibs.print;

public class ContractEntrypoint {
    public UvmContract main() {
        print("hello java");
        ERC20Contract contract = new ERC20Contract();
//        contract.setStorage(new AuctionStorage());
        print(contract);
//        contract.init();

        return contract;
    }
}
