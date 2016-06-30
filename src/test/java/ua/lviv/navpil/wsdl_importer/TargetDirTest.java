package ua.lviv.navpil.wsdl_importer;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.*;

public class TargetDirTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @org.junit.Test
    public void convert() throws Exception {
        File target = folder.newFolder("target");

        TargetDir targetDir = new TargetDir(target.getCanonicalPath());
        String convert = targetDir.createSubDirectory("http://www.w3.org/long/way/to/Heaven.xsd");

        assertEquals(normalize(target.getCanonicalPath() + "/www.w3.org/long/way/to/Heaven.xsd"), normalize(convert));
    }

    private String normalize(String convert) {
        return convert.replace('/', File.separatorChar);
    }

}