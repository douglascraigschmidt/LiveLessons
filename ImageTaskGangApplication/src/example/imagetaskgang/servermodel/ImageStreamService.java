package example.imagetaskgang.servermodel;

import java.net.URL;
import java.util.List;

import retrofit.http.Body;
import retrofit.http.POST;

public interface ImageStreamService {
    @POST("/ImageStreamServlet")
    ServerResponse execute(@Body List<List<URL>> inputURLs);
}
