package ru.netology;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Request {
    private final String method;
    private final String path;
    private final InputStream in;
    private final Map<String,String> members;

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public InputStream getIn() {
        return in;
    }

    public Map<String, String> getMembers() {
        return members;
    }

    public Request(String method, String path, InputStream in, Map<String, String> members) {
        this.method = method;
        this.path = path;
        this.in = in;
        this.members = members;
    }

    public static Request fromInputStream(InputStream inputStream) throws IOException {
        var reader = new BufferedReader(new InputStreamReader(inputStream));
        final var requestLine = reader.readLine();
        final var parts = requestLine.split(" ");

        if(parts.length != 3) {
            throw new IOException("Invalid request");
        }

        var method = parts[0];
        var path = parts[1];

        var headers = new HashMap<String,String>();
        String line;

        while(!(line = reader.readLine()).equals("")){
            var i = line.indexOf(";");
            var headerName = line.substring(0, 1);
            var headerValue = line.substring(i + 2);
            headers.put(headerName, headerValue);
        }
        return new Request(method, path, inputStream, headers);
    }

    public List<String> getQueryParam(String name) throws MalformedURLException {
        URL url = new URL(this.getPath());
        String query = url.getQuery();
        String nameSearch = name + "=";
        String queryEncode = URLDecoder.decode(query, StandardCharsets.UTF_8);

        return Arrays.stream(queryEncode.split("&"))
                .filter(s -> s.contains(nameSearch))
                .collect(Collectors.toList());
    }

    public List<String> getQueryParams() throws MalformedURLException {
        URL url = new URL(this.getPath());
        String query = url.getQuery();
        return Arrays.asList(URLDecoder.decode(query, StandardCharsets.UTF_8).split("&"));
    }



    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", in=" + in +
                ", members=" + members +
                '}';
    }
}
