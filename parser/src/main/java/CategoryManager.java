import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryManager {

    private static CategoryManager instance = null;

    private CategoryManager() {}

    public static void init() {
        if(instance == null) {
            instance = new CategoryManager();
        }

    }

    private Map<String,Integer> categories = new HashMap<>();

    public static Integer getCategoryId(String category) {
        return instance._getCategoryId(category);
    }

    public Integer _getCategoryId(String category) {
        if(categories.containsKey(category)) {
            return categories.get(category);
        } else {
            Integer categoryId = addCategory(category);
            categories.put(category, categoryId);
            return categoryId;
        }
    }

    private Integer addCategory(String category) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<prestashop xmlns:xlink=\"http://www.w3.org/1999/xlink\"><category><active><![CDATA[1]]></active><name><language id=\"1\">")
                     .append(category)
                     .append("</language></name><link_rewrite><language id=\"1\">")
                     .append(category)
                     .append("</language></link_rewrite><id_parent>2</id_parent></category></prestashop>");
        String response = postCategory(stringBuilder.toString());
        String[] cutouts = response.split("<id><!\\[CDATA\\[[0-9]+\\]\\]><\\/id>");
        response.replace(cutouts[0],"");
        response.replace(cutouts[1],"");
        response.replace("<id><![CDATA[","");
        response.replace("]]></id>","");
        return new Integer(response);
    }

    private String postCategory(String xml) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(Main.API_URL + "/categories");
            connection.setDoOutput(true);
            connection = (HttpURLConnection) url.openConnection();
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
