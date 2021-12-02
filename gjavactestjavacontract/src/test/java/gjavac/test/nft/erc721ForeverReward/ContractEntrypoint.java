package gjavac.test.nft.erc721ForeverReward;

import gjavac.lib.UvmContract;

import static gjavac.lib.UvmCoreLibs.print;

public class ContractEntrypoint {
    public UvmContract main() {
        print("hello java");
        ERC721ForeverRewardContract contract = new ERC721ForeverRewardContract();
        contract.setStorage(new ERC721ForeverRewardStorage());
        print(contract);
//        contract.init();

        return contract;
    }
}
