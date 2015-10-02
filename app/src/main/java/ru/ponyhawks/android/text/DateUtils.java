package ru.ponyhawks.android.text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 23:11 on 23/09/15
 *
 * @author cab404
 */
public class DateUtils {

//    private final static

    public static String formPreciseDate(Calendar date) {
        if (date == null) return "BAD-WOLF";
        Date current = Calendar.getInstance().getTime();
        Date time = date.getTime();
        long diff = current.getTime() - time.getTime();

        if (diff < TimeUnit.DAYS.toMillis(1))
            return SimpleDateFormat.getTimeInstance(DateFormat.MEDIUM).format(time);
        if (diff < TimeUnit.DAYS.toMillis(30))
            return SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(time);
        return SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(time);
    }

}
