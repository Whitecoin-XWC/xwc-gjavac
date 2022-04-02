package gjavac.test.issuethree;

import gjavac.lib.Contract;
import gjavac.lib.Offline;
import gjavac.lib.UvmContract;
import gjavac.lib.UvmMap;

/**
 * Description: gjavac
 * Created by moloq on 2021/12/10 15:54
 */

@Contract(storage = Storage.class)
public class IssueThreeContract extends UvmContract<Storage> {

    @Override
    public void init() {
        this.getStorage().uvmMap = UvmMap.create();
    }

    @Offline
    public void test() {
        this.getStorage().uvmMap.set("test", "hello");
    }
}
