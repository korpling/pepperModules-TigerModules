package de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.tests;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperModuleProperty;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.Tiger2Properties;

public class Tiger2PropertiesTest 
{
	private Tiger2Properties fixture= null;

	public Tiger2Properties getFixture() {
		return fixture;
	}

	public void setFixture(Tiger2Properties fixture) {
		this.fixture = fixture;
	}
	
	public void setUp()
	{
		setFixture(new Tiger2Properties());
	}
	
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
