package thread;

import java.lang.Thread.State;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import util.CpuIntensiveAlgorithm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CreateThreadTest {

  @Test
  public void testCreateThread() throws InterruptedException {
    AtomicBoolean changed = new AtomicBoolean(false);
    Thread thread = new Thread(() -> {
      // executed concurrently with main thread
      changed.compareAndSet(false, true);
    });

    // Before starting the thread, the thread is not running.
    assertThat(thread.isAlive(), is(false));
    assertThat(thread.getState(), is(Thread.State.NEW));
    assertThat(changed.get(), is(false));

    // Start the thread.
    thread.start();
    assertThat(thread.isAlive(), is(true));
    assertThat(thread.getState(), is(Thread.State.RUNNABLE));
    assertThat(changed.get(), is(false));

    // Wait for the thread to finish.
    thread.join();
    assertThat(thread.isAlive(), is(false));
    assertThat(thread.getState(), is(Thread.State.TERMINATED));
    assertThat(changed.get(), is(true));
  }

  @Test
  public void testThreadInWaitingState() throws InterruptedException {
    Thread thread = new Thread(() -> {
      // executed concurrently with main thread
      var child = new Thread(CpuIntensiveAlgorithm::run1s);
      child.start();
      try {
        child.join();
      } catch (InterruptedException e) {
        //ignore in test
      }
    });
    thread.start();
    // let the child thread
    thread.join(100);
    assertThat(thread.getState(), is(Thread.State.WAITING));
  }

  @Test
  public void testThreadInTimedWaitingState() throws InterruptedException {
    Thread thread = new Thread(() -> {
      // executed concurrently with main thread
      var child = new Thread(CpuIntensiveAlgorithm::run1s);
      child.start();
      try {
        child.join(200);
      } catch (InterruptedException e) {
        //ignore in test
      }
    });
    thread.start();
    thread.join(100);
    assertThat(thread.getState(), is(State.TIMED_WAITING));
  }

  @Test
  public void testChildThreadIsIndependentFromParent() throws InterruptedException {
    AtomicBoolean calculationIsDone = new AtomicBoolean(false);
    Thread childThread = new Thread(() -> {
      CpuIntensiveAlgorithm.run100Ms();
      calculationIsDone.set(true);
    });
    Thread parentThread = new Thread(childThread::start);
    parentThread.start();
    parentThread.join(100);
    assertThat(parentThread.isAlive(), is(false));
    assertThat(childThread.isAlive(), is(true));
    childThread.join(200);
    assertThat(calculationIsDone.get(), is(true));
  }

}
