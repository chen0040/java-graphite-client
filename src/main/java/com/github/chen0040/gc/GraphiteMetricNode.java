package com.github.chen0040.gc;


import lombok.Getter;
import lombok.Setter;

import java.util.Map;


/**
 * Created by xschen on 18/3/2017.
 */
@Getter
@Setter
public class GraphiteMetricNode {
   private int leaf;
   private Map<String,String> context;
   private String text;
   private int expandable;
   private String id;
   private int allowChildren;
}
