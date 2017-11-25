import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

public class CategoryManager {

    private static CategoryManager instance = null;

    private CategoryManager() {}

    public static void init() {
        if(instance == null) {
            instance = new CategoryManager();
            instance._init();
        }
    }

    Map<Integer, List<String>> categories = new HashMap<>();

    public void _init() {
        Integer m = addCategory("Mężczyźni");
        Integer w = addCategory("Kobiety");
        Integer k = addCategory("Dzieci");
        categories.put(m, Arrays.asList("mezczyzni","meskie"));
        categories.put(w, Arrays.asList("kobiety", "damska"));
        categories.put(addCategory("Odzież", m), Arrays.asList("odziez-damska"));
        categories.put(addCategory("Odzież", w), Arrays.asList("odziez-meska"));
        categories.put(addCategory("Odzież", k), Arrays.asList("odziez-dziecieca"));
        categories.put(addCategory("Obuwie", m), Arrays.asList("obuwie-damskie"));
        categories.put(addCategory("Obuwie", w), Arrays.asList("obuwie-meskie"));
        categories.put(addCategory("Obuwie", k), Arrays.asList("obuwie-dzieciece"));
        categories.put(addCategory("Sport", m), Arrays.asList("sport-kobiety"));
        categories.put(addCategory("Sport", w), Arrays.asList("sport-mezczyzni"));
        categories.put(addCategory("Sport", k), Arrays.asList("sport-dzieci"));
        categories.put(addCategory("Akcesoria", m), Arrays.asList("mezczyzni-akcesoria"));
        categories.put(addCategory("Akcesoria", w), Arrays.asList("kobiety-akcesoria"));
        categories.put(addCategory("Akcesoria", k), Arrays.asList("dzieci-akcesoria"));
        categories.put(addCategory("Bielizna", m), Arrays.asList("odziez-damska-bielizna"));;
        categories.put(addCategory("Bielizna", w), Arrays.asList("odziez-meska-bielizna"));
        categories.put(addCategory("Bielizna", k), Arrays.asList("odziez-dziecieca-bielizna"));
    }

    public static Integer getCategoryId(String category) {
        return instance._getCategoryId(category);
    }

    public Integer _getCategoryId(String category) {
        for(Map.Entry<Integer,List<String>> entry : categories.entrySet()) {
            if(entry.getValue().contains(category)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private Integer addCategory(String category) {
        return addCategory(category, 2);
    }

    private Integer addCategory(String category, int parentCategory) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<prestashop xmlns:xlink=\"http://www.w3.org/1999/xlink\"><category><active><![CDATA[1]]></active><name><language id=\"1\">")
                     .append(category)
                     .append("</language></name><link_rewrite><language id=\"1\">")
                     .append(URLEncoder.encode(category).replace("%",""))
                     .append("</language></link_rewrite><id_parent>")
                     .append(parentCategory)
                     .append("</id_parent></category></prestashop>");
        String response = postCategory(stringBuilder.toString());
        String[] cutouts = response.split("<id><!\\[CDATA\\[[0-9]+\\]\\]><\\/id>");
        response = response.replace(cutouts[0],"");
        response = response.replace(cutouts[1],"");
        response = response.replace("<id><![CDATA[","");
        response = response.replace("]]></id>","");
        return new Integer(response);
    }

    private String postCategory(String xml) {
        if(Scraper.PRINT_XMLS) {
            System.out.println(xml);
        }
        try {
            URL url = new URL(Scraper.API_URL + "/categories");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty( "Content-Type", "text/xml");
            try( DataOutputStream wr = new DataOutputStream( connection.getOutputStream())) {
                wr.write( xml.getBytes() );
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
            e.printStackTrace();
            return null;
        }
    }

}
