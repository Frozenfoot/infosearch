import com.google.gson.Gson;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by frozenfoot on 31.05.18.
 */
public class ElasticSearchAgent {
    private static final String INDEX ="news_index";
    private static final String HOST = "localhost";
    private static final int PORT = 9200;
    private static final String SCHEME = "http";
    private RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(HOST, PORT, SCHEME)));
    private Gson gson = new Gson();

/*    ElasticSearchAgent () {
        this.node = NodeBuilder.nodeBuilder().settings(Settings.settingsBuilder()
                .put("path.home", "/usr/share/elasticsearch"))
                .clusterName("elasticsearch")
                .client(true)
                .node();
        this.client = this.node.client();
        this.gson = new Gson();
    }*/

    public void addNews(List<News> news){
        for (News news_ : news) {
            IndexRequest request = new IndexRequest(INDEX, "doc");
            request.source(gson.toJson(news_), XContentType.JSON);
            try {
                client.index(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<News> getAllNews() throws IOException {
        List<News> result = new ArrayList<>();

        SearchRequest request = new SearchRequest(INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(100);

        request.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(request);
        String scrollId = searchResponse.getScrollId();
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
