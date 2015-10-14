/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.corpus_tools.pepper.modules.PepperModuleProperty;
import org.corpus_tools.salt.SALT_TYPE;
import org.junit.Before;
import org.junit.Test;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.Tiger2ImporterProperties;

public class Tiger2PropertiesTest {
	private Tiger2ImporterProperties fixture = null;

	public Tiger2ImporterProperties getFixture() {
		return fixture;
	}

	public void setFixture(Tiger2ImporterProperties fixture) {
		this.fixture = fixture;
	}

	@Before
	public void setUp() {
		setFixture(new Tiger2ImporterProperties());
	}

	/**
	 * Tests whether the property {@link Tiger2ImporterProperties#PROP_EDGE_2_SRELATION}
	 * is retrieved as defined.
	 */
	@Test
	public void testGetPropRelation2SRelation() {
		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) getFixture().getProperty(Tiger2ImporterProperties.PROP_EDGE_2_SRELATION);
		String edge2relationMapping = "prim:" + SALT_TYPE.SDOMINANCE_RELATION + ", secedge:" + SALT_TYPE.SPOINTING_RELATION;
		prop.setValue(edge2relationMapping);

		Map<String, SALT_TYPE> edge2relation = getFixture().getPropRelation2SRelation();

		assertNotNull(edge2relation);
		assertNotNull(edge2relation.get("prim"));
		assertEquals(SALT_TYPE.SDOMINANCE_RELATION, edge2relation.get("prim"));
		assertNotNull(edge2relation.get("secedge"));
		assertEquals(SALT_TYPE.SPOINTING_RELATION, edge2relation.get("secedge"));

	}

	@Test
	public void testSRelationSTypeRenaming() {
		Map<String, String> renamingMapping;

		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) getFixture().getProperty(Tiger2ImporterProperties.PROP_RENAME_EDGE_TYPE);
		renamingMapping = getFixture().getRenamingMap_RelationType();
		assertEquals(0, renamingMapping.size());

		getFixture().reset();

		prop.setValue("a=b");
		renamingMapping = getFixture().getRenamingMap_RelationType();
		assertEquals(1, renamingMapping.size());
		assertEquals("b", renamingMapping.get("a"));

		getFixture().reset();

		prop.setValue("a=b, c=d");
		renamingMapping = getFixture().getRenamingMap_RelationType();
		assertEquals(2, renamingMapping.size());
		assertEquals("b", renamingMapping.get("a"));
		assertEquals("d", renamingMapping.get("c"));

		getFixture().reset();

		prop.setValue("a=b,c=d");
		renamingMapping = getFixture().getRenamingMap_RelationType();
		assertEquals(2, renamingMapping.size());
		assertEquals("b", renamingMapping.get("a"));
		assertEquals("d", renamingMapping.get("c"));
	}

	@Test
	public void testRenameAnnotationName() {
		Map<String, String> renamingMapping;

		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) getFixture().getProperty(Tiger2ImporterProperties.PROP_RENAME_ANNOTATION_NAME);
		renamingMapping = getFixture().getRenamingMap_AnnotationName();
		assertEquals(0, renamingMapping.size());

		getFixture().reset();

		prop.setValue("a=b");
		renamingMapping = getFixture().getRenamingMap_AnnotationName();
		assertEquals(1, renamingMapping.size());
		assertEquals("b", renamingMapping.get("a"));

		getFixture().reset();

		prop.setValue("a=b, c=d");
		renamingMapping = getFixture().getRenamingMap_AnnotationName();
		assertEquals(2, renamingMapping.size());
		assertEquals("b", renamingMapping.get("a"));
		assertEquals("d", renamingMapping.get("c"));

		getFixture().reset();

		prop.setValue("a=b,c=d");
		renamingMapping = getFixture().getRenamingMap_AnnotationName();
		assertEquals(2, renamingMapping.size());
		assertEquals("b", renamingMapping.get("a"));
		assertEquals("d", renamingMapping.get("c"));
	}
  
  @Test
	public void testRelationReverse() {
		PepperModuleProperty<String> prop = 
      (PepperModuleProperty<String>) getFixture().getProperty(Tiger2ImporterProperties.PROP_EDGE_REVERSE);
		// test default mappings
		assertEquals(2, getFixture().getRelationReversed().size());
    assertTrue(getFixture().getRelationReversed().contains("sec"));
    assertTrue(getFixture().getRelationReversed().contains("secedge"));

		getFixture().reset();

		prop.setValue("a,,b,c,d  ,  e");
		assertEquals(5, getFixture().getRelationReversed().size());
    assertTrue(getFixture().getRelationReversed().contains("a"));
    assertTrue(getFixture().getRelationReversed().contains("b"));
    assertTrue(getFixture().getRelationReversed().contains("c"));
    assertTrue(getFixture().getRelationReversed().contains("d"));
    assertTrue(getFixture().getRelationReversed().contains("e"));

		getFixture().reset();

		prop.setValue("a");
		assertEquals(1, getFixture().getRelationReversed().size());
    assertTrue(getFixture().getRelationReversed().contains("a"));
	}

}
