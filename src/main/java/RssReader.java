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
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        List<URL> sources = new ArrayList<>();
        sources.add(new URL("https://lenta.ru/rss/news"));
        sources.add(new URL("http://www.aif.ru/rss/news.php"));
        sources.add(new URL("https://www.kommersant.ru/RSS/main.xml"));
        sources.add(new URL("http://www.secnews.ru/rss/all.xml"));

        final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        final List<Future<List<News>>> futures = new ArrayList<>();
        for (URL site : sources) {
            futures.add(threadPool.submit(new GetNewsFromSite(site)));
        }

        final List<News> result = new ArrayList<>();
        for (Future<List<News>> future : futures) {
            result.addAll(future.get());
        }

        System.out.println(result.size());

        ElasticSearchAgent agent = new ElasticSearchAgent();
        agent.addNews(result);
        List<News> similar = agent.getSimilarNews(result.get(0), 5);
        System.out.println(similar);
        agent.shutDown();
    }
}
