package gjavac.test.nft.erc721;

import gjavac.lib.UvmContract;

import static gjavac.lib.UvmCoreLibs.print;

public class ContractEntrypoint {
    public UvmContract main() {
        print("hello java");
        ERC721Contract contract = new ERC721Contract();
//        contract.setStorage(new ERC721Storage());
        print(contract);
//        contract.init();

        return contract;
    }
}
