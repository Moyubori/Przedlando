import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScrapedProduct {

    private String name;
    private double price;
    private String description;
    private Map<String, Integer> categories;
    private List<String> imageUrls;

    public ScrapedProduct(JSONObject productData) {
        JSONObject articleInfo = productData.getJSONObject("model").getJSONObject("articleInfo");
        name = articleInfo.getString("name");
        price = articleInfo.getJSONObject("displayPrice").getJSONObject("price").getDouble("value");
        description = getDescription(articleInfo);
        categories = getCategories(articleInfo);
        imageUrls = new ArrayList<>();
        JSONArray images = articleInfo.getJSONObject("media").getJSONArray("images");
        for(int i = 0; i < images.length(); i++) {
            imageUrls.add(images.getJSONObject(i).getJSONObject("sources").getString("color"));
        }
    }

    private String getDescription(JSONObject articleInfo) {
        StringBuilder stringBuilder = new StringBuilder();
        JSONArray attributesData = articleInfo.getJSONArray("attributes").getJSONObject(0).getJSONArray("data");
        for(int i = 0; i < attributesData.length(); i++) {
            stringBuilder.append(attributesData.getJSONObject(i).getString("name"))
                         .append(": ")
                         .append(attributesData.getJSONObject(i).getString("values"))
                         .append("; ");
        }
        return stringBuilder.toString();
    }

    private Map<String, Integer> getCategories(JSONObject articleInfo) {
        JSONArray categoriesJson = articleInfo.getJSONArray("categories");
        Map<String, Integer> categoriesMap = new HashMap<>();
        for(int i = 0; i < categoriesJson.length(); i++) {
            String category = categoriesJson.getString(i);
            Integer categoryId = CategoryManager.getCategoryId(category);
            categoriesMap.put(category, categoryId);
        }
        return categoriesMap;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public String toXml() {
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<prestashop xmlns:xlink=\"http://www.w3.org/1999/xlink\"><product><active><![CDATA[1]]></active>")
                  .append("<name><language id=\"1\">").append(name).append("</language></name>")
                  .append("<description><language id=\"1\">").append(description).append("</language></description>")
                  .append("<description_short><language id=\"1\">").append(description).append("</language></description_short>")
                  .append("<price>").append(price).append("</price>")
                  .append("<id_category_default>").append("2").append("</id_category_default>")
                  .append("<associations><categories>");
        for(Map.Entry<String, Integer> entry : categories.entrySet()) {
            xmlBuilder.append("<category><id>")
                      .append(entry.getValue())
                      .append("</id></category>");
        }
        xmlBuilder.append("</categories></associations>");
        xmlBuilder.append("</product></prestashop>");
        return xmlBuilder.toString();
    }

}
