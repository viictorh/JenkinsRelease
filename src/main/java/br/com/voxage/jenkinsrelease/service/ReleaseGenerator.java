package br.com.voxage.jenkinsrelease.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import br.com.voxage.jenkinsrelease.bean.Commit;
import br.com.voxage.jenkinsrelease.bean.Release;
import br.com.voxage.jenkinsrelease.bean.Settings;
import br.com.voxage.jenkinsrelease.constant.BlockType;
import br.com.voxage.jenkinsrelease.util.CommitReader;
import br.com.voxage.jenkinsrelease.util.ReadResource;

public enum ReleaseGenerator {
    INSTANCE;
    private static final DateTimeFormatter RELEASE_DATE_PATTERN = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String            MANTIS_URL           = "http://svox-teste03/mantis/view.php?id={0}";
    private GitService                     gitService;
    private Settings                       settings;

    public void start(Settings settings) throws IOException {
        this.settings = settings;
        gitService = new GitService(settings);
        LocalDateTime tagDate = gitService.findTagDate(settings.getTag());
        String tagHash = gitService.findHashFromTag(settings.getTag());
        String fromHash = findEndCommit(tagHash);
        List<Commit> commits = findCommitMessages(tagHash, fromHash);
        Release release = new CommitReader(commits).read();
        generateFile(tagDate, tagHash, release);
    }

    private String findEndCommit(String commit) throws IOException {
        String previousTag = gitService.findPreviousTagFromHash(commit);
        String previousHash = gitService.findHashFromTag(previousTag);
        String lines[] = previousHash.split("\\r?\\n");
        previousHash = lines[0];
        if (!GitService.validHash(previousHash)) {
            previousHash = gitService.findFirstCommit();
        }
        return previousHash;
    }

    private List<Commit> findCommitMessages(String startCommit, String endCommit) throws IOException {
        String[] hashes = gitService.findHashesBetween(startCommit, endCommit).split(System.lineSeparator());
        List<Commit> commits = new ArrayList<>();
        for (String hash : hashes) {
            gitService.findCommitMessage(hash).ifPresent(c -> commits.add(c));
        }
        return commits;
    }

    private void generateFile(LocalDateTime tagDate, String tagHash, Release release) throws IOException {
        String projectName = settings.getWorkspace().substring(settings.getWorkspace().lastIndexOf("\\") + 1);
        String fileContent = ReadResource.read("release_template.html").replace("@[TagDate]", tagDate.format(RELEASE_DATE_PATTERN));
        fileContent = fileContent.replace("@[Compatibility]", release.isCompatibilityBreak() ? "" : "none").replace("@[Tag]", settings.getTag()).replace("@[TagHash]", tagHash);
        fileContent = fileContent.replace("@[Old]", release.isOld() ? "" : "none").replace("@[Project]", projectName);
        StringBuilder sb = new StringBuilder();
        for (Entry<BlockType, Set<String>> entry : release.getBlockCommits().entrySet()) {
            Set<String> set = release.getBlockCommits().get(entry.getKey());
            if (BlockType.HtmlType.LIST == entry.getKey().getHtmlType()) {
                sb.append("<ul>");
                for (String item : set) {
                    sb.append("<li>").append(item).append("</li>");
                }
                sb.append("</ul>");
            } else {
                for (String item : set) {
                    sb.append("<span class='multi-item-line pipe-border'>").append(item).append("</span>");
                }
            }
            fileContent = fileContent.replace(entry.getKey().getBlockName(), sb.toString());
            sb = new StringBuilder();
        }

        String filePath = settings.getWorkspace() + File.separator + "test.html";
        Files.write(Paths.get(filePath), fileContent.getBytes());
        System.out.println("FilePath: " + filePath);
    }

}
