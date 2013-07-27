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
