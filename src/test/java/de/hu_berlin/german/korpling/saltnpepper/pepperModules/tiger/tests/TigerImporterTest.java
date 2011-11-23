package de.hu_berlin.german.korpling.saltnpepper.pepperModules.tiger.tests;

import java.io.IOException;

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
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltCommonFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;

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
		URI exportURI= URI.createFileURI(this.temproraryURI+"/TigerImporter/Case1/doc1/corpusFalko1.saltCommon");
		URI corpusPath= URI.createFileURI("./src/test/resources/TigerImporter/Case1/");
		
		this.testStart(expectedURI, exportURI, corpusPath);
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
		
		this.testStart(expectedURI, exportURI, corpusPath);
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
		
		this.testStart(expectedURI, exportURI, corpusPath);
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
	
	//TODO much more tests for example getSupportedFormats, getName
}
