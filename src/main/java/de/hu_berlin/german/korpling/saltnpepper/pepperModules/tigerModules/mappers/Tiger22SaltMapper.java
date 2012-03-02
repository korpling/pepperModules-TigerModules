package de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.mappers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.Tiger2Properties;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.TigerProperties;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.exceptions.TigerImportInternalException;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.exceptions.TigerImportMappingException;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.exceptions.TigerImporterException;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDominanceRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SPointingRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructure;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotatableElement;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SMetaAnnotatableElement;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SMetaAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNamedElement;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import de.hu_berlin.german.korpling.tiger2.AnnotatableElement;
import de.hu_berlin.german.korpling.tiger2.Annotation;
import de.hu_berlin.german.korpling.tiger2.Corpus;
import de.hu_berlin.german.korpling.tiger2.Edge;
import de.hu_berlin.german.korpling.tiger2.Graph;
import de.hu_berlin.german.korpling.tiger2.NonTerminal;
import de.hu_berlin.german.korpling.tiger2.Segment;
import de.hu_berlin.german.korpling.tiger2.SyntacticNode;
import de.hu_berlin.german.korpling.tiger2.Terminal;

/**
 * Maps a &lt;tiger2/&gt; model given by the tiger-api to a Salt model.
 * @author Florian Zipser
 *
 */
public class Tiger22SaltMapper 
{
	/**
	 * The default separator to separate to tokens, when no default separator is given.
	 */
	public static final String DEFAULT_SEPARATOR=" ";
	
	/**
	 * The used separator to separate to tokens.
	 */
	private static String separator= DEFAULT_SEPARATOR;
	
	/**
	 * Sets the used separator to separate to tokens.
	 */
	public static void setSeparator(String separator) {
		Tiger22SaltMapper.separator = separator;
	}

	/**
	 * Returns the used separator to separate to tokens.
	 */
	public static String getSeparator() {
		return separator;
	}
	
	/**
	 * The main object of the &lt;tiger2/&gt; model.
	 */
	protected Corpus corpus= null;

	/**
	 * Sets the main object of the &lt;tiger2/&gt; model.
	 */
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * Returns the main object of the &lt;tiger2/&gt; model.
	 */
	public void setCorpus(Corpus corpus) {
		this.corpus = corpus;
	}
	
	/**
	 * The {@link SDocument} object to be filled by the mapping with the data of the &lt;tiger2/&gt; model.
	 */
	protected SDocument sDocument= null;
	/**
	 * Returns the {@link SDocument} object to be filled by the mapping with the data of the &lt;tiger2/&gt; model.
	 */
	public SDocument getsDocument() {
		return sDocument;
	}
	/**
	 * Sets the {@link SDocument} object to be filled by the mapping with the data of the &lt;tiger2/&gt; model.
	 */
	public void setsDocument(SDocument sDocument) {
		this.sDocument = sDocument;
	}
	
	/**
	 * {@link Tiger2Properties} object containing properties to customize the mapping from data coming from a tiger2 model to a 
	 * Salt model.
	 */
	private Tiger2Properties mappingProperties= null;
	
	/**
	 * Sets the {@link Tiger2Properties} object containing properties to customize the mapping from data coming from a tiger2 model to a 
	 * Salt model.
	 * @param mappingProperties the mappingProperties to set
	 */
	public void setMappingProperties(Tiger2Properties mappingProperties) {
		this.mappingProperties = mappingProperties;
	}

	/**
	 * Returns the {@link Tiger2Properties} object containing properties to customize the mapping from data coming from a tiger2 model to a 
	 * Salt model.
	 * @return the mappingProperties
	 */
	public Tiger2Properties getMappingProperties() {
		return mappingProperties;
	}
	
	/**
	 * Maps the data contained in the given {@link Corpus} object to the given {@link SDocument} object.
	 * @param corpus {@link Corpus} object to be mapped
	 * @param sDocument {@link SDocument} object to be filled
	 */
	public void map(Corpus corpus, SDocument sDocument)
	{
		this.setCorpus(corpus);
		this.setsDocument(sDocument);
		this.map();
	}
	
	/**
	 * Maps a {@link SyntacticNode} object to the corresponding mapped {@link SNode} object.
	 */
	protected Map<SyntacticNode, SNode> synNode2sNode= null;
	
	/**
	 * Maps a {@link Edge} object to the corresponding mapped {@link SRelation} object.
	 */
	protected Map<Edge, SRelation> edge2sRelation= null;
	
	/**
	 * Maps the data contained in the set {@link Corpus} object ({@link #setCorpus(Corpus)}) to the set {@link SDocument} 
	 * {@link #setsDocument(SDocument)} object.
	 */
	public void map()
	{
		if (this.getsDocument().getSDocumentGraph()== null)
			this.getsDocument().setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
		synNode2sNode= Collections.synchronizedMap(new HashMap<SyntacticNode, SNode>());
		edge2sRelation= Collections.synchronizedMap(new HashMap<Edge, SRelation>());
		
		//start: map document meta data
			this.mapMetaAnnotations(corpus, this.getsDocument());
		//end: map document meta data
		
		//init internal string buffer to store entire text
		entireTextBuffer= new StringBuffer();
		STextualDS sTextualDs= SaltFactory.eINSTANCE.createSTextualDS();
		this.getsDocument().getSDocumentGraph().addSNode(sTextualDs);
		
		if (this.getCorpus().getSegments()!= null)
		{
			for (Segment segment: this.getCorpus().getSegments())
			{// walk through all segments
				if (segment!= null)
				{// map segments
					EList<SToken> sTokens= null;
					if (segment.getGraphs().size()> 1)
						sTokens= new BasicEList<SToken>();
					if (segment.getGraphs()!= null)
					{// walk through all graphs
						for (Graph graph: segment.getGraphs())
						{
							if (graph!= null)
							{
								//start: map terminals
									if (sTokens== null)
										sTokens= this.mapTerminals(graph.getTerminals(), sTextualDs);
									else sTokens.addAll(this.mapTerminals(graph.getTerminals(), sTextualDs));
								//end: map terminals
								//start: map non-terminals
									this.mapNonTerminals(graph.getNonTerminals());
								//end: map non-terminals
								//start: map edges
									this.mapEdges(graph.getEdges());
								//end: map edges
							}
						}
					}// walk through all graphs
					if (	(this.getMappingProperties()!= null)&&
							(this.getMappingProperties().propCreateSSpan4Segment()))
					{//start: create span for segment
						this.getsDocument().getSDocumentGraph().createSSpan(sTokens);
					}//end: create span for segment
				}// map segments
			}// walk through all segments
		}
		//set SText to value of internal string buffer
		sTextualDs.setSText(entireTextBuffer.toString());
		
	}
	
	/**
	 * Maps the given list of {@link Terminal} objects to a list of {@link SToken} objects and adds them into the global 
	 * {@link SDocumentGraph} object. The overlapped text will be mapped to the given {@link STextualDS} object.    
	 * @param terminals
	 * @param sTextualDs
	 * @return
	 */
	protected EList<SToken> mapTerminals(EList<Terminal> terminals, STextualDS sTextualDs)
	{
		EList<SToken> sTokens= null;
		if (terminals== null)
			throw new TigerImportInternalException("Cannot map terminals, because the given list is empty.");
		if (terminals.size()>0)
		{
			sTokens= new BasicEList<SToken>();
			for (Terminal terminal: terminals)
			{
				sTokens.add(this.mapTerminal(terminal, sTextualDs));
			}
		}
		return(sTokens);
	}
	
	/**
	 * Contains the entire text of one {@link SDocument} object over all graphs.
	 */
	private StringBuffer entireTextBuffer= new StringBuffer();
	
	/**
	 * Creates an {@link SToken} object and the corresponding {@link STextualDS} object and adds it to the globel {@link SDocumentGraph} object.
	 * @param terminal
	 * @param sTextualDs
	 * @return
	 */
	protected SToken mapTerminal(Terminal terminal, STextualDS sTextualDs)
	{
		if (terminal== null)
			throw new TigerImportMappingException("Cannot map a terminal to salt, because the terminal is empty.");
		if (sTextualDs== null)
			throw new TigerImportMappingException("Cannot map the terminal '"+terminal+"' to salt, because the given sTextualDs is empty.");
		
		//start: adding the overlapped text to the data source 
			int startPos= 0;
			int endPos= 0;
			if (entireTextBuffer.length()!= 0)
				entireTextBuffer.append(getSeparator());
			if (	(entireTextBuffer!= null)&&
					(entireTextBuffer.length()!= 0))
				startPos= entireTextBuffer.length();
			//sTextualDs.setSText(sTextualDs.getSText()+getSeparator()+terminal.getWord());
			
			entireTextBuffer.append(terminal.getWord());
			if (	(entireTextBuffer!= null)&&
					(entireTextBuffer.length()!= 0))
				endPos= entireTextBuffer.length();
		//end: adding the overlapped text to the data source
		SToken sToken=	this.getsDocument().getSDocumentGraph().createSToken(sTextualDs, startPos, endPos);
		this.synNode2sNode.put(terminal, sToken);
		//maps all annotations
		this.mapAnnotations(terminal, sToken);
				
		return(sToken);
	}
	
	/**
	 * Maps the elements of the given list of {@link Edge} objects to {@link SRelation} objects. The mapping follows the
	 * following rules: 
	 * <ol>
	 * 	<li>object corresponding to source of {@link Edge} object is a {@link SToken} object --> {@link SPointingRelation}</li>
	 * 	<li>object corresponding to source of {@link Edge} object is a {@link SSpan} object and  target of {@link Edge} object is a {@link SToken} object--> {@link SSpanningRelation}</li>
	 *  <li>object corresponding to source of {@link Edge} object is a {@link SStructure} object --> {@link SDominanceRelation}</li>
	 *  <li>otherwise --> {@link SPointingRelation}</li>
	 * </ol> 
	 * The user can customize this mapping (only in a valid range that does not violate constraints) by using the flag 
	 * {@link TigerProperties#PROP_IMPORTER_MAPPING_EDGES}.
	 * TODO currently, the customization is not supported
	 * @param edges
	 */
	protected void mapEdges(EList<Edge> edges)
	{
		if (edges!= null)
		{
			for (Edge edge: edges)
			{
				if (edge!= null)
				{
					SRelation sRelation= null;
					if (edge.getSource()== null)
						throw new TigerImporterException("Cannot map the edge '"+edge+"', because its source is empty");
					SNode sourceSNode= this.synNode2sNode.get(edge.getSource());
					if (sourceSNode== null)
						throw new TigerImporterException("Cannot map the edge '"+edge+"', because its source '"+edge.getSource()+"' has no corresponding SNode object.");
					if (edge.getTarget()== null)
						throw new TigerImporterException("Cannot map the edge '"+edge+"', because its target is empty");
					SNode targetSNode= this.synNode2sNode.get(edge.getTarget());
					if (targetSNode== null)
						throw new TigerImporterException("Cannot map the edge '"+edge+"', because its source '"+edge.getTarget()+"' has no corresponding SNode object.");
					STYPE_NAME saltType= null;
					if (	(this.getMappingProperties()!= null)&&
							(edge.getType()!= null))
					{
						Map<String, STYPE_NAME> saltTypes= this.getMappingProperties().propEdge2SRelation();
						saltType= saltTypes.get(edge.getType());
					}
					
					
					//start: mapping rules
						if (sourceSNode instanceof SToken)
							sRelation= SaltFactory.eINSTANCE.createSPointingRelation();
						else if (	(sourceSNode instanceof SSpan) &&
									(targetSNode instanceof SToken))
						{
							if (	(saltType!= null)&&
									(STYPE_NAME.SPOINTING_RELATION.equals(saltType)))
							{//also SPointingRelation is possible, when using customization
								sRelation= SaltFactory.eINSTANCE.createSPointingRelation();
							}//also SPointingRelation is possible, when using customization
							else sRelation= SaltFactory.eINSTANCE.createSSpanningRelation();
						}
						else if (sourceSNode instanceof SStructure)
						{
							if (	(saltType!= null)&&
									(STYPE_NAME.SPOINTING_RELATION.equals(saltType)))
							{//also SPointingRelation is possible, when using customization
								sRelation= SaltFactory.eINSTANCE.createSPointingRelation();
							}//also SPointingRelation is possible, when using customization
							else sRelation= SaltFactory.eINSTANCE.createSDominanceRelation();
						}
						else sRelation= SaltFactory.eINSTANCE.createSPointingRelation();
					//end: mapping rules
					
					if (	(edge.getType()!= null)&&
							(!edge.getType().isEmpty()))
						sRelation.addSType(edge.getType());
					sRelation.setSSource(sourceSNode);
					sRelation.setSTarget(targetSNode);
					this.mapAnnotations(edge, sRelation);
					this.getsDocument().getSDocumentGraph().addSRelation(sRelation);
					this.edge2sRelation.put(edge, sRelation);
				}
			}
		}
	}
	
	/**
	 * Maps the given list of {@link NonTerminal} objects to {@link SNode} objects. The default mapping is to map a {@link NonTerminal}
	 * object to a {@link SStructure} object, but with the flag {@link TigerProperties#PROP_IMPORTER_MAPPING_NODES} the user can customize
	 * that mapping.
	 * TODO currently, the customization is not supported 
	 * @param nonTerminals
	 */
	protected void mapNonTerminals(EList<NonTerminal> nonTerminals)
	{
		if (nonTerminals!= null)
		{
			for (NonTerminal nonTerminal: nonTerminals)
			{
				if (nonTerminal!= null)
				{
					//TODO read property pepperModules.tigerModules.importer.map.TYPE to differentiate between span and structure
					SStructure sStructure= SaltFactory.eINSTANCE.createSStructure();
					this.mapAnnotations(nonTerminal, sStructure);
					this.getsDocument().getSDocumentGraph().addSNode(sStructure);
					this.synNode2sNode.put(nonTerminal, sStructure);
				}
			}
		}
	}
	
	/**
	 * Maps all annotations of the given object to {@link SAnnotation} objects and adds them to the corresponding object.
	 */
	protected void mapAnnotations(AnnotatableElement annotatableElement, SAnnotatableElement sAnnotatableElement)
	{
		if (annotatableElement== null)
			throw new TigerImportInternalException("Cannot map annotations, because the source element is empty.");
		if (sAnnotatableElement== null)
			throw new TigerImportInternalException("Cannot map annotations, because the target element is empty.");
		for (Annotation annotation: annotatableElement.getAnnotations())
		{
			sAnnotatableElement.createSAnnotation(null, annotation.getName(), annotation.getValue());
		}
	}
	
	/**
	 * Maps all annotations of the given object to {@link SMetaAnnotation} objects and adds them to the corresponding object.
	 */
	protected void mapMetaAnnotations(Corpus corpus, SMetaAnnotatableElement sMetaAnnotatableElement)
	{
		if (corpus== null)
			throw new TigerImportInternalException("Cannot map annotations, because the source element is empty.");
		if (sMetaAnnotatableElement== null)
			throw new TigerImportInternalException("Cannot map annotations, because the target element is empty.");
		
		if (corpus.getMeta().getAuthor()!= null)
			sMetaAnnotatableElement.createSMetaAnnotation(null, "author", corpus.getMeta().getAuthor());
		if (corpus.getMeta().getDate()!= null)
			sMetaAnnotatableElement.createSMetaAnnotation(null, "date", corpus.getMeta().getDate());
		if (corpus.getMeta().getDescription()!= null)
			sMetaAnnotatableElement.createSMetaAnnotation(null, "description", corpus.getMeta().getDescription());
		if (corpus.getMeta().getFormat()!= null)
			sMetaAnnotatableElement.createSMetaAnnotation(null, "format", corpus.getMeta().getFormat());
		if (corpus.getMeta().getHistory()!= null)
			sMetaAnnotatableElement.createSMetaAnnotation(null, "history", corpus.getMeta().getHistory());
		if (sMetaAnnotatableElement instanceof SNamedElement)
			((SNamedElement) sMetaAnnotatableElement).setSName(corpus.getMeta().getName());
	}
}
