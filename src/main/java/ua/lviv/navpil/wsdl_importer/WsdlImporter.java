package ua.lviv.navpil.wsdl_importer;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class WsdlImporter {
    private static final Logger LOG = Logger.getLogger(WsdlImporter.class.getName());
    private static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    private final DocumentBuilder documentBuilder;
    private Set<String> processed;
    private final TargetDir targetDir;

    private WsdlImporter(String rootDir) throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        documentBuilder = dbf.newDocumentBuilder();
        processed = new HashSet<>();
        String targetDir = rootDir + "src/main/resources/http_imported/";
        this.targetDir = new TargetDir(targetDir);
    }

    public static void main(String[] args) {
        String rootDir = System.getProperty("user.dir") + File.separator;
        WsdlImporter importer;
        try {
            importer = new WsdlImporter(rootDir);
        } catch (ParserConfigurationException e) {
            LOG.severe("Parser can not be configured, exit");
            throw new RuntimeException(e);
        }

        String wsdlRoot = rootDir + "src/main/resources/wsdl";
        try (Stream<Path> paths = Files.walk(Paths.get(wsdlRoot))) {
            paths
                    .filter(path -> !path.toFile().isDirectory() && path.toString().endsWith(".wsdl"))
                    .map(Path::toString)
                    .forEach(importer::processFile);

        } catch (IOException e) {
            LOG.severe("Could not walk through " + wsdlRoot);
        }
    }

    private void processFile(String fileName) {
        if (processed.contains(fileName)) {
            return;
        }
        processed.add(fileName);
        for (String schemaLocation : getAllSchemaLocations(fileName)) {
            if (schemaLocation.startsWith("http://")) {
                processHttp(schemaLocation);
            } else {
                File file = new File(Paths.get(fileName).getParent().toString() + File.separator + schemaLocation);
                try {
                    processFile(file.getCanonicalPath());
                } catch (IOException e) {
                    LOG.warning("Could not process file " + file);
                }
            }
        }
    }

    private void processHttp(String url) {
        if (!processed.contains(url)) {
            processed.add(url);
            String schemaFile = downloadSchema(url);
            if (schemaFile != null) {
                processFile(schemaFile);
            }
        }
    }

    private String downloadSchema(String url) {
        try {
            String schemaFileFullPath = getTargetFileName(url);
            if(schemaFileFullPath == null)
                return null;
            URL u = new URL(url);
            ReadableByteChannel in = Channels.newChannel(u.openStream());
            FileChannel out = new FileOutputStream(schemaFileFullPath).getChannel();
            out.transferFrom(in, 0, Long.MAX_VALUE);
            return schemaFileFullPath;
        } catch (IOException e) {
            LOG.warning("Cannot download file from " + url + ", reason: " + e.getMessage());
            return null;
        }
    }

    private String getTargetFileName(String url) {
        try {
            return targetDir.createSubDirectory(url);
        } catch (MalformedURLException e) {
            LOG.warning("Could not parse " + url);
            return null;
        }
    }

    private List<String> getAllSchemaLocations(String fileName) {
        Document doc;
        try {
            doc = documentBuilder.parse(new File(fileName));
        } catch (SAXException | IOException e) {
            LOG.warning("Cannot process " + fileName + ", returning empty list");
            return Collections.emptyList();
        }

        ArrayList<String> schemaLocations = new ArrayList<>();
        for (Node node : nodes(doc.getElementsByTagNameNS(XML_SCHEMA, "import"))) {
            schemaLocations.add(getSchemaLocation(node));
        }
        for (Node node : nodes(doc.getElementsByTagNameNS(XML_SCHEMA, "include"))) {
            schemaLocations.add(getSchemaLocation(node));
        }
        return schemaLocations;

    }

    private String getSchemaLocation(Node node) {
        return node.getAttributes().getNamedItem("schemaLocation").getNodeValue();
    }

    private static Iterable<Node> nodes(NodeList nodeList) {
        return new NodeListIterable(nodeList);
    }
}
