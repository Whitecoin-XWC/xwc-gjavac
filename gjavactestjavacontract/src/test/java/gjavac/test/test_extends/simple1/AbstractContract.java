package gjavac.test.test_extends.simple1;

import gjavac.lib.Contract;
import gjavac.lib.UvmContract;

/**
 * Description: gjavac
 * Created by moloq on 2022/2/17 10:41
 */
@Contract(storage = Storage.class)
public class AbstractContract extends UvmContract<Storage> {
    @Override
    public void init() {
        this.getStorage().name = "test";
    }

    public String test(String arg) {
        return arg;
    }
}
