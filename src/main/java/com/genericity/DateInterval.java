package com.genericity;

import java.time.LocalDate;

public class DateInterval extends Pair<LocalDate> {

    @Override
    public void setSecond(LocalDate second) {
        System.out.println("复写");
        super.setSecond(second);
    }
}
