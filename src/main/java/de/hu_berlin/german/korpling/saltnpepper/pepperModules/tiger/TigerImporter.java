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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.tiger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;

import tigerAPI.Corpus;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperImporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperImporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.tiger.exceptions.TigerImporterException;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

@Component(name="TigerImporterComponent", factory="PepperImporterComponentFactory")
@Service(value=PepperImporter.class)
public class TigerImporter extends PepperImporterImpl implements PepperImporter
{
	public TigerImporter()
	{
		super();
		//setting name of module
		this.name= "TigerImporter";
		//set list of formats supported by this module
		this.addSupportedFormat("UAM", "1.0", null);
	}
	
	/**
	 * Stores relation between documents and their resource 
	 */
	private Map<SElementId, URI> documentResourceTable= null;
	
	@Override
	public void importCorpusStructure(SCorpusGraph corpusGraph)
			throws TigerImporterException 
	{
		this.setSCorpusGraph(corpusGraph);
		if (this.getSCorpusGraph()== null)
			throw new TigerImporterException(this.name+": Cannot start with importing corpus, because salt project isn�t set.");
		
		if (this.getCorpusDefinition()== null)
			throw new TigerImporterException(this.name+": Cannot start with importing corpus, because no corpus definition to import is given.");
		if (this.getCorpusDefinition().getCorpusPath()== null)
			throw new TigerImporterException(this.name+": Cannot start with importing corpus, because the path of given corpus definition is null.");
		
		if (this.getCorpusDefinition().getCorpusPath().isFile())
		{
			this.documentResourceTable= new Hashtable<SElementId, URI>();
			//clean uri in corpus path (if it is a folder and ends with/, / has to be removed)
			if (	(this.getCorpusDefinition().getCorpusPath().toFileString().endsWith("/")) || 
					(this.getCorpusDefinition().getCorpusPath().toFileString().endsWith("\\")))
			{
				this.getCorpusDefinition().setCorpusPath(this.getCorpusDefinition().getCorpusPath().trimSegments(1));
			}
			try {
				EList<String> endings= new BasicEList<String>();
				endings.add("tig");
				endings.add("xml");
				endings.add("tiger");
				this.documentResourceTable= this.createCorpusStructure(this.getCorpusDefinition().getCorpusPath(), null, endings);
			} catch (IOException e) {
				throw new TigerImporterException(this.name+": Cannot start with importing corpus, because saome exception occurs: ",e);
			}
		}	
	}
	
	
	
	@Override
	public void start(SElementId sElementId) throws TigerImporterException 
	{
		{//checking special parameter
//			if (this.getSpecialParams()== null)
//				throw new TigerImporterException("Cannot start converting, because no special parameters are set.");
			if (this.getSpecialParams()!= null)
			{
				File specialParamFile= new File(this.getSpecialParams().toFileString());
				if (	(specialParamFile.exists()) &&
						(!specialParamFile.isFile()))
					throw new TigerImporterException("Cannot start converting, because the file for special parameters is not a file: "+ specialParamFile);
			}
//			if (!specialParamFile.exists())
//				throw new TigerImporterException("Cannot start converting, because the file for special parameters does not exists: "+ specialParamFile);
			
		}
		
		//if elementId belongs to SDocument
		if((sElementId.getSIdentifiableElement() instanceof SDocument))
		{	
			//throw new TigerImporterException("Cannot import data to given sElementID "+sElementId.getSId()+", because the corresponding element is not of kind SDocument. It is of kind: "+ sElementId.getSIdentifiableElement().getClass().getName());
			//getting uri of elementID
			URI documentPath= this.documentResourceTable.get(sElementId);
			if (documentPath!= null)
			{
				SDocument sDoc= (SDocument) sElementId.getSIdentifiableElement();
				{
					Corpus corpus = new Corpus(documentPath.toFileString());
					Tiger2SaltMapper mapper= new Tiger2SaltMapper();
					if (this.getSpecialParams()!= null)
					{
						Properties props= new Properties();
						try {
							props.load(new InputStreamReader(new FileInputStream(this.getSpecialParams().toFileString())));
						} catch (FileNotFoundException e) {
							
						} catch (IOException e) {
							throw new TigerImporterException("Cannot start converting, because can not read the given file for special parameters: "+ this.getSpecialParams());
						}
						mapper.setProps(props);
					}
					mapper.mapCorpus2SDocument(corpus, sDoc);
				}	
			}
		}
	}
}
