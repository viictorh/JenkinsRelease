package br.com.voxage.jenkinsrelease.bean;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;

/**
 * 
 * @author victor.bello
 *
 */
public class Settings {
    private String workspace;
    private String tag;
    private String fromTag;
    private Level  logLevel;

    public Settings(String workspace, String tag, String fromTag, String logLevel) {
        this.workspace = workspace;
        this.tag = tag;
        this.fromTag = fromTag;
        if (StringUtils.isBlank(logLevel)) {
            this.logLevel = Level.INFO;
        } else {
            this.logLevel = Level.toLevel(logLevel.toUpperCase());
        }
    }

    public String getWorkspace() {
        return workspace;
    }

    public String getTag() {
        return tag;
    }

    public String getFromTag() {
        return fromTag;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    @Override
    public String toString() {
        return "Settings [workspace=" + workspace + ", tag=" + tag + ", fromTag=" + fromTag + ", logLevel=" + logLevel + "]";
    }

}
