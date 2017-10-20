import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Scraper {

    private static final int RETRY_LIMIT = 10;
    private static final int PAGE_LIMIT = 1;

    private List<ScrapedProduct> scrapedProducts = new ArrayList<ScrapedProduct>();

    public Scraper(String baseUrl, String catalogUrl) {
        System.out.println("Scraping " + baseUrl + catalogUrl + ".");
        int pages = 0;
        boolean scrapingSuccessfull = false;
        int retries = 0;
        while(!scrapingSuccessfull) {
            if(retries > RETRY_LIMIT) {
                throw new NullPointerException("Can't scrape page " + baseUrl + catalogUrl + ". Retry limit exceeded.");
            }
            try {
                String pageContents = getPage(baseUrl + catalogUrl);
                Document parsedPage = Jsoup.parse(pageContents);
                Elements elements = parsedPage.getElementsByClass("z-nvg-cognac_label-2W3Y8");
                Element element = elements.first();
                String str = element.text();
                pages = Integer.parseInt(str.replaceFirst(".+[z ]",""));
                System.out.println("Found " + pages + " pages.");
                scrapingSuccessfull = true;
            } catch (NullPointerException e) {
                System.out.println("Couldn't scrap " + baseUrl + catalogUrl + ". Trying again...");
                retries++;
                scrapingSuccessfull = false;
            }
        }
        if(PAGE_LIMIT < pages) {
            pages = PAGE_LIMIT;
        }
        for(int i = 0; i < pages; i++) {
            List<String> productUrls = scrapProductUrls(baseUrl, catalogUrl + "?p=" + (i+1));
        }
    }

    private List<String> scrapProductUrls(String baseUrl, String catalogUrl) {
        System.out.println("Scraping product URLs from " + baseUrl + catalogUrl + ".");
        List<String> productUrls = new ArrayList<String>();
        boolean scrapingSuccessfull = false;
        int retries = 0;
        while(!scrapingSuccessfull) {
            if(retries > RETRY_LIMIT) {
                throw new NullPointerException("Can't scrape page " + baseUrl + catalogUrl + ". Retry limit exceeded.");
            }
            try {
                String pageContents = getPage(baseUrl + catalogUrl);
                Document parsedPage = Jsoup.parse(pageContents);
                Elements elements = parsedPage.getElementsByClass("z-nvg-cognac_imageLink-OPGGa");
                if(elements.size() == 0) {
                    throw new NullPointerException();
                }
                System.out.println("Found " + elements.size() + " products on page " + baseUrl + catalogUrl + ".");
                for (Element element: elements) {
                    productUrls.add(element.attr("href"));
                }
                scrapingSuccessfull = true;
            } catch (NullPointerException e) {
                System.out.println("Couldn't scrap " + baseUrl + catalogUrl + ". Trying again...");
                retries++;
                scrapingSuccessfull = false;
            }
        }
        return productUrls;
    }

    private void scrapProduct(String url) {
        String rawPage = getPage(url);
        Document parsedPage = Jsoup.parse(rawPage);
        String productData = parsedPage.getElementById("z-vegas-pdp-props").data();
        productData = productData.replace("<![CDATA[", "").replace("]]>","");
        JSONObject productJson = new JSONObject(productData);
        scrapedProducts.add(new ScrapedProduct(productJson));
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
