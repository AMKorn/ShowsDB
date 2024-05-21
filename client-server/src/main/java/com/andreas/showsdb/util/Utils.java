package com.andreas.showsdb.util;

import com.andreas.showsdb.exception.ShowsDatabaseException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Set;

public class Utils {

    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    private static final Random RANDOM = new Random();

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

    public static String dateToString(Date date) {
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

    public static Set<ConstraintViolation<Object>> validate(Object o) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            return validator.validate(o);
        }
    }

    public static String saveFile(MultipartFile multipartFile, String path) throws ShowsDatabaseException {
        String originalName = multipartFile.getOriginalFilename();
        if(originalName == null) {
            throw new ShowsDatabaseException("File name null", HttpStatus.BAD_REQUEST);
        }
        String finalName = "%s%s".formatted(randomAlphaNumeric(8), originalName.replace(" ", "-"));
        try {
            File file = new File(path + finalName);
            if(file.mkdirs()) {
                multipartFile.transferTo(file);
                logger.info("Created file: {}", finalName);
                return path + finalName;
            } else throw new ShowsDatabaseException("Error creating directory", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            throw new ShowsDatabaseException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static String randomAlphaNumeric(int count) {
        try {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
            StringBuilder sb = new StringBuilder();
            while (count-- != 0) {
                int character = (RANDOM.nextInt(chars.length()));
                sb.append(chars.charAt(character));
            }
            return sb.toString();
        } catch (Exception e){
            logger.error(e.getMessage());
            return null;
        }
    }
}
