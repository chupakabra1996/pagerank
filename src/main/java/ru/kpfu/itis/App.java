package ru.kpfu.itis;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class App {

    public static void main( String[] args ) throws IOException {


        final String originLink = "https://www.kpfu.ru";
        final int totalLinks = 20;

        Document document = Jsoup.connect(originLink).get();

        // select only links -> get their href values -> filter [no relative links] -> top
        List<String> hrefs = document.select("a[href]").eachAttr("href").stream()
                .filter(x -> !x.endsWith("kpfu.ru/") && !x.contains("javascript") && !x.startsWith("#")
                        && !x.startsWith("/") && !x.contains("tab") && !x.equals(originLink))
                .distinct()
                .limit(totalLinks - 1) // except origin link
                .collect(Collectors.toList());

        hrefs.add(0, originLink);

        String [][] linksMatrix = new String[hrefs.size()][hrefs.size()];

        for (int i = 0; i < linksMatrix.length; i++) {
            linksMatrix[0][i] = hrefs.get(i);
            linksMatrix[i][i] = hrefs.get(i);
        }

        // iterate through all links (except the 1st)
        for (int i = 1; i < hrefs.size(); i++) {

            String currentLink = hrefs.get(i);
            document = Jsoup.connect(currentLink).get();
            System.out.println("Done parsing: " + currentLink);
            Set<String> currentLinkHrefs = new HashSet<>(document.select("a[href]").eachAttr("href"));

            // columns
            for (int j = 0; j < linksMatrix.length; j++) {
                if (currentLinkHrefs.contains(hrefs.get(j))) {
                    linksMatrix[i][j] = hrefs.get(j);
                }
            }
        }

        System.out.println("done parsing websites");

        // page rank

        // convert String[][] to double[][]
        double[][] matrix = new double[linksMatrix.length][linksMatrix.length];

        for (int i = 0; i < linksMatrix.length; i++) {
            for (int j = 0; j < linksMatrix.length; j++) {
                if (Objects.isNull(linksMatrix[i][j])) {
                    matrix[i][j] = 0d;
                } else {
                    matrix[i][j] = 1d;
                }
            }
        }

        System.out.println(Arrays.deepToString(matrix));

        double[] pageranks = new double[linksMatrix.length];
        Arrays.fill(pageranks, 1.);

        System.out.println("start pagerank");

        long startTime = System.nanoTime();
        for (int i = 0; i < 70; i++) {
            pageranks = multMatrixVector(matrix, pageranks);
            normPageranks(pageranks);
        }
        long endTime = System.nanoTime();
        System.out.printf("Time elapsed: %d ms\n", TimeUnit.NANOSECONDS.toMillis(endTime - startTime));

        List<Pair> pairs = new ArrayList<>(pageranks.length);
        for (int i = 0; i < pageranks.length; i++) {
            pairs.add(new Pair(hrefs.get(i), pageranks[i]));
        }

        System.out.println(pairs.stream().sorted((Comparator.comparing(o -> o.value))).collect(Collectors.toList()).toString());
    }

    public static double[] multMatrixVector(double[][] m, double[] v) {

        double[] pageranks = new double[v.length];

        Arrays.fill(pageranks, 0d);

        for (int i = 0; i < v.length; ++i) {
            for (int j = 0; j < v.length; ++j) {
                pageranks[i] += m[i][j] * v[j];
            }
        }

        return pageranks;
    }

    static void normPageranks(double[] pageranks) {
        double sum = 0;

        for (double pagerank : pageranks) {
            sum += pagerank;
        }

        for (int j = 0; j < pageranks.length; ++j) {
            pageranks[j] /= sum;
        }
    }

    static class Pair {
        String key;
        Double value;

        public Pair(String key, Double value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "{" +
                    "ресурс='" + key + '\'' +
                    ", ранк=" + value +
                    '}';
        }
    }
}
