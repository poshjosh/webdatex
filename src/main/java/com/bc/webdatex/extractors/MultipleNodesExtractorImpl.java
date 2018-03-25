package com.bc.webdatex.extractors;

import com.bc.webdatex.visitors.CommentBoundsVisitor;
import com.bc.json.config.JsonConfig;
import com.bc.util.Log;
import com.bc.webdatex.extractors.node.NodeExtractor;
import com.bc.webdatex.extractors.node.NodeExtractorImpl;
import com.bc.webdatex.config.Config;
import com.bc.webdatex.context.CapturerContext;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import com.bc.webdatex.extractors.node.NodeExtractorConfig;

public class MultipleNodesExtractorImpl extends PageExtractorImpl implements MultipleNodesExtractor {
    
  private Map<String, NodeExtractor> nodeVisitors;
  private Map<String, MappingsExtractor> mappingsExtractors;
  private final Map<String, Object[]> columns;
  private final CommentBoundsVisitor withinComments;
  
  private final class HashMapNoNulls extends HashMap {
    public NodeExtractor put(String key, NodeExtractor value) {
      if ((key == null) || (value == null)) throw new NullPointerException();
      return (NodeExtractor)super.put(key, value);
    }
  }
  
  public MultipleNodesExtractorImpl(CapturerContext context){
    super(context);
    
    JsonConfig config = context.getConfig();
    
    this.nodeVisitors = new HashMapNoNulls();
    
    this.columns = new HashMapNoNulls();
    String parentNode = config.getString(new Object[] { "parentNode", "value" });
    if (parentNode != null) {
      addExtractor("parentNode");
    }
    
    int maxFiltersPerKey = config.getInt(new Object[] { Config.Extractor.maxFiltersPerKey });
    
    for (int i = 0; i < maxFiltersPerKey; i++) {
        
      String propertyKey = "targetNode" + i;
      
      addExtractor(propertyKey);
      
      MappingsExtractor mappingsExt = MappingsExtractor.getInstance(propertyKey, config);
      
      if (mappingsExt != null){

        if (this.mappingsExtractors == null) {
          this.mappingsExtractors = new HashMap();
        }
        this.mappingsExtractors.put(propertyKey, mappingsExt);
      }
    }
    this.withinComments = new CommentBoundsVisitor();
  }
  
  @Override
  public void reset() {
      
    super.reset();
    
    this.withinComments.reset();
    
    for (NodeExtractor extractor : this.nodeVisitors.values()) {
      extractor.reset();
    }
  }
  
  @Override
  public boolean isSuccessfulCompletion(Set<String> columns) {
    boolean output;
    if (columns != null) {
      output = getExtractedData().size() >= columns.size();
    } else {
      output = getFailedNodeExtractors().isEmpty();
    }
    return output;
  }
  
  @Override
  public void finishedParsing() {

    for (String name : this.nodeVisitors.keySet()) {
        
      NodeExtractor extractor = (NodeExtractor)this.nodeVisitors.get(name);
      
      extractor.finishedParsing();
      
      Object[] cols = (Object[])this.columns.get(name);
      
      boolean append = extractor.isConcatenateMultipleExtracts();
      
      if (cols != null) {
          
        for (Object column : cols) {
            
          Log.getInstance().log(Level.FINEST, "Extractor: {0}", getClass(), name);
          
          add(column.toString(), extractor.getExtract(), append, false);
        }
      }
    }
    Log.getInstance().log(Level.FINER, "Extractors: {0}, Extracted data: {1}", 
            getClass(), this.nodeVisitors.size(), getExtractedData().size());
  }
  
  @Override
  public Set<String> getFailedNodeExtractors() {
    HashSet<String> failed = new HashSet();
    Set keys = getExtractedData().keySet();
    for (String key : this.nodeVisitors.keySet()) {
      NodeExtractor extractor = (NodeExtractor)this.nodeVisitors.get(key);
      Object[] cols = (Object[])this.columns.get(key);
      if (cols != null) {
        for (Object col : cols) {
          if (!keys.contains(col)) {
            failed.add(extractor.getId());
            break;
          }
        }
      }
    }
    return failed;
  }
  
  @Override
  public Set<String> getSuccessfulNodeExtractors() {
      
    Set<String> failed = getFailedNodeExtractors();
    Set<String> all = getNodeExtractorIds();
    all.removeAll(failed);
    return all;
  }
  
  @Override
  public Set<String> getNodeExtractorIds() {
      
    return new HashSet(this.nodeVisitors.keySet());
  }
  
  @Override
  public void visitTag(Tag tag) {
      
    Log.getInstance().log(Level.FINER, "visitTag: {0}", getClass(), tag);
    
    this.withinComments.visitTag(tag);
    
    if (rejectComment(tag)) {
      return;
    }
    Log.getInstance().log(Level.FINER, "Extracting with: {0}", getClass(), this.nodeVisitors.keySet());
    

    for (String key : this.nodeVisitors.keySet()) {
      NodeExtractor extractor = (NodeExtractor)this.nodeVisitors.get(key);
      extractor.visitTag(tag);
    }
  }
  
  @Override
  public void visitEndTag(Tag tag) {
      
    Log.getInstance().log(Level.FINER, "visitEndTag: {0}", getClass(), tag);
    
    this.withinComments.visitEndTag(tag);
    
    if (rejectComment(tag)) {
      return;
    }
    
    for (String key : this.nodeVisitors.keySet()) {
      NodeExtractor extractor = (NodeExtractor)this.nodeVisitors.get(key);
      extractor.visitEndTag(tag);
    }
  }
  
  @Override
  public void visitStringNode(Text node) {
      
    Log.getInstance().log(Level.FINER, "visitStringNode: {0}", getClass(), node);
    
    this.withinComments.visitStringNode(node);
    
    if (rejectComment(node)) {
      return;
    }
    
    for (String key : this.nodeVisitors.keySet()) {
      NodeExtractor extractor = (NodeExtractor)this.nodeVisitors.get(key);
      extractor.visitStringNode(node);
    }
    
    if (this.mappingsExtractors == null) {
      return;
    }
    
    Map m;
    for (String id : this.mappingsExtractors.keySet())
    {
      MappingsExtractor extractor = (MappingsExtractor)this.mappingsExtractors.get(id);
      
      String text = node.getText();
//      text = Translate.decode(text);
      
      m = extractor.extractData(text);
      
      if (m != null)
      {
        for (Object key : m.keySet())
        {
          add(key.toString(), m.get(key), false, false);
        }
      }
    }
  }
  
  @Override
  public void visitRemarkNode(Remark node) {
      
    Log.getInstance().log(Level.FINER, "visitRemarkNode: {0}", getClass(), node);
    
    this.withinComments.visitRemarkNode(node);
  }
  
  @Override
  public NodeExtractor getExtractor(String id) {
      
    return (NodeExtractor)this.nodeVisitors.get(id);
  }
  
  @Override
  public NodeExtractor createExtractor(String id) {
      
    final NodeExtractorConfig config = getCapturerContext().getNodeExtractorConfig();
    final NodeExtractorImpl extractor = new NodeExtractorImpl(config, id);
    
    return extractor;
  }
  
  private void addExtractor(String id) {
      
    NodeExtractorConfig cs = getCapturerContext().getNodeExtractorConfig();
    
    Object[] cols = cs.getColumns(id);
    
    if (cols == null) {
      Log.getInstance().log(Level.FINER, "{0}.{1} == null", getClass(), id, Config.Extractor.columns);
    } else {
      this.columns.put(id, cols);
      NodeExtractor extractor = createExtractor(id);
      this.nodeVisitors.put(id, extractor);
    }
    
    Log.getInstance().log(Level.FINER, "Added Extractor for property key: {0}", getClass(), id);
  }
  
  private boolean rejectComment(Node node) {
    boolean reject = (this.withinComments.isStarted()) && (!this.withinComments.isDone());
    if (!reject) {
      reject = this.withinComments.isComment(node);
    }
    return reject;
  }
}