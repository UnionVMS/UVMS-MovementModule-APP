package eu.europa.fisheries.uvms.movement.module.arquillian;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ArquillianSuiteDeployment
public abstract class BuildMovementModuleTestDeployment {

	final static Logger LOG = LoggerFactory.getLogger(BuildMovementModuleTestDeployment.class);

	@Deployment(name = "movementmodule", order = 1)
	public static Archive<?> createMovementModuleDeployment() throws IOException {
		final EnterpriseArchive ear = ShrinkWrap.createFromZipFile(EnterpriseArchive.class,
				new File("target/movement-module.ear"));
		
		modifyWarWithTestPackages(ear);		
		return ear;
	}

	private static void modifyWarWithTestPackages(final EnterpriseArchive ear) {
		Node node = ear.get(getWarContext(ear));
		System.out.println(node.getPath().toString());
		
		if (node.getAsset() instanceof ArchiveAsset) {
			ArchiveAsset archiveAsset = (ArchiveAsset) node.getAsset();
			if (archiveAsset.getArchive() instanceof WebArchive) {
				WebArchive webArchive = (WebArchive) archiveAsset.getArchive();
				webArchive.addPackages(true,"eu.europa.fisheries.uvms.movement.module.arquillian");
			}
		}
	}

	private static String getWarContext(final EnterpriseArchive ear) {
		Map<ArchivePath, Node> content = ear.getContent();
		
		Set<ArchivePath> paths = content.keySet();
		for (ArchivePath archivePath : paths) {
			if (archivePath.get().contains(".war")) {
				return archivePath.get();
			}
		}
		return null;
	}
}
