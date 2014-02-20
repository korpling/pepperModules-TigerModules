package de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.mappers.tests;

import junit.framework.TestCase;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperModuleProperty;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.Tiger2Properties;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.mappers.Tiger22SaltMapper;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDominanceRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.tiger2.samples.Tiger2Sample;

public class Tiger22SaltMapperTest extends TestCase {

	private Tiger22SaltMapper fixture= null;

	public Tiger22SaltMapper getFixture() {
		return fixture;
	}

	public void setFixture(Tiger22SaltMapper fixture) {
		this.fixture = fixture;
	}
	
	public void setUp()
	{
		setFixture(new Tiger22SaltMapper());
		getFixture().setSDocument(SaltFactory.eINSTANCE.createSDocument());
		getFixture().setProperties(new Tiger2Properties());
		getFixture().setCorpus(Tiger2Sample.createSampleCorpus1());
	}
	
	public void testRenameEdgeType()
	{
		PepperModuleProperty<String> prop= (PepperModuleProperty<String>)getFixture().getProps().getProperty(Tiger2Properties.PROP_RENAME_EDGE_TYPE);
		prop.setValue("prim=edge");
		
		getFixture().mapSDocument();
		
		for (SDominanceRelation sDomRel: getFixture().getSDocument().getSDocumentGraph().getSDominanceRelations())
		{
			assertNotNull(sDomRel.getSTypes());
			assertEquals(1, sDomRel.getSTypes().size());
			assertEquals("edge", sDomRel.getSTypes().get(0));
		}
	}
	
	public void testRenameAnnotationName()
	{
		PepperModuleProperty<String> prop= (PepperModuleProperty<String>)getFixture().getProps().getProperty(Tiger2Properties.PROP_RENAME_ANNOTATION_NAME);
		prop.setValue("lemma=A");
				
		getFixture().mapSDocument();
		
		for (SToken sTok: getFixture().getSDocument().getSDocumentGraph().getSTokens())
		{
			if (sTok.getSName().equals("sTok1"))
				assertNotNull(sTok.getSAnnotation("A"));
			else if (sTok.getSName().equals("sTok2"))
				assertNotNull(sTok.getSAnnotation("A"));
			assertNull(sTok.getSAnnotation("lemma"));
		}
		
	}
}
