package ru.kpfu.itis;

public class SparseMatrix {

    private final int size;

    private SparseVector[] rows;

    public SparseMatrix(int size) {

        this.size = size;
        rows = new SparseVector[size];

        for (int i = 0; i < size; i++) {
            rows[i] = new SparseVector(size);
        }
    }

    public void put(int i, int j, Double value) {
        rows[i].put(j, value);
    }

    public double get(int i, int j) {
        assert (i >= 0 && i < size && j >= 0 && j < size);
        return rows[i].get(j);
    }

    public SparseVector times(SparseVector x) {

        SparseVector b = new SparseVector(size);

        for (int i = 0; i < size; i++) {
            b.put(i, this.rows[i].dot(x));
        }

        return b;
    }

    public SparseVector parallelTimes(SparseVector x) throws InterruptedException {

        final SparseVector b = new SparseVector(size);

        Thread[] threads = new Thread[2];

        threads[0] = new Thread(() -> {
            for (int j = 0; j < size / 2; j++) {
                b.put(j, rows[j].dot(x));
            }
        });

        threads[1] = new Thread(() -> {
            for (int j = size / 2; j < size; j++) {
                b.put(j, rows[j].dot(x));
            }
        });

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        return b;
    }

    public SparseMatrix plus(SparseMatrix other) {

        SparseMatrix result = new SparseMatrix(size);

        for (int i = 0; i < size; i++) {
            result.rows[i] = this.rows[i].plus(other.rows[i]);
        }

        return result;
    }

    public int size() {
        return size;
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < size; i++) {
            stringBuilder.append(rows[i]).append('\n');
        }
        return stringBuilder.toString();
    }
}
