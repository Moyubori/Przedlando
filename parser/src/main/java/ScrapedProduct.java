import org.json.JSONArray;
import org.json.JSONObject;

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
            imageUrls.add(images.getJSONObject(i).getJSONObject("sources").getString("gallery"));
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
        Map<String, Integer> categoriesMap = new HashMap<>();
        for(String category : Scraper.currentCategories) {
            categoriesMap.put(category, CategoryManager.getCategoryId(category));
        }
        return categoriesMap;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public String toXml() {
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<prestashop xmlns:xlink=\"http://www.w3.org/1999/xlink\"><product><active><![CDATA[1]]></active><available_for_order>1</available_for_order><show_price>1</show_price><advanced_stock_management>1</advanced_stock_management><id_tax_rules_group>1</id_tax_rules_group>")
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
