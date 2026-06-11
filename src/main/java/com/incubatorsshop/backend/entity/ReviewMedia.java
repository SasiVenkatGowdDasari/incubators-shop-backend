package com.incubatorsshop.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "review_media")
public class ReviewMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    @JsonIgnore // CRITICAL: Prevents infinite JSON loop crash!
    private Review review;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_type", nullable = false)
    private String fileType; // E.g., 'IMAGE' or 'VIDEO'
    
    public ReviewMedia() {
    }

    public ReviewMedia(Long id, Review review, String filePath, String fileType) {
        this.id = id;
        this.review = review;
        this.filePath = filePath;
        this.fileType = fileType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Review getReview() { return review; }
    public void setReview(Review review) { this.review = review; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
}