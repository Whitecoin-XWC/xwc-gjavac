package gjavac.test.stableToken;

import gjavac.lib.*;

import static gjavac.lib.UvmCoreLibs.*;

/**
 * Description: DemoContract
 * Created by moloq on 2021/11/11 14:12
 */
@Contract(storage = Storage.class)
public class StableTokenContract extends UvmContract<Storage> {
    @Override
    public void init() {
        print("token contract creating");
        this.getStorage().name = "";
        this.getStorage().symbol = "";
        this.getStorage().supply = 0L;
        this.getStorage().precision = 0L;
        this.getStorage().state = "NOT_INITED";
        this.getStorage().admin = caller_address();
        this.getStorage().minter = "";
        this.getStorage().allowLock = false;
        this.getStorage().fee = 0L;
        this.getStorage().minTransferAmount = 0L;
        this.getStorage().feeReceiveAddress = caller_address();
        print("token contract created");
    }

    @Offline
    public String state(String arg) {
        return this.getStorage().state;
    }

    @Offline
    public String tokenName(String arg) {
        new Utils().checkStateInited(this);
        return this.getStorage().name;
    }

    @Offline
    public Long precision(String arg) {
        new Utils().checkStateInited(this);
        return this.getStorage().precision;
    }

    @Offline
    public String admin(String arg) {
        new Utils().checkStateInited(this);
        return this.getStorage().admin;
    }

    public long totalSupply(String arg) {
        new Utils().checkStateInited(this);
        return this.getStorage().supply;
    }

    @Offline
    public String isAllowLock(String arg) {
        return tostring(this.getStorage().allowLock);
    }


    @Offline
    public long supply(String arg) {
        return this.getStorage().supply;
    }

    @Offline
    public String tokenSymbol(String arg) {
        return this.getStorage().symbol;
    }

    @Offline
    public String fee(String arg) {
        return tostring(this.getStorage().fee);
    }

    @Offline
    public String minTransferAmount(String arg) {
        return tostring(this.getStorage().minTransferAmount);
    }

    @Offline
    public String feeReceiveAddress(String arg) {
        return this.getStorage().feeReceiveAddress;
    }

    @Offline
    public String minter(String arg) {
        return this.getStorage().minter;
    }


    private void onDeposit(int amount) {
        error("not support deposit to token");
    }

    public void onDestroy() {
        error("can't destroy token contract");
    }

    public void initToken(String arg) {
        Utils utils = new Utils();
        Storage storage = this.getStorage();
        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        utils.checkAdmin(this);
        pprint("arg:" + arg);
        if (state(arg)!= utils.NOT_INITED()) {
            error("this token contract inited before");
            return;
        }
        UvmArray<String> parsed = utils.parseArgs(arg, 4, "argument format error, need format: name,symbol,minter_contract,precision");
        UvmMap<Object> info = UvmMap.create();
        String name = parsed.get(1);
        String symbol = parsed.get(2);
        String minter = parsed.get(3);
        long precision = tointeger(parsed.get(4));
        info.set("name", name);
        info.set("symbol", symbol);
        info.set("minter", minter);
        info.set("precision", precision);
        if (utils.isBlank(name)) {
            error("name needed");
            return;
        }
        if (utils.isBlank(symbol)) {
            error("symbol needed");
            return;
        }
        if (utils.isBlank(minter)) {
            error("minter needed");
            return;
        }
        if (!is_valid_contract_address(minter)) {
            error("minter must be contract");
            return;
        }
        if (precision <= 0) {
            error("precision must be positive integer");
            return;
        }
        UvmArray<Long> allowedPrecisions = UvmArray.create();
        allowedPrecisions.add(1L);
        allowedPrecisions.add(10L);
        allowedPrecisions.add(100L);
        allowedPrecisions.add(1000L);
        allowedPrecisions.add(10000L);
        allowedPrecisions.add(100000L);
        allowedPrecisions.add(1000000L);
        allowedPrecisions.add(10000000L);
        allowedPrecisions.add(100000000L);
        if (!utils.arrayContains(allowedPrecisions, precision)) {
            error("precision can only be positive integer in " + json.dumps(allowedPrecisions));
            return;
        }
        storage.setMinter(minter);
        storage.setPrecision(precision);
        storage.setState(utils.COMMON());
        emit("Inited", json.dumps(info));
    }

    public void openAllowLock(String arg) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.checkState(this);
        if (this.getStorage().getAllowLock()) {
            error("this contract had been opened allowLock before");
            return;
        }
        this.getStorage().setAllowLock(true);
        emit("AllowedLock", "");
    }

    public void setFee(String feeStr) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.checkState(this);
        if (tointeger(feeStr) < 0) {
            error("error fee format");
            return;
        }
        this.getStorage().setFee(tointeger(feeStr));
        emit("FeeChanged", feeStr);
    }

    public void setMinTransferAmount(String minTransferAmountStr) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.checkState(this);
        if (tointeger(minTransferAmountStr) < 0) {
            error("error minTransferAmount format");
            return;
        }
        this.getStorage().setMinTransferAmount(tointeger(minTransferAmountStr));
        emit("MinTransferAmountChanged", minTransferAmountStr);
    }

    public void setFeeReceiveAddress(String feeReceiveAddress) {
        Utils utils = new Utils();
        utils.checkAdmin(this);
        utils.checkState(this);
        if (!is_valid_address(feeReceiveAddress)) {
            error("invalid address");
            return;
        }
        if (is_valid_contract_address(feeReceiveAddress)) {
            error("can't use contract address");
            return;
        }
        this.getStorage().setFeeReceiveAddress(feeReceiveAddress);
        emit("FeeReceiveAddressChanged", feeReceiveAddress);
    }

    public void transfer(String arg) {
        Utils utils = new Utils();
        utils.checkState(this);
        if ((Storage) this.getStorage() != null) {
            UvmArray parsed = utils.parseAtLeastArgs(arg, 2, "argument format error, need format is to_address,integer_amount[,memo]");
            String to = UvmCoreLibs.tostring(parsed.get(1));
            String amountStr = (String) parsed.get(2);
            utils.checkAddress(to);
            UvmSafeMathModule safemathModule = (UvmSafeMathModule) UvmCoreLibs.importModule(UvmSafeMathModule.class, "safemath");
            UvmBigInt bigintAmount = safemathModule.bigint(amountStr);
            UvmBigInt bigint0 = safemathModule.bigint(0);
            if (amountStr == null || safemathModule.le(bigintAmount, bigint0)) {
                UvmCoreLibs.error("invalid amount:" + amountStr);
                return;
            }

            String fromAddress = utils.getFromAddress();
            if (fromAddress == to) {
                UvmCoreLibs.error("fromAddress and toAddress is same：" + fromAddress);
                return;
            }
            Object temp = UvmCoreLibs.fast_map_get("users", fromAddress);
            if (temp == null) {
                temp = "0";
            }

            UvmBigInt fromBalance = safemathModule.bigint(temp);
            temp = UvmCoreLibs.fast_map_get("users", to);
            if (temp == null) {
                temp = "0";
            }

            UvmBigInt toBalance = safemathModule.bigint(temp);
            if (safemathModule.lt(fromBalance, bigintAmount)) {
                UvmCoreLibs.error("insufficient balance:" + safemathModule.tostring(fromBalance));
            }

            fromBalance = safemathModule.sub(fromBalance, bigintAmount);
            toBalance = safemathModule.add(toBalance, bigintAmount);
            String frombalanceStr = safemathModule.tostring(fromBalance);
            if (frombalanceStr == "0") {
                UvmCoreLibs.fast_map_set("users", fromAddress, (Object) null);
            } else {
                UvmCoreLibs.fast_map_set("users", fromAddress, frombalanceStr);
            }

            UvmCoreLibs.fast_map_set("users", to, safemathModule.tostring(toBalance));
            if (UvmCoreLibs.is_valid_contract_address(to)) {
                MultiOwnedContractSimpleInterface multiOwnedContract = (MultiOwnedContractSimpleInterface) UvmCoreLibs.importContractFromAddress(MultiOwnedContractSimpleInterface.class, to);
                if (multiOwnedContract != null && multiOwnedContract.getOn_deposit_contract_token() != null) {
                    multiOwnedContract.on_deposit_contract_token(amountStr);
                }
            }

            UvmMap eventArg = UvmMap.create();
            eventArg.set("from", fromAddress);
            eventArg.set("to", to);
            eventArg.set("amount", amountStr);
            String eventArgStr = UvmCoreLibs.tojsonstring(eventArg);
            UvmCoreLibs.emit("Transfer", eventArgStr);
        }
    }

    public void transferFrom(String arg) {
        Utils utils = new Utils();
        utils.checkState(this);
        if ((Storage) this.getStorage() != null) {
            UvmArray parsed = utils.parseAtLeastArgs(arg, 3, "argument format error, need format is fromAddress,toAddress,amount(with precision)");
            String fromAddress = UvmCoreLibs.tostring(parsed.get(1));
            String toAddress = UvmCoreLibs.tostring(parsed.get(2));
            String amountStr = UvmCoreLibs.tostring(parsed.get(3));
            utils.checkAddress(fromAddress);
            utils.checkAddress(toAddress);
            if (fromAddress == toAddress) {
                UvmCoreLibs.error("fromAddress and toAddress is same：" + fromAddress);
                return;
            }
            UvmSafeMathModule safemathModule = (UvmSafeMathModule) UvmCoreLibs.importModule(UvmSafeMathModule.class, "safemath");
            UvmBigInt bigintAmount = safemathModule.bigint(amountStr);
            UvmBigInt bigint0 = safemathModule.bigint(0);
            if (amountStr == null || safemathModule.le(bigintAmount, bigint0)) {
                UvmCoreLibs.error("invalid amount:" + amountStr);
            }

            Object temp = UvmCoreLibs.fast_map_get("users", fromAddress);
            if (temp == null) {
                temp = "0";
            }

            UvmBigInt bigintFromBalance = safemathModule.bigint(temp);
            Object temp2 = UvmCoreLibs.fast_map_get("users", toAddress);
            if (temp2 == null) {
                temp2 = "0";
            }

            UvmBigInt bigintToBalance = safemathModule.bigint(temp2);
            if (safemathModule.lt(bigintFromBalance, bigintAmount)) {
                UvmCoreLibs.error("insufficient balance :" + safemathModule.tostring(bigintFromBalance));
            }

            Object allowedDataStr = UvmCoreLibs.fast_map_get("allowed", fromAddress);
            if (allowedDataStr == null) {
                UvmCoreLibs.error("not enough approved amount to withdraw");
            } else {
                UvmJsonModule jsonModule = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
                UvmMap allowedData = (UvmMap) UvmCoreLibs.totable(jsonModule.loads(UvmCoreLibs.tostring(allowedDataStr)));
                String contractCaller = utils.getFromAddress();
                if (allowedData == null) {
                    UvmCoreLibs.error("not enough approved amount to withdraw");
                } else {
                    String approvedAmountStr = (String) allowedData.get(contractCaller);
                    if (approvedAmountStr == null) {
                        UvmCoreLibs.error("no approved amount to withdraw");
                    }

                    UvmBigInt bigintApprovedAmount = safemathModule.bigint(approvedAmountStr);
                    if (bigintApprovedAmount != null && !safemathModule.gt(bigintAmount, bigintApprovedAmount)) {
                        bigintFromBalance = safemathModule.sub(bigintFromBalance, bigintAmount);
                        String bigintFromBalanceStr = safemathModule.tostring(bigintFromBalance);
                        if (bigintFromBalanceStr == "0") {
                            bigintFromBalance = null;
                        }
                        bigintToBalance = safemathModule.add(bigintToBalance, bigintAmount);
                        String bigintToBalanceStr = safemathModule.tostring(bigintToBalance);
                        if (bigintToBalanceStr == "0") {
                            bigintToBalanceStr = null;
                        }

                        bigintApprovedAmount = safemathModule.sub(bigintApprovedAmount, bigintAmount);
                        UvmCoreLibs.fast_map_set("users", fromAddress, bigintFromBalanceStr);
                        UvmCoreLibs.fast_map_set("users", toAddress, bigintToBalanceStr);
                        if (safemathModule.tostring(bigintApprovedAmount) == "0") {
                            allowedData.set(contractCaller, null);
                        } else {
                            allowedData.set(contractCaller, safemathModule.tostring(bigintApprovedAmount));
                        }

                        allowedDataStr = UvmCoreLibs.tojsonstring(allowedData);
                        UvmCoreLibs.fast_map_set("allowed", fromAddress, allowedDataStr);
                        if (UvmCoreLibs.is_valid_contract_address(toAddress)) {
                            MultiOwnedContractSimpleInterface multiOwnedContract = (MultiOwnedContractSimpleInterface) UvmCoreLibs.importContractFromAddress(MultiOwnedContractSimpleInterface.class, toAddress);
                            if (multiOwnedContract != null && multiOwnedContract.getOn_deposit_contract_token() != null) {
                                multiOwnedContract.on_deposit_contract_token(amountStr);
                            }
                        }

                        UvmMap eventArg = UvmMap.create();
                        eventArg.set("from", fromAddress);
                        eventArg.set("to", toAddress);
                        eventArg.set("amount", amountStr);
                        String eventArgStr = UvmCoreLibs.tojsonstring(eventArg);
                        UvmCoreLibs.emit("Transfer", eventArgStr);
                    } else {
                        UvmCoreLibs.error("not enough approved amount to withdraw");
                    }
                }
            }
        }
    }

    public void approve(String arg) {
        Utils utils = new Utils();
        utils.checkState(this);
        if ((Storage) this.getStorage() != null) {
            UvmArray parsed = utils.parseAtLeastArgs(arg, 2, "argument format error, need format is spenderAddress,amount(with precision)");
            String spender = UvmCoreLibs.tostring(parsed.get(1));
            utils.checkAddress(spender);
            String amountStr = UvmCoreLibs.tostring(parsed.get(2));
            UvmSafeMathModule safemathModule = (UvmSafeMathModule) UvmCoreLibs.importModule(UvmSafeMathModule.class, "safemath");
            UvmBigInt bigintAmount = safemathModule.bigint(amountStr);
            UvmBigInt bigint0 = safemathModule.bigint(0);
            if (amountStr == null || safemathModule.lt(bigintAmount, bigint0)) {
                UvmCoreLibs.error("amount must be non-negative integer");
            }

            String contractCaller = utils.getFromAddress();
            UvmJsonModule jsonModule = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
            UvmMap allowedDataTable = (UvmMap) null;
            Object allowedDataStr = UvmCoreLibs.fast_map_get("allowed", contractCaller);
            if (allowedDataStr == null) {
                allowedDataTable = UvmMap.create();
            } else {
                allowedDataTable = (UvmMap) UvmCoreLibs.totable(jsonModule.loads(UvmCoreLibs.tostring(allowedDataStr)));
                if (allowedDataTable == null) {
                    UvmCoreLibs.error("allowed storage data error");
                    return;
                }
            }

            if (safemathModule.eq(bigintAmount, bigint0)) {
                allowedDataTable.set(spender, null);
            } else {
                allowedDataTable.set(spender, amountStr);
            }

            UvmCoreLibs.fast_map_set("allowed", contractCaller, UvmCoreLibs.tojsonstring(allowedDataTable));
            UvmMap eventArg = UvmMap.create();
            eventArg.set("from", contractCaller);
            eventArg.set("spender", spender);
            eventArg.set("amount", amountStr);
            String eventArgStr = UvmCoreLibs.tojsonstring(eventArg);
            UvmCoreLibs.emit("Approved", eventArgStr);
        }
    }

    public void pause(String arg) {
        Utils utils = new Utils();
        Storage var10000 = (Storage) this.getStorage();
        if (var10000 != null) {
            Storage storage = var10000;
            String state = storage.getState();
            if (state == utils.STOPPED()) {
                UvmCoreLibs.error("this contract stopped now, can't pause");
            } else if (state == utils.PAUSED()) {
                UvmCoreLibs.error("this contract paused now, can't pause");
            } else {
                utils.checkAdmin(this);
                storage.setState(utils.PAUSED());
                UvmCoreLibs.emit("Paused", "");
            }
        }
    }


    public void resume(String arg) {
        Utils utils = new Utils();
        Storage var10000 = (Storage) this.getStorage();
        if (var10000 != null) {
            Storage storage = var10000;
            String state = storage.getState();
            if (state != utils.PAUSED()) {
                UvmCoreLibs.error("this contract not paused now, can't resume");
            } else {
                utils.checkAdmin(this);
                storage.setState(utils.COMMON());
                UvmCoreLibs.emit("Resumed", "");
            }
        }
    }

    public void stop(String arg) {
        Utils utils = new Utils();
        Storage var10000 = (Storage) this.getStorage();
        if (var10000 != null) {
            Storage storage = var10000;
            String state = storage.getState();
            if (state == utils.STOPPED()) {
                UvmCoreLibs.error("this contract stopped now, can't stop");
            } else if (state == utils.PAUSED()) {
                UvmCoreLibs.error("this contract paused now, can't stop");
            } else {
                utils.checkAdmin(this);
                storage.setState(utils.STOPPED());
                UvmCoreLibs.emit("Stopped", "");
            }
        }
    }

    public void lock(String arg) {
        Utils utils = new Utils();
        utils.checkState(this);
        Storage var10000 = (Storage) this.getStorage();
        if (var10000 != null) {
            Storage storage = var10000;
            if (!storage.getAllowLock()) {
                UvmCoreLibs.error("this token contract not allow lock balance");
            } else {
                UvmArray parsed = utils.parseAtLeastArgs(arg, 2, "arg format error, need format is integer_amount,unlockBlockNumber");
                String toLockAmount = (String) parsed.get(1);
                long unlockBlockNumber = UvmCoreLibs.tointeger(parsed.get(2));
                UvmSafeMathModule safemathModule = (UvmSafeMathModule) UvmCoreLibs.importModule(UvmSafeMathModule.class, "safemath");
                UvmBigInt bigintToLockAmount = safemathModule.bigint(toLockAmount);
                UvmBigInt bigint0 = safemathModule.bigint(0L);
                if (toLockAmount != null && !safemathModule.le(bigintToLockAmount, bigint0)) {
                    if (unlockBlockNumber < UvmCoreLibs.get_header_block_num()) {
                        UvmCoreLibs.error("to unlock block number can't be earlier than current block number " + UvmCoreLibs.tostring(UvmCoreLibs.get_header_block_num()));
                    } else {
                        String fromAddress = utils.getFromAddress();
                        if (fromAddress != UvmCoreLibs.caller_address()) {
                            UvmCoreLibs.error("only common user account can lock balance");
                        } else {
                            Object temp = UvmCoreLibs.fast_map_get("users", fromAddress);
                            if (temp == null) {
                                UvmCoreLibs.error("your balance is 0");
                            } else {
                                UvmBigInt bigintFromBalance = safemathModule.bigint(temp);
                                if (safemathModule.gt(bigintToLockAmount, bigintFromBalance)) {
                                    UvmCoreLibs.error("you have not enough balance to lock");
                                } else {
                                    Object lockedAmount = UvmCoreLibs.fast_map_get("lockedAmounts", fromAddress);
                                    if (lockedAmount == null) {
                                        UvmCoreLibs.fast_map_set("lockedAmounts", fromAddress, UvmCoreLibs.tostring(toLockAmount) + "," + UvmCoreLibs.tostring(unlockBlockNumber));
                                        bigintFromBalance = safemathModule.sub(bigintFromBalance, bigintToLockAmount);
                                        UvmCoreLibs.fast_map_set("users", fromAddress, safemathModule.tostring(bigintFromBalance));
                                        UvmCoreLibs.emit("Locked", UvmCoreLibs.tostring(toLockAmount));
                                    } else {
                                        UvmCoreLibs.error("you have locked balance now, before lock again, you need unlock them or use other address to lock");
                                    }
                                }
                            }
                        }
                    }
                } else {
                    UvmCoreLibs.error("to unlock amount must be positive integer");
                }
            }
        }
    }


    public void unlock(String arg) {
        Utils utils = new Utils();
        String fromAddress = utils.getFromAddress();
        forceUnlock(fromAddress);
    }

    public void forceUnlock(String unlockAddress) {
        Utils utils = new Utils();
        utils.checkState(this);

        if (this.getStorage().getAllowLock() == false) {
            UvmCoreLibs.error("this token contract not allow lock balance");
        } else {
            Object lockedStr = UvmCoreLibs.fast_map_get("lockedAmounts", unlockAddress);
            if (lockedStr == null) {
                UvmCoreLibs.error("you have not locked balance");
            } else {
                UvmArray lockedInfoParsed = utils.parseAtLeastArgs(UvmCoreLibs.tostring(lockedStr), 2, "locked amount info format error");
                String lockedAmountStr = UvmCoreLibs.tostring(lockedInfoParsed.get(1));
                long canUnlockBlockNumber = UvmCoreLibs.tointeger(lockedInfoParsed.get(2));
                if (UvmCoreLibs.get_header_block_num() < canUnlockBlockNumber) {
                    UvmCoreLibs.error("your locked balance only can be unlock after block #" + UvmCoreLibs.tostring(canUnlockBlockNumber));
                    return;
                }
                UvmCoreLibs.fast_map_set("lockedAmounts", unlockAddress, (Object) null);
                Object temp = UvmCoreLibs.fast_map_get("users", unlockAddress);
                if (temp == null) {
                    temp = "0";
                }

                UvmSafeMathModule safemathModule = (UvmSafeMathModule) UvmCoreLibs.importModule(UvmSafeMathModule.class, "safemath");
                UvmBigInt bigintFromBalance = safemathModule.bigint(temp);
                UvmBigInt bigintLockedAmount = safemathModule.bigint(UvmCoreLibs.tostring(lockedAmountStr));
                bigintFromBalance = safemathModule.add(bigintFromBalance, bigintLockedAmount);
                UvmCoreLibs.fast_map_set("users", unlockAddress, safemathModule.tostring(bigintFromBalance));
                String tempevent = unlockAddress + "," + UvmCoreLibs.tostring(lockedStr);
                UvmCoreLibs.emit("Unlocked", tempevent);
            }
        }
    }


    public void mint(String arg) {
        Utils utils = new Utils();
        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        utils.checkState(this);
        utils.checkMinter(this);
        UvmArray<String> parsed = utils.parseArgs(arg, 2, "argument format error, need format: to_address,token_amount");
        String toAddress = parsed.get(1);
        String amountStr = parsed.get(2);
        long amount = utils.checkInteger(amountStr);
        if (!is_valid_address(toAddress)) {
            error("to_address is not valid address");
            return;
        }
        if (amount <= 0) {
            error("arg token_amount must > 0");
            return;
        }

        long originSupply = this.getStorage().getSupply();
        long newSupply = originSupply + amount;
        if (newSupply <= originSupply) {
            error("supply over flow");
            return;
        }

        long userOldBalance = tointeger(fast_map_get("users", toAddress));
        fast_map_set("users", toAddress, userOldBalance + amount);
        UvmMap eventArg = UvmMap.create();
        eventArg.set("address", toAddress);
        eventArg.set("amount", amount);
        String eventArgStr = UvmCoreLibs.tojsonstring(eventArg);
        emit("Mint", json.dumps(eventArgStr));
    }

    public void destoryAndTrans(String arg) {
        Utils utils = new Utils();
        utils.checkState(this);
        utils.checkMinter(this);
        UvmArray<String> parsed = utils.parseArgs(arg, 4, "argument format error, need format: from_address,destory_amount,trans_to_address,trans_amount");
        String fromAddress = parsed.get(1);
        long destoryAmount = utils.checkInteger(parsed.get(2));
        if (destoryAmount < 0) {
            error("arg destory_amount must >= 0");
            return;
        }
        String transToAddress = parsed.get(3);
        long transAmount = utils.checkInteger(parsed.get(4));
        if (transAmount < 0) {
            error("arg trans_amount must >= 0");
            return;
        }
        if (destoryAmount == 0 && transAmount == 0) {
            error("destory_amount and trans_amount is 0");
            return;
        }
        long originSupple = this.getStorage().getSupply();
        if (originSupple < destoryAmount) {
            error("supply minus error");
            return;
        }
        this.getStorage().setSupply(originSupple - destoryAmount);
        long fromOldBalance = tointeger(fast_map_get("users", fromAddress));
        long subFromAmount = destoryAmount + transAmount;
        if (fromOldBalance < subFromAmount) {
            error("not enough balance to destory and trans , now balance:" + tostring(fromOldBalance) + " need amount:" + tostring(subFromAmount));
            return;
        }

        if (fromOldBalance == subFromAmount) {
            fast_map_set("users", fromAddress, null);
        } else {
            fast_map_set("users", fromAddress, fromOldBalance - subFromAmount);
        }

        if (transAmount > 0) {
            if (!is_valid_address(transToAddress)) {
                error("trans_to_address is not valid address");
                return;
            }
            long toOldBalance = tointeger(fast_map_get("users", transToAddress));
            fast_map_set("users", transToAddress, toOldBalance + transAmount);
        }

        UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
        UvmMap eventArg = UvmMap.create();
        eventArg.set("from_address", fromAddress);
        eventArg.set("destory_amount", destoryAmount);
        eventArg.set("trans_to_address", transToAddress);
        eventArg.set("trans_amount", transAmount);
        String eventArgStr = UvmCoreLibs.tojsonstring(eventArg);
        emit("DestoryAndTrans", json.dumps(eventArgStr));
    }

    @Offline
    public String lockedBalanceOf(String owner) {
        Object resultStr = fast_map_get("lockedAmounts", owner);
        if (resultStr == null) {
            return "0,0";
        }
        return String.valueOf(resultStr);
    }

    @Offline
    public String balanceOf(String owner) {
        Utils utils = new Utils();
        utils.checkStateInited(this);
        utils.checkAddress(owner);
        String amountStr = utils.getBalanceOfUser(this, owner);
        return amountStr;
    }

    @Offline
    public String approvedBalanceFrom(String arg) {
        Utils utils = new Utils();
        if ((Storage) this.getStorage() != null) {
            UvmArray parsed = utils.parseAtLeastArgs(arg, 2, "argument format error, need format is spenderAddress,authorizerAddress");
            String spender = UvmCoreLibs.tostring(parsed.get(1));
            String authorizer = UvmCoreLibs.tostring(parsed.get(2));
            utils.checkAddress(spender);
            utils.checkAddress(authorizer);
            Object allowedDataStr = UvmCoreLibs.fast_map_get("allowed", authorizer);
            if (allowedDataStr == null) {
                return "0";
            } else {
                UvmJsonModule jsonModule = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");
                UvmMap allowedDataTable = (UvmMap) UvmCoreLibs.totable(jsonModule.loads(UvmCoreLibs.tostring(allowedDataStr)));
                if (allowedDataTable == null) {
                    return "0";
                } else {
                    String allowedAmount = (String) allowedDataTable.get(spender);
                    return allowedAmount == null ? "0" : allowedAmount;
                }
            }
        } else {
            return "";
        }
    }

    @Offline
    public String allApprovedFromUser(String arg) {
        Utils utils = new Utils();
        if ((Storage) this.getStorage() != null) {
            utils.checkAddress(arg);
            Object allowedDataStr = UvmCoreLibs.fast_map_get("allowed", "authorizer");
            if (allowedDataStr == null) {
                return "{}";
            } else {
                return UvmCoreLibs.tostring(allowedDataStr);
            }
        } else {
            return "";
        }
    }


}
