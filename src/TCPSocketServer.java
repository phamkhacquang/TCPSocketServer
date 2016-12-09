
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TCPSocketServer {

    private static void log(String message) {
        System.out.println(message);
    }

    public static void main(String[] args) throws IOException {
        ServerSocket listener = null;
        int server_port = 8080;
        if (args.length != 1) {
            System.out.println("Usage: tcpserver [tcp port]");
            System.exit(0);
        }
        try {
            server_port = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            System.out.println("Usage: tcpserver [tcp port]" + ex.toString());
            System.exit(0);
        }
        log("Server is waiting to accept user...");
        int clientNumber = 0;

        // Mở một ServerSocket tại cổng server_port.
        try {
            listener = new ServerSocket(server_port);
        } catch (IOException e) {
            log(e.toString());
            System.exit(0);
        }

        try {
            while (true) {
                // Chấp nhận một yêu cầu kết nối từ phía Client.
                // Đồng thời nhận được một đối tượng Socket tại server.
                Socket socketOfServer = listener.accept();
                new ServiceThread(socketOfServer, clientNumber++).start();
            }
        } finally {
            listener.close();
        }

    }

    private static class ServiceThread extends Thread {

        private final int clientNumber;
        private final Socket socketOfServer;

        public ServiceThread(Socket socketOfServer, int clientNumber) {
            this.clientNumber = clientNumber;
            this.socketOfServer = socketOfServer;
            log("New connection with client# " + this.clientNumber + " at " + socketOfServer);
        }

        @Override
        public void run() {
            try {
                // Mở luồng vào ra trên Socket tại Server.
                BufferedReader is = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
                BufferedWriter os = new BufferedWriter(new OutputStreamWriter(socketOfServer.getOutputStream()));

                // Đọc đường dẫn do client gửi tới
                String[] header = is.readLine().split(" ");
                String url = "/";
                if (header[0].equals("GET")) {
                    url = header[1];
                }
                String path = java.net.URLDecoder.decode(url, "UTF-8");
                log("path=" + path);
                final File myFile = new File(path);
                if (myFile.isDirectory()) {
                    //Thêm thông tin client
                    String clientInfo = socketOfServer.getRemoteSocketAddress().toString();
                    clientInfo = clientInfo.replaceFirst(":", ", PORT:").replaceFirst("/", "Client IP: ");
                    String fullHTML = HTML.getFullHtml(path, clientInfo);
                    //Gửi tới Client
                    os.write(HTML.getHeaderHTTP(fullHTML.getBytes().length));
                    os.write(fullHTML);
                    os.flush();
                } else {
                    //Gửi file
                    os.write("HTTP/1.1 200 OK\n");
                    os.write("Accept-Ranges: bytes\n");
                    os.write("Content-Length: " + myFile.length() + "\n");
                    os.write("Content-Type: application/octet-stream\n");
                    os.write("Content-Disposition: attachment; filename=\"" + myFile.getName() + "\"\n\n");
                    Files.copy(Paths.get(path), socketOfServer.getOutputStream());
                    os.flush();
                }
                is.close();
                os.close();
                socketOfServer.close();
            } catch (Exception e) {
                log(e.toString());
            }
        }
    }

}
