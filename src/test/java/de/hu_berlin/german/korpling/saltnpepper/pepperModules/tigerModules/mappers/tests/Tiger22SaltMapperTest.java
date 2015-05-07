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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.mappers.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperModuleProperty;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.Tiger2ImporterProperties;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.mappers.Tiger22SaltMapper;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDominanceRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.tiger2.samples.Tiger2Sample;

public class Tiger22SaltMapperTest {

	private Tiger22SaltMapper fixture = null;

	public Tiger22SaltMapper getFixture() {
		return fixture;
	}

	public void setFixture(Tiger22SaltMapper fixture) {
		this.fixture = fixture;
	}

	@Before
	public void setUp() {
		setFixture(new Tiger22SaltMapper());
		getFixture().setSDocument(SaltFactory.eINSTANCE.createSDocument());
		getFixture().setProperties(new Tiger2ImporterProperties());
		getFixture().setCorpus(Tiger2Sample.createSampleCorpus1());
	}

	@Test
	public void testRenameEdgeType() {
		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) getFixture().getProps().getProperty(Tiger2ImporterProperties.PROP_RENAME_EDGE_TYPE);
		prop.setValue("prim=edge");

		getFixture().mapSDocument();

		for (SDominanceRelation sDomRel : getFixture().getSDocument().getSDocumentGraph().getSDominanceRelations()) {
			assertNotNull(sDomRel.getSTypes());
			assertEquals(1, sDomRel.getSTypes().size());
			assertEquals("edge", sDomRel.getSTypes().get(0));
		}
	}

	@Test
	public void testRenameAnnotationName() {
		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) getFixture().getProps().getProperty(Tiger2ImporterProperties.PROP_RENAME_ANNOTATION_NAME);
		prop.setValue("lemma=A");

		getFixture().mapSDocument();

		for (SToken sTok : getFixture().getSDocument().getSDocumentGraph().getSTokens()) {
			if (sTok.getSName().equals("sTok1"))
				assertNotNull(sTok.getSAnnotation("A"));
			else if (sTok.getSName().equals("sTok2"))
				assertNotNull(sTok.getSAnnotation("A"));
			assertNull(sTok.getSAnnotation("lemma"));
		}

	}
}
