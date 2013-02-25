/************************************************************************
 *  KeYmaera-MetiTarski interface. 
 *  Copyright (C) 2012  s0805753@sms.ed.ac.uk University of Edinburgh.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 *  
 ************************************************************************/

package de.uka.ilkd.key.dl.arithmetics.impl.metitarski;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import de.uka.ilkd.key.dl.arithmetics.impl.metitarski.Options;
import de.uka.ilkd.key.dl.options.EPropertyConstant;
import de.uka.ilkd.key.dl.options.FilePropertyEditor;

public class OptionsBeanInfo extends SimpleBeanInfo{

   private static final Class<Options> beanClass = Options.class;

   public OptionsBeanInfo() {
   }

   public BeanDescriptor getBeanDescriptor() {
      
      BeanDescriptor d = new BeanDescriptor(beanClass);
      
      d.  setDisplayName       ( "MetiTarski Options"                );
      d.  setShortDescription  ( "Set MetiTarski options and flags"  );

      return d;
   }

   /*-------------------------[ Property descriptors ]-------------------------*/ 

   public PropertyDescriptor[] getPropertyDescriptors() {
      try { 
         PropertyDescriptor[] pds = new PropertyDescriptor[] {
            createDescriptor ( "metitBinary"                ,  EPropertyConstant.METIT_OPTIONS_BINARY                      ,  false ,  false ,  true  ,  FilePropertyEditor.class   ),
            createDescriptor ( "backtracking"               ,  EPropertyConstant.METIT_OPTIONS_BACKTRACKING                ,  false ,  false                                        ),
            createDescriptor ( "proj_ord"                   ,  EPropertyConstant.METIT_OPTIONS_PROJ_ORD                    ,  false ,  false                                        ),
            createDescriptor ( "nsatz_eadm"                 ,  EPropertyConstant.METIT_OPTIONS_NSATZ_EADM                  ,  false ,  false                                        ),
            createDescriptor ( "icp"                        ,  EPropertyConstant.METIT_OPTIONS_ICP                         ,  false ,  false                                        ),
            createDescriptor ( "mathematica"                ,  EPropertyConstant.METIT_OPTIONS_MATHEMATICA                 ,  false ,  false                                        ),
            createDescriptor ( "z3"                         ,  EPropertyConstant.METIT_OPTIONS_Z3                          ,  false ,  false                                        ),
            createDescriptor ( "qepcad"                     ,  EPropertyConstant.METIT_OPTIONS_QEPCAD                      ,  false ,  false                                        ),
            createDescriptor ( "icp_sat"                    ,  EPropertyConstant.METIT_OPTIONS_ICP_SAT                     ,  false ,  false                                        ),
            createDescriptor ( "univ_sat"                   ,  EPropertyConstant.METIT_OPTIONS_UNIV_SAT                    ,  false ,  false                                        ),
            createDescriptor ( "paramodulation"             ,  EPropertyConstant.METIT_OPTIONS_PARAMODULATION              ,  false ,  false                                        ),
            createDescriptor ( "autoInclude"                ,  EPropertyConstant.METIT_OPTIONS_AUTOINCLUDE                 ,  false ,  false                                        ),
            createDescriptor ( "autoIncludeExtended"        ,  EPropertyConstant.METIT_OPTIONS_AUTOINCLUDE_EXTENDED        ,  false ,  false                                        ),
            createDescriptor ( "autoIncludeSuperExtended"   ,  EPropertyConstant.METIT_OPTIONS_AUTOINCLUDE_SUPER_EXTENDED  ,  false ,  false                                        ),
            createDescriptor ( "rerun"                      ,  EPropertyConstant.METIT_OPTIONS_RERUN                       ,  false ,  false                                        ),
            createDescriptor ( "unsafe_divisors"            ,  EPropertyConstant.METIT_OPTIONS_UNSAFE_DIVISORS             ,  false ,  false                                        ),
            createDescriptor ( "full"                       ,  EPropertyConstant.METIT_OPTIONS_FULL                        ,  false ,  false                                        ),
            createDescriptor ( "maxweight"                  ,  EPropertyConstant.METIT_OPTIONS_MAXWEIGHT                   ,  false ,  false                                        ),
            createDescriptor ( "maxalg"                     ,  EPropertyConstant.METIT_OPTIONS_MAXALG                      ,  false ,  false                                        ),
            createDescriptor ( "maxnonSOS"                  ,  EPropertyConstant.METIT_OPTIONS_MAXNONSOS                   ,  false ,  false                                        ),
            createDescriptor ( "cases"                      ,  EPropertyConstant.METIT_OPTIONS_CASES                       ,  false ,  false                                        ),
            createDescriptor ( "strategy"                   ,  EPropertyConstant.METIT_OPTIONS_STRATEGY                    ,  false ,  false                                        ),
         };
         return pds;
      } 
      catch (IntrospectionException ex) 
      {
         ex.printStackTrace();
         return null;
      }
   }

   private static PropertyDescriptor createDescriptor(   String propertyName,
                                                         EPropertyConstant propertyConstants, 
                                                         boolean expert, 
                                                         boolean preferred) 
      throws IntrospectionException {
      return createDescriptor(   propertyName, 
                                 propertyConstants, 
                                 expert, 
                                 preferred, 
                                 null);
   }

   private static PropertyDescriptor createDescriptor(   String propertyName, 
                                                         EPropertyConstant propertyConstants, 
                                                         boolean expert, 
                                                         boolean preferred, 
                                                         Class<?> propertyEditor)
      throws IntrospectionException {
      return createDescriptor(   propertyName, 
                                 propertyConstants, 
                                 expert, 
                                 preferred, 
                                 false, 
                                 null);
   }

   private static PropertyDescriptor createDescriptor(   String propertyName,
                                                         EPropertyConstant propertyConstants, 
                                                         boolean expert, 
                                                         boolean preferred, 
                                                         boolean hidden, 
                                                         Class<?> propertyEditor)
      throws IntrospectionException {

      PropertyDescriptor result = new PropertyDescriptor( propertyName, beanClass );

      result.setDisplayName      (propertyConstants.getLabel());
      result.setShortDescription (propertyConstants.getToolTip());
      result.setExpert           (expert);
      result.setHidden           (hidden);
      result.setPreferred        (preferred);

      if (propertyEditor != null) result.setPropertyEditorClass(propertyEditor);
      
      return result;
   }
}
