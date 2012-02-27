/**
 * Copyright 2009 Humboldt University of Berlin, INRIA.
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

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.FormatDefinition;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperImporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperInterfaceFactory;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperImporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.exceptions.TigerImporterException;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.mappers.Tiger22SaltMapper;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;
import de.hu_berlin.german.korpling.tiger2.Corpus;
import de.hu_berlin.german.korpling.tiger2.resources.TigerResourceFactory;

/**
 * This is a sample PepperImporter, which can be used for creating individual Importers for the 
 * Pepper Framework. Therefore you have to take a look to todo's and adapt the code.
 * 
 * <ul>
 *  <li>the salt model to fill, manipulate or export can be accessed via SaltProject::this.getSaltProject()</li>
 * 	<li>special parameters given by Pepper workflow can be accessed via URI::this.getSpecialParams()</li>
 *  <li>a place to store temprorary datas for processing can be accessed via URI::this.getTemproraries()</li>
 *  <li>a place where resources of this bundle are, can be accessed via URL::this.getResources()</li>
 *  <li>a logService can be accessed via LogService::this.getLogService()</li>
 * </ul>
 * @author Florian Zipser
 * @version 1.0
 *
 */
@Component(name="Tiger2ImporterComponent", factory="TigerImporterComponentFactory")
@Service(value=PepperImporter.class)
public class Tiger2Importer extends PepperImporterImpl implements PepperImporter
{
	public Tiger2Importer()
	{
		super();
		
		//start: setting name of module
			this.name= "Tiger2Importer";
		//end: setting name of module
		
		//start: for testing the symbolic name has to be set without osgi
			if (	(this.getSymbolicName()==  null) ||
					(this.getSymbolicName().isEmpty()))
				this.setSymbolicName("de.hu_berlin.german.korpling.saltnpepper.pepperModules-TigerModules");
		//end: for testing the symbolic name has to be set without osgi
		
		{//set list of formats supported by this module
			this.supportedFormats= new BasicEList<FormatDefinition>();
//			FormatDefinition formatDef1= PepperInterfaceFactory.eINSTANCE.createFormatDefinition();
//			formatDef1.setFormatName("tiger");
//			formatDef1.setFormatVersion("1.0");
//			this.supportedFormats.add(formatDef1);
			
			FormatDefinition formatDef2= PepperInterfaceFactory.eINSTANCE.createFormatDefinition();
			formatDef2.setFormatName("tiger2");
			formatDef2.setFormatVersion("2.0.5");
			this.supportedFormats.add(formatDef2);
		}
		
		resourceSet= new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(TigerResourceFactory.FILE_ENDING_TIGER2,new TigerResourceFactory());
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(TigerResourceFactory.FILE_ENDING_TIGER2_2,new TigerResourceFactory());
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(TigerResourceFactory.FILE_ENDING_TIGERXML,new TigerResourceFactory());
		
		{//just for logging: to say, that the current module has been loaded
			if (this.getLogService()!= null)
				this.getLogService().log(LogService.LOG_DEBUG,this.getName()+" is created...");
		}//just for logging: to say, that the current module has been loaded
	}
	
	/**
	 * {@link ResourceSet} object to load models via emf resource mechanism.
	 */
	private ResourceSet resourceSet= null;
	
	/**
	 * Stores relation between documents and their resource 
	 */
	private Map<SElementId, URI> sDocumentResourceTable= null;
	
	/**
	 * Stores the current {@link SCorpusGraph} object to be used to import {@link SCorpus} and {@link SDocument} objects.
	 */
	private SCorpusGraph sCorpusGraph= null;
	
	/**
	 * This method is called by Pepper at the start of conversion process. 
	 * It shall create the structure the corpus to import. That means creating all necessary SCorpus, 
	 * SDocument and all Relation-objects between them. The path tp the corpus to import is given by
	 * this.getCorpusDefinition().getCorpusPath().
	 * @param an empty graph given by Pepper, which shall contains the corpus structure
	 */
	@Override
	public void importCorpusStructure(SCorpusGraph sCorpusGraph)
			throws PepperModuleException
	{
		if (this.getCorpusDefinition().getCorpusPath()== null)
			throw new TigerImporterException("Cannot import corpus-structure, because no corpus-path is given.");
		
		this.sDocumentResourceTable= new Hashtable<SElementId, URI>();	
		this.sCorpusGraph= sCorpusGraph;
		File corpusPath= new File(this.getCorpusDefinition().getCorpusPath().toFileString());
		try {
			EList<String> endings= new BasicEList<String>();
			endings.add("tiger2");
			endings.add("xml");
			endings.add("tig2");
			this.sDocumentResourceTable= this.createCorpusStructure(this.getCorpusDefinition().getCorpusPath(), null, endings);
		} catch (IOException e) {
			throw new TigerImporterException(this.name+": Cannot start with importing corpus, because saome exception occurs: ",e);
		}
	}
	
	/**
	 * If this method is not really implemented, it will call the Method start(sElementId) for every document 
	 * and corpus, which shall be processed. If it is not really implemented, the method-call will be serial and
	 * and not parallel. To implement a parallelization override this method and take care, that your code is
	 * thread-safe. 
	 * For getting an impression how to implement this method, here is a snipplet of super class 
	 * PepperImporter of this method:
	 * <br/>
	 * boolean isStart= true;
	 * SElementId sElementId= null;
	 * while ((isStart) || (sElementId!= null))
	 * {	
	 *  isStart= false;
	 *		sElementId= this.getPepperModuleController().get();
	 *		if (sElementId== null)
	 *			break;
	 *		
	 *		//call for using push-method
	 *		this.start(sElementId);
	 *		
	 *		if (this.returningMode== RETURNING_MODE.PUT)
	 *		{	
	 *			this.getPepperModuleController().put(sElementId);
	 *		}
	 *		else if (this.returningMode== RETURNING_MODE.FINISH)
	 *		{	
	 *			this.getPepperModuleController().finish(sElementId);
	 *		}
	 *		else 
	 *			throw new PepperModuleException("An error occurs in this module (name: "+this.getName()+"). The returningMode isn�t correctly set (it�s "+this.getReturningMode()+"). Please contact module supplier.");
	 *		this.end();
	 *	}
	 * After all documents were processed this method of super class will call the method end().
	 */
	@Override
	public void start() throws PepperModuleException
	{
		super.start();
	}
	
	/**
	 * This method is called by method start() of superclass PepperImporter, if the method was not overriden
	 * by the current class. If this is not the case, this method will be called for every document which has
	 * to be processed.
	 * @param sElementId the id value for the current document or corpus to process  
	 */
	@Override
	public void start(SElementId sElementId) throws PepperModuleException 
	{
		if (	(sElementId!= null) &&
				(sElementId.getSIdentifiableElement()!= null) &&
				((sElementId.getSIdentifiableElement() instanceof SDocument) ||
				((sElementId.getSIdentifiableElement() instanceof SCorpus))))
		{//only if given sElementId belongs to an object of type SDocument or SCorpus	
			if (sElementId.getSIdentifiableElement() instanceof SCorpus)
			{//mapping SCorpus	
			}//mapping SCorpus
			if (sElementId.getSIdentifiableElement() instanceof SDocument)
			{//mapping SDocument
				SDocument sDocument= (SDocument) sElementId.getSIdentifiableElement();
				
				URI inputUri= this.sDocumentResourceTable.get(sDocument.getId());
				//load resource 
				Resource resourceLoad = resourceSet.createResource(inputUri);
				
				if (resourceLoad== null)
					throw new TigerImporterException("Cannot map the data stored at given uri '"+inputUri+"', because no resource object could have been created to read these data.");
				try {
					resourceLoad.load(null);
				} catch (IOException e) {
					throw new TigerImporterException("Cannot load <tiger2/> model from file '"+inputUri+"'.",e);
				}
				Object objCorpus= resourceLoad.getContents().get(0);
				if (!(objCorpus instanceof Corpus))
					throw new TigerImporterException("Cannot map the data stored at given uri '"+inputUri+"', because they could not have been mapped to a tiger2 corpus model object.");
				Corpus corpus= (Corpus) resourceLoad.getContents().get(0);
				
				Tiger22SaltMapper mapper= new Tiger22SaltMapper();
				mapper.setCorpus(corpus);
				mapper.setsDocument(sDocument);
				mapper.map();
			}//mapping SDocument
		}//only if given sElementId belongs to an object of type SDocument or SCorpus
	}
	
	/**
	 * This method is called by method start() of super class PepperModule. If you do not implement
	 * this method, it will call start(sElementId), for all super corpora in current SaltProject. The
	 * sElementId refers to one of the super corpora. 
	 */
	@Override
	public void end() throws PepperModuleException
	{
		super.end();
	}
	
//================================ start: methods used by OSGi
	/**
	 * This method is called by the OSGi framework, when a component with this class as class-entry
	 * gets activated.
	 * @param componentContext OSGi-context of the current component
	 */
	protected void activate(ComponentContext componentContext) 
	{
		this.setSymbolicName(componentContext.getBundleContext().getBundle().getSymbolicName());
		{//just for logging: to say, that the current module has been activated
			if (this.getLogService()!= null)
				this.getLogService().log(LogService.LOG_DEBUG,this.getName()+" is activated...");
		}//just for logging: to say, that the current module has been activated
	}

	/**
	 * This method is called by the OSGi framework, when a component with this class as class-entry
	 * gets deactivated.
	 * @param componentContext OSGi-context of the current component
	 */
	protected void deactivate(ComponentContext componentContext) 
	{
		{//just for logging: to say, that the current module has been deactivated
			if (this.getLogService()!= null)
				this.getLogService().log(LogService.LOG_DEBUG,this.getName()+" is deactivated...");
		}	
	}
//================================ start: methods used by OSGi
}
