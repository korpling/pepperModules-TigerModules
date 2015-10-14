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
package org.corpus_tools.peppermodules.tigerModules;

import java.io.IOException;

import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.impl.PepperImporterImpl;
import org.corpus_tools.pepper.modules.PepperImporter;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.peppermodules.tigerModules.mappers.Tiger22SaltMapper;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.osgi.service.component.annotations.Component;

import de.hu_berlin.german.korpling.tiger2.Corpus;
import de.hu_berlin.german.korpling.tiger2.resources.TigerResourceFactory;

/**
 * This is a sample PepperImporter, which can be used for creating individual
 * Importers for the Pepper Framework. Therefore you have to take a look to
 * todo's and adapt the code.
 * 
 * <ul>
 * <li>the salt model to fill, manipulate or export can be accessed via
 * SaltProject::this.getSaltProject()</li>
 * <li>special parameters given by Pepper workflow can be accessed via
 * URI::this.getSpecialParams()</li>
 * <li>a place to store temprorary datas for processing can be accessed via
 * URI::this.getTemproraries()</li>
 * <li>a place where resources of this bundle are, can be accessed via
 * URL::this.getResources()</li>
 * <li>a logService can be accessed via LogService::this.getLogService()</li>
 * </ul>
 * 
 * @author Florian Zipser
 * @version 1.0
 *
 */
@Component(name = "Tiger2ImporterComponent", factory = "PepperImporterComponentFactory")
public class Tiger2Importer extends PepperImporterImpl implements PepperImporter {
	/**
	 * Initializes an importer, importing data from a <tiger2/> model.
	 */
	public Tiger2Importer() {
		super();

		// start: setting name of module
		setName("Tiger2Importer");
	    setSupplierContact(URI.createURI(PepperConfiguration.EMAIL));
		setSupplierHomepage(URI.createURI("https://github.com/korpling/pepperModules-TigerModules"));
		setDesc("This importer transforms data in TigerXML and tiger2 format to a Salt model. ");
		// end: setting name of module

		// set list of formats supported by this module
		this.addSupportedFormat("tiger2", "2.0.5", null);
		this.addSupportedFormat("tigerXML", "1.0", null);
		getDocumentEndings().add(TigerResourceFactory.FILE_ENDING_TIGER2);
		getDocumentEndings().add(TigerResourceFactory.FILE_ENDING_TIGER2_2);
		getDocumentEndings().add(TigerResourceFactory.FILE_ENDING_TIGERXML);
		getDocumentEndings().add(TigerResourceFactory.FILE_ENDING_TIGERXML_2);
		setProperties(new Tiger2ImporterProperties());
	}

	/**
	 * {@link ResourceSet} object to load models via emf resource mechanism.
	 */
	private ResourceSet resourceSet = null;

	private ResourceSet getResourceSet() {
		if (resourceSet == null) {
			synchronized (this) {
				if (resourceSet == null) {
					resourceSet = new ResourceSetImpl();
					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(TigerResourceFactory.FILE_ENDING_TIGER2, new TigerResourceFactory());
					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(TigerResourceFactory.FILE_ENDING_TIGER2_2, new TigerResourceFactory());
					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(TigerResourceFactory.FILE_ENDING_TIGERXML, new TigerResourceFactory());
					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(TigerResourceFactory.FILE_ENDING_TIGERXML_2, new TigerResourceFactory());
				}
			}
		}
		return (resourceSet);
	}

	/**
	 * Creates a mapper of type {@link Tiger22SaltMapper}. {@inheritDoc
	 * PepperModule#createPepperMapper(Identifier)}
	 */
	@Override
	public PepperMapper createPepperMapper(Identifier sElementId) {
		Tiger22SaltMapper mapper = new Tiger22SaltMapper();

		if (sElementId.getIdentifiableElement() instanceof SDocument) {
			URI inputUri = this.getIdentifier2ResourceTable().get(sElementId);

			if (inputUri == null)
				throw new PepperModuleException(this, "There was no matching uri found corresponding to document '" + sElementId + "'.");

			// load resource
			Resource resourceLoad = getResourceSet().createResource(inputUri);

			if (resourceLoad == null)
				throw new PepperModuleException(this, "Cannot map the data stored at given uri '" + inputUri + "', because no resource object could have been created to read these data.");
			try {
				resourceLoad.load(null);
			} catch (IOException e) {
				throw new PepperModuleException(this, "Cannot load <tiger2/> model from file '" + inputUri + "'.", e);
			}
			Object objCorpus = resourceLoad.getContents().get(0);
			if (!(objCorpus instanceof Corpus))
				throw new PepperModuleException(this, "Cannot map the data stored at given uri '" + inputUri + "', because they could not have been mapped to a tiger2 corpus model object.");
			Corpus corpus = (Corpus) resourceLoad.getContents().get(0);
			mapper.setCorpus(corpus);
		}

		return (mapper);
	}
}
