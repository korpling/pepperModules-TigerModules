/*
 * Copyright 2015 Humboldt-Universität zu Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.mappers;

import de.hu_berlin.german.korpling.saltnpepper.pepper.common.DOCUMENT_STATUS;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperMapperImpl;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class TigerSegmentMapper extends PepperMapperImpl
{
  
  public TigerSegmentMapper()
  {
    
  }

  @Override
  public DOCUMENT_STATUS mapSDocument()
  {
    return DOCUMENT_STATUS.COMPLETED;
  }
  
}
