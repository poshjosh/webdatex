/*
 * Copyright 2016 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bc.webdatex.extractors.date;

import com.bc.webdatex.filters.AcceptDateHasTime;
import com.bc.webdatex.filters.Filter;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 14, 2016 2:42:21 PM
 */
public class SimpleDateFormatAddTimeIfNone extends SimpleDateFormat {

    private transient static final Logger LOG = Logger.getLogger(SimpleDateFormatAddTimeIfNone.class.getName());

    private final Filter<Date> acceptDateHasTime = new AcceptDateHasTime();
    
    private final Calendar mCalendar = new java.util.GregorianCalendar();
    
    public SimpleDateFormatAddTimeIfNone() { }

    public SimpleDateFormatAddTimeIfNone(String pattern) {
        super(pattern);
    }

    public SimpleDateFormatAddTimeIfNone(String pattern, Locale locale) {
        super(pattern, locale);
    }

    public SimpleDateFormatAddTimeIfNone(String pattern, DateFormatSymbols formatSymbols) {
        super(pattern, formatSymbols);
    }

    @Override
    public Date parse(String text) throws ParseException {

        final Date date = super.parse(text);
        
        LOG.finer(() -> "Pattern: "+this.toPattern()+", input: " + text + ", date: " + date);
        
        final Date output;

        if(date != null && !this.acceptDateHasTime.test(date)) {
            
            this.mCalendar.setTimeZone(this.getTimeZone());
         
            this.mCalendar.setTimeInMillis(System.currentTimeMillis());
            final int HOURS = this.mCalendar.get(Calendar.HOUR_OF_DAY);
            final int MINUTES = this.mCalendar.get(Calendar.MINUTE);
            final int SECONDS = this.mCalendar.get(Calendar.SECOND);
            
            this.mCalendar.setTime(date);
            this.mCalendar.set(Calendar.HOUR_OF_DAY, HOURS);
            this.mCalendar.set(Calendar.MINUTE, MINUTES);
            this.mCalendar.set(Calendar.SECOND, SECONDS);
            
            final Date update = this.mCalendar.getTime();
            
            if(LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Added time: {0}:{1}. From: {2} to {3}", 
                    new Object[]{HOURS, MINUTES, date, update});
            }

            output = update;
            
        }else{
            
            output = date;
        }
        
        return output;
    }
}
