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

import static org.apache.log4j.Level.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import info.fetter.logstashforwarder.util.LastModifiedFileFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileWatcherTest {
	Logger logger = Logger.getLogger(FileWatcherTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		BasicConfigurator.configure();
		RootLogger.getRootLogger().setLevel(TRACE);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		BasicConfigurator.resetConfiguration();
	}

	//@Test
	public void testFileWatch() throws InterruptedException, IOException {
		FileWatcher watcher = new FileWatcher(true);
		watcher.addFilesToWatch("./test.txt", new Event().addField("test", "test"), FileWatcher.ONE_DAY, null, null);
		for(int i = 0; i < 100; i++) {
			Thread.sleep(1000);
			watcher.checkFiles();
		}
	}

	//@Test
	public void testFileWatchWithMultilines() throws InterruptedException, IOException {
		FileWatcher watcher = new FileWatcher(true);
		Multiline multiline = new Multiline();
		watcher.addFilesToWatch("./test.txt", new Event().addField("test", "test"), FileWatcher.ONE_DAY, multiline, null);
		for(int i = 0; i < 100; i++) {
			Thread.sleep(1000);
			watcher.checkFiles();
		}
	}

	//@Test
	public void testWildcardWatch() throws InterruptedException, IOException {
		if(System.getProperty("os.name").toLowerCase().contains("win")) {
			logger.warn("Not executing this test on windows");
			return;
		}
		FileWatcher watcher = new FileWatcher(true);
		watcher.addFilesToWatch("./testFileWatcher*.txt", new Event().addField("test", "test"), FileWatcher.ONE_DAY, null, null);
		watcher.initialize();

		File file1 = new File("testFileWatcher1.txt");
		File file2 = new File("testFileWatcher2.txt");
		//File file3 = new File("test3.txt");
		//File file4 = new File("test4.txt");

		//File testDir = new File("testFileWatcher");
		//FileUtils.forceMkdir(new File("test"));

		watcher.checkFiles();
		Thread.sleep(100);
		FileUtils.write(file1, "file 1 line 1\n", true);
		Thread.sleep(100);
		watcher.checkFiles();
		FileUtils.write(file1, "file 1 line 2\n", true);
		//FileUtils.write(file2, "file 2 line 1\n", true);
		Thread.sleep(1000);
		watcher.checkFiles();
//		FileUtils.moveFileToDirectory(file1, testDir, true);
//		FileUtils.write(file2, "file 2 line 2\n", true);
		FileUtils.moveFile(file1, file2);
//		FileUtils.write(file2, "file 3 line 1\n", true);
//
		Thread.sleep(1000);
		watcher.checkFiles();
//
//
		watcher.close();
		FileUtils.deleteQuietly(file1);
		FileUtils.deleteQuietly(file2);
//		FileUtils.forceDelete(testDir);



	}

        @Test
	public void testWildcardWatchMultiline() throws InterruptedException, IOException {
		if(System.getProperty("os.name").toLowerCase().contains("win")) {
			logger.warn("Not executing this test on windows");
			return;
		}
		FileWatcher watcher = new FileWatcher(true);
                Map<String, String> m = new HashMap<String, String>();
                m.put("pattern", " nl");
                m.put("negate", "false");
                Multiline multiline = new Multiline(m);
		watcher.addFilesToWatch("./testFileWatcher*.txt", new Event().addField("test", "test"), FileWatcher.ONE_DAY, multiline, null);
		watcher.initialize();

		File file1 = new File("testFileWatcher1.txt");
		File file2 = new File("testFileWatcher2.txt");
		//File file3 = new File("test3.txt");
		//File file4 = new File("test4.txt");

		//File testDir = new File("testFileWatcher");
		//FileUtils.forceMkdir(new File("test"));

		watcher.checkFiles();
		Thread.sleep(100);
		FileUtils.write(file1, "file 1 line 1\n nl line 1-2", true);
		Thread.sleep(100);
		watcher.checkFiles();
		FileUtils.write(file1, "file 1 line 2\n", true);
                Thread.sleep(100);
		watcher.checkFiles();
		FileUtils.write(file1, " nl line 3\n", true);
		//FileUtils.write(file2, "file 2 line 1\n", true);
		Thread.sleep(1000);
		watcher.checkFiles();
//		FileUtils.moveFileToDirectory(file1, testDir, true);
//		FileUtils.write(file2, "file 2 line 2\n", true);
		FileUtils.moveFile(file1, file2);
//		FileUtils.write(file2, "file 3 line 1\n", true);
//
		Thread.sleep(1000);
		watcher.checkFiles();
//
//
		watcher.close();
		FileUtils.deleteQuietly(file1);
		FileUtils.deleteQuietly(file2);
//		FileUtils.forceDelete(testDir);



	}

	@Test
	public void testRegexWatch() throws InterruptedException, IOException {
		if(System.getProperty("os.name").toLowerCase().contains("win")) {
			logger.warn("Not executing this test on windows");
			return;
		}
		//不要的文件
		//String fileToWarth="/Users/maming/test/c/^[0-9]+.log";
		//String fileToWarth="/Users/maming/test/c/^d_[0-9]+_[0-9]+.log";
		String fileToWarth="/Users/maming/test/c/^[^.]+.log$"; //不包含.的任意字符
//		RegexFileFilter regex2 = new RegexFileFilter("^[0-9]+.log");
//		boolean bbb =Pattern.compile("^[2-9]+.log").matcher("1.log").matches();
//		boolean bb = regex2.accept(new File("/Users/maming/test/c/1.log"));
		String directory = FilenameUtils.getFullPath(fileToWarth);
		String regex = FilenameUtils.getName(fileToWarth);
		logger.trace("Directory : " + new File(directory).getCanonicalPath() + ", regex : " + regex);
		IOFileFilter fileFilter = FileFilterUtils.and(
				FileFilterUtils.fileFileFilter(),
				new RegexFileFilter(regex),
				new LastModifiedFileFilter(FileWatcher.ONE_DAY));
		File dir =new File(directory);


		Collection<File> collection = FileUtils.listFiles(dir, fileFilter, null);
		//FileWatcher watcher = new FileWatcher();
		//watcher.addFilesToWatch(fileToWarth, new Event().addField("test", "test"),FileWatcher.ONE_DAY , null, null);
		//watcher.initialize();
		for(File file : collection) {
			logger.info(file.getAbsolutePath());
		}


	}

	@Test
	public void dummy() {}
}
