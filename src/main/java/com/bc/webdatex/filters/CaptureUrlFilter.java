package com.bc.webdatex.filters;

import com.bc.json.config.JsonConfig;
import com.bc.nodelocator.ConfigName;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class CaptureUrlFilter extends DefaultUrlFilter {
    
  private transient static final Logger LOG = Logger.getLogger(CaptureUrlFilter.class.getName());
  
  public CaptureUrlFilter(JsonConfig config) {
      
    setId("captureUrlFilter");
    
    Object[] arr = config.getArray(new Object[] { ConfigName.captureUrlFilter_required });
    if ((arr != null) && (arr.length > 0)) {
      setRequired((String[])Arrays.copyOf(arr, arr.length, String[].class));
    }
    
    arr = config.getArray(new Object[] { ConfigName.captureUrlFilter_unwanted });
    if ((arr != null) && (arr.length > 0)) {
      setUnwanted((String[])Arrays.copyOf(arr, arr.length, String[].class));
    }
    
    if(LOG.isLoggable(Level.FINER)){
      LOG.log(Level.FINER, "Text::\nRequired: {0}\nUnwanted: {1}",new Object[]{ getRequired() == null ? null : Arrays.toString(getRequired()),  getUnwanted() == null ? null : Arrays.toString(getUnwanted())});
    }

    String regex = config.getString(new Object[] { ConfigName.captureUrlFilter_requiredRegex });
    
    if ((regex != null) && (!regex.trim().isEmpty())) {
      setRequiredPattern(Pattern.compile(regex, 2));
    }
    
    regex = config.getString(new Object[] { ConfigName.captureUrlFilter_unwantedRegex });
    if ((regex != null) && (!regex.trim().isEmpty())) {
      setUnwantedPattern(Pattern.compile(regex, 2));
    }
    if(LOG.isLoggable(Level.FINER)){
      LOG.log(Level.FINER, "Regex::\nRequired: {0}\nUnwanted: {1}",new Object[]{ getRequiredPattern(),  getUnwantedPattern()});
    }
  }
}
