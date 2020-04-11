/*
example from:
http://shengwangi.blogspot.com/2015/10/how-to-use-java-parallel-forkjoin-framework-example.html
 */

package LetterForkJoin;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class Main {
    private static final int ARRAY_SIZE = 100_000_000;
    private static char[] letterArray = new char[ARRAY_SIZE];

    private static int countLetterUsingForkJoin(char key) {
        int total = 0;
        ForkJoinPool pool = new ForkJoinPool(); // create thread pool for fork/join
        CountLetterTask task = new CountLetterTask(key, letterArray, 0, ARRAY_SIZE);
        total = pool.invoke(task); // submit the task to fork/join pool

        pool.shutdown();
        return total;
    }

    public static void main(String[] args) {
        char key = 'A';
        // fill the big array with A-Z randomly
        for (int i = 0; i < ARRAY_SIZE; i++) {
            letterArray[i] = (char) (Math.random() * 26 + 65); // A-Z
        }

        int count = countLetterUsingForkJoin(key);
        System.out.printf("Using ForkJoin, found %d '%c'\n", count, key);
    }

    static class CountLetterTask extends RecursiveTask<Integer> {

        private static final long serialVersionUID = 1L;
        private static final int ACCEPTABLE_SIZE = 10_000;
        private char[] letterArray;
        private char key;
        private int start;
        private int stop;

        public CountLetterTask(char key, char[] letterArray, int start, int stop) {
            this.key = key;
            this.letterArray = letterArray;
            this.start = start;
            this.stop = stop;
        }

        @Override
        protected Integer compute() {
            int count = 0;
            int workLoadSize = stop - start;
            if (workLoadSize < ACCEPTABLE_SIZE) {
                 String threadName = Thread.currentThread().getName();
                 System.out.printf("Calculation [%d-%d] in Thread %s\n",start,stop,threadName);
                for (int i = start; i < stop; i++) {
                    if (letterArray[i] == key)
                        count++;
                }
            } else {
                int mid = start + workLoadSize / 2;
                CountLetterTask left = new CountLetterTask(key, letterArray, start, mid);
                CountLetterTask right = new CountLetterTask(key, letterArray, mid, stop);

                // fork (push to queue)-> compute -> join
                left.fork();
                int rightResult = right.compute();
                int leftResult = left.join();
                count = leftResult + rightResult;
            }
            return count;
        }
    }
}