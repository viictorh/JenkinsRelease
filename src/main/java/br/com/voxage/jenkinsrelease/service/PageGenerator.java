package br.com.voxage.jenkinsrelease.service;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;

import br.com.voxage.jenkinsrelease.constant.Type.ReleaseType;
import br.com.voxage.jenkinsrelease.util.ReadResource;

/**
 * 
 * @author victor.bello
 *
 */
public class PageGenerator implements Generator {

    private File        filePath;
    private ReleaseType releaseType;

    public PageGenerator(String fromPath, ReleaseType releasetype) {
        this.filePath = new File(fromPath);
        this.releaseType = releasetype;
        System.out.println("Caminho: " + fromPath);
        System.out.println("ReleaseType: " + releaseType);
    }

    public PageGenerator(String fromPath) {
        this(fromPath, ReleaseType.ALL);
    }

    @Override
    public void process() throws IOException {
        final FilenameFilter filter = (dir, name) -> dir.isDirectory() && name.toLowerCase().endsWith(".html");
        String[] list = filePath.list(filter);
        if (list != null && list.length > 0) {

            StringBuilder span = new StringBuilder();
            StringBuilder option = new StringBuilder();
            StringBuilder iframe = new StringBuilder();
            for (String fileName : list) {
                Matcher matcher = releaseType.getPatternVersion().matcher(fileName);
                if (matcher.find()) {
                    String version = matcher.group();
                    String encodedUrl = encode(fileName);
                    span.append("<span>").append("<a href='").append(encodedUrl).append("'>").append(version).append("</a>").append("</span>");
                    option.append("<option value='").append(encodedUrl).append("'>").append(version).append("</option>");
                    iframe.append("<div>").append("<iframe id='").append(version).append("' src='").append(encodedUrl).append("'>").append("</iframe>").append("</div>");
                }
            }

            String html = ReadResource.read("index.html");
            String format = html.replace("$(indice)", span.toString()).replace("$(fixed-indice)", option.toString()).replace("$(iframes)", iframe.toString());
            Files.write(Paths.get(filePath + File.separator + "index.html"), format.getBytes());

        }
    }

    public String encode(String url) throws UnsupportedEncodingException {
        return URLEncoder.encode(url, "UTF-8").replace("+", "%20");
    }

}
