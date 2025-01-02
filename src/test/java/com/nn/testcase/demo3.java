package com.nn.testcase;

import com.nn.basetest.BaseTest;
import org.testng.Assert;
import org.testng.annotations.*;


public class demo3  {

    @BeforeSuite
    public void beforeSuite(){
        System.out.println("I am at before suite");
       //Assert.assertTrue(false);
    }

    @AfterSuite
    public void afterSuite(){
        System.out.println("I am at after suite");
       // Assert.assertTrue(false);
    }

    @BeforeTest(alwaysRun = true)
    public void beforeTest(){
        System.out.println("I am at before test");
       // Assert.assertTrue(false);
    }

    @AfterTest
    public void afterTest(){
        System.out.println("I am at after test");
        //Assert.assertTrue(false);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass(){
        System.out.println("I am at before class");
       // Assert.assertTrue(false);
    }

    @AfterClass
    public void afterClass(){
        System.out.println("I am at after class");
        //Assert.assertTrue(false);
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(){
        System.out.println("I am at before method");
       // Assert.assertTrue(false);
    }

    @AfterMethod
    public void afterMethod(){
        System.out.println("I am at after method");
       // Assert.assertTrue(false);
    }

   @Test
    public void test1() throws Exception{
       System.out.println("I am at test 1");
    }

  //  @Test
    public void test2() throws Exception{
      //  Assert.assertTrue(false);
    }



}
