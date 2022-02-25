package gjavac.test.ccsource;

import gjavac.lib.*;

import static gjavac.lib.UvmCoreLibs.*;
import static gjavac.lib.UvmCoreLibs.importContractFromAddress;

/**
 * Description: gjavac
 * Created by moloq on 2022/2/21 14:28
 */
@Contract(storage = Storage.class)
public class SourceContract extends UvmContract<Storage> {
    @Override
    public void init() {
        Storage storage = this.getStorage();
        storage.state = "NOT_INITED";
        storage._owner = caller_address();
        storage._witness = caller_address();
        storage._monitor = caller_address();
        storage._switch = true;
        storage._lastHandledNonce = 0L;
        storage._eventNonce = 0L;
        storage.nativeSymbol = "ETH";
        storage.nativeCap = 100 * 1000000000000000000L;
        storage.tokenContracts = UvmMap.create();
        storage.tokenCaps = UvmMap.create();
        storage.rollbackNonces = UvmMap.<Boolean>create();
        storage.handledNonces = UvmMap.<Boolean>create();
    }

    /* =========================== offline =========================== */
    @Offline
    public void setNativeSymbol(String _newSymbol) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        this.getStorage().nativeSymbol = _newSymbol;
    }

    @Offline
    public String getTokenContract(String _symbol) {
        return tostring(this.getStorage().tokenContracts.get(_symbol));
    }

    @Offline
    public long getTokenCap(String _symbol) {
        return tointeger(this.getStorage().tokenCaps.get(_symbol));
    }

    /* ========================== offline ============================= */

    public void setNativeCap(long _newCap) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.checkState(this);
        long old_cap = this.getStorage().nativeCap;
        this.getStorage().nativeCap = _newCap;
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("nativeSymbol", this.getStorage().nativeSymbol);
        uvmMap.set("old_cap", old_cap);
        uvmMap.set("_newCap", _newCap);
        emit("TokenCapChanged", tojsonstring(uvmMap));
    }

    public void setTokenCap(String arg) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.checkState(this);
        UvmArray<String> parsed = utils.parseArgs(arg, 2, "need arg symbol,newCap");
        String _symbol = parsed.get(1);
        long _newCap = tointeger(parsed.get(2));
        long old_cap = tointeger(this.getStorage().tokenCaps.get(_symbol));
        this.getStorage().tokenCaps.set(_symbol, _newCap);
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("_symbol", _symbol);
        uvmMap.set("old_cap", old_cap);
        uvmMap.set("_newCap", _newCap);
        emit("TokenCapChanged", tojsonstring(uvmMap));
    }

    public void regTokenContract(String arg) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.checkState(this);
        UvmArray<String> parsed = utils.parseArgs(arg, 3, "need _symbol,_contract,_cap");
        String _symbol = parsed.get(1);
        String _contract = parsed.get(2);
        long _cap = tointeger(parsed.get(3));
        utils.require(_symbol.length() > 0 && _symbol.length() < 10, "Symbol can't be null or longer than 9 string!");
        utils.require(this.getStorage().nativeSymbol == _symbol, "Native symbol is reserved!");
        utils.require(is_valid_contract_address(_contract), "Isn't a contract address!");
        utils.require(this.getStorage().tokenContracts.get(_symbol) == _contract, "Token contract with same symbol has been registered!");
        this.getStorage().tokenContracts.set(_symbol, _contract);
        this.getStorage().tokenCaps.set(_symbol, _cap);
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("_symbol", _symbol);
        uvmMap.set("_contract", _contract);
        uvmMap.set("_cap", _cap);
        emit("TokenRegistered", tojsonstring(uvmMap));
        uvmMap.set("_contract", "0");
        emit("TokenCapChanged", tojsonstring(uvmMap));
    }

    public void lockAsset(String arg) {
        Utils utils = new Utils();
        utils.switchOn(this);
        utils.checkState(this);
        UvmArray<String> parsed = utils.parseArgs(arg, 3, "need _remoteAddress,_symbol,_amount");
        String _remoteAddress = parsed.get(1);
        String _symbol = parsed.get(2);
        long _amount = tointeger(parsed.get(3));
        utils.require(_remoteAddress.length() == this.getStorage().VALID_REMOTE_ADDRESS_LENGTH, "Invalid length of remote address!");
        utils.require(_amount > 0, "Can't lock 0 amount!");
        if (this.getStorage().nativeSymbol == _symbol) {

            // Make sure there is some native asset to lock
            String from_address = utils.get_from_address();
            if (from_address != caller_address()) {
                error("only common user account can lock balance");
            }
            long balance = get_contract_balance_amount(caller_address(), _symbol);
            if (tointeger(balance) <= getStorage().nativeCap) {
                error("Exceed native asset lock limit!");
            }
        } else {
            String tokenContract = tostring(this.getStorage().tokenContracts.get(_symbol));
            utils.require(is_valid_address(tokenContract), "Token contract hasn't registered!");
//
            utils.require(_amount == 0, "Token contract can't receive the native amount");
            MultiOwnedContractSimpleInterface multiOwnedContractSimpleInterface = importContractFromAddress(MultiOwnedContractSimpleInterface.class, tokenContract);
            utils.require(multiOwnedContractSimpleInterface.balanceOf(utils.get_from_address()) >= _amount, "Insufficient balance!");
            utils.require(multiOwnedContractSimpleInterface.allowance(utils.get_from_address(), caller_address()) >= _amount, "Insufficient allowance!");
            utils.require(multiOwnedContractSimpleInterface.balanceOf(caller_address()) + _amount <= tointeger(this.getStorage().tokenCaps.get(_symbol)), "Exceed token lock limit!");
            multiOwnedContractSimpleInterface.safeTransferFrom(utils.get_from_address(), caller_address(), _amount);
        }
        this.getStorage()._eventNonce = this.getStorage()._eventNonce + 1;
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("_eventNonce", this.getStorage()._eventNonce);
        uvmMap.set("from", utils.get_from_address());
        uvmMap.set("to", _remoteAddress);
        uvmMap.set("symbol", _symbol);
        uvmMap.set("amount", _amount);
        emit("AssetLocked", tojsonstring(uvmMap));

    }

    public void releaseAsset(String arg) {
        Utils utils = new Utils();
        utils.checkState(this);
        UvmArray<String> parsed = utils.parseArgs(arg, 5, "need _eventNonce,_localAddress,_remoteAddress,_symbol and _amount");
        String _eventNonce = parsed.get(1);
        utils.require(!this.getStorage().handledNonces.get(_eventNonce), "Event was already handled!");
        String _localAddress = parsed.get(2);
        String _remoteAddress = parsed.get(3);
        String _symbol = parsed.get(4);
        long _amount = tointeger(parsed.get(5));
        utils._releaseAsset(this, _localAddress, _symbol, _amount);
        this.getStorage()._lastHandledNonce = tointeger(_eventNonce);
        this.getStorage().handledNonces.set(_eventNonce, true);
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("_eventNonce", _eventNonce);
        uvmMap.set("_localAddress", _localAddress);
        uvmMap.set("_remoteAddress", _remoteAddress);
        uvmMap.set("_symbol", _symbol);
        uvmMap.set("_amount", _amount);
        emit("AssetReleased", tojsonstring(uvmMap));
    }

    public void rollBackToken(String arg) {
        Utils utils = new Utils();
        utils.switchOn(this);
        utils.onlyWitness(this);
        utils.checkState(this);
        UvmArray<String> parsed = utils.parseArgs(arg, 4, "need _eventNonce,_recviceAddress,_symbol and _amount");
        String _eventNonce = parsed.get(1);
        utils.require(this.getStorage().rollbackNonces.get(_eventNonce), "Event was already handled!");
        String _recviceAddress = parsed.get(2);
        String _symbol = parsed.get(3);
        long _amount = tointeger(parsed.get(4));
        utils._releaseAsset(this, _recviceAddress, _symbol, _amount);
        this.getStorage().handledNonces.set(_eventNonce, true);
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("_eventNonce", _eventNonce);
        uvmMap.set("_recviceAddress", _recviceAddress);
        uvmMap.set("_symbol", _symbol);
        uvmMap.set("_amount", _amount);
        emit("AssetLockRollback", tojsonstring(uvmMap));
    }

    public void pause() {
        Utils utils = new Utils();
        utils.check_caller_frame_valid(this);
        if (this.getStorage().state == "STOPPED") {
            error("this contract stopped now, can't pause");
        }
        if (this.getStorage().state == "PAUSED") {
            error("this contract paused now, can't pause");
        }
        utils.checkAdmin(this);
        this.getStorage().state = "PAUSED";
        emit("Paused", "");
    }

    public void resume() {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        if (this.getStorage().state != "PAUSED") {
            error("this contract not paused now, can't resume");
        }
        utils.checkAdmin(this);
        this.getStorage().state = "COMMON";
        emit("Resumed", "");
    }

    public void stop() {
        Utils utils = new Utils();
        utils.check_caller_frame_valid(this);
        if (this.getStorage().state == "STOPPED") {
            error("this contract stopped now, can't stop");
        }
        if (this.getStorage().state == "PAUSED") {
            error("this contract paused now, can't stop");
        }
        utils.checkAdmin(this);
        this.getStorage().state = "STOPPED";
        emit("Stopped", "");
    }
}
