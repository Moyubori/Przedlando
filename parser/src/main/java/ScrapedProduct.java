import org.jsoup.nodes.Document;

public class ScrapedProduct {

    private String title;
    private String price;
    private String description;

    public ScrapedProduct(Document productPage) {
        this.title = productPage.getElementsByClass("zvui-product-title-productname").first().text();
        System.out.println(this.title);
        this.price = productPage.getElementsByClass("zvui-product-price-actual").last().text();
        System.out.println(this.price);
    }


}
