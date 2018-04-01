package ru.kpfu.itis;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class App {

    public static final String BASE_URL = "https://www.kpfu.ru";
    public static final int TOTAL_LINKS = 50;

    public static void main(String[] args) throws IOException, InterruptedException {

        Document document = Jsoup.connect(BASE_URL).get();

        // select only links -> get their href values -> filter [no relative links] -> top
        List<String> hrefs = document.select("a[href]").eachAttr("href").stream()
                .filter(x -> !x.endsWith("kpfu.ru/") && !x.contains("javascript") && !x.startsWith("#")
                        && !x.startsWith("/") && !x.contains("tab") && !x.equals(BASE_URL))
                .distinct()
                .limit(TOTAL_LINKS - 1) // except origin link
                .collect(Collectors.toList());

        hrefs.add(0, BASE_URL);

        assert (hrefs.size() == TOTAL_LINKS);

        // =============

        SparseMatrix sparseMatrix = new SparseMatrix(TOTAL_LINKS);

        for (int i = 0; i < TOTAL_LINKS; i++) {

            sparseMatrix.put(0, i, 1D);
            sparseMatrix.put(i, i, 1D);
        }

        for (int i = 1; i < hrefs.size(); i++) {

            String currentLink = hrefs.get(i);

            document = Jsoup.connect(currentLink).get();

            System.out.println("Done parsing: " + currentLink);

            Set<String> currentLinkHrefs = new HashSet<>(document.select("a[href]").eachAttr("href"));

            for (int j = 0; j < sparseMatrix.size(); j++) {
                if (currentLinkHrefs.contains(hrefs.get(j))) {
                    sparseMatrix.put(i, j, 1D);
                }
            }
        }

        System.out.println("Done parsing!");

        // ============== Sequential ===============

        SparseVector pageRank = new SparseVector(sparseMatrix.size());

        pageRank.fillWith(1D);

        long startTime = System.nanoTime();

        for (int i = 0; i < 1; i++) {
            pageRank = sparseMatrix.times(pageRank);
            pageRank.normalize();
        }

        long endTime = System.nanoTime();

        System.out.println(pageRank.toString());

        System.out.printf("Time elapsed (sequential): %d ms\n", TimeUnit.NANOSECONDS.toMillis(endTime - startTime));

        // ============== Parallel ===============

        SparseVector pageRankParallel = new SparseVector(sparseMatrix.size());

        pageRankParallel.fillWith(1D);

        startTime = System.nanoTime();

        for (int i = 0; i < 1; i++) {

            // parallel
            pageRankParallel = sparseMatrix.parallelTimes(pageRankParallel);
            pageRankParallel.normalize();
        }

        endTime = System.nanoTime();

        System.out.println(pageRankParallel.toString());

        System.out.printf("Time elapsed (parallel): %d ms\n", TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
    }

}
