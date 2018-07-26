package br.com.voxage.jenkinsrelease.service;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import br.com.voxage.jenkinsrelease.bean.Commit;
import br.com.voxage.jenkinsrelease.util.CommandPrompt;

public class GitService {
    private final static Logger            LOGGER                 = Logger.getLogger(GitService.class);
    private static final DateTimeFormatter GIT_DATE_FORMAT        = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
    private static final Pattern           GIT_EMAIL_PATTERN      = Pattern.compile("(?<=\\<)(.*?)(?=\\>)");
    private static final Pattern           GIT_HASH_PATTERN       = Pattern.compile("^[a-f0-9]{40}");
    private static final String            PREVIOUS_TAG_FROM_HEAD = "git describe --tags --abbrev=0 --first-parent HEAD^^1";
    private static final String            PREVIOUS_TAG_FROM_HASH = "git describe --tags --abbrev=0 --first-parent {0}^^1";
    private static final String            HASH_FROM_TAG          = "git show-ref -s {0}";
    private static final String            FIRST_COMMIT           = "git rev-list --max-parents=0 HEAD";
    private static final String            HASHES_BETWEEN         = "git rev-list {0} ^^{1}";
    private static final String            COMMIT_MESSAGE         = "git log {0} -n 1";
    private String                         command;
    private boolean                        ignoreMerge;

    public GitService(String workspace) {
        this(workspace, true);
    }

    public GitService(String workspace, boolean ignoreMerge) {
        this.ignoreMerge = ignoreMerge;
        this.command = "cd " + workspace + " && ";
    }

    public String findPreviousTagFromHead() throws IOException {
        String cmd = command + PREVIOUS_TAG_FROM_HEAD;
        LOGGER.info("findPreviousTagFromHead: " + cmd);
        String result = CommandPrompt.executeCommand(cmd);
        LOGGER.info("findPreviousTagFromHead - [RESULT]: " + result);
        return result;
    }

    public String findPreviousTagFromHash(String hash) throws IOException {
        String cmd = command + MessageFormat.format(PREVIOUS_TAG_FROM_HASH, hash);
        LOGGER.info("findPreviousTagFromHash: " + cmd);
        String result = CommandPrompt.executeCommand(cmd);
        LOGGER.info("findPreviousTagFromHash - [RESULT]: " + result);
        return result;
    }

    public String findHashFromTag(String tag) throws IOException {
        String cmd = command + MessageFormat.format(HASH_FROM_TAG, tag);
        LOGGER.info("findHashFromTag: " + cmd);
        String result = CommandPrompt.executeCommand(cmd);
        LOGGER.info("findHashFromTag: - [RESULT]: " + result);
        return result;
    }

    public String findHashesBetween(String hashStart, String hashEnd) throws IOException {
        String cmd = command + MessageFormat.format(HASHES_BETWEEN, hashStart, hashEnd);
        LOGGER.info("findHashesBetween: " + cmd);
        String result = CommandPrompt.executeCommand(cmd);
        LOGGER.info("findHashesBetween: - [RESULT]: " + result);
        return result;
    }

    public String findFirstCommit() throws IOException {
        String cmd = command + FIRST_COMMIT;
        LOGGER.info("findHashesBetween: " + cmd);
        String result = CommandPrompt.executeCommand(cmd);
        LOGGER.info("findHashesBetween: - [RESULT]: " + result);
        return result;
    }

    public Optional<Commit> findCommitMessage(String hash) throws IOException {
        int authorLineNum = 1;
        int dateLineNum = 2;
        int titleLineNum = 4;
        String cmd = command + MessageFormat.format(COMMIT_MESSAGE, hash);
        LOGGER.info("findCommitMessage: " + cmd);
        String result = CommandPrompt.executeCommand(cmd);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("findCommitMessage: - [RESULT]: " + result);
        }
        String[] lines = result.split(System.lineSeparator());
        if (lines.length < 5 || (lines[1].contains("Merge:") && ignoreMerge) || result.contains("@[ReleaseIgnore]")) {
            return Optional.empty();
        }
        if (lines[1].contains("Merge:") && !ignoreMerge) {
            authorLineNum = 2;
            dateLineNum = 3;
            titleLineNum = 5;
        }

        String authorLine = lines[authorLineNum];
        String titleLine = lines[titleLineNum].trim();
        String email = match(GIT_EMAIL_PATTERN, authorLine);
        String authorName = authorLine.replaceAll("Au.*:|<|>|" + email, "").trim();
        String dateLine = lines[dateLineNum];
        String date = dateLine.replace("Date:", "").trim();
        String message = result.substring(result.indexOf(System.lineSeparator()) + 1).replaceAll(authorLine + "|" + dateLine + "|" + titleLine, "").trim();
        Commit commit = new Commit(hash, authorName, email, LocalDateTime.parse(date, GIT_DATE_FORMAT), titleLine, message);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("findCommitMessage: [PARSE] " + commit);
        }
        return Optional.of(commit);
    }

    private String match(Pattern pattern, String check) {
        Matcher matcher = GIT_EMAIL_PATTERN.matcher(check);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    public static boolean validHash(String hash) {
        return GIT_HASH_PATTERN.matcher(hash).matches();
    }

}
