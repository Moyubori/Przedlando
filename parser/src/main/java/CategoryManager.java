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
        categories.put(m, Arrays.asList("m"));
        categories.put(w, Arrays.asList("w"));
        {
            Integer ow = addCategory("Odzież", w);
            categories.put(ow, Arrays.asList("ow"));
            {
                categories.put(addCategory("Kurtki",ow), Arrays.asList("owkurtki"));
                categories.put(addCategory("Bluzy",ow), Arrays.asList("owbluzy"));
                categories.put(addCategory("Koszulki",ow), Arrays.asList("owkoszulki"));
                categories.put(addCategory("Spodnie",ow), Arrays.asList("owspodnie"));
            }
            Integer om = addCategory("Odzież", m);
            categories.put(om, Arrays.asList("om"));
            {
                categories.put(addCategory("Kurtki",om), Arrays.asList("omkurtki"));
                categories.put(addCategory("Bluzy",om), Arrays.asList("ombluzy"));
                categories.put(addCategory("Koszulki",om), Arrays.asList("omkoszulki"));
                categories.put(addCategory("Spodnie",om), Arrays.asList("omspodnie"));
            }
            Integer sw = addCategory("Obuwie", w);
            categories.put(sw, Arrays.asList("sw"));
            Integer sm = addCategory("Obuwie", m);
            categories.put(sm, Arrays.asList("sm"));
            Integer aw = addCategory("Akcesoria", w);
            categories.put(aw, Arrays.asList("aw"));
            {
                categories.put(addCategory("Torby",aw), Arrays.asList("awtorby"));
                categories.put(addCategory("Portfele",aw), Arrays.asList("awportfele"));
                categories.put(addCategory("Zegarki",aw), Arrays.asList("awzegarki"));
                categories.put(addCategory("Czapki",aw), Arrays.asList("awczapki"));
            }
            Integer am = addCategory("Akcesoria", m);
            categories.put(am, Arrays.asList("am"));
            {
                categories.put(addCategory("Torby",am), Arrays.asList("amtorby"));
                categories.put(addCategory("Portfele",am), Arrays.asList("amportfele"));
                categories.put(addCategory("Zegarki",am), Arrays.asList("amzegarki"));
                categories.put(addCategory("Czapki",am), Arrays.asList("amczapki"));
            }
        }
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
