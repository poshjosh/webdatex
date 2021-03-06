package com.bc.webdatex;

import com.bc.json.config.JsonConfig;
import com.bc.nodelocator.ConfigName;
import com.bc.webdatex.context.ExtractionContextFactoryImpl;
import com.bc.webdatex.filters.DefaultUrlFilter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.parser.ParseException;
import com.bc.webdatex.context.ExtractionContextFactory;

/**
 * @(#)Tools.java   27-Oct-2014 12:21:27
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public class Tools {
    
    static final URI configsDir = null;
    static final ExtractionContextFactory factory;
    static {
        factory = new ExtractionContextFactoryImpl(
                Paths.get(configsDir).toFile()
        );
    }
    
    private static Pattern referencePattern;
    private static String resolveReferences(Properties props, String value) {
        if(referencePattern == null) {
            referencePattern = Pattern.compile("\\$\\{(.+?)\\}");
        }
        Matcher matcher = referencePattern.matcher(value);
        StringBuffer buff = null;
        while(matcher.find()) {
            String key = matcher.group(1);
            String val = props.getProperty(key);
            if(val == null) {
                val = System.getProperty(key);
            }
            if(val != null) {
                if(buff == null) {
                    buff = new StringBuffer();
                }
                matcher.appendReplacement(buff, val);
            }
        }
        String output;
        if(buff != null) {
            matcher.appendTail(buff);
            output = buff.toString();
        }else{
            output = value;
        }
        return output;
    }
    
    public static void main(String [] args) {

        try{
            
            Properties props = new Properties();
            props.setProperty("username", "Nonso");
            props.setProperty("color", "Blue");
            props.setProperty("greeting", "Hello ${username}, your favorite color is ${color}");
            props.setProperty("question", "Please enter your password");
            
            String value = props.getProperty("greeting");
System.out.println(value+" resolved to "+resolveReferences(props, value));
            value = props.getProperty("question");
System.out.println(value+" resolved to "+resolveReferences(props, value));
if(true) {
    return;
}
//            String s = "C:\\Users/USER";
//System.out.println(s);
//System.out.println(s.replaceAll("/", "[X]"));
//System.out.println(s.replaceAll("\\\\", "[X]"));
//if(true) {
//    return;
//}

            DefaultUrlFilter duf = new DefaultUrlFilter();
            duf.setRequiredPattern(Pattern.compile("/\\d{4}/\\d{1,2}/\\d{1,2}/"));
System.out.println(duf.test("/2016/01/21/we-will-make-nigeria-work-again-buhari-promises/"));
System.out.println(duf.test("/2016/01/20/we-will-make-nigeria-work-again-buhari-promises/"));
System.out.println(duf.test("/2016/01/19/we-will-make-nigeria-work-again-buhari-promises/"));
            duf.setRequiredPattern(null);
            duf.setUnwantedPattern(Pattern.compile("/\\d{4}/\\d{1,2}/\\d{1,2}/"));
System.out.println(duf.test("/2016/01/21/we-will-make-nigeria-work-again-buhari-promises/"));
System.out.println(duf.test("/2016/01/20/we-will-make-nigeria-work-again-buhari-promises/"));
System.out.println(duf.test("/2016/01/19/we-will-make-nigeria-work-again-buhari-promises/"));
if(true) {
    return;
}            
            
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat();
            sdf.applyPattern("yyyy");
            Date date = sdf.parse("2016");
System.out.println(date);
            
            Calendar cal = Calendar.getInstance();
            cal.clear();
            cal.set(Calendar.YEAR, 2016);
System.out.println(cal.getTime());            
            if(date.equals(cal.getTime())) {
System.out.println("YES");
            }
            
        }catch(Exception e) {
            
            e.printStackTrace();
        }
    }
    
    public void run1() throws IOException, ParseException {

        final List<String> sitenames = factory.getConfigService().getConfigNames();
        
        List<String> forUpdate = new ArrayList<String>();
        
        boolean started = false;
        
        for(String sitename:sitenames) {
            
            if("default".equals(sitename)) {
                continue;
            }
            
            if(!started) {
                started = true;
            }
            
            if(!started) {
                continue;
            }
            
System.out.println(sitename);            
            JsonConfig config = factory.getConfigService().getConfig(sitename, null);
            
            List values = config.getList(ConfigName.datePatterns);

System.out.println("Date patterns: "+values);                    

            if(values == null) {
                continue;
            }
            
            List defaults = config.getDefaultsOrException().getList(ConfigName.datePatterns);

System.out.println("Default dates: "+defaults);                    

            if(defaults.containsAll(values) && !defaults.equals(values)) {

System.out.println("YES!!!");
                forUpdate.add(sitename);
            }
        }
        
System.out.println();        
        this.set(forUpdate, ConfigName.datePatterns, null, true);
    }
    
    /**
     * If val is null, method #remove is called rather than #set
     */
    public void set(List<String> names, Object key, Object val, boolean sync) 
            throws IOException {
        
        for(String name:names) {

            try{
                
                JsonConfig config = factory.getConfigService().getConfig(name, null);
                
                if(val == null) {
                    config.remove(key);
                }else{
                    config.setObject(key, val);
                }

System.out.println("Updating: "+name+"."+key+" = "+val);
                factory.getConfigService().save(config);

//System.out.println("Syncing: "+name);
//                factory.sync(config);
                
            }catch(IOException | org.json.simple.parser.ParseException e) {

System.out.println(name+". "+e);                
            }
        }
    }
}
/**
 * 
    private void run0() throws IOException {

        List<String> minData = Arrays.asList(new String[]{"default"});
        
        this.set(minData, Config.Extractor.minDataToAccept, 2, true);
        
        List<String> disable = Arrays.asList(new String[]{"kara", "fouani"});
        
        this.set(disable, Config.Extractor.disabled, true, true);
        
        List<String> explicit = Arrays.asList(new String[]{"second", "jumia_ng"});

        this.set(explicit, Config.Extractor.hasExplicitLinks, true, true);
    }

 * 
 */