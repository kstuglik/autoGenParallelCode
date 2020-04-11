package SumForkJoin;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class Sum extends RecursiveTask<Long> {

    private static final int THRESHOLD = 1_000;

    private final int[] numbersToSum;
    private int low;
    private int high;

    Sum(int[] numbersToSum) {
        this.numbersToSum = numbersToSum;
        this.high = numbersToSum.length - 1;
    }

    private Sum(int[] numbersToSum, int low, int high) {
        this.numbersToSum = numbersToSum;
        this.low = low;
        this.high = high;
    }

    protected Long compute() {
        if ((high - low) <= THRESHOLD) {
            long sum = 0;
            for (int i = low; i < high; i++) {
                sum += numbersToSum[i];
            }
            return sum;
        }

        int mid = low + (high - low) / 2; //get middle index

        Sum left = new Sum(numbersToSum, low, mid); //get first part
        Sum right = new Sum(numbersToSum, mid, high); //get second part

        left.fork(); //split job

        long resultFromRight = right.compute(); //compute result
        long resultFromLeft = left.join(); //wait for result

        return resultFromRight + resultFromLeft;
    }

    public static void main(String[] args) {
        ForkJoinPool pool = new ForkJoinPool();
        int[] ints = new Random()
                .ints(0, 100)
                .limit(1_000_000)
                .toArray();
        Long invoke = pool.invoke(new Sum(ints));
        System.out.println(invoke);
    }

}