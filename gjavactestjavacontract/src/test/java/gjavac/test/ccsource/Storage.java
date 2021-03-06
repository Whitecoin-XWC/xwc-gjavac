package gjavac.test.ccsource;

import gjavac.lib.UvmMap;

/**
 * Description: gjavac
 * Created by moloq on 2022/2/21 14:24
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
    public String nativeSymbol;
    public long nativeCap;
    public UvmMap tokenContracts;
    public UvmMap<Long> tokenCaps;
    public UvmMap<Boolean> rollbackNonces;
    public UvmMap<Boolean> handledNonces;
}
