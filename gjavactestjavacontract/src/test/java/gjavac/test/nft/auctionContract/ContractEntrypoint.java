package gjavac.test.nft.auctionContract;

import gjavac.lib.UvmContract;

import static gjavac.lib.UvmCoreLibs.print;

public class ContractEntrypoint {
    public UvmContract main() {
        print("hello java");
        AuctionContract contract = new AuctionContract();
        contract.setStorage(new AuctionStorage());
        print(contract);
//        contract.init();

        return contract;
    }
}
