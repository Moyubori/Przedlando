import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
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
import java.util.*;

public class Scraper {

    public static final boolean PRINT_XMLS = true;

    private static final int RETRY_LIMIT = 10;
    private static int PAGE_LIMIT = 1;

    public static String API_URL = "http://localhost:8080/api";
    private static String API_KEY = "12345678901234567890123456789012";

    private static final Map<String, List<String>> categoryAddressMap;
    static {
        categoryAddressMap = new HashMap<>();
        categoryAddressMap.put("odziez-meska-kurtki/", Arrays.asList("m","om","omkurtki"));
        categoryAddressMap.put("odziez-meska-bluzy-kardigany/", Arrays.asList("m","om","ombluzy"));
        categoryAddressMap.put("odziez-meska-koszulki/", Arrays.asList("m","om","omkoszulki"));
        categoryAddressMap.put("odziez-meska-spodnie/", Arrays.asList("m","om","omspodnie"));
        categoryAddressMap.put("odziez-damska-kurtki/", Arrays.asList("w","ow","owkurtki"));
        categoryAddressMap.put("odziez-damska-bluzki-tuniki/", Arrays.asList("w","ow","owbluzy"));
        categoryAddressMap.put("odziez-damska-koszulki/", Arrays.asList("w","ow","owkoszulki"));
        categoryAddressMap.put("odziez-damska-spodnie/", Arrays.asList("w","ow","owspodnie"));
        categoryAddressMap.put("akcesoria-torby-kobiety/", Arrays.asList("w","aw","awtorby"));
        categoryAddressMap.put("portfele-etui-kobiety/", Arrays.asList("w","aw","awportfele"));
        categoryAddressMap.put("zegarki-kobiety/", Arrays.asList("w","aw","awzegarki"));
        categoryAddressMap.put("czapki-kapelusze-kobiety/", Arrays.asList("w","aw","awczapki"));
        categoryAddressMap.put("czapki-kapelusze-mezczyzni/", Arrays.asList("m","am","amczapki"));
        categoryAddressMap.put("zegarki-mezczyzni/", Arrays.asList("m","am","amzegarki"));
        categoryAddressMap.put("portfele-etui-mezczyzni/", Arrays.asList("m","am","amportfele"));
        categoryAddressMap.put("akcesoria-torby-mezczyzni/", Arrays.asList("m","am","amtorby"));
        categoryAddressMap.put("obuwie-meskie/", Arrays.asList("m","sm"));
        categoryAddressMap.put("obuwie-damskie/", Arrays.asList("w","sw"));
    }
    private static final String baseUrl = "https://www.zalando.pl/";

    public static List<String> currentCategories = null;

    public static void main(String[] args) {
        if(args.length == 3) {
            API_URL = args[0];
            API_KEY = args[1];
            PAGE_LIMIT = Integer.parseInt(args[2]);
        }
        System.out.println("API URL set to " + API_URL);
        System.out.println("API KEY set to " + API_KEY);
        System.out.println("PAGE LIMIT set to " + PAGE_LIMIT);
        Authenticator.setDefault (new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication (API_KEY, "".toCharArray());
            }
        });
        CategoryManager.init();
        Scraper scraper = new Scraper();
    }

    public Scraper() {
        int categoriesParsed = 0;
        for(Map.Entry<String,List<String>> entry : categoryAddressMap.entrySet()) {
            categoriesParsed++;
            String catalogUrl = entry.getKey();
            currentCategories = entry.getValue();
            System.out.println(categoriesParsed + ". Scraping " + PAGE_LIMIT + " pages from " + baseUrl + catalogUrl + ".");
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
            List<String> productUrls = new ArrayList<>();
            for(int i = 0; i < pages; i++) {
                productUrls.addAll(scrapProductUrls(baseUrl, catalogUrl + "?p=" + (i+1)));
            }
            scrapProducts(baseUrl, productUrls);
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
        String response = postProduct(product.toXml());
        if(response != null) {
            String productId = parseProductIdFromResponse(response);
            String stockAvailableId = parseStockAvailabeIdFromResponse(response);
            postStockAvailable(productId, stockAvailableId);
            List<String> imageUrls = product.getImageUrls();
            for (String imageUrl : imageUrls) {
                try {
                    postImage(imageUrl, productId);
                } catch (IOException e) {
                    System.out.println("Posting image failed.");
                }
            }
        } else {
            System.out.println("Scraping failed.");
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
            System.out.println("Scraping page " + pageUrl + " failed. Retrying...");
            return getPage(pageUrl);
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
            return response.toString();
        } catch (Exception e) {
            System.out.println("Posting product failed.");
            return null;
        }
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

    private void postStockAvailable(String productId, String stockAvailablesId) {
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<prestashop xmlns:xlink=\"http://www.w3.org/1999/xlink\"><stock_available><id_product>")
                  .append(productId)
                  .append("</id_product><id_shop>1</id_shop><depends_on_stock>0</depends_on_stock><out_of_stock>1</out_of_stock><id>")
                  .append(stockAvailablesId)
                .append("</id><id_product_attribute>0</id_product_attribute><quantity>")
                .append(10)
                .append("</quantity></stock_available></prestashop>");
        try {
            URL url = new URL(Scraper.API_URL + "/stock_availables/" + stockAvailablesId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            connection.setRequestProperty( "Content-Type", "text/xml");
            try( DataOutputStream wr = new DataOutputStream( connection.getOutputStream())) {
                wr.write( xmlBuilder.toString().getBytes() );
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
        } catch (Exception e) {
            System.out.println("Posting stock_available failed.");
        }
    }

    private String parseProductIdFromResponse(String response) {
        response = response.replaceAll("(\\r|\\t|\\n)", "");
        String[] cutouts = response.split("<product><id><!\\[CDATA\\[[0-9]+\\]\\]><\\/id>");
        response = response.replace(cutouts[0],"");
        response = response.replace(cutouts[1],"");
        response = response.replace("<product><id><![CDATA[","");
        response = response.replace("]]></id>","");
        return response;
    }

    private String parseStockAvailabeIdFromResponse(String response) {
        response = response.replaceAll("(\\r|\\t|\\n)", "");
        String[] cutouts = response.split("<stock_available .+><id><!\\[CDATA\\[[0-9]+\\]\\]><\\/id>");
        response = response.replace(cutouts[0],"");
        response = response.replace(cutouts[1],"");
        response = response.replaceAll("<stock_available .+><id><!\\[CDATA\\[","");
        response = response.replace("]]></id>","");
        return response;
    }

}
