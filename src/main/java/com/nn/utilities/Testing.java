package com.nn.utilities;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class Testing {
    public static String getSEPADueDate(int dueDate){
        Log.info("Calculating SEPA due date");
        LocalDate newDueDate = LocalDate.now().plusDays(dueDate);
        // Skip weekends and add workdays until the target number of working days is reached
        for (int i = 0; i < 1; i++) {
            newDueDate = newDueDate.plusDays(0);
            if (newDueDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
                newDueDate = newDueDate.plusDays(2); // Skip Saturday, move to Monday
            } else if (newDueDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                newDueDate = newDueDate.plusDays(1); // Skip Sunday, move to Monday
            }
        }
        return newDueDate.toString();
    }
}
