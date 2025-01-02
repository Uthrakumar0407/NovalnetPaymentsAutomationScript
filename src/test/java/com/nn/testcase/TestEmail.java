package com.nn.testcase;

import com.nn.utilities.ThunderBirdEmailHelper;

import javax.mail.Store;

public class TestEmail {

    public static void main (String a[]) {
        Store store = ThunderBirdEmailHelper.connectToThunderBird();

      //ThunderBirdEmailHelper.deleteAllJunkEmails(store,"Undelivered Mail Returned to Sender");
   //    ThunderBirdEmailHelper.deleteAllEmails(store,"Novalnet Callback Script");
    //    ThunderBirdEmailHelper.deleteAllEmails(store,"callback");

  //  ThunderBirdEmailHelper.deleteAllJunkEmails(store,"order");
    //  ThunderBirdEmailHelper.deleteAllEmails(store,"Demoshop");
    //    ThunderBirdEmailHelper.deleteAllEmails(store,"Order");
        //ThunderBirdEmailHelper.deleteAllEmails(store,"Demoshop");
     //   ThunderBirdEmailHelper.deleteAllJunkEmails(store,"Demoshop");
      //  ThunderBirdEmailHelper.deleteAllEmails(store,"Warning: could not send message for past 4 hours");
     //  ThunderBirdEmailHelper.deleteAllEmails(store,"Returned");
      //ThunderBirdEmailHelper.deleteAllEmails(store,"Barzahlen/viacash");
    //    ThunderBirdEmailHelper.deleteAllEmails(store,"Returned");
        ThunderBirdEmailHelper.deleteAllEmails(store,"SANDBOX");
     //   ThunderBirdEmailHelper.deleteAllEmails(store,"Payment Confirmation");

       // ThunderBirdEmailHelper.deleteAllEmails(store,"Booking Confirmation");
      //  ThunderBirdEmailHelper.deleteAllEmails(store,"Payment Confirmation");
    }

}
