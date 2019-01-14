package br.com.voxage.jenkinsrelease.service;

import static br.com.voxage.jenkinsrelease.util.Log.log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import br.com.voxage.jenkinsrelease.bean.BlockMessage;
import br.com.voxage.jenkinsrelease.bean.Commit;
import br.com.voxage.jenkinsrelease.bean.Release;
import br.com.voxage.jenkinsrelease.bean.Settings;
import br.com.voxage.jenkinsrelease.constant.BlockType.Html;
import br.com.voxage.jenkinsrelease.constant.Type.ReleaseType;
import br.com.voxage.jenkinsrelease.util.CommitReader;
import br.com.voxage.jenkinsrelease.util.ReadResource;

public enum ReleaseGenerator {
    INSTANCE;

    private static final DateTimeFormatter RELEASE_DATE_PATTERN = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String            MANTIS_URL           = "http://svox-teste03/mantis/view.php?id={0}";
    private static final Pattern           MANTIS_PATTERN       = Pattern.compile("@\\[mantis[^\\]]*\\]", Pattern.CASE_INSENSITIVE);
    private static final Pattern           GITBLIT_REPO         = Pattern.compile("http://.*/r/", Pattern.CASE_INSENSITIVE);
    private static final Pattern           SVOX_REPO            = Pattern.compile("(\\\\\\\\svox-back01\\\\VoxAge_Back01\\\\GIT\\\\DSS\\\\).+?\\\\.*?\\\\", Pattern.CASE_INSENSITIVE);
    private static final Pattern           NUMBER_PATTERN       = Pattern.compile("\\d+");
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
        String remoteRepo = gitService.findRemote();
        generateFile(tagDate, tagHash, remoteRepo, release);
    }

    /**
     * Busca pelo commit de inicio da versão que está sendo gerado. <br />
     * Verificações: <br />
     * 1 - Verifica se foi informada a tag anterior da qual se deseja iniciar a leitura dos hashes; senão <br />
     * 2 - Verifica se está sendo gerada a versão de release do RC1 do master. Uma vez que no master o RC1 não tem tag anterior (a linha do master fica separada da linha do dev na leitura de tags anteriores, impossibilitando a leitura da tag anterior da forma convencional) <br />
     * 3 - Busca a tag anterior a tag que está sendo gerada; <br />
     * 4 - Caso nenhuma busca traga resultado, será gerado o release notes a partir do primeiro commit no repositório.
     * 
     * @param currentTagHash
     *            - Tag da versão que está sendo gerado o release notes;
     * @return - Hash a partir de onde a versão se inicia, ou seja, de onde os commits serão lidos
     * @throws IOException
     */
    private String findEndCommit(String currentTagHash) throws IOException {
        String previousTag = null;
        if (StringUtils.isNotBlank(settings.getFromTag())) {
            previousTag = settings.getFromTag();
        } else {
            String tag = settings.getTag().toUpperCase();
            if (ReleaseType.RELEASE == settings.getReleaseType() && !tag.contains("SP") && tag.contains("RC1")) {
                String masterHash = gitService.findBranchHash("master");
                previousTag = gitService.findMostRecentTagFromHash(masterHash);
            } else {
                previousTag = gitService.findPreviousTagFromHash(currentTagHash);
            }
        }
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
        int threadQuantity = hashes.length / 5;
        if (threadQuantity < 1) {
            threadQuantity = 1;
        } else if (threadQuantity > 10) {
            threadQuantity = 10;
        }

        log.info("Lendo a mensagem dos commits utilizando " + threadQuantity + " thread(s)");
        ExecutorService executor = Executors.newFixedThreadPool(threadQuantity);
        List<Commit> commits = new ArrayList<>();
        List<Future<Optional<Commit>>> futures = new ArrayList<Future<Optional<Commit>>>();
        for (final String hash : hashes) {
            futures.add(executor.submit(() -> gitService.findCommitMessage(hash)));
        }

        for (Future<Optional<Commit>> future : futures) {
            try {
                future.get().ifPresent(c -> commits.add(c));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        return commits;
    }

    private void generateFile(LocalDateTime tagDate, String tagHash, String remoteRepo, Release release) throws IOException {
        String projectName = settings.getWorkspace().substring(settings.getWorkspace().lastIndexOf("\\") + 1);
        if (remoteRepo.startsWith("http")) {
            remoteRepo = GITBLIT_REPO.matcher(remoteRepo).replaceAll("");
        } else {
            remoteRepo = SVOX_REPO.matcher(remoteRepo).replaceAll("");
        }
        String fileContent = ReadResource.read("release_template.html").replace("@[TagDate]", tagDate.format(RELEASE_DATE_PATTERN));

        fileContent = fileContent.replace("@[Compatibility]", release.isCompatibilityBreak() ? "" : "none").replace("@[Tag]", settings.getTag()).replace("@[TagHash]", tagHash);
        fileContent = fileContent.replace("@[Old]", release.containsUnaddressedCommit() ? "" : "none").replace("@[Project]", projectName);
        fileContent = fileContent.replace("@[gitBlit]", remoteRepo + "/" + tagHash);
        StringBuilder sb = new StringBuilder();
        for (BlockMessage blockMessage : release.getBlockMessages()) {
            for (String item : blockMessage.getMessageItems()) {
                sb.append(blockMessage.getBlockType().getHtmlType().asHtml(item));
                sb.append(System.lineSeparator());
            }
            fileContent = fileContent.replace(blockMessage.getBlockType().getBlockName(), sb.toString());
            sb = new StringBuilder();
        }

        fileContent = generateMantisUrl(fileContent);
        String filePath = settings.getWorkspace() + File.separator + "ReleaseNotes_" + settings.getTag() + ".html";
        Files.write(Paths.get(filePath), fileContent.getBytes("UTF-8"));
        log.info("Release notes criado no caminho: " + filePath);
    }

    private String generateMantisUrl(String fileContent) {
        Matcher mantisMatcher = MANTIS_PATTERN.matcher(fileContent);
        while (mantisMatcher.find()) {
            String mantisTag = mantisMatcher.group();
            Matcher numberMatcher = NUMBER_PATTERN.matcher(mantisTag);
            while (numberMatcher.find()) {
                String number = numberMatcher.group();
                fileContent = fileContent.replace(mantisTag, Html.LINK.asHtml(MessageFormat.format(MANTIS_URL, number), "Mantis-" + number));
            }
        }
        return fileContent;
    }
}
