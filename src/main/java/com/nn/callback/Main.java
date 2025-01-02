package com.nn.callback;

import org.testng.annotations.Test;

public class Main {

    @Test
    public void callbackTest(){

            ICallback callback = new DirectDebitSEPA();
            callback.submissionToCollection("14840900037316211");

            callback = new InstalmentInvoice();
            callback.instalment("14841400036026300",2,0);
       // callback.instalmentCancel("14841400055111349", CallbackV2.InstalmentCancelTypes.ALL_CYCLES);

    }
}
