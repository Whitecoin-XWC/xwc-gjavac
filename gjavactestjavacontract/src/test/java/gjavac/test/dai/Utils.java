package gjavac.test.dai;

import gjavac.lib.Component;
import gjavac.lib.*;
import gjavac.lib.UvmCoreLibs;
import static gjavac.lib.UvmCoreLibs.*;

@Component
public class Utils {
    public String getFromAddress() {
        //支持合约作为代币持有者
        String fromAddress;
        String prev_contract_id = UvmCoreLibs.get_prev_call_frame_contract_address();
        if (prev_contract_id != null && UvmCoreLibs.is_valid_contract_address(prev_contract_id)) {
            //如果来源方是合约时
            fromAddress = prev_contract_id;
        } else {
            fromAddress = UvmCoreLibs.caller_address();
        }
        return fromAddress;
    }

    public void checkOwner(DaiPriceFeederForCdcContract self) {
        String ownerAddress = self.getStorage().owner;
        String callerAddress = UvmCoreLibs.caller_address();
        if(ownerAddress != callerAddress){
            UvmCoreLibs.error("you are not owner of price feeder contract, can't call this function");
        }
    }

    //parse a,b,c format string to [a,b,c]
    public UvmArray<String> parseArgs(String arg, int count,  String errorMsg) {
        if (arg == null) {
            UvmCoreLibs.error(errorMsg);
            return UvmArray.create();
        } else {
            UvmStringModule stringModule = (UvmStringModule)UvmCoreLibs.importModule(UvmStringModule.class, "string");
            UvmArray parsedArray = stringModule.split(arg, ",");
            if (parsedArray != null && parsedArray.size() == count) {
                return parsedArray;
            } else {
                UvmCoreLibs.error(errorMsg);
                return UvmArray.create();
            }
        }
    }

    public boolean checkAddress( String address) {
        boolean result = UvmCoreLibs.is_valid_address(address);
        if (!result) {
            UvmCoreLibs.error("address format error");
            return false;
        }
        return true;
    }

    public String meanOfArray(UvmArray<String> priceArray) {
        if(priceArray == null) {
            return null;
        }
        int iCount = priceArray.size();
        if(iCount < 1) {
            return null;
        }

        //UvmSafeMathModule safeMathModule = (UvmSafeMathModule)UvmCoreLibs.importModule(UvmSafeMathModule.class, "safemath");
        //UvmBigInt sum = safeMathModule.bigint(0);
        double sum = UvmCoreLibs.tonumber("0");

        for(int iIdx = 1; iIdx <= iCount; iIdx++) {
            //sum = safeMathModule.add(safeMathModule.bigint(priceArray.get(iIdx)), sum);
            sum = sum + UvmCoreLibs.tonumber(priceArray.get(iIdx));
        }
        //UvmBigInt meanPrice = safeMathModule.div(sum, safeMathModule.bigint(iCount));
        double meanPrice = sum / iCount;
        //String result = safeMathModule.tostring(meanPrice);
        String result = UvmCoreLibs.tostring(meanPrice);

        return result;
    }

    public void checkState( DaiPriceFeederForCdcContract self) {
        String state = self.getStorage().state;
        if(state != DaiPriceFeederForCdcContract.STATE_COMMON){
            UvmCoreLibs.error("state error, now state is " + state);
            return;
        }
        return;
    }

/*
    public UvmArray<String> sortArray(UvmArray<String> originalArray) {
        if(originalArray == null || originalArray.size() == 1) {
            return originalArray;
        }

        UvmTableModule uvmTableModule = (UvmTableModule)UvmCoreLibs.importModule(UvmTableModule.class, "table");
        //UvmSafeMathModule safeMathModule = (UvmSafeMathModule)UvmCoreLibs.importModule(UvmSafeMathModule.class, "safemath");
        UvmArray<String> sortedArray = UvmArray.<String>create();
        int iCount = originalArray.size();
        String currentRecStr = null;
        //UvmBigInt currentRecValue = null;
        double currentRecValue;

        for (int iIdx = 1; iIdx <= iCount; iIdx++) {
            currentRecStr = originalArray.get(iIdx);
            //currentRecValue = safeMathModule.bigint(currentRecStr);
            currentRecValue = UvmCoreLibs.tonumber(currentRecStr);

            if(iIdx == 1) {
                sortedArray.set(iIdx, currentRecStr);
            } else {
                int i = 1;
                while(i <= iIdx - 1) {
                    //if(safeMathModule.lt(currentRecValue, safeMathModule.bigint(sortedArray.get(i)))){
                    if(currentRecValue < UvmCoreLibs.tonumber(sortedArray.get(i))){
                        uvmTableModule.insert(sortedArray, i, currentRecStr);
                        break;
                    }
                    i++;
                }
                if(i > sortedArray.size()) {
                    uvmTableModule.append(sortedArray, currentRecStr);
                }
            }
        }

        return sortedArray;
    }
*/

    //计算喂价均值
    public String calculatePrice(UvmArray<String> feedPrices) {
        if(feedPrices == null || feedPrices.size() == 0) {
            return null;
        }
        String result = meanOfArray(feedPrices);
        return result;
    }

    public boolean arrayContains(UvmArray<String> strArray, String strToFind) {
        if(strArray == null || strToFind == null) {
            return false;
        }
        int iCount = strArray.size();
        for(int iIdx = 1; iIdx <= iCount; iIdx++) {
            if(strToFind == strArray.get(iIdx)){
                return true;
            }
        }
        return false;
    }

    public int findElementInArray(UvmArray<String> strArray, String strToFind, String strErrorMsg) {
        if(strArray == null || strToFind == null) {
            UvmCoreLibs.error(strErrorMsg);
            return 0;
        }
        int iCount = strArray.size();
        for(int iIdx = 1; iIdx <= iCount; iIdx++) {
            if(strToFind == strArray.get(iIdx)){
                return iIdx;
            }
        }
        UvmCoreLibs.error(strErrorMsg);
        return 0;
    }

}
