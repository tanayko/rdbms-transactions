package com.lseg.acadia.skills.rdbmstx.demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

class ThreadTests extends AbstractDatabaseTest {
  private static final Logger logger
      = LoggerFactory.getLogger(ThreadTests.class);

  @Test
  public void multiThreadExample() throws Exception {

    final ExecutorService executor = Executors.newCachedThreadPool();

    final CountDownLatch t1step1 = new CountDownLatch(1);
    final CountDownLatch t1step2 = new CountDownLatch(1);
    final CountDownLatch t1step3 = new CountDownLatch(1);

    final CountDownLatch t2step1 = new CountDownLatch(1);
    final CountDownLatch t2step2 = new CountDownLatch(1);
    final CountDownLatch t2step3 = new CountDownLatch(1);

    // T1
    final Future<?> t1future = executor.submit(() -> {
      try {
        // first, nothing to wait on
        System.out.println("T1step1 (first)");
        t1step1.countDown(); // release T1step1 latch

        // wait for T2step1
        Assertions.assertTrue(t2step1.await(5, TimeUnit.SECONDS));
        System.out.println("T1step2 (after T2step1)");
        t1step2.countDown(); // release T1step2 latch

        // wait for T2step2
        Assertions.assertTrue(t2step2.await(5, TimeUnit.SECONDS));
        System.out.println("T1step3 (after T2step2)");
        t1step3.countDown(); // release T1step3 latch

        // wait for T2step3
        Assertions.assertTrue(t2step3.await(5, TimeUnit.SECONDS));
        System.out.println("T1 done!");


      } catch (Exception e) {
        if (e instanceof RuntimeException) {
          throw (RuntimeException) e;
        }
        throw new RuntimeException(e);
      }
    });

    // T2
    final Future<?> t2future = executor.submit(() -> {
      try {

        // wait for T1step1
        Assertions.assertTrue(t1step1.await(5, TimeUnit.SECONDS));
        System.out.println("T2step1 (after T1step1)");
        t2step1.countDown(); // release T2step1 latch

        // wait for T1step2
        Assertions.assertTrue(t1step2.await(5, TimeUnit.SECONDS));
        System.out.println("T2step2 (after T1step2)");
        t2step2.countDown(); // release T2step2 latch

        // wait for T1step3
        Assertions.assertTrue(t1step3.await(5, TimeUnit.SECONDS));
        System.out.println("T2step3 (after T1step3)");
        t2step3.countDown(); // release T2step3 latch

        System.out.println("T2 done!");


      } catch (Exception e) {
        if (e instanceof RuntimeException) {
          throw (RuntimeException) e;
        }
        throw new RuntimeException(e);
      }
    });

    // wait for threads T1 and T2 to complete and ensure they didn't throw
    Assertions.assertDoesNotThrow(() -> t1future.get());
    Assertions.assertDoesNotThrow(() -> t2future.get());


  }

}
