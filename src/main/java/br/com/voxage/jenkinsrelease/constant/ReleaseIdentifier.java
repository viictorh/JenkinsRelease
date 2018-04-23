package br.com.voxage.jenkinsrelease.constant;

import br.com.voxage.jenkinsrelease.constant.Type.ReleaseType;

/**
 * 
 * @author victor.bello
 *
 */
public interface ReleaseIdentifier {
    ReleaseType releaseType(String value);
}
