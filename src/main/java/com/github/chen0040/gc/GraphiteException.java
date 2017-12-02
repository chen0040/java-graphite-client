package com.github.chen0040.gc;


/**
 * Created by xschen on 18/12/2016.
 */
public class GraphiteException extends RuntimeException {
   private static final long serialVersionUID = 1L;

   public GraphiteException(String message) {
      super(message);
   }

   public GraphiteException(String message, Throwable cause) {
      super(message, cause);
   }
}
