package br.com.voxage.jenkinsrelease.start;

import static br.com.voxage.jenkinsrelease.util.Log.log;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;

import br.com.voxage.jenkinsrelease.bean.Settings;
import br.com.voxage.jenkinsrelease.constant.Type;
import br.com.voxage.jenkinsrelease.service.IndexGenerator;
import br.com.voxage.jenkinsrelease.service.ReleaseGenerator;

/**
 * 
 * @author victor.bello
 *
 */
public class Main {

    public static void main(String[] args) throws IOException, ParseException {
        long start = System.currentTimeMillis();
        log.debug(args);
        Settings settings = options(args);
        log.changeLevel(settings.getLogLevel());
        log.info("Parametros enviados: " + settings);
        log.info("Iniciando geração do release notes da versão");
        ReleaseGenerator.INSTANCE.start(settings);
        log.info("Iniciando geração do índice");
        IndexGenerator.INSTANCE.start(settings.getWorkspace(), Type.TAG.releaseType(settings.getTag()));
        long end = System.currentTimeMillis();
        log.info("*************************************************************");
        log.info("Execução realizada com sucesso em " + ((end - start) / 1000) + " segundos");
        log.info("*************************************************************");
    }

    public static Settings options(String[] args) throws ParseException {
        Options options = new Options();
        options.addRequiredOption("w", "workspace", true, "Workspace do git que contém a versão que será gerada do release notes");
        options.addRequiredOption("t", "to", true, "Tag que será gerada");
        options.addOption(new Option("f", "from", true, "Tag até a qual irá"));
        options.addOption(new Option("l", "log-level", true, "Informa o log level, as opções são: " + Level.ERROR + " " + Level.INFO + " " + Level.WARN + " " + Level.DEBUG + " " + Level.TRACE + " " + Level.OFF));
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar JenkinsRelease.jar", options, true);
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        return new Settings(cmd.getOptionValue("w"), cmd.getOptionValue("t"), cmd.getOptionValue("f"), cmd.getOptionValue("l"));
    }

}
