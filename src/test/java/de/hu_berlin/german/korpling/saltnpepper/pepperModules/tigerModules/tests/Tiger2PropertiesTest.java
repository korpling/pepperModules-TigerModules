package de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.tests;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperModuleProperty;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.Tiger2Properties;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;

public class Tiger2PropertiesTest 
{
	private Tiger2Properties fixture= null;

	public Tiger2Properties getFixture() {
		return fixture;
	}

	public void setFixture(Tiger2Properties fixture) {
		this.fixture = fixture;
	}
	@Before
	public void setUp()
	{
		setFixture(new Tiger2Properties());
	}
	
	/**
	 * Tests whether the property {@link Tiger2Properties#PROP_EDGE_2_SRELATION} is retrieved as defined.
	 */
	@Test
	public void testGetPropEdge2SRelation(){
		PepperModuleProperty<String> prop= (PepperModuleProperty<String>)this.getFixture().getProperty(Tiger2Properties.PROP_EDGE_2_SRELATION);
		String edge2relationMapping= "prim:"+STYPE_NAME.SDOMINANCE_RELATION+", secedge:"+STYPE_NAME.SPOINTING_RELATION;
		prop.setValue(edge2relationMapping);
		
		Map<String, STYPE_NAME> edge2relation= getFixture().getPropEdge2SRelation();
		
		assertNotNull(edge2relation);
		assertNotNull(edge2relation.get("prim"));
		assertEquals(STYPE_NAME.SDOMINANCE_RELATION, edge2relation.get("prim"));
		assertNotNull(edge2relation.get("secedge"));
		assertEquals(STYPE_NAME.SPOINTING_RELATION, edge2relation.get("secedge"));
		
	}
	
	@Test
	public void testSRelationSTypeRenaming()
	{
		Map<String, String> renamingMapping;
		
		PepperModuleProperty<String> prop= (PepperModuleProperty<String>)this.getFixture().getProperty(Tiger2Properties.PROP_RENAME_EDGE_TYPE);
		renamingMapping= getFixture().getRenamingMap_EdgeType();
		assertEquals(0, renamingMapping.size());
		
		getFixture().reset();
		
		prop.setValue("a=b");
		renamingMapping= getFixture().getRenamingMap_EdgeType();
		assertEquals(1, renamingMapping.size());
		assertEquals("b", renamingMapping.get("a"));
		
		getFixture().reset();
		
		prop.setValue("a=b, c=d");
		renamingMapping= getFixture().getRenamingMap_EdgeType();
		assertEquals(2, renamingMapping.size());
		assertEquals("b", renamingMapping.get("a"));
		assertEquals("d", renamingMapping.get("c"));
		
		getFixture().reset();
		
		prop.setValue("a=b,c=d");
		renamingMapping= getFixture().getRenamingMap_EdgeType();
		assertEquals(2, renamingMapping.size());
		assertEquals("b", renamingMapping.get("a"));
		assertEquals("d", renamingMapping.get("c"));
	}
	@Test
	public void testRenameAnnotationName()
	{
		Map<String, String> renamingMapping;
		
		PepperModuleProperty<String> prop= (PepperModuleProperty<String>)this.getFixture().getProperty(Tiger2Properties.PROP_RENAME_ANNOTATION_NAME);
		renamingMapping= getFixture().getRenamingMap_AnnotationName();
		assertEquals(0, renamingMapping.size());
		
		getFixture().reset();
		
		prop.setValue("a=b");
		renamingMapping= getFixture().getRenamingMap_AnnotationName();
		assertEquals(1, renamingMapping.size());
		assertEquals("b", renamingMapping.get("a"));
		
		getFixture().reset();
		
		prop.setValue("a=b, c=d");
		renamingMapping= getFixture().getRenamingMap_AnnotationName();
		assertEquals(2, renamingMapping.size());
		assertEquals("b", renamingMapping.get("a"));
		assertEquals("d", renamingMapping.get("c"));
		
		getFixture().reset();
		
		prop.setValue("a=b,c=d");
		renamingMapping= getFixture().getRenamingMap_AnnotationName();
		assertEquals(2, renamingMapping.size());
		assertEquals("b", renamingMapping.get("a"));
		assertEquals("d", renamingMapping.get("c"));
	}
	
}
