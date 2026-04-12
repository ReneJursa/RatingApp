/*
MIT License

Copyright (c) 2023 Vivek Vashistha

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package com.ratingapp;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileManager {
    protected Context context;

    public FileManager(Context context) {
        this.context = context;
    }

    private File createFile(String filePath) throws IOException {
        int dirIndex = filePath.lastIndexOf(File.separator);
        File rootDirectory = context.getFilesDir();
        File dir = rootDirectory;
        if (dirIndex > 0) {
            dir = new File(rootDirectory, filePath.substring(0, dirIndex));
            dir.mkdirs();
        }
        String fileName = filePath.substring(dirIndex + 1);
        File file = null;
        file = new File(dir, fileName);
        if(!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    public String readFileAsString(String filePath) throws IOException {
        return null;
    }

    public boolean writeFile(String filePath, byte[] data, boolean toAppend) throws IOException {
        File file = createFile(filePath);
        if(file == null || !file.exists()) {
            return false;
        }
        FileOutputStream fos = new FileOutputStream(createFile(filePath), false);
        fos.write(data);
        fos.close();
        return true;
    }

    public boolean copyFile(String fromPath, String toPath) {
        return false;
    }

    public boolean moveFile(String fromPath, String toPath) {
        return false;
    }

    public File makeDirs(String filePath) {
        File rootDirectory = context.getFilesDir();
        File dir = new File(rootDirectory, filePath);
        dir.mkdirs();
        return dir;
    }
}
