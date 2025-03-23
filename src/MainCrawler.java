import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class MainCrawler {
    public static final int MAX_DEPTH = 5;
    public static final int MAX_PAGES = 100;

    public static void main(String[] args) {
        String url = "https://www.hs-heilbronn.de/";
        ArrayList<String> visited = new ArrayList<>();
        ArrayList<String> connectionFailed = new ArrayList<>();

        // Datei wird automatisch geschlossen (try-with-resources)
        try (PrintWriter writer = new PrintWriter(new FileWriter("output.txt"))) {
            crawl(1, url, visited, connectionFailed, writer);
            System.out.println("\n✅ Ergebnisse in output.txt gespeichert!");
        } catch (Exception e) {
            System.err.println("❌ Fehler beim Schreiben der Datei: " + e.getMessage());
        }
    }

    // Rekursive Methode mit zusätzlichem "writer"
    public static void crawl(int depth, String url, ArrayList<String> visited, ArrayList<String> connectionFailed, PrintWriter writer) {
        if (depth > MAX_DEPTH || visited.size() >= MAX_PAGES) {
            return;
        }

        Document doc = request(url, visited, connectionFailed, writer);

        if (doc != null) {
            for (Element link : doc.select("a[href]")) {
                String nextLink = link.absUrl("href");
                if (!nextLink.isEmpty() && !visited.contains(nextLink) && !connectionFailed.contains(nextLink)) {
                    crawl(depth + 1, nextLink, visited, connectionFailed, writer);
                }
            }
        }
    }

    private static Document request(String url, ArrayList<String> visited, ArrayList<String> connectionFailed, PrintWriter writer) {
        try {
            Connection con = Jsoup.connect(url)
                    .timeout(5000)
                    .userAgent("Mozilla/5.0 (compatible; MyCrawler/1.0)");

            Document doc = con.get();

            if (con.response().statusCode() == 200) {
                System.out.println("\n🔗 URL: " + url);
                System.out.println("📝 Titel: " + doc.title());

                // In Datei schreiben
                writer.println("URL: " + url);
                writer.println("Titel: " + doc.title());
                writer.println("------------------------------------");

                visited.add(url);
                return doc;
            }
            return null;

        } catch (Exception e) {
            System.err.println("❌ Fehler beim Aufruf der URL: " + url);
            System.err.println("🛑 Fehler: " + e.getMessage());
            connectionFailed.add(url);
            return null;
        }
    }
}
