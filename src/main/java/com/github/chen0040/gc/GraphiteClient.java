package com.github.chen0040.gc;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Created by xschen on 17/12/2016.
 */
public interface GraphiteClient {
   void sendMetric(String name, Number value);

   void sendMetrics(Map<String, Number> metrics, long timeStamp);

   @SuppressWarnings("serial") void sendMetric(String key, Number value, long timeStamp);

   void sendMetrics(Map<String, Number> metrics);

   byte[] getChartImage(int width, int height, String from, List<String> targets);

   String getChartJson(String from, String until, List<String> targets);

   List<GraphiteTimeSeries> getChartData(String from, String until, List<String> targets);

   List<String> findTargets(String path);
   String queryMetrics(String path);
   List<GraphiteMetricNode> getChildren(String path);
   List<String> getAllMetrics();

   String getChartJson(String from, List<String> targets);

   List<GraphiteTimeSeries> getChartData(String from, List<String> targets);

   String deleteMetric(String path);

   List<String> getMessageIds();
}
