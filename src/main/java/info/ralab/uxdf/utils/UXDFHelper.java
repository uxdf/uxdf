package info.ralab.uxdf.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * UXDF辅助类
 */
@Slf4j
public class UXDFHelper {

    /**
     * 对字符串md5加密
     *
     * @param input 需要加密的字符串
     * @return 返回加密后的MD5
     */
    public static String generate(final String input) {
        assert input != null;
        return DigestUtils.md5Hex(input).toUpperCase();
    }

    /**
     * 删除文件夹及下面所有文件
     *
     * @param dir 需要删除的文件夹
     * @return 删除是否成功
     */
    public static boolean deleteDir(final File dir) {
        // 如果不是文件夹，返回失败
        if (!dir.isDirectory()) {
            return false;
        }
        String[] files = dir.list();
        boolean deleteSuccess;
        if (files != null) {
            for (String fileName : files) {
                File childFile = new File(dir, fileName);
                // 如果是文件夹继续递归
                if (childFile.isDirectory()) {
                    deleteSuccess = deleteDir(childFile);
                } else {
                    deleteSuccess = childFile.delete();
                }
                // 如果删除失败直接返回
                if (!deleteSuccess) {
                    log.warn("can not delete file [{}]", childFile.getAbsolutePath());
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * 获取文件后缀
     *
     * @param file 文件路径
     * @return 文件后缀，如果没有后缀分隔符<b>.</b>，则返回文件名称。
     */
    public static String getNameWithoutExtension(final String file) {
        checkNotNull(file);
        String fileName = new File(file).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }
}
