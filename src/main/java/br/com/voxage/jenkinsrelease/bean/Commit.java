package br.com.voxage.jenkinsrelease.bean;

import java.time.LocalDateTime;

public class Commit {
    private String        hash;
    private String        authorName;
    private String        authorEmail;
    private LocalDateTime dateTime;
    private String        title;
    private String        message;

    public Commit(String hash, String authorName, String authorEmail, LocalDateTime dateTime, String title, String message) {
        this.hash = hash;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.dateTime = dateTime;
        this.title = title;
        this.message = message;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hash == null) ? 0 : hash.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Commit other = (Commit) obj;
        if (hash == null) {
            if (other.hash != null)
                return false;
        } else if (!hash.equals(other.hash))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Commit [hash=" + hash + ", authorName=" + authorName + ", authorEmail=" + authorEmail + ", dateTime=" + dateTime + ", title=" + title + ", message=" + message + "]";
    }

}
