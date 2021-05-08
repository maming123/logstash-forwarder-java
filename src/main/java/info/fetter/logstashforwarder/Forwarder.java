package info.fetter.logstashforwarder;

/*
 * Copyright 2015 Didier Fetter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import info.fetter.logstashforwarder.config.ConfigurationManager;
import info.fetter.logstashforwarder.config.FilesSection;
import info.fetter.logstashforwarder.protocol.LumberjackClient;
import info.fetter.logstashforwarder.protocol.MyKafkaClient;
import info.fetter.logstashforwarder.protocol.StdoutClient;
import info.fetter.logstashforwarder.util.*;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.*;
import org.apache.log4j.spi.RootLogger;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static org.apache.log4j.Level.*;

public class Forwarder {
    private static final String SINCEDB = ".logstash-forwarder-java";
    private static Logger logger = Logger.getLogger(Forwarder.class);
    private static int spoolSize = 1024 * 4;
    private static int idleTimeout = 5000;
    //初始空闲时间
    private static int idleTimeoutInit = 5000;
    private static int networkTimeout = 15000;
    private static String config;
    private static ConfigurationManager configManager;
    private static FileWatcher watcher;
    private static FileReader fileReader;
    private static InputReader inputReader;
    private static Level logLevel = INFO;
    private static boolean debugWatcherSelected = false;
    private static ProtocolAdapter adapter;
    private static Random random = new Random();
    private static int signatureLength = 4096; //
    private static boolean tailSelected = false;
    private static String logfile = null;
    private static String logfileSize = "10MB";
    private static int logfileNumber = 50;
    private static String sincedbFile = SINCEDB;
    private static int proxyPort=0;



    public static void main(String[] args) {

        try {
            System.out.println("Signal handling example.");
            SignalHandler handler = new SignalHandlerCust();
            // kill -TERM 命令
            Signal termSignal = new Signal("TERM");
            Signal.handle(termSignal, handler);

            parseOptions(args);
            setupLogging();
            configManager = new ConfigurationManager(config);
            configManager.readConfiguration();
            String str = configManager.writeConfiguration(configManager.getConfig());
            boolean usingInode = false;
            if (configManager.getConfig().getSettings() != null) {
                usingInode = configManager.getConfig().getSettings().isUsingInode();
            }

            if (usingInode) {
                logger.info("file id in cache using inode");
            } else {
                logger.info("file id in cache using signature");
            }

            //是否启动
            getProxyPort();
            if(proxyPort>0) {
                //延迟10秒后启动timer，然后每间隔5秒运行
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    public void run() {
                        //从proxy获取cpu和内存的状态结果，看是否需要限速
                        getSpeedLimitStatusFromProxy();
                    }
                }, 10000, 30000);
            }else{
                logger.info( "because get proxyPort failed, so getSpeedLimitStatusFromProxy api not run,please check proxy config application.yml");
            }
            //

            watcher = new FileWatcher(usingInode);
            watcher.setMaxSignatureLength(signatureLength);
            watcher.setTail(tailSelected);
            watcher.setSincedb(sincedbFile);
            int count = 0;
            logger.info("watcher.addFilesToWatch... start");
            for (FilesSection files : configManager.getConfig().getFiles()) {
                for (String path : files.getPaths()) {
                    watcher.addFilesToWatch(path, new Event(files.getFields()), files.getDeadTimeInSeconds() * 1000, files.getMultiline(), files.getFilter());
                    count++;
                    System.out.println("file number: " + count + " filename: " + path);
                }
            }
            logger.info("watcher.addFilesToWatch... end");
            logger.info("watcher.initialize() start");
            watcher.initialize();
            logger.info("watcher.initialize() end");

            fileReader = new FileReader(spoolSize);
            inputReader = new InputReader(spoolSize, System.in);
            connectToServer();
            infiniteLoop();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(3);
        }

    }


    private static void infiniteLoop() throws IOException, InterruptedException {
        while (true) {
            try {
                logger.info("watcher.checkFiles() start...");
                watcher.checkFiles();
                logger.info("watcher.checkFiles() complete...");
                logger.info("watcher.readFiles(fileReader) begin");
                while (watcher.readFiles(fileReader) == spoolSize) ;
                logger.info("watcher.readFiles(fileReader) end");
                while (watcher.readStdin(inputReader) == spoolSize) ;
                Thread.sleep(idleTimeout);
                if (SignalHandlerCust.getKILLSIGNAL() == 15) {
                    System.out.println("infiniteLoop kill by number: " + SignalHandlerCust.getKILLSIGNAL());
                    break;
                }
            } catch (AdapterException e) {
                logger.error("Lost server connection: " + e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
                Thread.sleep(networkTimeout);
                connectToServer();
            }
        }
    }

    private static void connectToServer() {
        if (configManager.getConfig().getOutput().getStdout() != null) {
            if (adapter == null) {
                adapter = new StdoutClient();
                fileReader.setAdapter(adapter);
                inputReader.setAdapter(adapter);
            }
        } else if (configManager.getConfig().getOutput().getKafka() != null) {
            logger.info("connectToServer: kafka adapter: " + adapter);
            if (adapter == null) {
                List<String> hostList = configManager.getConfig().getOutput().getKafka().getHosts();
                String topic = configManager.getConfig().getOutput().getKafka().getTopic();
                String acks = configManager.getConfig().getOutput().getKafka().getAcks();
                Integer retries = configManager.getConfig().getOutput().getKafka().getRetries();
                Integer batchSize = configManager.getConfig().getOutput().getKafka().getBatchSize();
                Integer lingerMs = configManager.getConfig().getOutput().getKafka().getLingerMs();
                Integer bufferMemory = configManager.getConfig().getOutput().getKafka().getBufferMemory();
                String charset = configManager.getConfig().getOutput().getKafka().getCharset();
                Integer output2kafka = configManager.getConfig().getOutput().getKafka().getOutput2kafka();
                String hosts = String.join(",", hostList);
                try {
                    adapter = new MyKafkaClient(hosts, topic, acks, retries, batchSize, lingerMs, bufferMemory, charset, output2kafka);
                    fileReader.setAdapter(adapter);
                    inputReader.setAdapter(adapter);
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        } else if (configManager.getConfig().getOutput().getNetwork() != null) {
            int randomServerIndex = 0;
            List<String> serverList = configManager.getConfig().getOutput().getNetwork().getServers();
            networkTimeout = configManager.getConfig().getOutput().getNetwork().getTimeout() * 1000;
            if (adapter != null) {
                try {
                    adapter.close();
                } catch (AdapterException e) {
                    logger.error("Error while closing connection to " + adapter.getServer() + ":" + adapter.getPort());
                } finally {
                    adapter = null;
                }
            }
            while (adapter == null) {
                try {
                    randomServerIndex = random.nextInt(serverList.size());
                    String[] serverAndPort = serverList.get(randomServerIndex).split(":");
                    logger.info("Trying to connect to " + serverList.get(randomServerIndex));
                    adapter = new LumberjackClient(configManager.getConfig().getOutput().getNetwork().getSslCA(), serverAndPort[0], Integer.parseInt(serverAndPort[1]), networkTimeout);
                    fileReader.setAdapter(adapter);
                    inputReader.setAdapter(adapter);
                } catch (Exception ex) {
                    if (logger.isDebugEnabled()) {
                        logger.error("Failed to connect to server " + serverList.get(randomServerIndex) + " : ", ex);
                    } else {
                        logger.error("Failed to connect to server " + serverList.get(randomServerIndex) + " : " + ex.getMessage());
                    }
                    try {
                        Thread.sleep(networkTimeout);
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        }
    }

    @SuppressWarnings("static-access")
    static void parseOptions(String[] args) {
        Options options = new Options();
        Option helpOption = new Option("help", "print this message");
        Option quietOption = new Option("quiet", "operate in quiet mode - only emit errors to log");
        Option debugOption = new Option("debug", "operate in debug mode");
        Option debugWatcherOption = new Option("debugwatcher", "operate watcher in debug mode");
        Option traceOption = new Option("trace", "operate in trace mode");
        Option infoOption = new Option("info", "operate in info mode");
        Option tailOption = new Option("tail", "read new files from the end");

        Option spoolSizeOption = OptionBuilder.withArgName("number of events")
                .hasArg()
                .withDescription("event count spool threshold - forces network flush")
                .create("spoolsize");
        Option idleTimeoutOption = OptionBuilder.withArgName("")
                .hasArg()
                .withDescription("time between file reads in seconds")
                .create("idletimeout");
        Option configOption = OptionBuilder.withArgName("config file")
                .hasArg()
                .isRequired()
                .withDescription("path to logstash-forwarder configuration file")
                .create("config");
        Option signatureLengthOption = OptionBuilder.withArgName("signature length")
                .hasArg()
                .withDescription("Maximum length of file signature")
                .create("signaturelength");
        Option logfileOption = OptionBuilder.withArgName("logfile name")
                .hasArg()
                .withDescription("Logfile name")
                .create("logfile");
        Option logfileSizeOption = OptionBuilder.withArgName("logfile size")
                .hasArg()
                .withDescription("Logfile size (default 10M)")
                .create("logfilesize");
        Option logfileNumberOption = OptionBuilder.withArgName("number of logfiles")
                .hasArg()
                .withDescription("Number of logfiles (default 5)")
                .create("logfilenumber");
        Option sincedbOption = OptionBuilder.withArgName("sincedb file")
                .hasArg()
                .withDescription("Sincedb file name")
                .create("sincedb");

        options.addOption(helpOption)
                .addOption(idleTimeoutOption)
                .addOption(spoolSizeOption)
                .addOption(quietOption)
                .addOption(debugOption)
                .addOption(debugWatcherOption)
                .addOption(traceOption)
                .addOption(tailOption)
                .addOption(signatureLengthOption)
                .addOption(configOption)
                .addOption(logfileOption)
                .addOption(logfileNumberOption)
                .addOption(logfileSizeOption)
                .addOption(sincedbOption)
                .addOption(infoOption);

        CommandLineParser parser = new GnuParser();
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("spoolsize")) {
                spoolSize = Integer.parseInt(line.getOptionValue("spoolsize"));
            }
            if (line.hasOption("idletimeout")) {
                idleTimeout = Integer.parseInt(line.getOptionValue("idletimeout"));
                idleTimeoutInit =idleTimeout;
            }
            if (line.hasOption("config")) {
                config = line.getOptionValue("config");
            }
            if (line.hasOption("signaturelength")) {
                signatureLength = Integer.parseInt(line.getOptionValue("signaturelength"));
            }
            if (line.hasOption("quiet")) {
                logLevel = ERROR;
            }

            if (line.hasOption("info")) {
                logLevel = INFO;
            }
            if (line.hasOption("debug")) {
                logLevel = DEBUG;
            }
            if (line.hasOption("trace")) {
                logLevel = TRACE;
            }

            if (line.hasOption("debugwatcher")) {
                debugWatcherSelected = true;
            }
            if (line.hasOption("tail")) {
                tailSelected = true;
            }
            if (line.hasOption("logfile")) {
                logfile = line.getOptionValue("logfile");
            }
            if (line.hasOption("logfilesize")) {
                logfileSize = line.getOptionValue("logfilesize");
            }
            if (line.hasOption("logfilenumber")) {
                logfileNumber = Integer.parseInt(line.getOptionValue("logfilenumber"));
            }
            if (line.hasOption("sincedb")) {
                sincedbFile = line.getOptionValue("sincedb");
            }
        } catch (ParseException e) {
            printHelp(options);
            System.exit(1);
        } catch (NumberFormatException e) {
            System.err.println("Value must be an integer");
            printHelp(options);
            System.exit(2);
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("logstash-forwarder", options);
    }

    private static void setupLogging() throws IOException {
        Appender appender;
        Layout layout = new PatternLayout("%d %p %c{1} - %m%n");
        if (logfile == null) {
            appender = new ConsoleAppender(layout);
        } else {
            RollingFileAppender rolling = new RollingFileAppender(layout, logfile, true);
            rolling.setMaxFileSize(logfileSize);
            rolling.setMaxBackupIndex(logfileNumber);
            appender = rolling;
        }
        BasicConfigurator.configure(appender);
        RootLogger.getRootLogger().setLevel(logLevel);
        if (debugWatcherSelected) {
            Logger.getLogger(FileWatcher.class).addAppender(appender);
            Logger.getLogger(FileWatcher.class).setLevel(DEBUG);
            Logger.getLogger(FileWatcher.class).setAdditivity(false);
        }
    }

    private static void getSpeedLimitStatusFromProxy(){

           String url ="http://localhost:"+proxyPort+"/lam-proxy/proxyController/getSpeedLimitStatus";
           try {
               boolean enabledSpeedLimit = configManager.getConfig().getSettings().getEnabledSpeedLimit();
               idleTimeout =idleTimeoutInit;
               if(enabledSpeedLimit) {
                   String str = HttpClientUtil.get(url);

                   ReturnJsonType json = JsonHelper.fromJson(str, ReturnJsonType.class);
                   //resCode=0，object 前一位 是 -1,0,0,0： "未开启指标采集,object 第二位是限制日志采集时间（秒），第三位cpuused，第四位memoryused"
                   //resCode=0，object 前一位 是 0,0,0,0： "指标正常"
                   //resCode=0，object 前一位 是 1,0,0,0： "指标超限，采取限流措施"
                   //resCode=1，获取代理url失败
                   logger.debug(url+"  response: "+str);
                   String v = json.getData().toString().split(",")[0];
                   if (json.getResCode().equals(ExceptionEnum.SUCESS.getStatus())) {
                       if (v.equals("1")) {
                           String idleTime =json.getData().toString().split(",")[1];

                           idleTimeout = Integer.parseInt(idleTime)*1000;
                           logger.info("speed limit [" + idleTimeout + "] millisecond ");
                       }
                   }
               }else {
                   logger.debug("enabledSpeedLimit status is close，if need open please set enabled_speed_limit : true");
               }
           } catch (Exception ex) {
               logger.error(url + " error: " + ex.getMessage());
               //System.exit(4);
           }

    }

    private static void getProxyPort() {
        String userDir = System.getProperty("user.dir");
        int ii = userDir.lastIndexOf("/");
        String proxyApplicationYml = userDir.substring(0, ii) + "/conf/application.yml";
        System.out.println("proxyApplicationYml " + proxyApplicationYml);
        File file = new File(proxyApplicationYml);
        if (file.exists()) {
            Map<String, Object> map = Utils.findAndReadConfigFile(proxyApplicationYml);
            System.out.println("map" + JsonHelper.toJson(map));
            proxyPort = Integer.parseInt(((HashMap) map.get("server")).get("port").toString());

        } else {
            logger.info(proxyApplicationYml + " is not exist");
        }
    }

}


