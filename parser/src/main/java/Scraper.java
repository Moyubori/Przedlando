import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Scraper {

    public static final boolean PRINT_XMLS = true;

    private static final int RETRY_LIMIT = 10;
    private static final int PAGE_LIMIT = 1;

    public static String API_URL = "http://localhost:8080/api";
    private static String API_KEY = "IJP82WEE6LDEBZ7BJXYZQWLVSUZJLBUH";

    public static void main(String[] args) {
        Authenticator.setDefault (new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication (API_KEY, "".toCharArray());
            }
        });
        CategoryManager.init();
        Scraper scraper = new Scraper("https://www.zalando.pl", "/okazje/");
    }

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
        List<String> productUrls = null;
        for(int i = 0; i < pages; i++) {
            productUrls = scrapProductUrls(baseUrl, catalogUrl + "?p=" + (i+1));
        }
        scrapProducts(baseUrl, productUrls);
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

    private void scrapProducts(String baseUrl, List<String> productUrls) {
        for(String productUrl : productUrls) {
            scrapProduct(baseUrl, productUrl);
        }
    }

    private void scrapProduct(String baseUrl, String productUrl) {
        System.out.println("Scraping product from " + baseUrl + productUrl + ".");
        String rawPage = getPage(baseUrl + productUrl);
        Document parsedPage = Jsoup.parse(rawPage);
        String productData = parsedPage.getElementById("z-vegas-pdp-props").data();
        productData = productData.replace("<![CDATA[", "").replace("]]>","");
        JSONObject productJson = new JSONObject(productData);
        ScrapedProduct product = new ScrapedProduct(productJson);
        String productId = postProduct(product.toXml());
        List<String> imageUrls = product.getImageUrls();
        for(String imageUrl : imageUrls) {
            try {
                postImage(imageUrl, productId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    private String postProduct(String productXml) {
        if(PRINT_XMLS) {
            System.out.println(productXml);
        }
        try {
            URL url = new URL(Scraper.API_URL + "/products");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty( "Content-Type", "text/xml");
            try( DataOutputStream wr = new DataOutputStream( connection.getOutputStream())) {
                wr.write( productXml.getBytes() );
            }
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuffer response = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return parseIdFromResponse(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void postImage(String imageUrl, String productId) throws IOException {
        URL url = new URL(imageUrl);
        InputStream img = url.openStream();
        HttpClient httpclient = new DefaultHttpClient();
        byte[] apiKeyEncoded = Base64.getEncoder().encode((API_KEY + ":").getBytes());
        HttpPost httpPost = new HttpPost(API_URL + "/images/products/" + productId);
        httpPost.setHeader("Authorization", "Basic " + new String(apiKeyEncoded));
        InputStreamBody inputStreamBody = new InputStreamBody(img, "img.jpg");
        MultipartEntity reqEntity = new MultipartEntity();
        reqEntity.addPart("image", inputStreamBody);
        httpPost.setEntity(reqEntity);
        HttpResponse response = httpclient.execute(httpPost);
    }

    private String parseIdFromResponse(String response) {
        response = response.replaceAll("(\\r|\\t|\\n)", "");
        String[] cutouts = response.split("<product><id><!\\[CDATA\\[[0-9]+\\]\\]><\\/id>");
        response = response.replace(cutouts[0],"");
        response = response.replace(cutouts[1],"");
        response = response.replace("<product><id><![CDATA[","");
        response = response.replace("]]></id>","");
        return response;
    }

}