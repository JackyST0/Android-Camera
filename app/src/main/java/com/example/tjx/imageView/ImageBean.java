package com.example.tjx.imageView;

import java.io.File;

/**
 * 照相图片
 */
public class ImageBean {

    private Long id;
    private File file;
    private Long placeId;

    public ImageBean() {
    }

    public ImageBean(Long id, File file) {
        this.id = id;
        this.file = file;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }
}
