package br.com.voxage.jenkinsrelease.bean;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;

import br.com.voxage.jenkinsrelease.constant.Type;
import br.com.voxage.jenkinsrelease.constant.Type.ReleaseType;

/**
 * 
 * @author victor.bello
 *
 */
public class Settings {
    private String      workspace;
    private String      tag;
    private String      fromTag;
    private Level       logLevel;
    private ReleaseType releaseType;

    public Settings(String workspace, String tag, String fromTag, String logLevel) {
        this.workspace = workspace;
        this.tag = tag;
        this.releaseType = Type.TAG.releaseType(tag);
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

    public ReleaseType getReleaseType() {
        return releaseType;
    }

    @Override
    public String toString() {
        return "Settings [workspace=" + workspace + ", tag=" + tag + ", fromTag=" + fromTag + ", logLevel=" + logLevel + ", releaseType=" + releaseType + "]";
    }

}
