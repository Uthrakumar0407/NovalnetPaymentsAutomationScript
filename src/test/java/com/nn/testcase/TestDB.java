package com.nn.testcase;

import com.nn.utilities.DBUtil;
import org.testng.Assert;
import org.testng.annotations.Test;


public class TestDB {


   @Test
    public void testDBConnect() throws Exception{
     /*  DBUtil.connectDB("192.168.2.110","Woocommerce_63", "root", "root");
       DBUtil.countOfMagentoRecords("192.168.2.110","Woocommerce_63", "root", "root"," wp_usermeta");
*/
       DBUtil.updateWooCommerceRoleToAdministrator("192.168.2.110","Woocommerce_63","root","root",2);
    }

  //  @Test
    public void test2() throws Exception{
        Assert.assertTrue(false);
    }


}
