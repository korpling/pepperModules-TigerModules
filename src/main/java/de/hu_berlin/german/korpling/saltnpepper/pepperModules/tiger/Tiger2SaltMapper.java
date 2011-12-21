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

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

import tigerAPI.Corpus;
import tigerAPI.NT;
import tigerAPI.T;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltCommonFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDominanceRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructure;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltSemantics.SSentenceAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltSemantics.SaltSemanticsFactory;

public class Tiger2SaltMapper 
{
	private SDocument sDocument= null;

	public void setsDocument(SDocument sDocument) {
		this.sDocument = sDocument;
	}

	public SDocument getsDocument() {
		return sDocument;
	}
	
	private Corpus corpus= null;

	public void setCorpus(Corpus corpus) {
		this.corpus = corpus;
	}

	public Corpus getCorpus() {
		return corpus;
	}
// -------------------- properties	
	private String KW_TOKENSEP="salt.tokenSeperator";
	
	private Properties props= null;
	public void setProps(Properties props) {
		this.props = props;
	}

	public Properties getProps() {
		return props;
	}
// -------------------- properties
	/**
	 * If no seperator to seperate text of tokens is given, seperate by this seperator
	 */
	private String defaultSeperator= " ";
//=================================== start: mapping methods	
	/**
	 * Maps a corpus to SDocument 
	 */
	
	public void mapCorpus2SDocument(Corpus corpus, SDocument sDocument)
	{
		this.setCorpus(corpus);
		this.setsDocument(sDocument);
		this.getsDocument().setSDocumentGraph(SaltCommonFactory.eINSTANCE.createSDocumentGraph());
		this.getsDocument().getSDocumentGraph().setSName(this.getsDocument().getSName());
		this.getsDocument().getSDocumentGraph().setSId(this.getsDocument().getSId());
		@SuppressWarnings("unchecked")
		EList<T> terminals= new BasicEList<T>(corpus.getAllTs());
		@SuppressWarnings("unchecked")
		EList<NT> nonTerminals= new BasicEList<NT>(corpus.getAllNTs());
		
		{//mapping to STextual DS
			STextualDS sTextDS= SaltCommonFactory.eINSTANCE.createSTextualDS();
			this.getsDocument().getSDocumentGraph().addSNode(sTextDS);
			this.mapTerminals2STextualDS(terminals, sTextDS);
		}
		{//mapping all nt�s to SStructures
			this.mapNTs2Structures(nonTerminals);
		}
		{//mapping all references between t and nt and nt
			
			this.mapNTsTs2SDominanceRelations(nonTerminals, terminals);
		}
	}
	
	/**
	 * Contains all T�s corresponding to the SToken-objects
	 */
	private Map<T, SToken> t2STokenTable= null;
	
	/**
	 * Maps all terminal nodes to a textualDS and to Tokens and STextualrelations between them. 
	 * This funtion tehrefore calls mapT2STokenSTextualDS().
	 * @param terminals
	 * @param sTextDS
	 */
	private void mapTerminals2STextualDS(EList<T> terminals, STextualDS sTextDS)
	{
		t2STokenTable= new Hashtable<T, SToken>();
		for (T terminal: terminals)
		{
			SToken sToken= SaltCommonFactory.eINSTANCE.createSToken();
			this.sDocument.getSDocumentGraph().addSNode(sToken);
			this.mapT2STokenSTextualDS(terminal, sToken, sTextDS);
			t2STokenTable.put(terminal, sToken);
		}
	}
	
	/**
	 * Maps a terminal node of tiger to a SToken-object of salt. Therefore it expands the 
	 * STextualDS-object and creates an STextualRelation between SToken-object and STextualDS-object.
	 * @param terminal
	 * @param sToken
	 * @param sTextDS
	 */
	private void mapT2STokenSTextualDS(T terminal, SToken sToken, STextualDS sTextDS)
	{
		//map Pos anno if exists
		if (	(terminal.getPos()!= null)&&
				(!terminal.getPos().isEmpty()))
		{
			SAnnotation sAnno= SaltSemanticsFactory.eINSTANCE.createSPOSAnnotation();
			sAnno.setSValue(terminal.getPos());
			sToken.addSAnnotation(sAnno);
		}
		//map Morph anno if exists
		if (	(terminal.getMorph()!= null)&&
				(!terminal.getMorph().isEmpty()))
		{
			SAnnotation sAnno= SaltSemanticsFactory.eINSTANCE.createSLemmaAnnotation();
			sAnno.setSValue(terminal.getMorph());
			sToken.addSAnnotation(sAnno);
		}
		//map Text
		
		//create an empty text, if there is non
		if (sTextDS.getSText()== null)
			sTextDS.setSText("");
		//if text of token isn�t empty put a seperaor to its end
		sTextDS.setSText(sTextDS.getSText()+ this.getTokenSeparator());
		
		Integer startPos= 0;
		Integer endPos= 0;
		
		//set startpos to current text length
		startPos= sTextDS.getSText().length();
		sTextDS.setSText(sTextDS.getSText()+ terminal.getText());
		endPos= sTextDS.getSText().length();
		
		//create STextualRelation
		STextualRelation sTextRel= SaltCommonFactory.eINSTANCE.createSTextualRelation();
		sTextRel.setSTextualDS(sTextDS);
		sTextRel.setSToken(sToken);
		sTextRel.setSStart(startPos);
		sTextRel.setSEnd(endPos);
		this.getsDocument().getSDocumentGraph().addSRelation(sTextRel);
	}
	
	public Map<NT, SStructure> nt2SStructureTable= null;
	/**
	 * Maps all given non terminals to SSTructures.
	 * @param nts
	 */
	private void mapNTs2Structures(EList<NT> nts)
	{
		nt2SStructureTable= new Hashtable<NT, SStructure>();
		for (NT nt: nts)
		{//put a SStructure-object for every nt object into graph
			SStructure sStructure= SaltCommonFactory.eINSTANCE.createSStructure();
			this.getsDocument().getSDocumentGraph().addSNode(sStructure);
			this.mapNT2Structure(nt, sStructure);
			nt2SStructureTable.put(nt, sStructure);
		}
	}
	
	/**
	 * Creates a DominanceRelation between a SStructure and its childs. 
	 * It also creates SDominanceRelation between SStructures for nt and 
	 * SToken to SStructure for t.
	 * @param nts
	 * @param ts
	 */
	private void mapNTsTs2SDominanceRelations(EList<NT> nts, EList<T> ts)
	{
		for (NT nt: nts)
		{//create relations between SStructure for current nt and its parent
			SStructure currSStructure= nt2SStructureTable.get(nt);
			if (nt.getMother()!= null)
			{//nt has mother
				SStructure parentSStructure= nt2SStructureTable.get(nt.getMother()); 
				SDominanceRelation sDomRel= SaltCommonFactory.eINSTANCE.createSDominanceRelation();
				sDomRel.setSStructure(parentSStructure);
				sDomRel.setSTarget(currSStructure);
				this.getsDocument().getSDocumentGraph().addSRelation(sDomRel);
				if (	(nt.getEdge2Mother()!= null)&&
						(!nt.getEdge2Mother().isEmpty()))
					this.mapLabel2SDominanceRelation(nt.getEdge2Mother(), sDomRel);
			}
		}
		for (T t: ts)
		{//create relations between SToken for current t and its parent
			SToken currSToken= t2STokenTable.get(t);
			if (t.getMother()!= null)
			{//nt has mother
				SStructure parentSStructure= nt2SStructureTable.get(t.getMother()); 
				if (parentSStructure!= null)
				{//create an SDominancetrelation only if a corresponding SSTructure exists to nt, else it mother is an artificial node made by tiger API	
					SDominanceRelation sDomRel= SaltCommonFactory.eINSTANCE.createSDominanceRelation();
					sDomRel.setSStructure(parentSStructure);
					sDomRel.setSTarget(currSToken);
					this.getsDocument().getSDocumentGraph().addSRelation(sDomRel);
					if (	(t.getEdge2Mother()!= null)&&
							(!t.getEdge2Mother().isEmpty()))
						this.mapLabel2SDominanceRelation(t.getEdge2Mother(), sDomRel);
				}
			}
		}
	}
	
	private String EDGE_LABEL_NAME= "func";
	/**
	 * Maps a label to an SAnnotation and adds it to the given SDominanceRelation-object.
	 * @param label
	 * @param sDomRel
	 */
	private void mapLabel2SDominanceRelation(String label, SDominanceRelation sDomRel)
	{
		SAnnotation sAnno= SaltCommonFactory.eINSTANCE.createSAnnotation();
		sAnno.setSName(EDGE_LABEL_NAME);
		sAnno.setSValue(label);
		sDomRel.addSAnnotation(sAnno);
	}
	
	/**
	 * This function maps all NT terminal nodes to SStructure nodes.
	 * @param nt
	 * @param sStructure
	 */
	private void mapNT2Structure(NT nt, SStructure sStructure)
	{
		//map Cat anno if exists
		if (	(nt.getCat()!= null)&&
				(!nt.getCat().isEmpty()))
		{
			SAnnotation sAnno= SaltSemanticsFactory.eINSTANCE.createSCatAnnotation();
			sAnno.setSValue(nt.getCat());
			sStructure.addSAnnotation(sAnno);
		}
		// annotate with sentence if it has no mother
		if (!nt.hasMother())
		{
			SSentenceAnnotation sAnno= SaltSemanticsFactory.eINSTANCE.createSSentenceAnnotation();
			sStructure.addSAnnotation(sAnno);
		}
	}
//=================================== end: mapping methods

	private String currSeperator= null;
	/**
	 * Returns the separator which shall be used to separate token text. 
	 * @return
	 */
	private String getTokenSeparator()
	{
		String retVal= null;
		if (currSeperator== null)
		{	
			
			if (	(this.getProps()!= null)&&
					(this.getProps().getProperty(KW_TOKENSEP)!= null) &&
					(!this.getProps().getProperty(KW_TOKENSEP).isEmpty()))
			{
				String preSep= this.getProps().getProperty(KW_TOKENSEP);
				
				if (preSep.length() > 2)
				{//seperatorString has to be larger than 2, because of the form " "
					preSep= preSep.replace("\"", "");
					retVal= preSep;
				}	
			}	
			else retVal= defaultSeperator;
			currSeperator= retVal;
		}
		else retVal= currSeperator;
		return(retVal);
	}
}
