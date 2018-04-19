package br.com.voxage.jenkinsrelease.constant;

import br.com.voxage.jenkinsrelease.constant.Type.ReleaseType;

public interface ReleaseIdentifier {
    ReleaseType releaseType(String value);
}
