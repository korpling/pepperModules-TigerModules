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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.tiger.tests;

import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperConvertException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.CorpusDefinition;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.FormatDefinition;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperInterfaceFactory;
import de.hu_berlin.german.korpling.saltnpepper.pepper.testSuite.moduleTests.PepperImporterTest;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.tiger.TigerImporter;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Edge;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Label;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltCommonFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.modules.SDocumentStructureAccessor;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.resources.dot.Salt2DOT;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusDocumentRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SDATATYPE;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltSemantics.SLemmaAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltSemantics.SPOSAnnotation;

public class TigerImporterTest extends PepperImporterTest
{	
	URI resourceURI= URI.createFileURI("./src/main/resources/de.hu_berlin.german.korpling.pepper.modules.TigerModules/");
	URI temproraryURI= URI.createFileURI("_TMP/de.hu_berlin.german.korpling.pepper.modules.tiger/TigerImporter");
	URI specialParamsURI= URI.createFileURI("./src/test/resources/TigerImporter/specialParams/specialParams1.prop");
	
	protected void setUp() throws Exception 
	{
		super.setFixture(new TigerImporter());
		super.getFixture().setSaltProject(SaltCommonFactory.eINSTANCE.createSaltProject());
		super.setResourcesURI(resourceURI);
		super.setTemprorariesURI(temproraryURI);
		
		//setting temproraries and resources
		this.getFixture().setTemproraries(temproraryURI);
		this.getFixture().setResources(resourceURI);
		
		//set formats to support
		FormatDefinition formatDef= PepperInterfaceFactory.eINSTANCE.createFormatDefinition();
		formatDef.setFormatName("tiger");
		formatDef.setFormatVersion("1.0");
		this.supportedFormatsCheck.add(formatDef);
		
		//set specialParams
		this.getFixture().setSpecialParams(specialParamsURI);
	}
	
	public void SetGetCorpusDefinition()
	{
		//TODO somethong to test???
		CorpusDefinition corpDef= PepperInterfaceFactory.eINSTANCE.createCorpusDefinition();
		FormatDefinition formatDef= PepperInterfaceFactory.eINSTANCE.createFormatDefinition();
		formatDef.setFormatName("tiger");
		formatDef.setFormatVersion("1.0");
		corpDef.setFormatDefinition(formatDef);
	}
	
	/**
	 * Tests if all token and texts and annotations are there. 
	 * Just Terminals.
	 * @throws IOException 
	 */
	public void testStart1() throws IOException
	{
		URI expectedURI= URI.createFileURI("./src/test/resources/TigerImporter/Case1/doc1/corpusFalko1.saltCommon");
		URI corpusPath= URI.createFileURI("./src/test/resources/TigerImporter/Case1/");
		
		{//creating and setting corpus definition
			CorpusDefinition corpDef= PepperInterfaceFactory.eINSTANCE.createCorpusDefinition();
			FormatDefinition formatDef= PepperInterfaceFactory.eINSTANCE.createFormatDefinition();
			formatDef.setFormatName("tiger");
			formatDef.setFormatVersion("1.0");
			corpDef.setFormatDefinition(formatDef);
			corpDef.setCorpusPath(corpusPath);
			this.getFixture().setCorpusDefinition(corpDef);
		}
		SElementId sElementId= SaltFactory.eINSTANCE.createSElementId();
		sElementId.setId("corpusGraph1");
		SCorpusGraph corpGraph= SaltCommonFactory.eINSTANCE.createSCorpusGraph();
//		corpGraph.setSId("corpusGraph1");
//		corpGraph.setId("corpusGraph1");
		corpGraph.setSElementId(sElementId);
		corpGraph.setSName("/Case1/doc1");
		SDocument sampleSDocument = this.createCorpusStructure(corpGraph);
		
		STextualDS sTextualDS = null;
		{//creating the primary text
			sTextualDS= SaltFactory.eINSTANCE.createSTextualDS();
			sTextualDS.setSText(" Pragmatische Erwerbsprinzipien Ein Hinweis fur eine wichtige Phase des Spracherwerbs bei den Kindern ist der Eintritt in den Wortschatzspurt .");
			sTextualDS.setSId("/Case1/doc1/corpusFalko1#sText1");
			sTextualDS.setSName("sText1");
			//adding the text to the document-graph
			sampleSDocument.getSDocumentGraph().addSNode(sTextualDS);
		}//creating the primary text
		this.createTokens(sampleSDocument);
		this.createMorphologyAnnotations(sampleSDocument);
		
		this.testStart(expectedURI, corpGraph, corpusPath);
		System.out.println("Graphs are equal");
	}
	
	/**
	 * Tests if all token and texts and annotations are there.
	 * Terminals and non terminals with edges between them. 
	 * @throws IOException 
	 */
	public void testStart2() throws IOException
	{
		URI expectedURI= URI.createFileURI("./src/test/resources/TigerImporter/Case2/doc1/corpusFalko2.saltCommon");
		URI exportURI= URI.createFileURI(this.temproraryURI+"/TigerImporter/Case2/doc1/corpusFalko2.saltCommon");
		URI corpusPath= URI.createFileURI("./src/test/resources/TigerImporter/Case2/");
		
//		this.testStart(expectedURI, exportURI, corpusPath);
	}
	
	/**
	 * Tests if all token and texts and annotations are there.
	 * Terminals and non terminals with edges and secedgesbetween them. 
	 * @throws IOException 
	 */
	public void testStart3() throws IOException
	{
		URI expectedURI= URI.createFileURI("./src/test/resources/TigerImporter/Case3/doc1/corpusFalko2.saltCommon");
		URI exportURI= URI.createFileURI(this.temproraryURI+"/TigerImporter/Case3/doc1/corpusFalko2.saltCommon");
		URI corpusPath= URI.createFileURI("./src/test/resources/TigerImporter/Case3/");
		
//		this.testStart(expectedURI, exportURI, corpusPath);
	}
	
	/**
	 * Tests if all token and texts and annotations are there. 
	 * @throws IOException 
	 */
	private void testStart(URI expectedURI, URI exportURI, URI corpusPath) throws IOException
	{
		{//creating and setting corpus definition
			CorpusDefinition corpDef= PepperInterfaceFactory.eINSTANCE.createCorpusDefinition();
			FormatDefinition formatDef= PepperInterfaceFactory.eINSTANCE.createFormatDefinition();
			formatDef.setFormatName("tiger");
			formatDef.setFormatVersion("1.0");
			corpDef.setFormatDefinition(formatDef);
			corpDef.setCorpusPath(corpusPath);
			this.getFixture().setCorpusDefinition(corpDef);
		}
		
		{//setting corpus graph and importing corpus structure
			SCorpusGraph corpGraph= SaltCommonFactory.eINSTANCE.createSCorpusGraph();
			this.getFixture().getSaltProject().getSCorpusGraphs().add(corpGraph);
			this.getFixture().importCorpusStructure(corpGraph);
		}
		
		this.start();
		
		SDocument sDocument= this.getFixture().getSaltProject().getSCorpusGraphs().get(0).getSDocuments().get(0);
//		{//print saltGraph to dot (just for testing)
//			Salt2DOT salt2Dot= new Salt2DOT();
//			salt2Dot.salt2Dot(sDocument.getSElementId(), URI.createFileURI("_TMP/sampleCorpus1/doc1.dot"));
//		}
		
		{//print saltGraph (just for testing)
			SDocumentGraph sDocGraph= sDocument.getSDocumentGraph();
			sDocGraph.setSDocument(null);
			// create resource set and resource 
			ResourceSet resourceSet = new ResourceSetImpl();

			// Register XML resource factory
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("saltCommon",new XMIResourceFactoryImpl());
			//load resource 
			Resource resource = resourceSet.createResource(exportURI);
			if (resource== null)
				throw new PepperConvertException("The resource is null.");
			resource.getContents().add(sDocGraph);
			resource.save(null);
			sDocGraph.setSDocument(sDocument);
		}
		assertTrue("the files '"+expectedURI+"' and '"+exportURI+"' are not equal", this.compareFiles(expectedURI, exportURI));
//		SDocumentGraph sDocGraph= sDocument.getSDocumentGraph();
//		SDocumentGraph sDocGraph2= null;
//		{//load ecpected model and compare to created model
//			// create resource set and resource 
//			ResourceSet resourceSet = new ResourceSetImpl();
//
//			// Register XML resource factory
//			resourceSet.getPackageRegistry().put(SaltCorePackage.eINSTANCE.getNsURI(), SaltCorePackage.eINSTANCE);
//			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("saltCommon",new XMIResourceFactoryImpl());
//			//load resource 
//			Resource resource = resourceSet.createResource(expectedURI);
//			
//			if (resource== null)
//				throw new NullPointerException("The resource is null.");
//			resource.load(null);
//			sDocGraph2 =(SDocumentGraph) resource.getContents().get(0);
//			
//		}
//		assertEquals(sDocGraph, sDocGraph2);
	}
	
	private void testStart(URI expectedURI, SCorpusGraph sampleCorpusGraph, URI corpusPath) throws IOException
	{
		{//creating and setting corpus definition
			CorpusDefinition corpDef= PepperInterfaceFactory.eINSTANCE.createCorpusDefinition();
			FormatDefinition formatDef= PepperInterfaceFactory.eINSTANCE.createFormatDefinition();
			formatDef.setFormatName("tiger");
			formatDef.setFormatVersion("1.0");
			corpDef.setFormatDefinition(formatDef);
			corpDef.setCorpusPath(corpusPath);
			this.getFixture().setCorpusDefinition(corpDef);
		}
		
			SCorpusGraph importedCorpusGraph= SaltCommonFactory.eINSTANCE.createSCorpusGraph();
			this.getFixture().getSaltProject().getSCorpusGraphs().add(importedCorpusGraph);
			this.getFixture().importCorpusStructure(importedCorpusGraph);
		
		this.start();
		
		
		
		SDocument importedSDocument= this.getFixture().getSaltProject().getSCorpusGraphs().get(0).getSDocuments().get(0);

		
		System.out.println("Imported Corpus: "+importedCorpusGraph);
		System.out.println("Sample Corpus: "+sampleCorpusGraph);
		System.out.println("Imported Document: "+importedSDocument);
		System.out.println("Sample Document: "+sampleCorpusGraph.getSDocuments().get(0));
		System.out.println("Imported Corpus Label count: "+importedCorpusGraph.getLabels().size());
		System.out.println("Sample Corpus Label count: "+sampleCorpusGraph.getLabels().size());
		System.out.println("Imported Textual DS: "+importedSDocument.getSDocumentGraph().getSTextualDSs().get(0));
		System.out.println("Sample   Textual DS: "+sampleCorpusGraph.getSDocuments().get(0).getSDocumentGraph().getSTextualDSs().get(0));
		
//		for (SRelation edge : importedCorpusGraph.getSRelations()){
//			System.out.println("Imported Edge "+edge.getSName()+
//					" with id "+edge.getSId()+
//					" has source "+ edge.getSSource().getSId()+
//					" and target "+ edge.getSTarget().getSId());
//		}
//		for (SRelation edge : sampleCorpusGraph.getSRelations()){
//			System.out.println("Sample   Edge "+edge.getSName()+
//					" with id "+edge.getSId()+
//					" has source "+ edge.getSSource().getSId()+
//					" and target "+ edge.getSTarget().getSId());
//		}
//		for (Label label : importedCorpusGraph.getLabels()){
//			System.out.println("Label of imported corpus Graph: "
//					+label +" \n has following properties: "
//					+label.getQName());
//		}
//		for (Label label : sampleCorpusGraph.getLabels()){
//			System.out.println("Label of sample   corpus Graph: "
//					+label+" \n has following properties: "
//					+label.getQName());
//		}
//		System.out.println("Diffs of labels id: \n"
//				+ importedCorpusGraph.getLabels().get(0).differences(
//						sampleCorpusGraph.getLabels().get(0)));
		
//		
		

		
		//		{//print saltGraph to dot (just for testing)
//			Salt2DOT salt2Dot= new Salt2DOT();
//			salt2Dot.salt2Dot(importedSDocument.getSElementId(), URI.createFileURI("_TMP/sampleCorpus1/doc1.dot"));
//		}
		
		assertTrue("The documents are not equal", importedSDocument.equals(sampleCorpusGraph.getSDocuments().get(0)));
		assertTrue("The document graphs are not equal. Differences: "+ importedSDocument.getSDocumentGraph().differences(sampleCorpusGraph.getSDocuments().get(0).getSDocumentGraph()), importedSDocument.getSDocumentGraph().equals(sampleCorpusGraph.getSDocuments().get(0).getSDocumentGraph()));
		assertTrue("The root corpora are not equal. Differences:"+ importedCorpusGraph.getSRootCorpus().get(0).differences(sampleCorpusGraph.getSRootCorpus().get(0)),importedCorpusGraph.getSRootCorpus().get(0).equals(sampleCorpusGraph.getSRootCorpus().get(0)));
		assertTrue("The sub corpora are not equal. Differences:"+ importedCorpusGraph.getSCorpusRelations().get(0).getSTarget().differences(sampleCorpusGraph.getSCorpusRelations().get(0).getSTarget()),importedCorpusGraph.getSCorpusRelations().get(0).getSTarget().equals(sampleCorpusGraph.getSCorpusRelations().get(0).getSTarget()));
		assertTrue("The corpus graphs are not equal. Differences: "+importedCorpusGraph.differences(sampleCorpusGraph), importedCorpusGraph.equals(sampleCorpusGraph));
		
	}
	
	
	private SDocument createCorpusStructure(SCorpusGraph corpGraph)
	{
		
		{//creating corpus structure
			//corpGraph= SaltFactory.eINSTANCE.createSCorpusGraph();
			//this.getFixture().getSaltProject().getSCorpusGraphs().add(corpGraph);
			//		rootCorp
			//		|
			//		corp1
			//		|
			//		doc1
			
			//corp1
			SElementId sElementId= SaltFactory.eINSTANCE.createSElementId();
			
			// Create root corpus (/Case1)
			SCorpus rootCorp = SaltFactory.eINSTANCE.createSCorpus();
			rootCorp.setSName("Case1");
			rootCorp.setSId("/Case1");
			corpGraph.addSNode(rootCorp);
			
			// Create sub corpus (/Case1/doc1)
			sElementId= SaltFactory.eINSTANCE.createSElementId();
			SCorpus corp1= SaltFactory.eINSTANCE.createSCorpus();
			//corp1.setSName("corpusFalko1");
			corp1.setSId("/Case1/doc1");
			corpGraph.addSNode(corp1);
			
			// Create Corpus relation
			SCorpusRelation corpRel1 = SaltFactory.eINSTANCE.createSCorpusRelation();
			corpRel1.setSName("corpRel1");
			corpRel1.setSId("salt:/corpRel1");
			corpRel1.setSTarget(corp1);
			corpRel1.setSSource(rootCorp);
			corpGraph.addSRelation(corpRel1);
			
			// Create Document
			SDocument doc1= SaltFactory.eINSTANCE.createSDocument();
			doc1.setSName("corpusFalko1");
			doc1.setSId("/Case1/doc1/corpusFalko1");
			doc1.setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
			doc1.getSDocumentGraph().setSId("/Case1/doc1/corpusFalko1");
			corpGraph.addSNode(doc1);
			
			// Create Corpus-Document Relation
			SCorpusDocumentRelation corpDocRel1= SaltFactory.eINSTANCE.createSCorpusDocumentRelation();
			corpDocRel1.setSId("salt:/corpDocRel1");
			corpDocRel1.setSName("corpDocRel1");
			corpDocRel1.setSSource(corp1);
			corpDocRel1.setSTarget(doc1);
			corpGraph.addSRelation(corpDocRel1);
			return(doc1);
		}
	}
	
	private void createTokens(SDocument sDocument){
		STextualDS sTextualDS = sDocument.getSDocumentGraph().getSTextualDSs().get(0);
		//as a means to group elements, layers (SLayer) can be used. here, a layer
		//named "morphology" is created and the tokens will be added to it
		//SLayer morphLayer = SaltFactory.eINSTANCE.createSLayer();
		//morphLayer.setSName("morphology");
		//sDocument.getSDocumentGraph().addSLayer(morphLayer);

		createToken(1,13,"sTok1", sTextualDS,sDocument,null);	
		//creating tokenization for the token 'Pragmatische' and adding it to the morphology layer
		createToken(14,31,"sTok2",sTextualDS,sDocument,null);	
		//creating tokenization for the token 'Erwerbsprinzipien' and adding it to the morphology layer
		createToken(32,35,"sTok3",sTextualDS,sDocument,null);		
		//creating tokenization for the token 'ein' and adding it to the morphology layer
		createToken(36,43,"sTok4",sTextualDS,sDocument,null);		
		//creating tokenization for the token 'Hinweisd' and adding it to the morphology layer
		createToken(44,47,"sTok5",sTextualDS,sDocument,null);
		//creating tokenization for the token 'für' and adding it to the morphology layer
		createToken(48,52,"sTok6",sTextualDS,sDocument,null);		
		//creating tokenization for the token 'eine' and adding it to the morphology layer
		createToken(53,61,"sTok7",sTextualDS,sDocument,null);	
		//creating tokenization for the token 'wichtige' and adding it to the morphology layer
		createToken(62,67,"sTok8",sTextualDS,sDocument,null);
		//creating tokenization for the token 'Phase' and adding it to the morphology layer
		createToken(68,71,"sTok9",sTextualDS,sDocument,null);
		//creating tokenization for the token 'des' and adding it to the morphology layer
		createToken(72,85,"sTok10",sTextualDS,sDocument,null);
		//creating tokenization for the token 'Spracherwerbs' and adding it to the morphology layer
		createToken(86,89,"sTok11",sTextualDS,sDocument,null);
		//creating tokenization for the token 'bei' and adding it to the morphology layer
		createToken(90,93,"sTok12",sTextualDS,sDocument,null);
		//creating tokenization for the token 'den' and adding it to the morphology layer
		createToken(94,101,"sTok13",sTextualDS,sDocument,null);
		//creating tenenization for the token 'Kindern' and adding it to the morphology layer
		createToken(102,105,"sTok14",sTextualDS,sDocument,null);
		//creating tokenization for the token 'ist' and adding it to the morphology layer
		createToken(106,109,"sTok15",sTextualDS,sDocument,null);
		//creating tokenization for the token 'der' and adding it to the morphology layer
		createToken(110,118,"sTok16",sTextualDS,sDocument,null);
		//creating tokenization for the token 'Eintritt' and adding it to the morphology layer
		createToken(119,121,"sTok17",sTextualDS,sDocument,null);
		//creating tokenization for the token 'in' and adding it to the morphology layer
		createToken(122,125,"sTok18",sTextualDS,sDocument,null);
		//creating tokenization for the token 'den' and adding it to the morphology layer
		createToken(126,141,"sTok19",sTextualDS,sDocument,null);
		//creating tokenization for the token 'Wortschatzspurt' and adding it to the morphology layer
		createToken(142,143,"sTok20",sTextualDS,sDocument,null);
		//creating tokenization for the token '.' and adding it to the morphology layer
		
	}
	
	private void createToken(int start, int end, String sName , STextualDS sTextualDS, SDocument sDocument, SLayer layer){
		SToken sToken= SaltFactory.eINSTANCE.createSToken();
		sToken.setSName(sName);
		sToken.setSId("/Case1/doc1/corpusFalko1#"+sName);
		sToken.setId("/Case1/doc1/corpusFalko1#"+sName);
		sDocument.getSDocumentGraph().addSNode(sToken);
		//layer.getSNodes().add(sToken);
		STextualRelation sTextRel= SaltFactory.eINSTANCE.createSTextualRelation();
		sTextRel.setSToken(sToken);
		sTextRel.setSTextualDS(sTextualDS);
		sTextRel.setSName(sName.replace("sTok", "sTextRel"));
		sTextRel.setSId(sName.replace("sTok", "edge"));
		sTextRel.setId(sName.replace("sTok", "edge"));
		sTextRel.setSStart(start);
		sTextRel.setSEnd(end);
		sTextRel.setSSource(sToken);
		sTextRel.setSTarget(sTextualDS);
		sDocument.getSDocumentGraph().addSRelation(sTextRel);
	}
	
	
	private void createMorphologyAnnotations(SDocument sDocument){
		List<SToken> sTokens= Collections.synchronizedList(sDocument.getSDocumentGraph().getSTokens());
		
		{//adding part-of speech annotations
			SPOSAnnotation sPOSAnno= null;
				
			String[] posAnnotations={"ADJA","NN","ART","NN","APPR","ART","ADJA","NN","ART","NN","APPR","ART","NN","VAFIN","ART","NN","APPR","ART","NN","$."}; 
			for (int i= 0; i< sTokens.size();i++)
			{
				sPOSAnno= SaltFactory.eINSTANCE.createSPOSAnnotation();
				sPOSAnno.setSValue(posAnnotations[i]);
				//sPOSAnno.setSValueType(SDATATYPE.STEXT);
				sTokens.get(i).addSAnnotation(sPOSAnno);
				
			}
		}//adding part-of speech annotations
			
//		{//adding lemma annotations
//			SLemmaAnnotation sLemmaAnno= null;
//			
//			//a list of all lemma annotations for the words Is (be), this (this) ... be (be)
//			String[] lemmaAnnotations={"be", "this", "example", "more", "complicated", "than", "it", "appear", "to", "be"}; 
//			for (int i= 0; i< sTokens.size();i++)
//			{
//				sLemmaAnno= SaltFactory.eINSTANCE.createSLemmaAnnotation();
//				sLemmaAnno.setSValue(lemmaAnnotations[i]);
//				sTokens.get(i).addSAnnotation(sLemmaAnno);
//			}
//		}//adding lemma annotations
				
	}
	
	//TODO much more tests for example getSupportedFormats, getName
}
