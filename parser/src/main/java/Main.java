import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by Moyubori on 14.10.2017.
 */
public class Main {

    public static void main(String[] args) {
        Scraper scraper = new Scraper();
        scraper.scrap("https://www.zalando.pl/converse-sukienka-letnia-white-co421c000-a11.html");
    }

}
