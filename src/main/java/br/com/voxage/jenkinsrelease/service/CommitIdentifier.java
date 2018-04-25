package br.com.voxage.jenkinsrelease.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import br.com.voxage.jenkinsrelease.util.CommandPrompt;

/**
 * 
 * @author victor.bello
 *
 */

public class CommitIdentifier implements Generator {

    private static final Pattern GIT_HASH     = Pattern.compile("^[a-f0-9]{40}");
    private static final String  VARIABLE     = "GIT_PRIOR_COMMIT";
    private static final String  FILE         = "variable.properties";
    private static final String  TAG_NAME_CMD = "git describe --tags --abbrev=0 --first-parent HEAD^^1";
    private static final String  TAG_HASH_CMD = "git show-ref -s ";
    private static final String  FIRST_COMMIT = "git rev-list --max-parents=0 HEAD";
    private String               workspace;

    public CommitIdentifier(String workspace) {
        this.workspace = workspace;

    }

    @Override
    public void process() throws Exception {
        String result = CommandPrompt.executeCommand("cd " + workspace + " && " + TAG_NAME_CMD);
        System.out.println("Tag identificada: " + result);
        System.out.println("cd " + workspace + " && " + TAG_HASH_CMD + result);
        result = CommandPrompt.executeCommand("cd " + workspace + " && " + TAG_HASH_CMD + result);
        System.out.println("hash: " + result);
        String lines[] = result.split("\\r?\\n");
        result = lines[0];
        if (!GIT_HASH.matcher(result).matches()) {
            result = CommandPrompt.executeCommand("cd " + workspace + " && " + FIRST_COMMIT);
        }

        writeToFile(result);
    }

    private void writeToFile(String hash) throws IOException {
        String content = VARIABLE + "=" + hash;
        System.out.println("Variavel gerada no workspace: " + content);
        Files.write(Paths.get(workspace + File.separator + FILE), content.getBytes(Charset.forName("UTF-8")));
    }

}
