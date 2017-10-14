import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Scraper {

    List<ScrapedProduct> scrapedProducts = new ArrayList<ScrapedProduct>();

    public Scraper() {
    }

    public void scrap(String url) {
        String rawPage = getPage(url);
        Document parsedPage = Jsoup.parse(rawPage);
        scrapedProducts.add(new ScrapedProduct(parsedPage));
    }

    private String getPage(String pageUrl) {
        HttpsURLConnection connection = null;
        try {
            URL url = new URL(pageUrl);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuffer response = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
