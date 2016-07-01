package com.swifty.fillcolorbackend;

import java.io.File;

/**
 * Created by Swifty.Wang on 2015/9/14.
 */
public interface UploadSuccessListener {
    void uploadSuccess(File file, String filename);
}
