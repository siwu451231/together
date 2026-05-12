package com.example.backend.document.dto;

import jakarta.validation.constraints.Size;

public class UpdateDocumentRequest {
    @Size(max = 120)
    private String title;

    private String content;

    private Boolean manualSave;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getManualSave() {
        return manualSave;
    }

    public void setManualSave(Boolean manualSave) {
        this.manualSave = manualSave;
    }
}
