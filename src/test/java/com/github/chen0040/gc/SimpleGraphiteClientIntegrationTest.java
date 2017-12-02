package com.github.chen0040.gc;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


/**
 * Created by xschen on 18/12/2016.
 */
public class SimpleGraphiteClientIntegrationTest {

   private static final Logger logger = LoggerFactory.getLogger(SimpleGraphiteClientIntegrationTest.class);

   private static final String URL = "10.0.1.12"; // staging environment ip address

   @Test
   public void testRemote() {
      GraphiteClient client = new SimpleGraphiteClient(URL, 2003);
      client.sendMetric("chen0040.test.metric", 4711);
   }

   @Test
   public void testGetImage() throws IOException {
      GraphiteClient client = new SimpleGraphiteClient(URL, 2003);
      byte[] bytes = client.getChartImage(586, 300, "-24days", Arrays.asList("clientBrowser.browserType.Chrome.user.1", "clientBrowser.loginBrowser.Firefox.user.all"));
      try(FileOutputStream stream = new FileOutputStream("/tmp/graphite-image.png")){
         stream.write(bytes);
      }
   }

   @Test
   public void testGetJson() throws IOException {
      GraphiteClient client = new SimpleGraphiteClient(URL, 2003);
      String json = client.getChartJson("-24days", Arrays.asList("clientBrowser.browserType.Chrome.user.1", "clientBrowser.loginBrowser.Firefox.user.all"));

      logger.info(json);
   }


   @Test
   public void testGetData() throws IOException {
      GraphiteClient client = new SimpleGraphiteClient(URL, 2003);
      client.sendMetric("chen0040.test.metric", 4711);

      List<GraphiteTimeSeries> data = client.getChartData("-24days", Arrays.asList("chen0040.test.metric"));

      //logger.info("data: {}", JSON.toJSONString(data, SerializerFeature.PrettyFormat));

      for(int i=0; i < data.get(0).getPoints().size(); ++i){
         logger.info("{}", data.get(0).getPoints().get(i).formatTime());
      }
   }

   @Test
   public void testQueryMetrics() throws IOException {
      GraphiteClient client = new SimpleGraphiteClient(URL, 2003);
      String json = client.queryMetrics("chen0040.test");
      logger.info(json);
      json = client.queryMetrics("clientBrowser");
      logger.info(json);
   }

   @Test
   public void testGetChildren() {
      GraphiteClient client = new SimpleGraphiteClient(URL, 2003);
      List<GraphiteMetricNode> children = client.getChildren("clientBrowser");
      logger.info(JSON.toJSONString(children, SerializerFeature.PrettyFormat));

      children = client.getChildren("");
      logger.info(JSON.toJSONString(children, SerializerFeature.PrettyFormat));
   }

   @Test
   public void testGetAllMetrics() {
      GraphiteClient client = new SimpleGraphiteClient(URL, 2003);
      List<String> metrics = client.getAllMetrics();
      logger.info("metrics: {}", metrics);
   }

   @Test
   public void testDeleteMetrics() {
      GraphiteClient client = new SimpleGraphiteClient(URL, 2003);

      client.sendMetric("chen0040.test.metric", 4711);
      List<String> metrics = client.getAllMetrics();
      logger.info("metrics: {}", metrics);
      assertTrue(metrics.contains("chen0040.test.metric"));

      logger.info(client.deleteMetric("chen0040.test.metric"));

      metrics = client.getAllMetrics();
      logger.info("metrics: {}", metrics);
      assertFalse(metrics.contains("chen0040.test.metric"));
   }

   @Test
   public void testRemoteAggregationSum() throws InterruptedException {
      GraphiteClient client = new SimpleGraphiteClient(URL, 2023);

      Random random = new Random(new Date().getTime());

      for(int i=0; i < 18; ++i) {
         String domain = "chen0040";
         String measurement = "test";
         String instance = "aggregate-sum";
         int user = 1;

         int count1 = random.nextInt(100);
         client.sendMetric(domain + "." + measurement + "." + instance + ".user." + user, count1);

         user = 2;
         int count2 = random.nextInt(100);
         client.sendMetric(domain + "." + measurement + "." + instance + ".user." + user, count2);

         user = 3;
         int count3 = random.nextInt(100);
         client.sendMetric(domain + "." + measurement + "." + instance + ".user." + user, count3);

         Thread.sleep(10000L);

         logger.info("sending {} ...", count1 + count2 + count3);
      }

   }



}
