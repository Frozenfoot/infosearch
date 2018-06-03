import java.net.URL;
import java.util.Date;

/**
 * Created by frozenfoot on 30.05.18.
 */
public class News {
    private String title;
    private String description;
    private Date publishDate;
    private URL url;

    public News(String title, String description, Date publishDate, URL url) {
        this.title = title;
        this.description = description;
        this.publishDate = publishDate;
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

}
