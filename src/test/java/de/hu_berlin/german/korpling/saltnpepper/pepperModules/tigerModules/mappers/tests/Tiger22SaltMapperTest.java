package de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.mappers.tests;

import org.eclipse.emf.common.util.URI;

import junit.framework.TestCase;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.mappers.Tiger22SaltMapper;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.tiger2.Corpus;
import de.hu_berlin.german.korpling.tiger2.Edge;
import de.hu_berlin.german.korpling.tiger2.Graph;
import de.hu_berlin.german.korpling.tiger2.NonTerminal;
import de.hu_berlin.german.korpling.tiger2.Segment;
import de.hu_berlin.german.korpling.tiger2.Terminal;
import de.hu_berlin.german.korpling.tiger2.samples.Tiger2Sample;

public class Tiger22SaltMapperTest extends TestCase 
{
	private Tiger22SaltMapper fixture= null;

	public void setFixture(Tiger22SaltMapper fixture) {
		this.fixture = fixture;
	}

	public Tiger22SaltMapper getFixture() {
		return fixture;
	}
	
	public void setUp()
	{
		this.setFixture(new Tiger22SaltMapper());
	}

	/**
	 * Checks the mapping of a corpus given in <tiger2/> model to a given Salt model.
	 * Containing one {@link Segment}, containing one {@link Graph}, containing {@link Terminal} objects, {@link NonTerminal} objects,
	 * {@link Edge} objects between {@link Terminal}s (anaphoric) and between {@link NonTerminal}s and {@link Terminal}s (prim).
	 */
	public void testSample1()
	{
		Corpus corpus= Tiger2Sample.createSample1();
		Tiger22SaltMapper mapper= new Tiger22SaltMapper();
		SDocument sDocument= SaltFactory.eINSTANCE.createSDocument();
		mapper.map(corpus, sDocument);
		
		SaltProject saltProject= SaltFactory.eINSTANCE.createSaltProject();
		SCorpusGraph cGraph= SaltFactory.eINSTANCE.createSCorpusGraph();
		saltProject.getSCorpusGraphs().add(cGraph);
		SCorpus sCorpus= SaltFactory.eINSTANCE.createSCorpus();
		cGraph.addSNode(sCorpus);
		cGraph.addSDocument(sCorpus, sDocument);
		
		saltProject.saveSaltProject_DOT(URI.createFileURI("d:/Test/Piotr/tigerModulesTest/dot/"));
	}
}
	