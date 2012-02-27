package de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules;

import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import de.hu_berlin.german.korpling.tiger2.Edge;
import de.hu_berlin.german.korpling.tiger2.SyntacticNode;

/**
 * Contains all names of properties to customize a mapping from all supported tiger formats to Salt and vice versa
 * @author Florian Zipser
 *
 */
public interface TigerProperties {

	/**
	 * The prefix name of the property to customize the mapping of a {@link SyntacticNode} having a specific type
	 * to a {@link SNode} object. 
	 */
	public static final String PROP_IMPORTER_MAPPING_NODES= "pepperModules.tigerModules.importer.map";
	
	/**
	 * The prefix name of the property to customize the mapping of a {@link Edge} having a specific type
	 * to a {@link SRelation} object. 
	 */
	public static final String PROP_IMPORTER_MAPPING_EDGES= "pepperModules.tigerModules.importer.map";
}
