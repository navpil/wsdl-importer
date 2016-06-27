package ua.lviv.navpil.wsdl_importer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

class TargetDir {
    private static final Logger LOG = Logger.getLogger(WsdlImporter.class.getName());
    private String targetDir;

    TargetDir(String targetDir) {
        this.targetDir = targetDir;
    }

    public String createSubDirectory(String url) throws MalformedURLException {
        URL u = new URL(url);

        String relativeUrl = u.getPath();
        String target = targetDir + File.separator + u.getAuthority() + relativeUrl.substring(0, relativeUrl.lastIndexOf('/'));
        File dir = new File(target);
        if (!dir.exists() && !dir.mkdirs()) {
            LOG.warning("Directory " + dir + " could not be created");
            return null;
        }
        String[] segments = relativeUrl.split("/");
        String schemaFileName = segments[segments.length - 1];
        return target + File.separator + schemaFileName;
    }
}
