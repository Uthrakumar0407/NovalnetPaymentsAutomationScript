package com.nn.testcase;

import com.nn.basetest.BaseTest;
import com.nn.utilities.DBUtil;
import org.testng.Assert;
import org.testng.annotations.Test;


public class demo4  {

    @Test
    public void test1() throws Exception{
        DBUtil.updateWooCommerceProductName("192.168.2.91","wordpress_6_2_db" , "root", "novalnet");
    }

    @Test
    public void test2() throws Exception{
        Assert.assertTrue(false);
    }
    @Test
    public void test3() throws Exception{
        Assert.assertTrue(true);
    }
    @Test
    public void test4() throws Exception{
        Assert.assertTrue(true);
    }
   @Test
    public void test5() throws Exception{
        Assert.assertTrue(false);
    }


}
