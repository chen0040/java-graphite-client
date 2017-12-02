package com.github.chen0040.gc;


import com.alibaba.fastjson.JSON;
import com.github.chen0040.gc.utils.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Created by xschen on 18/12/2016.
 */
public class SimpleGraphiteClient implements GraphiteClient {

   private String host;
   private int port;

   private static final Logger logger = LoggerFactory.getLogger(SimpleGraphiteClient.class);

   private static final String DATA_ENCODING = "UTF-8";

   public SimpleGraphiteClient(String host, int port) {
      this.host = host;
      this.port = port;
   }

   @Override public void sendMetric(String key, Number value) {
      sendMetric(key, value, getCurrentTimestamp());
   }

   @Override
   public void sendMetrics(Map<String, Number> metrics, long timeStamp) {
      try {
         Socket socket = createSocket();
         OutputStream s = socket.getOutputStream();
         PrintWriter out = new PrintWriter(s, true);
         for (Map.Entry<String, Number> metric: metrics.entrySet()) {
            out.printf("%s %s %d%n", metric.getKey(), metric.getValue(), timeStamp);
         }
         out.close();
         socket.close();
      } catch (UnknownHostException e) {
         throw new GraphiteException("Unknown host: " + host);
      } catch (IOException e) {
         throw new GraphiteException("Error while writing data to graphite: " + e.getMessage(), e);
      }
   }

   @Override
   @SuppressWarnings("serial")
   public void sendMetric(final String key, final Number value, long timeStamp) {
      sendMetrics(new HashMap<String, Number>() {{
         put(key, value);
      }}, timeStamp);
   }

   @Override
   public void sendMetrics(Map<String, Number> metrics) {
      sendMetrics(metrics, getCurrentTimestamp());
   }


   private CloseableHttpClient buildClient() {
      //HttpClientBuilder builder = HttpClientBuilder.create();

      int timeout = 10;
      RequestConfig config = RequestConfig.custom().setSocketTimeout(timeout * 1000).setConnectTimeout(timeout * 1000).build();

      return HttpClients.custom().setDefaultRequestConfig(config).build(); //builder.build();

   }

   @Override public byte[] getChartImage(int width, int height, String from, List<String> targets) {
      StringBuilder sb = new StringBuilder();
      sb.append("http://".concat(host).concat("/render/?width="));
      sb.append(width);
      sb.append("&height=");
      sb.append(height);
      sb.append("&from=");
      sb.append(from);

      for(int i=0; i <targets.size(); ++i) {
         sb.append("&target=");
         sb.append(targets.get(i));
      }

      String url = sb.toString();

      byte[] bytes = null;
      try {
         CloseableHttpClient httpClient = buildClient();
         HttpGet request = new HttpGet(url);
         request.addHeader("content-type", "application/json");
         CloseableHttpResponse response = httpClient.execute(request);
         if (response.getEntity() != null) {
            bytes = EntityUtils.toByteArray(response.getEntity());
         }
         //logger.info("spark[tryReadAlgorithmModuleStatus]: "+json);
      }
      catch (Exception ex2) {
         logger.error("Failed to download image from graphite", ex2);
      }

      return bytes;
   }

   @Override public String getChartJson(String from, String until, List<String> targets) {
      StringBuilder sb = new StringBuilder();
      sb.append("http://".concat(host).concat("/render/?from="));
      sb.append(from);

      if(!StringUtils.isEmpty(until)) {
         sb.append("&until=");
         sb.append(until);
      }

      sb.append("&format=json");

      for(int i=0; i <targets.size(); ++i) {
         sb.append("&target=");
         sb.append(targets.get(i));
      }

      String url = sb.toString();
      return get(url);
   }


   @Override public String getChartJson(String from, List<String> targets) {
      StringBuilder sb = new StringBuilder();
      sb.append("http://".concat(host).concat("/render/?from="));
      sb.append(from);
      sb.append("&format=json");

      for(int i=0; i <targets.size(); ++i) {
         sb.append("&target=");
         sb.append(targets.get(i));
      }

      String url = sb.toString();
      return get(url);
   }


   @Override public List<GraphiteTimeSeries> getChartData(String from, List<String> targets) {
      String json = getChartJson(from, targets);
      List<GraphiteSeries> data = JSON.parseArray(json, GraphiteSeries.class);
      logger.info("data: {}", data.size());
      return data.stream().map(s -> new GraphiteTimeSeries(s)).collect(Collectors.toList());
   }


   @Override public String deleteMetric(String path) {
      return delete("http://".concat(host).concat(":8082").concat("/metrics/").concat(path));
   }




   @Override public List<String> getMessageIds() {
      String url = "http://".concat(host).concat(":8082").concat("/snort-ids");
      String json = get(url);
      return JSON.parseArray(json, String.class);
   }


   @Override public List<GraphiteTimeSeries> getChartData(String from, String until, List<String> targets) {
      String json = getChartJson(from, until, targets);
      List<GraphiteSeries> data = JSON.parseArray(json, GraphiteSeries.class);
      logger.info("data: {}", data.size());
      return data.stream().map(s -> new GraphiteTimeSeries(s)).collect(Collectors.toList());
   }


   // You may instead want to walk the tree as the graphite webapp does: use the /metrics/find endpoint. Start with /metrics/find?query=* and build up from there as needed (/metrics/find?query=carbon.* etc).
   @Override public List<String> findTargets(String path) {
      // String json = queryMetrics(path);
      // convert json to List<String>

      return null;
   }

   @Override
   public String queryMetrics(String path) {
      StringBuilder sb = new StringBuilder();
      sb.append("http://".concat(host).concat("/metrics/find?query="));
      sb.append(path);
      sb.append(".*");

      String url = sb.toString();

      return get(url);
   }

   private String get(String url){
      String json = null;
      try {
         CloseableHttpClient httpClient = buildClient();
         HttpGet request = new HttpGet(url);
         request.addHeader("content-type", "application/json");
         CloseableHttpResponse response = httpClient.execute(request);
         if (response.getEntity() != null) {
            json = EntityUtils.toString(response.getEntity());
         }
         //logger.info("spark[tryReadAlgorithmModuleStatus]: "+json);
      }
      catch (Exception ex2) {
         json = ex2.getMessage();
         logger.error("Failed to download image from graphite", ex2);
      }

      return json;
   }


   private String delete(String url){
      String json = null;
      try {
         CloseableHttpClient httpClient = buildClient();
         HttpDelete request = new HttpDelete(url);
         request.addHeader("content-type", "application/json");
         CloseableHttpResponse response = httpClient.execute(request);
         if (response.getEntity() != null) {
            json = EntityUtils.toString(response.getEntity());
         }
         //logger.info("spark[tryReadAlgorithmModuleStatus]: "+json);
      }
      catch (Exception ex2) {
         json = ex2.getMessage();
         logger.error("Failed to download image from graphite", ex2);
      }

      return json;
   }

   @Override public List<GraphiteMetricNode> getChildren(String path) {
      String json = queryMetrics(path);
      return JSON.parseArray(json, GraphiteMetricNode.class);
   }


   @Override public List<String> getAllMetrics() {
      String json = get("http://".concat(host).concat("/metrics/index.json"));
      return JSON.parseArray(json, String.class);
   }


   protected Socket createSocket() throws UnknownHostException, IOException {
      return new Socket(host, port);
   }

   protected long getCurrentTimestamp() {
      return System.currentTimeMillis() / 1000;
   }
}
