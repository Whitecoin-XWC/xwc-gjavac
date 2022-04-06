package gjavac.test.cctarget;

import gjavac.lib.UvmArray;
import gjavac.lib.UvmMap;

/**
 * Description: gjavac
 * Created by moloq on 2022/4/1 9:16
 */
public class Storage {
    public String state;
    public String owner;
    public String witness;
    public String monitor;
    public long eventNonce;
    public boolean _switch;
    public long lastHandledNonce;
    public final long VALID_REMOTE_ADDRESS_LENGTH = 42;
    public UvmMap<String> tokenContracts;
    public UvmMap<Long> tokenCaps;
    public UvmMap<Long> tokenFees;
    public UvmArray<String> allSymbols;
    public UvmMap<Boolean> rollbackNonces;
    public UvmMap<Boolean> handledNonces;
}
