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
package org.corpus_tools.peppermodules.tigerModules.mappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.peppermodules.tigerModules.Tiger2ImporterProperties;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.SStructure;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SAnnotationContainer;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.corpus_tools.salt.core.SNamedElement;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.graph.Relation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * 
 * @author Florian Zipser
 *
 */
public class Tiger22SaltMapper extends PepperMapperImpl {

	private final Logger log = LoggerFactory.getLogger(Tiger22SaltMapper.class);

	/**
	 * The main object of the &lt;tiger2/&gt; model.
	 */
	protected Corpus corpus = null;

	/**
	 * Sets the main object of the &lt;tiger2/&gt; model.
	 */
	public Corpus getTigerCorpus() {
		return corpus;
	}

	/**
	 * Returns the main object of the &lt;tiger2/&gt; model.
	 */
	public void setCorpus(Corpus corpus) {
		this.corpus = corpus;
	}

	/**
	 * Returns the {@link Tiger2ImporterProperties} object containing properties
	 * to customize the mapping from data coming from a tiger2 model to a Salt
	 * model.
	 * 
	 * @return the mappingProperties
	 */
	public Tiger2ImporterProperties getProps() {
		return (Tiger2ImporterProperties) getProperties();
	}

	/**
	 * Maps a {@link SyntacticNode} object to the corresponding mapped
	 * {@link SNode} object.
	 */
	protected Map<SyntacticNode, SNode> synNode2sNode = null;

	/**
	 * Maps a {@link Relation} object to the corresponding mapped
	 * {@link SRelation} object.
	 */
	protected Map<Edge, SRelation> edge2sRelation = null;

	/**
	 * Maps the data contained in the set {@link Corpus} object (
	 * {@link #setCorpus(Corpus)}) to the set {@link SDocument}
	 * {@link #setsDocument(SDocument)} object.
	 */
	@Override
	public DOCUMENT_STATUS mapSDocument() {
		if (getDocument().getDocumentGraph() == null)
			getDocument().setDocumentGraph(SaltFactory.createSDocumentGraph());
		synNode2sNode = Collections.synchronizedMap(new Hashtable<SyntacticNode, SNode>());
		edge2sRelation = Collections.synchronizedMap(new Hashtable<Edge, SRelation>());

		// start: map document meta data
		this.mapMetaAnnotations(corpus, getDocument());
		// end: map document meta data

		// init internal string buffer to store entire text
		entireTextBuffer = new StringBuffer();
		STextualDS sTextualDs = SaltFactory.createSTextualDS();
		getDocument().getDocumentGraph().addNode(sTextualDs);

		if (this.getTigerCorpus().getSegments() != null) {
			for (Segment segment : this.getTigerCorpus().getSegments()) {
				// walk through all segments
				if (segment != null) {// map segments
					List<SToken> sTokens = null;
					if (segment.getGraphs().size() > 1)
						sTokens = new ArrayList<SToken>();
					if (segment.getGraphs() != null) {// walk through all graphs
						for (Graph graph : segment.getGraphs()) {
							if (graph != null) {
								// start: map terminals
								if (sTokens == null)
									sTokens = this.mapTerminals(graph.getTerminals(), sTextualDs);
								else
									sTokens.addAll(this.mapTerminals(graph.getTerminals(), sTextualDs));
								// end: map terminals
								// start: map non-terminals
								this.mapNonTerminals(graph.getNonTerminals());
								// end: map non-terminals
								// start: map edges
								this.mapRelations(graph.getEdges());
								// end: map edges
							}
						}
					}// walk through all graphs
					if ((getProps() != null) && (getProps().propCreateSSpan4Segment())) {
						// start: create span for segment
						getDocument().getDocumentGraph().createSpan(sTokens);
					}// end: create span for segment
				}// map segments
			}// walk through all segments
		}
		// set SText to value of internal string buffer
		sTextualDs.setText(entireTextBuffer.toString());
		return (DOCUMENT_STATUS.COMPLETED);
	}

	/**
	 * Maps the given list of {@link Terminal} objects to a list of
	 * {@link SToken} objects and adds them into the global
	 * {@link SDocumentGraph} object. The overlapped text will be mapped to the
	 * given {@link STextualDS} object.
	 * 
	 * @param terminals
	 * @param sTextualDs
	 * @return
	 */
	protected List<SToken> mapTerminals(List<Terminal> terminals, STextualDS sTextualDs) {
		List<SToken> sTokens = null;
		if (terminals == null) {
			throw new PepperModuleException(this, "Cannot map terminals, because the given list is empty.");
		}
		if (terminals.size() > 0) {
			sTokens = new ArrayList<SToken>();
			for (Terminal terminal : terminals) {
				sTokens.add(this.mapTerminal(terminal, sTextualDs));
			}
		}
		return (sTokens);
	}

	/**
	 * Contains the entire text of one {@link SDocument} object over all graphs.
	 */
	private StringBuffer entireTextBuffer = new StringBuffer();

	/**
	 * Creates an {@link SToken} object and the corresponding {@link STextualDS}
	 * object and adds it to the globel {@link SDocumentGraph} object.
	 * 
	 * @param terminal
	 * @param sTextualDs
	 * @return
	 */
	protected SToken mapTerminal(Terminal terminal, STextualDS sTextualDs) {
		if (terminal == null) {
			throw new PepperModuleException(this, "Cannot map a terminal to salt, because the terminal is empty.");
		}
		if (sTextualDs == null) {
			throw new PepperModuleException(this, "Cannot map the terminal '" + terminal + "' to salt, because the given sTextualDs is empty.");
		}

		// start: adding the overlapped text to the data source
		int startPos = 0;
		int endPos = 0;
		if (entireTextBuffer.length() != 0) {
			entireTextBuffer.append(getProps().getSeparator());
		}
		if ((entireTextBuffer != null) && (entireTextBuffer.length() != 0)) {
			startPos = entireTextBuffer.length();
		}
		// sTextualDs.setText(sTextualDs.getText()+getSeparator()+terminal.getWord());

		entireTextBuffer.append(terminal.getWord());
		if ((entireTextBuffer != null) && (entireTextBuffer.length() != 0)) {
			endPos = entireTextBuffer.length();
		}
		// end: adding the overlapped text to the data source
		SToken sToken = getDocument().getDocumentGraph().createToken(sTextualDs, startPos, endPos);
		this.synNode2sNode.put(terminal, sToken);
		// maps all annotations
		this.mapAnnotations(terminal, sToken);

		return (sToken);
	}

	/**
	 * Maps the elements of the given list of {@link Relation} objects to
	 * {@link SRelation} objects. The mapping follows the following rules:
	 * <ol>
	 * <li>object corresponding to source of {@link Relation} object is a
	 * {@link SToken} object --> {@link SPointingRelation}</li>
	 * <li>object corresponding to source of {@link Relation} object is a
	 * {@link SSpan} object and target of {@link Relation} object is a
	 * {@link SToken} object--> {@link SSpanningRelation}</li>
	 * <li>object corresponding to source of {@link Relation} object is a
	 * {@link SStructure} object --> {@link SDominanceRelation}</li>
	 * <li>otherwise --> {@link SPointingRelation}</li>
	 * </ol>
	 * 
	 * @param edges
	 */
	protected void mapRelations(List<Edge> edges) {
		if (edges != null) {
			for (Edge edge : edges) {
				if (edge != null) {
					SRelation sRelation = null;
					if (edge.getSource() == null) {
						throw new PepperModuleException(this, "Cannot map the edge '" + edge + "', because its source is empty");
					}
					SNode sourceSNode = this.synNode2sNode.get(edge.getSource());
					if (sourceSNode == null) {
						throw new PepperModuleException(this, "Cannot map the edge '" + edge + "', because its source '" + edge.getSource() + "' has no corresponding SNode object.");
					}
					if (edge.getTarget() == null) {
						throw new PepperModuleException(this, "Cannot map the edge '" + edge + "', because its target is empty");
					}
					SNode targetNode = this.synNode2sNode.get(edge.getTarget());
					if (targetNode == null) {
						throw new PepperModuleException(this, "Cannot map the edge '" + edge + "', because its source '" + edge.getTarget() + "' has no corresponding SNode object.");
					}
					SALT_TYPE saltType = null;
					if ((getProps() != null) && (edge.getType() != null)) {
						Map<String, SALT_TYPE> saltTypes = getProps().getPropRelation2SRelation();
						saltType = saltTypes.get(edge.getType());
					}

					if (getProps() != null && getProps().getRelationReversed().contains(edge.getType())) {
						// reverse secondary edges
						SNode tmpNode = sourceSNode;
						sourceSNode = targetNode;
						targetNode = tmpNode;

					}

					// start: mapping rules
					if (sourceSNode instanceof SToken) {
						if ((saltType != null) && (SALT_TYPE.SDOMINANCE_RELATION.equals(saltType))) {

							log.warn("Had to reverse the order of the edge between {} and {} " + "since the source node can't be a token.", sourceSNode.getId(), targetNode.getId());

							sRelation = SaltFactory.createSDominanceRelation();
							SNode tmpNode = sourceSNode;
							sourceSNode = targetNode;
							targetNode = tmpNode;
						} else {
							sRelation = SaltFactory.createSPointingRelation();
						}
					} else if ((sourceSNode instanceof SSpan) && (targetNode instanceof SToken)) {
						if ((saltType != null) && (SALT_TYPE.SPOINTING_RELATION.equals(saltType))) {
							// also SPointingRelation is possible, when using
							// customization
							sRelation = SaltFactory.createSPointingRelation();
						} else {
							sRelation = SaltFactory.createSSpanningRelation();
						}
					} else if (sourceSNode instanceof SStructure) {
						if ((saltType != null) && (SALT_TYPE.SPOINTING_RELATION.equals(saltType))) {
							// also SPointingRelation is possible, when using
							// customization
							sRelation = SaltFactory.createSPointingRelation();
						} else {
							sRelation = SaltFactory.createSDominanceRelation();
						}
					} else {
						sRelation = SaltFactory.createSPointingRelation();
					}
					// end: mapping rules

					if ((edge.getType() != null) && (!edge.getType().isEmpty())) {
						String newType = null;
						if (getProps().getRenamingMap_RelationType() != null) {
							newType = getProps().getRenamingMap_RelationType().get(edge.getType());
						}
						if (newType != null) {
							sRelation.setType(newType);
						} else {
							sRelation.setType(edge.getType());
						}
					}

					sRelation.setSource(sourceSNode);
					sRelation.setTarget(targetNode);

					this.mapAnnotations(edge, sRelation);
					getDocument().getDocumentGraph().addRelation(sRelation);
					this.edge2sRelation.put(edge, sRelation);
				}
			} // end for each edge
		}
	}

	/**
	 * Maps the given list of {@link NonTerminal} objects to {@link SNode}
	 * objects. The default mapping is to map a {@link NonTerminal} object to a
	 * {@link SStructure} object. that mapping.
	 * 
	 * @param nonTerminals
	 */
	protected void mapNonTerminals(List<NonTerminal> nonTerminals) {
		if (nonTerminals != null) {
			for (NonTerminal nonTerminal : nonTerminals) {
				if (nonTerminal != null) {
					SStructure sStructure = SaltFactory.createSStructure();
					this.mapAnnotations(nonTerminal, sStructure);
					getDocument().getDocumentGraph().addNode(sStructure);
					this.synNode2sNode.put(nonTerminal, sStructure);
				}
			}
		}
	}

	/**
	 * Maps all annotations of the given object to {@link SAnnotation} objects
	 * and adds them to the corresponding object.
	 */
	protected void mapAnnotations(AnnotatableElement annotatableElement, SAnnotationContainer sAnnotatableElement) {
		if (annotatableElement == null) {
			throw new PepperModuleException(this, "Cannot map annotations, because the source element is empty.");
		}
		if (sAnnotatableElement == null) {
			throw new PepperModuleException(this, "Cannot map annotations, because the target element is empty.");
		}
		for (Annotation annotation : annotatableElement.getAnnotations()) {
			String newName = null;
			String annoName = "";
			if (getProps().getRenamingMap_AnnotationName() != null) {
				newName = getProps().getRenamingMap_AnnotationName().get(annotation.getName());
			}
			if (newName != null) {
				annoName = newName;
			} else {
				annoName = annotation.getName();
			}

			sAnnotatableElement.createAnnotation(null, annoName, annotation.getValue());
		}
	}

	/**
	 * Maps all annotations of the given object to {@link SMetaAnnotation}
	 * objects and adds them to the corresponding object.
	 */
	protected void mapMetaAnnotations(Corpus corpus, SAnnotationContainer sMetaAnnotatableElement) {
		if (corpus == null) {
			throw new PepperModuleException(this, "Cannot map annotations, because the source element is empty.");
		}
		if (sMetaAnnotatableElement == null) {
			throw new PepperModuleException(this, "Cannot map annotations, because the target element is empty.");
		}

		if (corpus.getMeta() != null) {
			if (corpus.getMeta().getAuthor() != null) {
				sMetaAnnotatableElement.createMetaAnnotation(null, "author", corpus.getMeta().getAuthor());
			}
			if (corpus.getMeta().getDate() != null) {
				sMetaAnnotatableElement.createMetaAnnotation(null, "date", corpus.getMeta().getDate());
			}
			if (corpus.getMeta().getDescription() != null) {
				sMetaAnnotatableElement.createMetaAnnotation(null, "description", corpus.getMeta().getDescription());
			}
			if (corpus.getMeta().getFormat() != null) {
				sMetaAnnotatableElement.createMetaAnnotation(null, "format", corpus.getMeta().getFormat());
			}
			if (corpus.getMeta().getHistory() != null) {
				sMetaAnnotatableElement.createMetaAnnotation(null, "history", corpus.getMeta().getHistory());
			}
			if (sMetaAnnotatableElement instanceof SNamedElement) {
				((SNamedElement) sMetaAnnotatableElement).setName(corpus.getMeta().getName());
			}
		}
	}
}
