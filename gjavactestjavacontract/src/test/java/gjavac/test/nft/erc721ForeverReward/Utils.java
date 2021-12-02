package gjavac.test.nft.erc721ForeverReward;

import gjavac.lib.*;

import static gjavac.lib.UvmCoreLibs.*;

/**
 * Description: DemoContract
 * Created by moloq on 2021/11/17 14:13
 */
@Component
public class Utils {
    UvmJsonModule json = (UvmJsonModule) UvmCoreLibs.importModule(UvmJsonModule.class, "json");

    public String NOT_INITED() {
        return "NOT_INITED";
    }


    public String COMMON() {
        return "COMMON";
    }


    public String PAUSED() {
        return "PAUSED";
    }


    public String STOPPED() {
        return "STOPPED";
    }

    public final String getFromAddress() {
        String fromAddress;
        final String prevContractId = get_prev_call_frame_contract_address();
        if (isNotBlank(prevContractId) && is_valid_address(prevContractId)) {
            fromAddress = prevContractId;
        } else {
            fromAddress = caller_address();
        }
        return fromAddress;
    }

    public final void checkAdmin(ERC721ForeverRewardContract self) {
        String fromAddress = getFromAddress();
        if (self.getStorage().admin != fromAddress) {
            error("you are not admin, can't call this function");
        }
    }

    public final void checkState(ERC721ForeverRewardContract self) {
        String state = self.getStorage().state;
        if (state != COMMON()) {
            error("state error, now state is " + state);
        }
    }

    public final void checkStateInited(ERC721ForeverRewardContract self) {
        if (self.getStorage().state == NOT_INITED())
            error("contract token not inited");
    }

    public final Object _ownerOf(String tokenId) {
        Object owners = fast_map_get("_owners", tokenId);
        if (owners == null) {
            error("ERC721: owner query for nonexistent token");
            return "";
        }
        return owners;
    }

    public final UvmArray<String> parseArgs(String arg, int count, String errorMsg) {
        if (isBlank(arg)) {
            error(errorMsg);
            return UvmArray.create();
        }
        UvmStringModule stringModule = importModule(UvmStringModule.class, "string");
        UvmArray<String> parsed = stringModule.split(arg, ",");
        if (parsed != null && parsed.size() == count) {
            return parsed;
        } else {
            error(errorMsg);
            return UvmArray.create();
        }
    }

    public final String baseUri() {
        return "http://www.testnft.com/";
    }

    public final UvmArray<String> parseAtLeastArgs(String arg, int count, String errorMsg) {
        if (isBlank(arg)) {
            error(errorMsg);
            return UvmArray.create();
        }

        UvmStringModule stringModule = importModule(UvmStringModule.class, "string");
        UvmArray<String> parsed = stringModule.split(arg, ",");
        if (parsed != null && parsed.size() >= count) {
            return parsed;
        } else {
            error(errorMsg);
            return UvmArray.create();
        }

    }

    public final boolean checkAddress(String addr) {
        boolean result = is_valid_address(addr);
        if (!result) {
            error("address format error");
        }
        return result;
    }

    public final boolean isApprovedForAll(String owner, String operator) {
        Object operatorApprovals = fast_map_get("_operatorApprovals", owner);
        String data;
        if (operatorApprovals == null) {
            data = "{}";
        } else {
            data = tostring(operatorApprovals);
        }
        UvmMap uvmMap = (UvmMap) totable(json.loads(data));
        if (uvmMap.get("operator") == null) {
            return false;
        }
        return true;
    }

    public final void withdrawNativeAssetPrivate(ERC721ForeverRewardContract self, String from, String symbol, String amountStr) {
        Utils utils = new Utils();
        utils.checkState(self);
        long amount = tointeger(amountStr);
        if (symbol == null || symbol.length() < 1 || amount < 0) {
            error("invalid params");
            return;
        }
        final String fromAddress = from;
        Object nativeBalances = fast_map_get("_nativeBalances", fromAddress);
        if (nativeBalances == null) {
            nativeBalances = "{}";
        }
        UvmMap userAssets = (UvmMap) json.loads(tostring(nativeBalances));
        Object oldBalanceObj = userAssets.get("symbol");
        if (oldBalanceObj == null) {
            oldBalanceObj = 0;
        }
        long oldBalance = tointeger(oldBalanceObj);
        if (oldBalance < amount) {
            error("amount exceed balance");
            return;
        }
        final long newBalance = oldBalance - amount;
        userAssets.set("symbol", newBalance);
        String newUserAssetsStr = json.dumps(userAssets);
        fast_map_set("_nativeBalances", fromAddress, newUserAssetsStr);
        int res1 = transfer_from_contract_to_address(from, symbol, amount);
        if (res1 != 0) {
            error("transfer asset " + symbol + " to " + from + " amount:" + tostring(amount) + " error, error code: " + tostring(res1));
        }
        UvmMap<Object> uvmMap = new UvmMap<>();
        uvmMap.set("address", fromAddress);
        uvmMap.set("symbol", symbol);
        uvmMap.set("change", 0 - amount);
        uvmMap.set("reason", "reason");
        emit("NativeBalanceChange", json.dumps(uvmMap));
    }


    public final boolean require(boolean success, String text) {
        if (success)
            return true;
        error(text);
        return false;
    }

    public final void _approve(String to, String tokenId) {
        Object owner = _ownerOf(tokenId);
        fast_map_set("_tokenApprovals", tokenId, to);
        UvmMap<String> uvmMap = new UvmMap<>();
        uvmMap.set("owner", tostring(owner));
        uvmMap.set("to", to);
        uvmMap.set("tokenId", tokenId);
        emit("Approval", json.dumps(uvmMap));
    }

    public final boolean checkContractAddress(String addr) {
        boolean valid_address = is_valid_address(addr);
        if (!valid_address) {
            error("contract address format error");
            return false;
        }
        return valid_address;
    }

    public final boolean _isApprovedOrOwner(String spender, String tokenId) {
        Object owner = _ownerOf(tokenId);
        require(owner != "", "ERC721: operator query for nonexistent token");
        Object approve = fast_map_get("_tokenApprovals", tokenId);
        if (approve == null) {
            approve = "";
        }
        return spender == owner || tostring(approve) == owner || isApprovedForAll(tostring(owner), spender);
    }

    public final void _addTokenToOwnerEnumeration(ERC721ForeverRewardContract self, String to, String tokenId) {
        long amount = fast_map_get("_balances", to) == null ? 0 : tointeger(fast_map_get("_balances", to));
        String data = fast_map_get("_ownedTokens", to) == null ? "{}" : tostring(fast_map_get("_ownedTokens", to));
        UvmMap json_data = (UvmMap) totable(json.loads(data));
        json_data.set(tostring(amount + 1), tokenId);
        fast_map_set("_ownedTokens", to, json.dumps(json_data));
        fast_map_set("_ownedTokensIndex", tokenId, tostring(amount + 1));
    }

    public final void _addTokenToAllTokensEnumeration(ERC721ForeverRewardContract self, String tokenId) {
        self.getStorage().allTokenCount = self.getStorage().allTokenCount + 1;
        fast_map_set("_allTokensIndex", tokenId, tostring(self.getStorage().allTokenCount));
        fast_map_set("_allTokens", tostring(self.getStorage().allTokenCount), tokenId);
    }


    public final void _removeTokenFromOwnerEnumeration(ERC721ForeverRewardContract self, String from, String tokenId) {
        final long lastTokenIndex = fast_map_get("_balances", from) == null ? 0 : tointeger(fast_map_get("_balances", from));
        final long tokenIndex = tointeger(fast_map_get("_ownedTokensIndex", tokenId));
        if (lastTokenIndex <= 0 || tokenIndex <= 0) {
            error("unkown token index error");
            return;
        }

        final String data = tostring(fast_map_get("_ownedTokens", from));
        UvmArray<String> json_data = (UvmArray) totable(json.loads(data));
        if (tokenIndex != lastTokenIndex) {
            String lastTokenId = json_data.get((int) lastTokenIndex);
            json_data.set((int) tokenIndex, lastTokenId);
            fast_map_set("_ownedTokensIndex", lastTokenId, tostring(tokenIndex));
        }

        //TODO table.remove( json_data,tointeger(lastTokenIndex))
        fast_map_set("_ownedTokens", from, json.dumps(json_data));
        fast_map_set("_ownedTokensIndex", tokenId, null);
    }

    public final void _removeTokenFromAllTokensEnumeration(ERC721ForeverRewardContract self, String tokenId) {
        String lastTokenIndex = tostring(self.getStorage().allTokenCount);
        self.getStorage().allTokenCount = self.getStorage().allTokenCount - 1;
        Object tokenIndex = fast_map_get("_allTokensIndex", tokenId);
        Object lastTokenId = fast_map_get("_allTokens", lastTokenIndex);
        fast_map_set("_allTokens", tostring(tokenIndex), tostring(lastTokenId));
        fast_map_set("_allTokensIndex", tostring(lastTokenId), tostring(tokenIndex));
        fast_map_set("_allTokens", lastTokenIndex, null);
        fast_map_set("_allTokensIndex", tokenId, null);
    }

    public final void _beforeTokenTransfer(ERC721ForeverRewardContract self, String from, String to, String tokenId) {
        if (from == "") {
            _addTokenToAllTokensEnumeration(self, tokenId);
        } else if (from != to) {
            _removeTokenFromOwnerEnumeration(self, from, tokenId);
        }

        if (to == "") {
            _removeTokenFromAllTokensEnumeration(self, tokenId);
        } else if (to != from) {
            _addTokenToOwnerEnumeration(self, to, tokenId);
        }
    }

    public final UvmArray getTradePrice(String tokenId) {
        UvmArray array = new UvmArray();
        Object data = fast_map_get("_allTokenTradePrice", tokenId);
        if (data == null) {
            array.set(1, "");
            array.set(2, 0);
        }
        UvmArray<String> parseArgs = parseArgs(tostring(data), 2, "");
        return parseArgs;
    }

    public final boolean checkPrivilege(ERC721ForeverRewardContract self, String from, String to) {
        if (from == self.getStorage().auctionContract || to == self.getStorage().auctionContract) {
            return true;
        }

        if (from == self.getStorage().fixedSellContract || to == self.getStorage().fixedSellContract) {
            return true;
        }

        return false;
    }

    public final void _transfer(ERC721ForeverRewardContract self, String from, String to, String tokenId) {
        Utils utils = new Utils();
        Object owner = _ownerOf(tokenId);
        utils.require(owner == from, "ERC721: transfer of token that is not own");
        utils.require(is_valid_address(to), "ERC721: transfer to the zero address");
        UvmArray tradePrice = utils.getTradePrice(tokenId);
        final long feeRate = fast_map_get("_allTokenMintFee", tokenId) == null ? 0 : tointeger(fast_map_get("_allTokenMintFee", tokenId));
        final String minter = tostring(fast_map_get("_allTokenMinter", tokenId));
        final String symbol = tostring(tradePrice.get(1));
        final long amount = tointeger(feeRate * tointeger(tradePrice.get(2)) / 100);
        utils.require(minter != "", "The unknown minter does not exist an exception");
        if (minter != "" && feeRate > 0 && amount > 0 && utils.checkPrivilege(self, from, to) == false) {
            Object nativeBalances = fast_map_get("_nativeBalances", from);
            if (nativeBalances == null) {
                nativeBalances = "{}";
            }
            UvmMap userAssets = (UvmMap) json.loads(tostring(nativeBalances));
            long oldBalance = tointeger(userAssets.get("symbol") == null ? 0 : userAssets.get("symbol"));
            if (oldBalance < amount) {
                error("amount exceed balance");
                return;
            }

            long newBalance = oldBalance - amount;
            userAssets.set("symbol", newBalance);
            String newUserAssetsStr = json.dumps(userAssets);
            fast_map_set("_nativeBalances", from, newUserAssetsStr);
            int res1 = transfer_from_contract_to_address(minter, symbol, amount);
            if (res1 != 0) {
                error("transfer asset " + symbol + " to " + from + " amount:" + tostring(amount) + " error, error code: " + tostring(res1));
                return;
            }

            UvmMap<String> uvmMap = new UvmMap<>();
            uvmMap.set("minter", minter);
            uvmMap.set("symbol", symbol);
            uvmMap.set("amount", tostring(amount));
            uvmMap.set("from", from);
            uvmMap.set("to", to);
            uvmMap.set("tokenId", tokenId);
            emit("copyRightPayEvent", json.dumps(uvmMap));
        }
        _beforeTokenTransfer(self, from, to, tokenId);
        _approve("", tokenId);
        final long count = tointeger(fast_map_get("_balances", from));
        fast_map_set("_balances", from, tostring(count - 1));
        final long to_count = tointeger(fast_map_get("_balances", to));
        fast_map_set("_balances", to, tostring(to_count + 1));
        fast_map_set("_owners", tokenId, to);
        UvmMap<String> uvmMap = new UvmMap<>();
        uvmMap.set("from", from);
        uvmMap.set("to", to);
        uvmMap.set("tokenId", tokenId);
        emit("Transfer", json.dumps(uvmMap));
    }

    public final void _mint(ERC721ForeverRewardContract self, String to, String tokenId, long feeRate) {
        Utils utils = new Utils();
        utils.require(is_valid_address(to), "ERC721: mint to the zero address");
        utils.require(_ownerOf(tokenId) == "", "ERC721: token already minted");
        _beforeTokenTransfer(self, "", to, tokenId);
        long to_count = tointeger(fast_map_get("_balances", to));
        fast_map_set("_balances", to, tostring(to_count + 1));
        fast_map_set("_owners", tostring(tokenId), to);
        fast_map_set("_allTokenMinter", tostring(tokenId), to);
        fast_map_set("_allTokenMintFee", tostring(tokenId), tostring(feeRate));
        UvmMap<String> uvmMap = new UvmMap<>();
        uvmMap.set("to", to);
        uvmMap.set("tokenId", tokenId);
        uvmMap.set("memo", "mint");
        emit("Transfer", json.dumps(uvmMap));
    }

    public final boolean _checkOnERC721Received(String from, String to, String tokenId, String _data) {
        if (is_valid_address(to)) {
            MultiOwnedContractsInterface IERC721Receiver = importContractFromAddress(MultiOwnedContractsInterface.class, to);
            if (IERC721Receiver != null) {
                boolean ret = IERC721Receiver.onERC721Received(getFromAddress(), from, tokenId, _data);
                require(ret, "ERC721: transfer to non ERC721Receiver implementer");
            } else {
                error("ERC721: transfer to non ERC721Receiver implementer");
                return false;
            }
        }
        return true;
    }

    public final void _safeMint(ERC721ForeverRewardContract self, String to, String tokenId, String data, long feeRate) {
        if (is_valid_address(to)) {
            _mint(self, to, tokenId, feeRate);
            _checkOnERC721Received(getFromAddress(), to, tokenId, data);
        } else {
            _mint(self, to, tokenId, feeRate);
        }
    }

    public final void _burn(ERC721ForeverRewardContract self, String tokenId) {
        Utils utils = new Utils();
        Object owner = _ownerOf(tokenId);
        _beforeTokenTransfer(self, tostring(owner), "", tokenId);
        _approve("", tokenId);
        long count = tointeger(fast_map_get("_balances", tostring(owner)));
        fast_map_set("_balances", tostring(owner), tostring(count - 1));
        fast_map_set("_owners", tokenId, "");

        UvmMap<String> uvmMap = new UvmMap<>();
        uvmMap.set("from", tostring(owner));
        uvmMap.set("to", "");
        uvmMap.set("tokenId", tokenId);
        uvmMap.set("memo", "burn");
        emit("Transfer", json.dumps(uvmMap));
    }


    public final boolean isBlank(String str) {
        return str == null || str.length() == 0;
    }

    public final boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
