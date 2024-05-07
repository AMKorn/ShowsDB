package com.andreas.showsdb.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class Utils {

    private static final String DATE_FORMAT = "dd/MM/yyyy";

    private Utils() {
        throw new IllegalStateException("Utility class");
    }

    public static Date parseDate(String date) {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String dateToString(Date date){
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

    public static Set<ConstraintViolation<Object>> validate(Object o) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            return validator.validate(o);
        }
    }
}
