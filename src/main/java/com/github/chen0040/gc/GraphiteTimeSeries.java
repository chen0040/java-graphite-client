package com.github.chen0040.gc;

import com.github.chen0040.gc.models.TemporalDataPoint;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by xschen on 18/3/2017.
 */
@Getter
@Setter
public class GraphiteTimeSeries {
   private String target;
   private List<TemporalDataPoint> points = new ArrayList<>();

   public GraphiteTimeSeries(){

   }

   public GraphiteTimeSeries(GraphiteSeries series) {
      target = series.getTarget();
      List<List<Double>> dpoints = series.getDatapoints();
      int pointCount = dpoints.size();

      for(int i=0; i< pointCount; ++i){
         List<Double> pt = dpoints.get(i);
         if(pt.size() < 2) {
            continue;
         }
         double value = 0.0;
         if(pt.get(0) != null){
            value = pt.get(0);
         }
         long timestamp = (long)Math.ceil(pt.get(1));
         TemporalDataPoint tpt = new TemporalDataPoint();
         tpt.setValue(value);
         tpt.setTime(timestamp);
         points.add(tpt);
      }

   }
}
