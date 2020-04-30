package com.example.multi_file_picker;

import java.util.List;

import me.rosuh.filepicker.filetype.FileType;

public class CustomAudioFileType implements FileType {
    private List<String> type;

    public CustomAudioFileType(List<String> type) {
        this.type = type;
    }

    @Override
    public int getFileIconResId() {
        return R.drawable.ic_music_file_picker;
    }

    @Override
    public String getFileType() {
        return "Audio";
    }

    @Override
    public boolean verify(String fileName) {
        /**
         * 使用 endWith 是不可靠的，因为文件名有可能是以格式结尾，但是没有 . 符号
         * 比如 文件名仅为：example_png
         */
        boolean isHasSuffix = fileName.contains(".");
        if (!isHasSuffix) {
            // 如果没有 . 符号，即是没有文件后缀
            return false;
        }
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        return type.contains(suffix);
    }

}
