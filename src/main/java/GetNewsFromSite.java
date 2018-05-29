import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by frozenfoot on 30.05.18.
 */
public class GetNewsFromSite implements Callable<List<News>> {

    private URL site;

    public GetNewsFromSite(URL site) {
        this.site = site;
    }

    @Override
    public List<News> call() throws IllegalArgumentException, FeedException, IOException {
        final SyndFeedInput input = new SyndFeedInput();
        final SyndFeed feed = input.build(new XmlReader(site));
        final List<News> result = new ArrayList<>();
        for (SyndEntry entry : feed.getEntries()){
            result.add(new News(entry.getTitle(), entry.getDescription().getValue(), entry.getPublishedDate(), site));
        }
        return result;
    }
}
