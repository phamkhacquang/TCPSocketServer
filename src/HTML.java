
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Quang
 */
public class HTML {

    private static String breadcrumbs(String path) {
        path += "/";
        String tmp = "<div class=\"breadcrumbs\">";
        tmp += "<a href=\"/\"><span>root</span></a>";
        if (path.length() > 2) {
            for (int i = 1; i < path.length(); i++) {
                if (path.charAt(i) == '/') {
                    String pathTmp = path.substring(0, i);
                    String fileName = Paths.get(pathTmp).getFileName().toString();
                    tmp += "<span class = \"arrow\"> \\ </span>";
                    tmp += "<a href=\"" + pathTmp + "\">";
                    tmp += "<span>" + fileName + "</span></a>";
                }
            }
        }
        tmp += "</div>";
        return tmp;
    }

    private static String getDetails(File fileEntry) {
        try {
            if (fileEntry.isDirectory()) {
                return fileEntry.listFiles().length + " items";
            } else {
                long filesize = fileEntry.length();
                if (filesize < 1024) {
                    return filesize + " Byte";
                }
                if (filesize < 1024 * 1024) {
                    return filesize / 1024 + " KB";
                }
                if (filesize < 1024 * 1024 * 1024) {
                    return filesize / (1024 * 1024) + " MB";
                } else {
                    return filesize / (1024 * 1024 * 1024) + " GB";
                }
            }
        } catch (Exception ex) {
            System.err.println("File = " + fileEntry.getAbsoluteFile() + " Error = " + ex.toString());
            return "N/A";
        }
    }

    private static String folderToHTML(final File folder) {
        String returnHTML = "";
        for (final File fileEntry : folder.listFiles()) {
            if (!fileEntry.isHidden()) {
                returnHTML += "<li><a href=\"" + fileEntry.toString() + "\">";
                if (fileEntry.isDirectory()) {
                    returnHTML += "<span class=\"icon folder full\"></span>";
                } else {
                    returnHTML += "<span class=\"icon file f - " + getExtensionOfFile(fileEntry) + "\">." + getExtensionOfFile(fileEntry) + "</span>";
                }
                returnHTML += "<span class=\"name\">" + fileEntry.getName() + "</span>";
                returnHTML += "<span class=\"details\">" + getDetails(fileEntry) + "</span>";
                returnHTML += "</a></li>";
            }
        }
        return returnHTML;
    }

    private static String fileToHTML(final File folder) {
        return "<p>abcd</p>";//TODO
    }

    private static String getExtensionOfFile(File file) {
        String fileExtension = "";
        String fileName = file.getName();
        if (fileName.contains(".") && fileName.lastIndexOf(".") != 0) {
            fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return fileExtension;
    }

    private static String addCSS(String bodyHTML) throws IOException {
        String html = "";
        HTML tmp = new HTML();
        InputStream in = tmp.getClass().getResourceAsStream("form.html");
        BufferedReader fr = new BufferedReader(new InputStreamReader(in, "utf-8"));
        String line;
        while ((line = fr.readLine()) != null) {
            if (line.contains("bodyhere")) {
                html += bodyHTML;
            } else {
                html += line;
            }
        }
        return html;
    }

    /**
     * Tạo Header HTTP tương ứng với Content-Length nhập vào
     *
     * @param ContentLength
     * @return
     */
    public static String getHeaderHTTP(int ContentLength) {
        String headerHTTP = "HTTP/1.1 200 OK\n";
        headerHTTP += "Content-Length:" + ContentLength + "\n";
        headerHTTP += "Content-Type: text/html\n\n";
        return headerHTTP;
    }

    /**
     * Xử lý đường dẫn file, và thông tin client thành HTML
     *
     * @param path
     * @param clientInfo
     * @return Toàn bộ trang web gồm cả phần css
     */
    public static String getFullHtml(String path, String clientInfo) {
        String bodyHTML = "<h1 class =\"info\">" + clientInfo + "</h1>";
        bodyHTML += "<div class=\"filemanager\"><ul class = \"data animated\">";
        bodyHTML += HTML.breadcrumbs(path);
        final File folder = new File(path);
        if (folder.isDirectory()) {
            bodyHTML += HTML.folderToHTML(folder);
        } else {
            bodyHTML += HTML.fileToHTML(folder);
        }
        bodyHTML += "</ul></div>";
        try {
            return addCSS(bodyHTML);
        } catch (IOException ex) {
            System.err.println("NO CSS, ERROR=" + ex.toString());
            return bodyHTML;
        }
    }
}
