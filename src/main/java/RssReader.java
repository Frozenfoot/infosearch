import com.rometools.rome.io.FeedException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by frozenfoot on 29.05.18.
 */
public class RssReader {
    public static void main(String[] args) throws IOException, FeedException, ExecutionException, InterruptedException {
        List<URL> sources  = new ArrayList<>();
        sources.add(new URL("https://lenta.ru/rss/news"));
        sources.add(new URL("http://www.aif.ru/rss/news.php"));
        sources.add(new URL("https://www.kommersant.ru/RSS/main.xml"));
        sources.add(new URL("http://www.secnews.ru/rss/all.xml"));

        final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        final List<Future<List<News>>> futures = new ArrayList<>();
        for (URL site : sources){
            futures.add(threadPool.submit(new GetNewsFromSite(site)));
        }

        final List<News> result = new ArrayList<>();
        for (Future<List<News>> future : futures){
            result.addAll(future.get());
        }

        System.out.println(result.size());

        for (News news: result) {
            System.out.println(news.getUrl());
            System.out.println(news.getPublishDate());
            System.out.println(news.getTitle());
            System.out.println(news.getDescription());
        }
    }
//
//        SyndFeedInput input = new SyndFeedInput();
//        SyndFeed feed = input.build(new XmlReader(sources.get(0)));
////        System.out.println(feed);
//        for(URL rssUrl : sources){
//            SyndFeedInput input = new SyndFeedInput();
//            SyndFeed feed = input.build(new XmlReader(rssUrl));
//            for (SyndEntry entry : feed.getEntries()){
//                System.out.println(entry.getTitle());
//                System.out.println(entry.getDescription().getValue());
//                System.out.println(entry.getPublishedDate());
//            }
//        }
//
//        final List<News> result = sources.parallelStream()
//                .flatMap()
//                .collect(Collectors.toList());
//    }
}
