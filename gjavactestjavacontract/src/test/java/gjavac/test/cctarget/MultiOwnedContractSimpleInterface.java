package gjavac.test.cctarget;

/**
 * Description: gjavac
 * Created by moloq on 2021/11/22 17:51
 */
public interface MultiOwnedContractSimpleInterface {
    long balanceOf(String addr);

    boolean mintTo(String addr, long redeemFee);

    void burnFrom(String from, long amount);

    void transfer(String recviceAddress, long amount);
}
