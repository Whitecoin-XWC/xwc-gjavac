package gjavac.test.issueone;

import gjavac.lib.Contract;
import gjavac.lib.Offline;
import gjavac.lib.UvmContract;

/**
 * Description: gjavac
 * Created by moloq on 2021/12/10 15:54
 */

@Contract(storage = Storage.class)
public class IssueOneContract extends UvmContract<Storage> {

    @Override
    public void init() {
        this.getStorage().name = "";
    }

    @Offline
    public void test(){
        int a = 10;
        /* if i use negative(-) before a variable,then will throw exception*/
        int b = -a;
    }
}
