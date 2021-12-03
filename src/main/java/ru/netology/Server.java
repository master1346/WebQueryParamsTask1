package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final static List<String> VALIDPATHS = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private final ExecutorService executorService;

    public Server(int countThreadsPool) {
        this.executorService = Executors.newFixedThreadPool(countThreadsPool);
    }


    public void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                var clientSocket = serverSocket.accept();
                executorService.submit(() -> handleConnection(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleConnection(Socket socket) {
        try (
                socket;
                final var in = socket.getInputStream();
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {

            var request = Request.fromInputStream(in);
            final var path = request.getPath();

            if (!VALIDPATHS.contains(path)) {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                return;
            }

            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);

            // special case for classic
            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();

                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
                return;
            }

            final var length = Files.size(filePath);
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
