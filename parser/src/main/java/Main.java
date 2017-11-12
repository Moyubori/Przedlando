

public class Main {

    public static final String API_URL = "http://localhost:8080/api";

    public static void main(String[] args) {
        CategoryManager.init();
        Scraper scraper = new Scraper("https://www.zalando.pl", "/okazje/");
    }

}
