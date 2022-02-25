package gjavac.test.erc20;

import gjavac.lib.*;

import static gjavac.lib.UvmCoreLibs.*;

/**
 * Description: gjavac
 * Created by moloq on 2022/2/8 14:47
 */
@Contract(storage = Storage.class)
public class ERC20Contract extends UvmContract<Storage> {
    @Override
    public void init() {
        Storage storage = this.getStorage();
        storage.name = "";
        storage.symbol = "";
        storage.supply = 0L;
        storage.precision = 0L;
        storage.state = "NOT_INITED";
        storage.admin = caller_address();
        storage.allowLock = false;
        storage.fee = 0L;
        storage.minTransferAmount = 0L;
        storage.feeReceiveAddress = caller_address();

    }


    /* ========================== off line api ======================== */
    @Offline
    public String state() {
        return this.getStorage().state;
    }

    @Offline
    public String tokenName() {
        Utils utils = new Utils();
        utils.checkStateInited(this);
        return this.getStorage().name;
    }

    @Offline
    public long precision() {
        Utils utils = new Utils();
        utils.checkStateInited(this);
        return this.getStorage().precision;
    }

    @Offline
    public String tokenSymbol() {
        Utils utils = new Utils();
        utils.checkStateInited(this);
        return this.getStorage().symbol;
    }

    @Offline
    public String admin() {
        Utils utils = new Utils();
        utils.checkStateInited(this);
        return this.getStorage().admin;
    }

    @Offline
    public long totalSupply() {
        Utils utils = new Utils();
        utils.checkStateInited(this);
        return this.getStorage().supply;
    }

    @Offline
    public String isAllowLock() {
        return tostring(this.getStorage().allowLock);
    }

    @Offline
    public String fee() {
        return tostring(this.getStorage().fee);
    }

    @Offline
    public String minTransferAmount() {
        return tostring(this.getStorage().minTransferAmount);
    }

    @Offline
    public String feeReceiveAddress() {
        return this.getStorage().feeReceiveAddress;
    }

    @Offline
    public String balanceOf(String owner) {
        Utils utils = new Utils();
        utils.checkStateInited(this);
        if (owner == null || owner.length() < 1) {
            error("arg error, need owner address as argument");
        }
        utils.checkAddress(owner);
        long amount = getBalanceOfUser(owner);
        return tostring(amount);
    }

    @Offline
    public void users(String arg) {
        error("not implemented, you can find users from contract transaction history");
    }

    /**
     * @param arg arg format: spenderAddress,authorizerAddress
     * @return
     */
    @Offline
    public String approvedBalanceFrom(String arg) {
        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        Utils utils = new Utils();
        UvmArray<String> parsed = utils.parse_at_least_args(arg, 2, "argument format error, need format is spenderAddress,authorizerAddress");
        String spender = parsed.get(1);
        String authorizer = parsed.get(2);
        utils.checkAddress(spender);
        utils.checkAddress(authorizer);
        String allowedDataStr = tostring(fast_map_get("allowed", authorizer));
        if (allowedDataStr == null) {
            return "0";
        }
        UvmMap allowedData = (UvmMap) totable(json.loads(tostring(allowedDataStr)));
        if (allowedData == null) {
            return "0";
        }
        Object allowedAmount = allowedData.get("spender");
        if (allowedAmount == null) {
            return "0";
        }
        return tostring(allowedAmount);

    }

    @Offline
    public String allApprovedFromUser(String arg) {
        Utils utils = new Utils();
        String authorizer = arg;
        utils.checkAddress(authorizer);
        Object allowedDataStr = fast_map_get("allowed", authorizer);
        if (allowedDataStr == null) {
            return "{}";
        }
        return tostring(allowedDataStr);
    }

    @Offline
    public String lockedBalanceOf(String owner) {
        if (fast_map_get("lockedAmounts", owner) == null) {
            return "0,0";
        } else {
            return tostring(fast_map_get("lockedAmounts", owner));
        }

    }


    /* =========================================================================== */

    /**
     * @param arg name,symbol,supply,precision
     */
    public void init_token(String arg) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.check_caller_frame_valid(this);
        Storage storage = this.getStorage();
        if (storage.state != "NOT_INITED") {
            error("this token contract inited before");
        }
        UvmArray<String> parsed = utils.parse_args(arg, 4, "argument format error, need format: name,symbol,supply,precision");
        if (parsed.get(1) == null) {
            error("name needed");
        }
        storage.name = parsed.get(1);
        if (parsed.get(2) == null) {
            error("symbol needed");
        }
        storage.symbol = parsed.get(2);
        if (parsed.get(3) == null) {
            error("supply needed");
        }
        long supply = tointeger(parsed.get(3));
        if (supply <= 0) {
            error("supply must be positive integer");
        }
        storage.supply = supply;

        String from_address = utils.get_from_address();
        if (from_address != caller_address()) {
            error("init_token can't be called from other contract");
        }
        fast_map_set("users", caller_address(), tointeger(parsed.get(3)));
        if (parsed.get(4) == null) {
            error("precision needed");
        }
        long precision = tointeger(parsed.get(4));
        if (precision <= 0) {
            error("precision must be positive integer");
        }
        storage.precision = precision;
        storage.state = "COMMON";
        UvmArray allowedPrecisions = UvmArray.create();
        allowedPrecisions.add(1);
        allowedPrecisions.add(10);
        allowedPrecisions.add(100);
        allowedPrecisions.add(1000);
        allowedPrecisions.add(10000);
        allowedPrecisions.add(100000);
        allowedPrecisions.add(1000000);
        allowedPrecisions.add(10000000);
        allowedPrecisions.add(100000000);
        if (!utils.arrayContains(allowedPrecisions, precision)) {
            error("precision can only be positive integer in " + tojsonstring(allowedPrecisions));
        }
        emit("Inited", tostring(supply));
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("from", null);
        uvmMap.set("to", caller_address());
        uvmMap.set("amount", supply);
        uvmMap.set("fee", 0);
        uvmMap.set("memo", "Init");
        emit("Transfer", tojsonstring(uvmMap));
    }


    public void setFee(String feeStr) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.checkState(this);
        long fee = tointeger(feeStr);
        if (fee < 0) {
            error("error fee format");
        }
        this.getStorage().fee = fee;
        emit("FeeChanged", feeStr);
    }

    /**
     * @param arg to_address,integer_amount
     */
    public void issue(String arg) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.checkState(this);
        UvmArray<String> parsed = utils.parse_args(arg, 2, "argument format error, need format is to_address,integer_amount");
        String to = parsed.get(1);
        long amount = tointeger(parsed.get(2));
        if (to == null || to.length() < 1) {
            error("to address format error");
        }
        if (amount < 1) {
            error("amount format error");
        }
        utils.checkAddress(to);
        long to_balance = fast_map_get("users", to) == null ? 0 : tointeger(fast_map_get("users", to));
        if (to_balance + amount < 0) {
            error("amount overflow");
        }

        fast_map_set("users", to, to_balance + amount);
        long supplyOld = this.getStorage().supply;
        long supply = supplyOld + amount;
        if (supply < supplyOld || supply < amount || supply < 0) {
            error("supply overflow");
        }
        this.getStorage().supply = supply;
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("from", caller_address());
        uvmMap.set("to", to);
        uvmMap.set("amount", amount);
        emit("Issued", tojsonstring(uvmMap));
    }

    /**
     * @param arg amount
     */
    public void burn(String arg) {
        Utils utils = new Utils();
        utils.checkState(this);
        long amount = tointeger(arg);
        if (amount < 1) {
            error("amount format error");
        }
        String from = utils.get_from_address();
        long from_balance = fast_map_get("users", from) == null ? 0 : tointeger(fast_map_get("users", from));
        if (from_balance < amount) {
            error("Insufficient balance to destroy");
        }

        if (from_balance - amount < 0) {
            error("amount overflow");
        }

        fast_map_set("users", from, from_balance - amount);
        long supplyOld = this.getStorage().supply;
        long supply = supplyOld - amount;
        if (supply < supplyOld || supply < amount || supply < 0) {
            error("supply overflow");
        }
        this.getStorage().supply = supply;
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("from", caller_address());
        uvmMap.set("amount", amount);
        emit("Burned", tojsonstring(uvmMap));
    }

    public void setMinTransferAmount(String minTransferAmountStr) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.checkState(this);
        long minTransferAmount = tointeger(minTransferAmountStr);
        if (minTransferAmount < 0) {
            error("error minTransferAmount format");
        }
        this.getStorage().minTransferAmount = minTransferAmount;
        emit("MinTransferAmountChanged", minTransferAmountStr);
    }

    public void setFeeReceiveAddress(String feeReceiveAddress) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.checkState(this);
        if (!is_valid_address(feeReceiveAddress)) {
            error("invalid address");
        }

        if (is_valid_contract_address(feeReceiveAddress)) {
            error("can't use contract address");
        }

        this.getStorage().feeReceiveAddress = feeReceiveAddress;
        emit("FeeReceiveAddressChanged", feeReceiveAddress);
    }

    public void openAllowLock() {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.checkState(this);
        utils.checkState(this);
        if (this.getStorage().allowLock) {
            error("this contract had been opened allowLock before");
        }
        this.getStorage().allowLock = true;
        emit("AllowedLock", "");
    }

    public long getBalanceOfUser(String addr) {
        return fast_map_get("users", addr) == null ? 0 : tointeger(fast_map_get("users", addr));
    }

    /**
     * @param arg to_address,integer_amount
     */
    public void transfer(String arg) {
        Utils utils = new Utils();
        utils.checkState(this);
        utils.checkState(this);
        UvmArray<String> parsed = utils.parse_at_least_args(arg, 2, "argument format error, need format is to_address,integer_amount[,memo]");
        String to = parsed.get(1);
        long amount = tointeger(parsed.get(2));
        String memo = null;
        if (parsed.size() >= 3) {
            memo = parsed.get(3);
        }

        if (to == null || to.length() < 1) {
            error("to address format error");
        }

        long fee = this.getStorage().fee;
        long minTransferAmount = this.getStorage().minTransferAmount;
        String feeReceiveAddress = this.getStorage().feeReceiveAddress;
        if (amount < 1) {
            error("amount format error");
        }
        if (amount < fee) {
            error("amount not enough for fee");
        }
        if (amount < minTransferAmount) {
            error("only transfer amount >= " + tostring(minTransferAmount) + " will be accepted");
        }
        utils.checkAddress(to);
        String from_address = utils.get_from_address();
        long from_address_balance = fast_map_get("users", from_address) == null ?
                0 : tointeger(fast_map_get("users", from_address));
        if (from_address_balance < amount) {
            error("you have not enoungh amount to transfer out");
        }
        from_address_balance = from_address_balance - amount;
        if (from_address_balance == 0) {
            fast_map_set("users", from_address, null);
        } else {
            fast_map_set("users", from_address, from_address_balance);
        }
        long to_balance = fast_map_get("users", to) == null ?
                0 : tointeger(fast_map_get("users", to));

        if (to_balance + amount < 0) {
            error("amount overflow");
        }
        fast_map_set("users", to, to_balance + amount - fee);
        if (fee > 0) {
            long feeReceiveAddressOldBalance = fast_map_get("users", feeReceiveAddress) == null ?
                    0 : tointeger(fast_map_get("users", feeReceiveAddress));
            if (feeReceiveAddressOldBalance + fee < 0) {
                error("amount overflow");
            }
            fast_map_set("users", feeReceiveAddress, feeReceiveAddressOldBalance + fee);
        }

        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("from", from_address);
        uvmMap.set("to", to);
        uvmMap.set("amount", amount - fee);
        uvmMap.set("fee", fee);
        uvmMap.set("memo", memo);
        emit("Transfer", tojsonstring(uvmMap));
        if (is_valid_contract_address(to)) {
            MultiOwnedContractInterface multiOwnedContractInterface = importContractFromAddress(MultiOwnedContractInterface.class, to);
            String amountStr = tostring(amount - fee);
            multiOwnedContractInterface.on_deposit_contract_token(amountStr);
        }
    }

    /**
     * @param arg arg format: fromAddress,toAddress,amount(with precision)
     */
    public void transferFrom(String arg) {
        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        Utils utils = new Utils();
        utils.checkState(this);
        utils.checkState(this);
        UvmArray<String> parsed = utils.parse_at_least_args(arg, 3, "argument format error, need format is fromAddress,toAddress,amount(with precision)");
        String fromAddress = parsed.get(1);
        String toAddress = parsed.get(2);
        long amount = tointeger(parsed.get(3));
        String memo = null;
        if (parsed.size() >= 4) {
            memo = parsed.get(4);
        }
        utils.checkAddress(fromAddress);
        utils.checkAddress(toAddress);
        if (amount <= 0) {
            error("amount must be positive integer");
        }

        long fee = this.getStorage().fee;
        long minTransferAmount = this.getStorage().minTransferAmount;
        String feeReceiveAddress = this.getStorage().feeReceiveAddress;
        if (amount <= fee) {
            error("amount not enough for fee");
        }
        if (amount < minTransferAmount) {
            error("only transfer amount >= " + tostring(minTransferAmount) + " will be accepted");
        }

        long from_address_balance = fast_map_get("users", fromAddress) == null ?
                0 : tointeger(fast_map_get("users", fromAddress));
        if (amount > from_address_balance) {
            error("fromAddress not have enough token to withdraw");
        }

        String allowedDataStr = tostring(fast_map_get("allowed", fromAddress));
        if (allowedDataStr == null) {
            error("not enough approved amount to withdraw from:" + fromAddress + "to:" + toAddress);
        }
        UvmMap allowedData = (UvmMap) totable(json.loads(allowedDataStr));
        String contractCaller = utils.get_from_address();
        if (allowedData == null || allowedData.get("contractCaller") == null) {
            error("not enough approved amount to withdraw from:" + fromAddress + "to:" + toAddress + "contractCaller: " + contractCaller);
        }
        long approvedAmount = tointeger(allowedData.get("contractCaller"));
        if (amount > approvedAmount) {
            error("not enough approved amount to withdraw from:" + fromAddress + "to:" + toAddress + "contractCaller: " + contractCaller);
        }
        long toAddressOldBalance = fast_map_get("users", toAddress) == null ?
                0 : tointeger(fast_map_get("users", toAddress));
        if (toAddressOldBalance + amount < 0) {
            error("amount overflow");
        }

        fast_map_set("users", toAddress, toAddressOldBalance + amount - fee);
        if (fee > 0) {
            long feeReceiveAddressOldBalance = fast_map_get("users", feeReceiveAddress) == null ?
                    0 : tointeger(fast_map_get("users", feeReceiveAddress));
            if (feeReceiveAddressOldBalance + fee < 0) {
                error("amount overflow");
            }
            fast_map_set("users", feeReceiveAddress, feeReceiveAddressOldBalance + fee);
        }
        fast_map_set("users", fromAddress, tointeger(fast_map_get("users", fromAddress)) - amount);
        if (tointeger(fast_map_get("users", fromAddress)) == 0) {
            fast_map_set("users", fromAddress, null);
        }
        allowedData.set("contractCaller", approvedAmount - amount);
        if (allowedData.get("contractCaller") == null) {
            allowedData.set("contractCaller", null);
        }
        fast_map_set("allowed", fromAddress, json.dumps(allowedData));
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("from", fromAddress);
        uvmMap.set("to", toAddress);
        uvmMap.set("amount", amount - fee);
        uvmMap.set("fee", fee);
        uvmMap.set("memo", memo);
        emit("Transfer", tojsonstring(uvmMap));
    }

    /**
     * @param arg arg format: spenderAddress,amount(with precision)
     */
    public void approve(String arg) {
        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        Utils utils = new Utils();
        utils.checkState(this);
        utils.checkState(this);
        UvmArray<String> parsed = utils.parse_at_least_args(arg, 2, "argument format error, need format is spenderAddress,amount(with precision)");
        String spender = parsed.get(1);
        utils.checkAddress(spender);
        long amount = tointeger(parsed.get(2));
        if (amount < 0) {
            error("amount must be non-negative integer");
        }

        UvmMap allowedData = null;
        String contractCaller = utils.get_from_address();
        if (fast_map_get("allowed", contractCaller) == null) {
            allowedData = UvmMap.create();
        } else {
            allowedData = (UvmMap) totable(json.loads(tostring(fast_map_get("allowed", contractCaller))));
            if (allowedData == null) {
                error("allowed storage data error");
            }
        }
        allowedData.set(spender, amount);
        fast_map_set("allowed", contractCaller, json.dumps(allowedData));
        UvmMap uvmMap = UvmMap.create();
        uvmMap.set("from", contractCaller);
        uvmMap.set("spender", spender);
        uvmMap.set("amount", amount);
        emit("Approved", tojsonstring(uvmMap));
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

    /**
     * @param arg integer_amount,unlockBlockNumber
     */
    public void lock(String arg) {
        Utils utils = new Utils();
        utils.checkState(this);
        utils.check_caller_frame_valid(this);
        if (!this.getStorage().allowLock) {
            error("this token contract not allow lock balance");
        }
        UvmArray<String> parsed = utils.parse_args(arg, 2, "arg format error, need format is integer_amount,unlockBlockNumber");
        long toLockAmount = tointeger(parsed.get(1));
        long unlockBlockNumber = tointeger(parsed.get(2));
        if (toLockAmount < 1) {
            error("to unlock amount must be positive integer");
        }
        if (unlockBlockNumber < get_header_block_num()) {
            error("to unlock block number can't be earlier than current block number " + tostring(get_header_block_num()));
        }
        String from_address = utils.get_from_address();
        if (from_address != caller_address()) {
            error("only common user account can lock balance");
        }
        String balance = utils.getBalanceOfUser(this, from_address);
        if (toLockAmount > tointeger(balance)) {
            error("you have not enough balance to lock");
        }
        if (fast_map_get("lockedAmounts", from_address) == null) {
            fast_map_set("lockedAmounts", from_address, tostring(toLockAmount) + ',' + tostring(unlockBlockNumber));
        } else {
            error("you have locked balance now, before lock again, you need unlock them or use other address to lock");
        }

        fast_map_set("users", from_address, tointeger(balance) - toLockAmount);
        emit("Locked", tostring(toLockAmount));
    }

    public void unlock() {
        Utils utils = new Utils();
        utils.checkState(this);
        utils.check_caller_frame_valid(this);
        if (!this.getStorage().allowLock) {
            error("this token contract not allow lock balance");
        }
        String from_address = utils.get_from_address();
        if (fast_map_get("lockedAmounts", from_address) == null) {
            error("you have not locked balance");
        }
        UvmArray<String> parsed = utils.parse_args(tostring(fast_map_get("lockedAmounts", from_address)), 2, "locked amount info format error");
        long lockedAmount = tointeger(parsed.get(1));
        long canUnlockBlockNumber = tointeger(parsed.get(2));
        if (get_header_block_num() < canUnlockBlockNumber) {
            error("your locked balance only can be unlock after block #" + tostring(canUnlockBlockNumber));
        }
        fast_map_set("lockedAmounts", from_address, null);
        long fromAddressOldBalance = tointeger(utils.getBalanceOfUser(this, from_address));
        if (fromAddressOldBalance + lockedAmount < 0) {
            error("amount overflow");
        }
        fast_map_set("users", from_address, fromAddressOldBalance + lockedAmount);
        emit("Unlocked", from_address + ',' + tostring(lockedAmount));

    }

    /**
     * @param arg -- arg: userAddress
     *            -- only admin can call this api
     */
    public void forceUnlock(String arg) {
        Utils utils = new Utils();
        utils.checkState(this);
        utils.check_caller_frame_valid(this);
        if (!this.getStorage().allowLock) {
            error("this token contract not allow lock balance");
        }
        utils.checkAdmin(this);
        String userAddr = arg;
        if (userAddr == null || userAddr.length() < 1) {
            error("argument format error, need format userAddress");
        }
        utils.checkAddress(userAddr);
        if (fast_map_get("lockedAmounts", userAddr) == null) {
            error("this user have not locked balance");
        }

        UvmArray<String> parsed = utils.parse_args(tostring(fast_map_get("lockedAmounts", userAddr)), 2, "locked amount info format error");
        long lockedAmount = tointeger(parsed.get(1));
        long canUnlockBlockNumber = tointeger(parsed.get(2));
        if (get_header_block_num() < canUnlockBlockNumber) {
            error("your locked balance only can be unlock after block #" + tostring(canUnlockBlockNumber));
        }
        fast_map_set("lockedAmounts", userAddr, null);
        long fromAddressOldBalance = tointeger(utils.getBalanceOfUser(this, userAddr));
        if (fromAddressOldBalance + lockedAmount < 0) {
            error("amount overflow");
        }
        fast_map_set("users", userAddr, fromAddressOldBalance + lockedAmount);
        emit("Unlocked", userAddr + ',' + tostring(lockedAmount));
    }

}
