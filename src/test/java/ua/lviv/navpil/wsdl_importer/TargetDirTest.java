package ua.lviv.navpil.wsdl_importer;

import java.io.File;

import static org.junit.Assert.*;

public class TargetDirTest {
    @org.junit.Test
    public void convert() throws Exception {
        TargetDir creator = new TargetDir("/c/temp/target");
        String convert = creator.createSubDirectory("http://www.w3.org/long/way/to/Heaven.xsd");

        assertEquals("/c/temp/target/www.w3.org/long/way/to/Heaven.xsd".replace('/', File.separatorChar), convert.replace('/', File.separatorChar));

    }

}