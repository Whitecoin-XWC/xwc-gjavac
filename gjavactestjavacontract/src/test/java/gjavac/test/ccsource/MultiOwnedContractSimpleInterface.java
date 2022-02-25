package gjavac.test.ccsource;

/**
 * Description: gjavac
 * Created by moloq on 2021/11/22 17:51
 */
public interface MultiOwnedContractSimpleInterface {
    long balanceOf(String addr);
    long allowance(String fromAddr,String contractAddr);
    void safeTransferFrom(String from,String to,long amount);
    void safeTransfer(String recviceAddress, long amount);
}
