package ru.kpfu.itis;

import java.util.Map;
import java.util.TreeMap;

public class SparseVector {

    private final int size;

    private Map<Integer, Double> vector;

    public SparseVector(int size) {

        this.size = size;
        vector = new TreeMap<>();
    }

    public boolean put(int i, Double value) {

        if (Double.compare(Math.abs(value), 0D) != 0) {
            vector.put(i, value);
            return true;
        }

        return false;
    }

    public Double get(int i) {
        return vector.getOrDefault(i, 0D);
    }

    public void fillWith(Double value) {
        for (int i = 0; i < size; i++) {
            vector.put(i, value);
        }
    }

    public int nnz() {
        return vector.size();
    }

    public int size() {
        return size;
    }

    public Double dot(SparseVector other) {

        Double result = 0D;

        for (int i = 0; i < size; i++) {
            result += get(i) * other.get(i);
        }

        return result;
    }

    public SparseVector scale(Double alpha) {

        SparseVector result = new SparseVector(size);

        if (Double.compare(Math.abs(alpha), 0) == 0) return result;

        for (int key : this.vector.keySet()) {

            result.put(key, alpha * this.get(key));
        }

        return result;
    }

    public SparseVector plus(SparseVector other) {

        SparseVector result = new SparseVector(size);

        for (int i = 0; i < size; i++) {

            Double sum = get(i) + other.get(i);

            result.put(i, sum);
        }

        return result;
    }

    public void normalize() {

        // todo try using parallel stream
        Double sum = vector.values().stream().mapToDouble(Double::doubleValue).sum();

        for (int i = 0; i < size; i++) {
            if (vector.containsKey(i)) {
                vector.put(i, vector.get(i) / sum);
            }
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[').append('\t');
        for (int i = 0; i < size; i++) {
            stringBuilder
                    .append('(').append(i).append(" : ")
                    .append(String.format("%.3f", vector.getOrDefault(i, 0D)))
                    .append(')').append('\t');
        }
        stringBuilder.append('\t').append(']');
        return stringBuilder.toString();
    }
}
