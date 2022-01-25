package gjavac.test.nft.erc721ForeverReward;

/**
 * Description: gjavac
 * Created by moloq on 2021/11/25 9:27
 */
public interface MultiOwnedContractsInterface {
    boolean onERC721Received(String fromAddress, String from, String tokenId, String data);
}
