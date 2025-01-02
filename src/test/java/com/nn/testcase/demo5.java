package com.nn.testcase;

import com.nn.basetest.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;


public class demo5 {

    @Test
    public void test1() throws Exception{
        Assert.assertTrue(false);
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
