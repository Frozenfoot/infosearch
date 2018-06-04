import com.google.gson.Gson;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Adler32;

/**
 * Created by frozenfoot on 31.05.18.
 */
public class ElasticSearchAgent {
    private static final String INDEX ="news_index";
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 9200;
    private static final String SCHEME = "http";
    private RestHighLevelClient client;
    private Gson gson = new Gson();

    public ElasticSearchAgent() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", "123456"));

        RestClientBuilder builder = RestClient.builder(new HttpHost(HOST, PORT, SCHEME))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

        this.client = new RestHighLevelClient(builder);
    }

    public void addNews(List<News> news){
        for (News news_ : news) {
            Adler32 hash = new Adler32();
            hash.update(news_.toString().getBytes());
            String newsHash = String.valueOf(hash.getValue());
            IndexRequest request = new IndexRequest(INDEX, "doc", newsHash);
            request.source(gson.toJson(news_), XContentType.JSON);
            try {
                client.index(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<News> getSimilarNews(News news, int limit) throws IOException {
        Adler32 hash = new Adler32();
        hash.update(news.toString().getBytes());
        String newsHash = String.valueOf(hash.getValue());

        MoreLikeThisQueryBuilder.Item newsItem = new MoreLikeThisQueryBuilder.Item(INDEX, "doc", newsHash);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.moreLikeThisQuery(new String[]{"title, description"}, new MoreLikeThisQueryBuilder.Item[]{newsItem}));
        builder.size(limit);
        builder.sort(new ScoreSortBuilder().order(SortOrder.DESC));

        SearchRequest request = new SearchRequest();
        request.source(builder);
        List<News> result = new ArrayList<>();
        client.search(request).getHits().forEach(h -> result.add(gson.fromJson(h.getSourceAsString(), News.class)));

        return result;
    }

    public void shutDown() throws IOException {
        client.close();
    }

    public List<News> getAllNews() throws IOException {

        SearchRequest request = new SearchRequest(INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(100);

        request.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(request);
        String scrollId = searchResponse.getScrollId();

        List<News> result = new ArrayList<>();
        searchResponse.getHits().iterator().forEachRemaining(h -> result.add(gson.fromJson(
                h.getSourceAsString(), News.class
        )));

        do {
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            SearchResponse searchScrollResponse = client.searchScroll(scrollRequest);
            searchScrollResponse.getHits().iterator().forEachRemaining(h -> result.add(gson.fromJson(
                    h.getSourceAsString(), News.class
            )));
            scrollId = searchResponse.getScrollId();
        } while (scrollId != null);

        return result;
    }

}
