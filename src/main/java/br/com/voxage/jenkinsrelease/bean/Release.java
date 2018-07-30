package br.com.voxage.jenkinsrelease.bean;

import java.util.Map;
import java.util.Set;

import br.com.voxage.jenkinsrelease.constant.BlockType;

/**
 * 
 * @author victor.bello
 *
 */
public class Release {
    private boolean                     compatibilityBreak;
    private boolean                     old;
    private Map<BlockType, Set<String>> blockCommits;

    public Release(Map<BlockType, Set<String>> blockCommits) {
        this.blockCommits = blockCommits;
    }

    public boolean isCompatibilityBreak() {
        return compatibilityBreak;
    }

    public void setCompatibilityBreak(boolean compatibilityBreak) {
        this.compatibilityBreak = compatibilityBreak;
    }

    public Map<BlockType, Set<String>> getBlockCommits() {
        return blockCommits;
    }

    public void setBlockCommits(Map<BlockType, Set<String>> blockCommits) {
        this.blockCommits = blockCommits;
    }

    public boolean isOld() {
        return old;
    }

    public void setOld(boolean old) {
        this.old = old;
    }

    @Override
    public String toString() {
        return "Release [compatibilityBreak=" + compatibilityBreak + ", blockCommits=" + blockCommits + "]";
    }

}
