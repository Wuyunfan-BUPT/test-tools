package org.apache.process.report_utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by wangtong.wt on 2017/3/20.
 *
 * @author wangtong.wt
 * @date 2017/03/20
 */
@Slf4j
public class FileUtils {
    public static String getFileMd5(File file) {
        try {
            InputStream inputStream = new FileInputStream(file);
            return DigestUtils.md5Hex(inputStream);
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

}
