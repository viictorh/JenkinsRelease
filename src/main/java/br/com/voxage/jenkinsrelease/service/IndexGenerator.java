package br.com.voxage.jenkinsrelease.service;

import static br.com.voxage.jenkinsrelease.util.Log.log;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Matcher;

import br.com.voxage.jenkinsrelease.constant.Type.ReleaseType;
import br.com.voxage.jenkinsrelease.util.ReadResource;

/**
 * 
 * @author victor.bello
 *
 */
public enum IndexGenerator {
    INSTANCE;

    public void start(String fromPath, ReleaseType releaseType) throws IOException {
        final FilenameFilter filter = (dir, name) -> dir.isDirectory() && name.toLowerCase().endsWith(".html");
        File filePath = new File(fromPath);
        String[] list = filePath.list(filter);
        Arrays.sort(list, (f1, f2) -> f2.compareToIgnoreCase(f1));
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
            Path path = Paths.get(filePath + File.separator + "index.html");
            Files.write(path, format.getBytes("UTF-8"));
            log.info("Índice criado no caminho: " + path);
        }
    }

    public String encode(String url) throws UnsupportedEncodingException {
        return URLEncoder.encode(url, "UTF-8").replace("+", "%20");
    }

}
