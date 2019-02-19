package hudson.plugins.mstest;

import java.io.File;

class FileOperator {

    static boolean safeDelete(File path, MsTestLogger logger) {
        boolean success = path.delete();
        if (!success) {
            logger.warn("Unable to delete the file %s", path.getAbsolutePath());
            logger.info("This file is a reserved temporary file. You can delete it safely.");
        }
        return success;
    }

    static boolean safeCreateFolder(File path, MsTestLogger logger) {
        boolean success = true;
        if (path.isFile()) {
            success = path.delete();
            if (!success) {
                logger.error("Unable to delete the file: %s.", path.getAbsolutePath());
            }
        }
        if (success && !path.exists()) {
            success = path.mkdirs();
            if (!success) {
                logger.error("Unable to create the folder: %s", path.getAbsolutePath());
            }
        }
        if (!success) {
            logger.info(
                "The path %s is expected to be a folder, writable by the Jenkins worker process.",
                path.getAbsolutePath());
        }
        return success;
    }
}
