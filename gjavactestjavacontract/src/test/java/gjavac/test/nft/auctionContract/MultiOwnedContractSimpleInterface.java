package gjavac.test.nft.auctionContract;

/**
 * Description: gjavac
 * Created by moloq on 2021/11/22 17:51
 */
public interface MultiOwnedContractSimpleInterface {
    boolean supportsERC721Interface();

    String queryTokenMinter(String tokenId);

    void safeTransferFrom(String token);

    String ownerOf(String tokenId);

    String getApproved(String tokenId);

    void transferFrom(String token);

    void feedTradePrice(String s);
}
