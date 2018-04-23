package br.com.voxage.jenkinsrelease.constant;

import java.util.regex.Pattern;

/**
 * 
 * @author victor.bello
 *
 */
public enum Type implements ReleaseIdentifier {
    BRANCH {
        @Override
        public ReleaseType releaseType(String value) {
            if (value == null || value.trim().isEmpty()) {
                return ReleaseType.ALL;
            }
            value = value.toUpperCase();
            if (value.endsWith("/MASTER")) {
                return ReleaseType.MASTER;
            } else if (value.contains("/PATCHES/")) {
                return ReleaseType.PATCH;
            } else if (value.contains("/RELEASE/")) {
                return ReleaseType.RELEASE;
            }
            return ReleaseType.ALL;
        }
    },
    TAG {

        @Override
        public ReleaseType releaseType(String value) {
            if (value == null || value.trim().isEmpty()) {
                return ReleaseType.ALL;
            }
            value = value.toUpperCase();
            if (Pattern.compile("RC\\d*").matcher(value).find()) {
                return ReleaseType.RELEASE;
            } else if (Pattern.compile("SP\\d*").matcher(value).find()) {
                return ReleaseType.PATCH;
            } else {
                return ReleaseType.MASTER;
            }
        }
    };

    public enum ReleaseType {
        RELEASE("(?<=ReleaseNotes_).*(RC\\d*)+(?=.html)"),
        MASTER("(?<=ReleaseNotes_)((?!SP|RC).)*(?=.html)"),
        PATCH("(?<=ReleaseNotes_).*(SP\\d*)+(?=.html)"),
        ALL("(?<=ReleaseNotes_).*(?=.html)");

        private final Pattern PATTERN_VERSION;

        private ReleaseType(String pattern) {
            PATTERN_VERSION = Pattern.compile(pattern);
        }

        public Pattern getPatternVersion() {
            return PATTERN_VERSION;
        }

    }

}
