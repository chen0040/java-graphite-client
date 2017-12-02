package com.github.chen0040.gc;


import lombok.Getter;
import lombok.Setter;

import java.util.List;


/**
 * Created by xschen on 3/17/17.
 */
@Setter
@Getter
public class GraphiteSeries {
   private String target;
   private List<List<Double>> datapoints;
}
