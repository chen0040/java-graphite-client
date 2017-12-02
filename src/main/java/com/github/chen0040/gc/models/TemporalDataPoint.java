package com.github.chen0040.gc.models;


import lombok.Getter;
import lombok.Setter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by xschen on 18/3/2017.
 */
@Getter
@Setter
public class TemporalDataPoint {
   private long time;
   private double value;
   private String name = "";


   @Override public String toString() {
      return "TemporalDataPoint{" +
              "time=" + time +
              ", value=" + value +
              ", name='" + name + '\'' +
              '}';
   }


   public String formatTime() {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      return dateFormat.format(new Date(time * 1000L));
   }
}
