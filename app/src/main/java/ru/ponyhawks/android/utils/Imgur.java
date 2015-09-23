package ru.ponyhawks.android.utils;

import com.cab404.moonlight.framework.AccessProfile;
import com.cab404.moonlight.framework.ShortRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.BasicHttpEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

/**
 * Simple imgur uploader
 * Created at 23:42 on 22/09/15
 *
 * @author cab404
 */
public final class Imgur {

    private Imgur() {
    }

    public static class Gateway extends AccessProfile {
        private String token;

        public Gateway(String token) {
            host = new HttpHost("api.imgur.com", 443, "https");
            this.token = token;
        }

        @Override
        public HttpResponse exec(HttpRequestBase request, boolean follow, int timeout) {
            request.addHeader("Authorization", "Client-ID " + token);
            return super.exec(request, follow, timeout);
        }
    }

    public static class Upload extends ShortRequest {

        private JSONObject response;
        private InputStream stream;
        int length = -1;

        public Upload(InputStream stream) {
            this.stream = stream;
        }

        public Upload(InputStream stream, int length) {
            this.stream = stream;
            this.length = length;
        }

        public Upload(File file) throws FileNotFoundException {
            this.stream = new FileInputStream(file);
            this.length = (int) file.length();
        }

        @Override
        protected HttpRequestBase getRequest(AccessProfile accessProfile) {
            HttpPost post = new HttpPost("/3/image");
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(stream);
            if (length > 0)
                entity.setContentLength(length);
            entity.setChunked(length <= 0);
            entity.setContentType("image/*");
            post.setEntity(entity);
            return post;
        }

        @Override
        protected void handleResponse(String response) {
            try {
                this.response = new JSONObject(response);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        public JSONObject getResponse() {
            return response;
        }
    }

}
