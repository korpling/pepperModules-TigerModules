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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules;

import com.google.common.base.Preconditions;
import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.osgi.service.component.annotations.Component;

import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperImporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperMapper;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.exceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperImporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.mappers.Tiger22SaltMapper;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;
import de.hu_berlin.german.korpling.tiger2.Corpus;
import de.hu_berlin.german.korpling.tiger2.resources.TigerResourceFactory;

import static de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.Tiger2ImporterProperties.PROP_SEGMENT_AS_DOC;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.mappers.TigerSegmentMapper;
import java.io.File;

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
		// end: setting name of module

		// set list of formats supported by this module
		this.addSupportedFormat("tiger2", "2.0.5", null);
		this.addSupportedFormat("tigerXML", "1.0", null);
		getSDocumentEndings().add(TigerResourceFactory.FILE_ENDING_TIGER2);
		getSDocumentEndings().add(TigerResourceFactory.FILE_ENDING_TIGER2_2);
		getSDocumentEndings().add(TigerResourceFactory.FILE_ENDING_TIGERXML);
		getSDocumentEndings().add(TigerResourceFactory.FILE_ENDING_TIGERXML_2);
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

  @Override
  public void importCorpusStructure(SCorpusGraph corpusGraph) throws
    PepperModuleException
  {
    
    if((Boolean) getProperties().getProperty(PROP_SEGMENT_AS_DOC).getValue())
    {
      // parse the file once and add a document for each segment
      URI fileURI = getCorpusDesc().getCorpusPath();
      Preconditions.checkArgument(fileURI.isFile(), "Corpus path must be a single file when treating segments as documents");
      File corpusFile = new File(fileURI.toFileString());
      Preconditions.checkArgument(corpusFile.isFile(),"Corpus path must be a single file when treating segments as documents");
      
      TigerXMLSegmentFinder segmentFinder = new TigerXMLSegmentFinder(corpusFile, corpusGraph);
      segmentFinder.parse();
      
    }
    else
    {
      // use default directory and file based corpus structure
      super.importCorpusStructure(corpusGraph);
    }
  }
  
  
  
	/**
	 * Creates a mapper of type {@link Tiger22SaltMapper}. {@inheritDoc
	 * PepperModule#createPepperMapper(SElementId)}
	 */
	@Override
	public PepperMapper createPepperMapper(SElementId sElementId) {
    
    if((Boolean) getProperties().getProperty(PROP_SEGMENT_AS_DOC).getValue()) {
    
      TigerSegmentMapper mapper = new TigerSegmentMapper();
      mapper.setResourceURI(getCorpusDesc().getCorpusPath());
      
      return mapper;
      
    } else {
      Tiger22SaltMapper mapper = new Tiger22SaltMapper();

      if (sElementId.getSIdentifiableElement() instanceof SDocument) {
        URI inputUri = this.getSElementId2ResourceTable().get(sElementId);

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
}
