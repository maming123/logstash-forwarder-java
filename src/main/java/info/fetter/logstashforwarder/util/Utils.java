package info.fetter.logstashforwarder.util;

import info.fetter.logstashforwarder.Forwarder;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.*;
import java.net.URL;
import java.util.*;

public class Utils {

    private static Logger logger = Logger.getLogger(Utils.class);

    public static Map<String, Object> findAndReadConfigFile(String fullpath, boolean mustExist) {
        InputStream in = null;
        boolean confFileEmpty = false;
        try {
            in = new FileInputStream(fullpath);
            if (null != in) {
                Yaml yaml = new Yaml(new SafeConstructor());
                @SuppressWarnings("unchecked")
                Map<String, Object> ret = (Map<String, Object>) yaml.load(new InputStreamReader(in));
                if (null != ret) {
                    return new HashMap<>(ret);
                } else {
                    confFileEmpty = true;
                }
            }

            if (mustExist) {
                if (confFileEmpty) {
                    throw new RuntimeException("Config file " + fullpath + " doesn't have any valid lam configs");
                } else {
                    throw new RuntimeException("Could not find config file  " + fullpath);
                }
            } else {
                return new HashMap<>();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    //throw new RuntimeException(e);
                    logger.error(e.getMessage());
                }
            }
        }
    }

    public static Map<String, Object> findAndReadConfigFile(String name) {
        return findAndReadConfigFile(name, true);
    }


}
