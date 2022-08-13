package thread;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ReusingThreadTest {

  @Test
  public void testReusingThread() throws InterruptedException, ExecutionException {
    // Create a pool of threads having only one thread.
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    ExecutorCompletionService<String> executorCompletionService = new ExecutorCompletionService<>(executorService);

    AtomicInteger executionNumber = new AtomicInteger(0);
    Callable<String> task = () -> {
      var threadName = Thread.currentThread().getName();
      return "thread name: " + threadName + ", number of execution: " + executionNumber.incrementAndGet();
    };

    // Put two tasks in the queue.
    executorCompletionService.submit(task);
    executorCompletionService.submit(task);

    Future<String> firstTaskResult = executorCompletionService.take();
    Future<String> secondTaskResult = executorCompletionService.take();
    assertThat(firstTaskResult.get(), is("thread name: pool-1-thread-1, number of execution: 1"));
    assertThat(secondTaskResult.get(), is("thread name: pool-1-thread-1, number of execution: 2"));
    executorService.shutdown();
  }

}
