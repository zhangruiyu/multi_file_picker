package com.mhz.multi_file_picker

import me.rosuh.filepicker.filetype.FileType
import com.mhz.multi_file_picker.R

class CustomAudioFileType(private val type: List<String>) : FileType {
    override val fileIconResId: Int
        get() = me.rosuh.filepicker.R.drawable.ic_music_file_picker
    override val fileType: String
        get() = "Audio"

    override fun verify(fileName: String): Boolean {
        /**
         * 使用 endWith 是不可靠的，因为文件名有可能是以格式结尾，但是没有 . 符号
         * 比如 文件名仅为：example_png
         */
        val isHasSuffix = fileName.contains(".")
        if (!isHasSuffix) {
            // 如果没有 . 符号，即是没有文件后缀
            return false
        }
        val suffix = fileName.substring(fileName.lastIndexOf(".") + 1)
        return type.contains(suffix)
    }
}