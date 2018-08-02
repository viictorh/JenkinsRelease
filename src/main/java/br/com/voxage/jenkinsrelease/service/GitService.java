package br.com.voxage.jenkinsrelease.service;

import static br.com.voxage.jenkinsrelease.util.Log.log;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.voxage.jenkinsrelease.bean.Commit;
import br.com.voxage.jenkinsrelease.bean.Settings;
import br.com.voxage.jenkinsrelease.util.CommandPrompt;

/**
 * 
 * @author victor.bello
 *
 */
public class GitService {

    private static final DateTimeFormatter GIT_DATE_FORMAT        = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
    private static final Pattern           GIT_EMAIL_PATTERN      = Pattern.compile("(?<=\\<)(.*?)(?=\\>)");
    private static final Pattern           GIT_HASH_PATTERN       = Pattern.compile("^[a-f0-9]{40}");
    private static final String            PREVIOUS_TAG_FROM_HEAD = "git describe --tags --abbrev=0 --first-parent HEAD^^1";
    private static final String            PREVIOUS_TAG_FROM_HASH = "git describe --tags --abbrev=0 --first-parent {0}^^1";
    private static final String            HASH_FROM_TAG          = "git show-ref -s {0}";
    private static final String            FIRST_COMMIT           = "git rev-list --max-parents=0 HEAD";
    private static final String            HASHES_BETWEEN         = "git rev-list {0} ^^{1}";
    private static final String            COMMIT_MESSAGE         = "git log {0} -n 1";
    private static final String            TAG_DATE               = "git log -1 --pretty='format:%cd' {0}";
    private static final String            REMOTE_REPO            = "git ls-remote --get-url";
    private String                         command;
    private boolean                        ignoreMerge;

    public GitService(Settings settings) {
        this(settings, true);
    }

    public GitService(Settings settings, boolean ignoreMerge) {
        this.ignoreMerge = ignoreMerge;
        this.command = "cd " + settings.getWorkspace() + " && ";
    }

    public String findPreviousTagFromHead() throws IOException {
        String cmd = command + PREVIOUS_TAG_FROM_HEAD;
        log.debug(cmd);
        String result = CommandPrompt.executeCommand(cmd);
        log.debug("[RESULT]: " + result);
        return result;
    }

    public String findPreviousTagFromHash(String hash) throws IOException {
        String cmd = command + MessageFormat.format(PREVIOUS_TAG_FROM_HASH, hash);
        log.debug(cmd);
        String result = CommandPrompt.executeCommand(cmd);
        log.debug("[RESULT]: " + result);
        return result;
    }

    public String findHashFromTag(String tag) throws IOException {
        String cmd = command + MessageFormat.format(HASH_FROM_TAG, tag);
        log.debug(cmd);
        String result = CommandPrompt.executeCommand(cmd);
        log.debug("[RESULT]: " + result);
        return result;
    }

    public String findHashesBetween(String hashStart, String hashEnd) throws IOException {
        String cmd = command + MessageFormat.format(HASHES_BETWEEN, hashStart, hashEnd);
        log.debug(cmd);
        String result = CommandPrompt.executeCommand(cmd);
        log.debug("[RESULT]: " + System.lineSeparator() + result);
        return result;
    }

    public String findFirstCommit() throws IOException {
        String cmd = command + FIRST_COMMIT;
        log.debug(cmd);
        String result = CommandPrompt.executeCommand(cmd);
        log.debug("[RESULT]: " + result);
        return result;
    }

    public LocalDateTime findTagDate(String tag) throws IOException {
        String cmd = command + MessageFormat.format(TAG_DATE, tag);
        log.debug(cmd);
        String result = CommandPrompt.executeCommand(cmd);
        log.debug("[RESULT]: " + result);
        return LocalDateTime.parse(result, GIT_DATE_FORMAT);
    }

    public String findRemote() throws IOException {
        String cmd = command + REMOTE_REPO;
        log.debug(cmd);
        String result = CommandPrompt.executeCommand(cmd);
        log.debug("[RESULT]: " + result);
        return result;
    }

    public Optional<Commit> findCommitMessage(String hash) throws IOException {
        String cmd = command + MessageFormat.format(COMMIT_MESSAGE, hash);
        log.debug(cmd);
        int authorLineNum = 1;
        int dateLineNum = 2;
        int titleLineNum = 4;
        String result = CommandPrompt.executeCommand(cmd);
        log.trace("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        log.trace("[RESULT]: " + result);
        log.trace("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
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
        log.trace("**********************************************************************************");
        log.trace("[RESULT PARSED] " + commit);
        log.trace("**********************************************************************************");
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
