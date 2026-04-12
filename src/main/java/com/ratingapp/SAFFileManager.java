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

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;

import androidx.documentfile.provider.DocumentFile;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class SAFFileManager extends FileManager{
    private Uri baseUri;
    public SAFFileManager(Context context, Uri baseUri) {
        super(context);
        this.baseUri = baseUri;
    }

    private DocumentFile createDocumentFile(String filePath, boolean createDirectory) {
        DocumentFile rootDirectory = DocumentFile.fromTreeUri(context, baseUri);
        int dirIndex = filePath.lastIndexOf(File.separator);
        String fileName = filePath;
        DocumentFile fileParentDirectory = rootDirectory;
        if (dirIndex > 0) {
            fileName = filePath.substring(dirIndex + 1);
            String[] dirs = filePath.substring(0, dirIndex).split("/");
            for (String dir : dirs) {
                if(dir == null || dir.isEmpty()) continue;
                DocumentFile existingDirectory = fileParentDirectory.findFile(dir);
                if (existingDirectory == null) {
                    fileParentDirectory = fileParentDirectory.createDirectory(dir);
                } else {
                    fileParentDirectory = existingDirectory;
                }
            }
        }
        DocumentFile file = Objects.requireNonNull(fileParentDirectory).findFile(fileName);
        if(file == null) {
            if(createDirectory) {
                return fileParentDirectory.createDirectory(fileName);
            } else {
                return Objects.requireNonNull(fileParentDirectory).createFile("*/*", fileName);
            }
        } else {
            if(!file.isDirectory() && createDirectory) {
                return fileParentDirectory.createDirectory(fileName);
            } else {
                return file;
            }
        }
    }

    @Override
    public boolean writeFile(String filePath, byte []data, boolean toAppend) throws IOException {
        DocumentFile file = createDocumentFile(filePath, false);
        if(file == null || !file.exists()) {
            return false;
        }
        DocumentsContract.deleteDocument(context.getContentResolver(), Objects.requireNonNull(file).getUri());
        file = createDocumentFile(filePath, false);
        if(file == null || !file.exists()) {
            return false;
        }
        if(toAppend) {
            String existingData = readFileAsString(filePath);
            if (existingData != null) {
                byte[] existingByteData = existingData.getBytes(StandardCharsets.UTF_8);
                byte[] finalData = new byte[existingByteData.length + data.length];
                System.arraycopy(existingByteData, 0, finalData, 0, existingByteData.length);
                System.arraycopy(data, 0, finalData, existingByteData.length, data.length);
                data = finalData;
            }
        }

        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(Objects.requireNonNull(file).getUri(), "w");
        FileOutputStream fos =  new FileOutputStream(pfd.getFileDescriptor());
        //fos.write(data);
        //fos.close();
        try (BufferedOutputStream out = new BufferedOutputStream(
                     new FileOutputStream(pfd.getFileDescriptor()))) {
            out.write(data);
            out.flush();
            out.close();
            pfd.close();
        }
        return true;
    }

    @Override
    public String readFileAsString(String filePath) throws IOException {
        // Open a specific media item using InputStream.
        String []segments = filePath.split("/");
        DocumentFile file = DocumentFile.fromTreeUri(context, baseUri);
        for (String segment : segments) {
            file = Objects.requireNonNull(file).findFile(segment);
            if (file == null) {
                return null;
            }
        }
        ContentResolver resolver = context.getContentResolver();
        Uri fileUri = Objects.requireNonNull(file).getUri();
//        Uri fileUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ANOAA/document/primary%3ANOAA%2FTrip%2Ffile_1.csv");
        try (InputStream stream = resolver.openInputStream(fileUri)) {
            // Perform operations on "stream".
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            stream.close();
            return builder.toString();
        }
    }

    public boolean exportAppPrivateFile(String from, String to) throws IOException {
        File fromFile = new File(context.getFilesDir(), from);
        DocumentFile targetFile = null;
        if(to == null) {
            targetFile = DocumentFile.fromTreeUri(context, baseUri);
        } else {
            targetFile = createDocumentFile(to, fromFile.isDirectory());
        }
        if(fromFile.isDirectory()) {
            return copyDirectory(fromFile, targetFile);
        }
        return copyFile(fromFile, targetFile);
    }

    private boolean copyDirectory(File fromDir, DocumentFile toDir) throws IOException {
        for(File file : fromDir.listFiles()) {
            if(file.isFile()) {
                DocumentFile toFile = toDir.findFile(file.getName());
                if(toFile == null) {
                    toFile = toDir.createFile("*/*", file.getName());
                }
                copyFile(file, toFile);
            } else {
                DocumentFile dir = toDir.findFile(file.getName());
                if(dir == null) {
                    dir = toDir.createDirectory(file.getName());
                }
                copyDirectory(file, dir);
            }
        }
        return true;
    }

    private boolean copyFile(File from, DocumentFile to) throws IOException {
        FileInputStream in = new FileInputStream((from));
        FileOutputStream out = new FileOutputStream(context.getContentResolver().openFileDescriptor(to.getUri(), "w").getFileDescriptor());
        // Copy the bits from instream to outstream
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.flush();
        out.close();
        return true;
    }

    @Override
    public File makeDirs(String filePath) {
        return super.makeDirs(filePath);
    }
}
