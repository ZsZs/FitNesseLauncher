package com.processpuzzle.fitnesse.launcher.maven.plugin.fitnesse.util;

import org.apache.maven.plugin.logging.Log;

import fitnesse.socketservice.SocketService;

public class FitNesseThreadLocator {
   private static final String FITNESSE_SOCKET_SERVICE = SocketService.class.getName();
   private final Log log;
   
   public FitNesseThreadLocator( final Log log ) {
      this.log = log;
   }
   
   public Thread findFitNesseServerThread() {
      final Thread[] activeThreads = findActiveThreads( 3 );
      for( int i = activeThreads.length - 1; i >= 0; i-- ){
         final StackTraceElement[] trace = activeThreads[i].getStackTrace();
         for( int j = trace.length - 1; j >= 0; j-- ){
            if( FITNESSE_SOCKET_SERVICE.equals( trace[j].getClassName() ) ){
               return activeThreads[i];
            }
         }
      }
      this.log.warn( "Could not identify FitNesse service Thread." );
      return null;
   }

   private Thread[] findActiveThreads( final int arraySize ) {
      final Thread[] activeThreads = new Thread[arraySize];
      final int threadsFound = Thread.currentThread().getThreadGroup().enumerate( activeThreads, false );
      if( threadsFound < arraySize ){
         final Thread[] foundThreads = new Thread[threadsFound];
         System.arraycopy( activeThreads, 0, foundThreads, 0, threadsFound );
         return foundThreads;
      }
      return findActiveThreads( arraySize + arraySize );
   }
}
