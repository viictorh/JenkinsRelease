package br.com.voxage.service;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.voxage.util.ReadResource;

/**
 * 
 * @author victor.bello
 *
 */
public class PageGenerator implements Generator {

    private File        filePath;
    private ReleaseType releaseType;

    private enum ReleaseType {
        RELEASE("(?<=Release_).*(RC\\d*)+(?=.html)"),
        MASTER("(?<=Release_)((?!SP|RC).)*(?=.html)"),
        PATCH("(?<=Release_).*(SP\\d*)+(?=.html)"),
        ALL("(?<=Release_).*(?=.html)");

        private final Pattern PATTERN_VERSION;

        private ReleaseType(String pattern) {
            PATTERN_VERSION = Pattern.compile(pattern);
        }

        public Pattern getPatternVersion() {
            return PATTERN_VERSION;
        }

        public static ReleaseType fromBranch(String branch) {
            if (branch == null || branch.trim().isEmpty()) {
                return ALL;
            }
            branch = branch.toLowerCase();
            if (branch.endsWith("/master")) {
                return ReleaseType.MASTER;
            } else if (branch.contains("/patches/")) {
                return ReleaseType.PATCH;
            } else if (branch.contains("/release/")) {
                return ReleaseType.RELEASE;
            }
            return ALL;
        }

    }

    public PageGenerator(String fromPath, String branch) {
        this.filePath = new File(fromPath);
        this.releaseType = ReleaseType.fromBranch(branch);
    }

    public PageGenerator(String fromPath) {
        this(fromPath, null);
    }

    @Override
    public void process() throws IOException {
        final FilenameFilter filter = (dir, name) -> dir.isDirectory() && name.toLowerCase().endsWith(".html");
        String[] list = filePath.list(filter);
        if (list != null && list.length > 0) {

            StringBuilder sb = new StringBuilder();
            StringBuilder iframe = new StringBuilder();
            for (String fileName : list) {
                Matcher matcher = releaseType.getPatternVersion().matcher(fileName);
                if (matcher.find()) {
                    String version = matcher.group();
                    sb.append("<span>").append("<a href='").append(fileName).append("'>").append(version).append("</a>").append("</span>");

                    iframe.append("<div>").append("<iframe id='").append(version).append("' src='").append(fileName).append("'>").append("</iframe>").append("</div>");
                }
            }

            String html = ReadResource.read("index.html");

            String format = html.replace("$(indice)", sb.toString()).replace("$(iframes)", iframe.toString());
            Files.write(Paths.get(filePath + File.separator + "index.html"), format.getBytes());

        }
    }

}
