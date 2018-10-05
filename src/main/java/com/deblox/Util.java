package com.deblox;

/*

Copyright 2015 Kegan Holtzhausen

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {

    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    /*
    Config loader
     */
    static public JsonObject loadConfig(String file) throws IOException {
        logger.info("loading file: " + file);
        try (InputStream stream = new FileInputStream(file)) {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

            String line = reader.readLine();
            while (line != null) {
                sb.append(line).append('\n');
                line = reader.readLine();
                logger.debug(line);
            }

            return new JsonObject(sb.toString());

        } catch (IOException e) {
            logger.error("Unable to load config file: " + file);
            e.printStackTrace();
            throw new IOException("Unable to open file: " + file );
        }
    }

    /*
    classpath resource hunting config loader
     */
    static public JsonObject loadConfig(Object o, String file) {

        try (InputStream stream = o.getClass().getResourceAsStream(file)) {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

            String line = reader.readLine();
            while (line != null) {
                sb.append(line).append('\n');
                line = reader.readLine();
            }

            return new JsonObject(sb.toString());

        } catch (IOException e) {
            System.err.println("Unable to load config, returning with nothing");
            e.printStackTrace();
            return new JsonObject();
        }

    }


    static public String loadFileAsString(Object o, String file) {
        try (InputStream stream = o.getClass().getResourceAsStream(file)) {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

            String line = reader.readLine();
            while (line != null) {
                sb.append(line).append('\n');
                line = reader.readLine();
            }
            return sb.toString();
        } catch (NullPointerException npe) {
            logger.error("Unable to load file, no such file: {}", file);
            npe.printStackTrace();
            return "";
        } catch (IOException e) {
            logger.error("Unable to load file, returning with nothing");
            e.printStackTrace();
            return "";
        }
    }

    public static String sha1(String input) {
        String sha1 = null;
        try {
            MessageDigest msdDigest = MessageDigest.getInstance("SHA-1");
            msdDigest.update(input.getBytes("UTF-8"), 0, input.length());
            sha1 = DatatypeConverter.printHexBinary(msdDigest.digest());
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            logger.error("unable to serialize");
        }
        return sha1;
    }


    public static void writeStringToFile(String content, String fileName, Handler<AsyncResult<Void>> handler) {
        writeStringToFile(content, fileName);
        handler.handle(Future.succeededFuture());
    }

    public static void writeStringToFile(String content, String fileName) {
        File file = new File(fileName);
        // if file doesn't exists, then create it

        if (file.exists()) {
            logger.debug("Not replacing file");
            return;
        } else {
            logger.info("Capture message for request: {}", content);
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                // get the content in bytes
                byte[] contentInBytes = content.getBytes();
                FileOutputStream fop = new FileOutputStream(file);

                fop.write(contentInBytes);
                fop.flush();
                fop.close();

                logger.info("Wrote: {}", fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String readStringFromFile(String fileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        File file = new File(fileName);
        FileInputStream fip = new FileInputStream(file);
        int content;
        while((content=fip.read())!=-1){
            sb.append((char)content);
        }
        return sb.toString();
    }


    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

}