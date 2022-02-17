package gjavac.test.test_extends.simple3;

import gjavac.lib.Contract;
import gjavac.lib.Offline;

/**
 * Description: gjavac
 * Created by moloq on 2022/2/17 10:41
 */
@Contract(storage = Storage.class)
public class DemoContract extends AbstractContract {

    @Offline
    public String son(String arg) {
        return this.test(arg);
    }
}
