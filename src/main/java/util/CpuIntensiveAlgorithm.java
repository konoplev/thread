package util;

public class CpuIntensiveAlgorithm {
  public static void run100Ms() {
    // Value from util.CpuIntensiveAlgorithmTest.testThereIsANumberToRunFor100Ms
    findAllPrimesBefore(15121);
  }

  public static void run1s() {
    // Value from util.CpuIntensiveAlgorithmTest.testThereIsANumberToRunFor1s
    findAllPrimesBefore(52021);
  }

  static long getBoundThatIsBigEnoughToKeepCpuBusyFor(long milliseconds) {
    var nextPrime = 1L;
    var start = System.currentTimeMillis();
    while (nextPrime < Long.MAX_VALUE) {
      nextPrime = getNextPrime(nextPrime);
      if (System.currentTimeMillis() - start > milliseconds) {
        return nextPrime;
      }
    }
    throw new RuntimeException("Could not find a prime number that is big enough to keep CPU busy for " + milliseconds + " milliseconds");
  }

  private static void findAllPrimesBefore(long rightBound) {
    var nextPrime = 1L;
    while (nextPrime < rightBound) {
      nextPrime = getNextPrime(nextPrime);
    }
  }

  private static long getNextPrime(long previous) {
    long potentiallyPrime = previous + 1;
    while (true) {
      int i = 2;
      for (; i < potentiallyPrime; i++) {
        if (potentiallyPrime % i == 0) {
          break;
        }
      }
      //we found prime
      if (potentiallyPrime == i) {
        return potentiallyPrime;
      }
      potentiallyPrime++;
    }
  }
}
