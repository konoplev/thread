package thread;

import java.io.*;
import java.lang.Thread.State;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import util.CpuIntensiveAlgorithm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class StopThreadTest {

  @Test
  public void testStopSleepingThread() throws InterruptedException {
    AtomicBoolean exceptionCaught = new AtomicBoolean(false);
    Thread threadToStop = new Thread(() -> {
      try {
        Thread.sleep(Long.MAX_VALUE);
      } catch (InterruptedException e) {
        exceptionCaught.compareAndSet(false, true);
      }
    });
    threadToStop.start();
    threadToStop.interrupt();
    threadToStop.join(100);
    assertThat(exceptionCaught.get(), is(true));
    assertThat(threadToStop.isAlive(), is(false));
  }

  @Test
  public void testStopThreadListeningForInput() throws InterruptedException, IOException {
    AtomicBoolean exceptionCaught = new AtomicBoolean(false);
    AtomicInteger receivedInput = new AtomicInteger(0);
    var port = ThreadLocalRandom.current().nextInt(10000, 20000);
    Thread threadToStop = new Thread(() -> {
      try (ServerSocket socket = new ServerSocket(port);
          var clientSocket = socket.accept();
          InputStreamReader inputStream = new InputStreamReader(clientSocket.getInputStream())) {
        var read = inputStream.read();
        receivedInput.compareAndSet(0, read);
      } catch (IOException e) {
        exceptionCaught.compareAndSet(false, true);
      }
    });
    threadToStop.start();
    threadToStop.interrupt();
    threadToStop.join(100);
    assertThat(exceptionCaught.get(), is(false));
    assertThat(threadToStop.isAlive(), is(true));
    assertThat(threadToStop.getState(), is(State.RUNNABLE));

    // after interrupting we even can send input to the thread
    try (Socket socket = new Socket("localhost", port); PrintWriter out = new PrintWriter(socket.getOutputStream(),
        true)) {
      out.write(10);
    }
    threadToStop.join(100);
    assertThat(receivedInput.get(), is(10));
  }

  @Test
  public void testHandleInterruptionsListeningOnInput() throws IOException, InterruptedException {
    AtomicBoolean exceptionCaught = new AtomicBoolean(false);
    var port = ThreadLocalRandom.current().nextInt(10000, 20000);
    Thread threadToStop = new Thread(() -> {
      try (ServerSocket socket = new ServerSocket(port)) {
        socket.setSoTimeout(10);
        while (!Thread.currentThread().isInterrupted()) {
          try (var clientSocket = socket.accept();
              InputStreamReader inputStream = new InputStreamReader(clientSocket.getInputStream())) {
            var read = inputStream.read();
            // process input
          }
        }
      } catch (IOException e) {
        exceptionCaught.compareAndSet(false, true);
      }
    });
    threadToStop.start();
    threadToStop.interrupt();
    threadToStop.join(100);
    assertThat(threadToStop.isAlive(), is(false));

  }

  @Test
  public void stopThreadBusyWithCalculating() throws InterruptedException {
    AtomicBoolean calculationIsDone = new AtomicBoolean(false);
    Thread threadToStop = new Thread(() -> {
      CpuIntensiveAlgorithm.run1s();
      calculationIsDone.set(true);
    });
    threadToStop.start();
    threadToStop.interrupt();
    threadToStop.join(100);
    assertThat(threadToStop.isAlive(), is(true));
    assertThat(calculationIsDone.get(), is(false));
  }

}
