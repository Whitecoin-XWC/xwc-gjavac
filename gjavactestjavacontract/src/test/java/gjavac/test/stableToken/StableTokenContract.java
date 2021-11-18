package gjavac.test.stableToken;

import gjavac.lib.Contract;
import gjavac.lib.Offline;

import gjavac.lib.*;
import gjavac.test.stableToken.Storage;

import static gjavac.lib.UvmCoreLibs.*;
import static gjavac.lib.UvmCoreLibs.caller_address;

public class StableTokenContract extends UvmContract<Storage>{
    @Override
    public void init() {
        print("token contract creating...");
        this.getStorage().name = "";
        this.getStorage().symbol = "";
        this.getStorage().supply = 0;
        this.getStorage().precision = 0;
        this.getStorage().state = "NOT_INITED";
        this.getStorage().admin = caller_address();
        this.getStorage().minter = "";
        this.getStorage().allowLock = false;
        this.getStorage().fee = 0;
        this.getStorage().minTransferAmount = 0;
        this.getStorage().feeReceiveAddress = caller_address();
        print("token contract created!");
    }

    @Override
    public void on_deposit(long amount) {

    }
}
