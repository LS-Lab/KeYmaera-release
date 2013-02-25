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

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import de.uka.ilkd.key.dl.options.EPropertyConstant;
import de.uka.ilkd.key.gui.GUIEvent;
import de.uka.ilkd.key.gui.configuration.Settings;
import de.uka.ilkd.key.gui.configuration.SettingsListener;
import de.uka.ilkd.key.proof.ProofSaver;

public class Options implements Settings{

	public static final Options INSTANCE = new Options();

	private File   metitBinary;
	private File   metitAxioms;

	/* (Pertinent) MetiTarski command-line options.
	  --autoInclude ...................... Automatically select axiom files to include
	  --autoIncludeExtended .............. auto includes extended axioms
	  --autoIncludeSuperExtended ......... auto includes super extended axioms
	  --hide ITEM ........................ hide ITEM (see below for list)
	  -p ................................. show proof
	  --time positive integer ............ processor time limit (in seconds)
	  --maxweight, -w positive integer ... maximum weight of a retained clause
	  --maxalg positive integer .......... maximum number of symbols in an algebraic clause
	  --maxnonSOS positive integer ....... maximum run of non SOS given clauses allowed before giving up
	  --rerun off/ON ..................... before giving up, rerun with high maxalg (default is on)
	  --tptp DIR ......................... specify the TPTP installation directory
	  --tstp ............................. generate standard TSTP: no infixes, etc.
	  --paramodulation off/ON ............ turn full paramodulation off (default is on)
	  --cases #cases+weight .............. max permitted active case splits/nonSOS weighting factor in tenths (10 = neutral weighting)
	  --backtracking off/ON .............. turn backtracking off (default is on)
	  --proj_ord off/ON .................. switch CAD projection ordering off (default is on)
	  --nsatz_eadm ....................... enable polynomial Nullstellensatz search before EADM
	  --icp .............................. enable only polynomial ICP, no EADM
	  -m, --mathematica .................. use Mathematica as EADM
	  --z3 ............................... use SMT solver Z3 (version>=4.0) as EADM, no model-sharing
	  --qepcad ........................... use QepcadB as the EADM
	  --icp_sat .......................... use ICP to search for RCF counter-example before refutation
	  --univ_sat ......................... use univariate relaxations for RCF SAT checks (EADM only)
	  --strategy positive integer ........ ID of RCF strategy (default is 1: Z3(no_univ_factor) + model-sharing)
	  --unsafe_divisors .................. don't verify that divisors are nonzero
	  --full ............................. include variable instantiations in proofs
	  -q, --quiet ........................ Run quietly; indicate provability with return value
	  --test ............................. Skip the proof search for the input problems
	  -- ................................. no more options
	  -t, --verbose 0..5 ................. the degree of verbosity
	  -?, -h, --help ..................... display option information and exit
	  -v, --version ...................... display version information

   	Possible ITEMs are {all,name,goal,clauses,size,category,proof,saturation}.  */

	/* MetiTarski options */
	private boolean   autoInclude                ;
	private boolean   autoIncludeExtended        ;
	private boolean   autoIncludeSuperExtended   ;
	private long      time                       ;
	private long      maxweight                  ;
	private long      maxalg                     ;
	private long      maxnonSOS                  ;
	private boolean   rerun                      ;
	private boolean   paramodulation             ;
	private long      cases                      ;
	private boolean   backtracking               ;
	private boolean   proj_ord                   ;
	private boolean   nsatz_eadm                 ;
	private boolean   icp                        ;
	private boolean   mathematica                ;
	private boolean   z3                         ;
	private boolean   qepcad                     ;
	private boolean   icp_sat                    ;
	private boolean   univ_sat                   ;
	private long      strategy                   ;
	private boolean   unsafe_divisors            ;
	private boolean   full                       ;

	private List<SettingsListener> listeners;

	private Options() {

		listeners   =  new LinkedList<SettingsListener>();
		metitBinary =  new File("/opt/metit-2.0/metit");
		metitAxioms =  new File("/opt/metit-2.0/tptp");
      
      /* Set options to their default values */
      reset();
	}

	public void addSettingsListener(SettingsListener l) {
		listeners.add(l);
	}

	private void firePropertyChanged() {
		for (SettingsListener l : listeners) {
			l.settingsChanged(new GUIEvent(this));
		}
	}
	
   @Override
   public void reset() {

		/* MetiTarski default options */
		autoInclude                =  true  ;
		autoIncludeExtended        =  false ;
		autoIncludeSuperExtended   =  false ;
		time                       =  -1    ;
		maxweight                  =  -1    ;
		maxalg                     =  -1    ;
		maxnonSOS                  =  -1    ;
		rerun                      =  true  ;
		paramodulation             =  true  ;
		cases                      =  -1    ;
		backtracking               =  true  ;
		proj_ord                   =  true  ;
		nsatz_eadm                 =  false ;
		icp                        =  false ;
		mathematica                =  false ;
		z3                         =  false ;
		qepcad                     =  false ;
		icp_sat                    =  false ;
		univ_sat                   =  false ;
		strategy                   =  -1    ;
		unsafe_divisors            =  false ;
		full                       =  false ;
      
      firePropertyChanged();
   }
	 
	public void readSettings(Properties props){
      String property;
      	property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_BINARY                      .getKey()   );
		if (property != null) metitBinary                 =   new File(property);
		
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_AXIOMS                      .getKey()   );
		if (property != null) metitAxioms                 =   new File(property);
		
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_AUTOINCLUDE                 .getKey()   );
		if (property != null) autoInclude                 =   Boolean.parseBoolean(property);
		
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_AUTOINCLUDE_EXTENDED        .getKey()   );
		if (property != null) autoIncludeExtended         =   Boolean.parseBoolean(property);
		
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_AUTOINCLUDE_SUPER_EXTENDED  .getKey()   );
		if (property != null) autoIncludeSuperExtended    =   Boolean.parseBoolean(property);
		 
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_TIME                        .getKey()   );
		if (property != null) time                        =   Long.parseLong(property);

		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_MAXWEIGHT                   .getKey()   );
		if (property != null) maxweight                   =   Long.parseLong(property);
		
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_MAXALG                      .getKey()   );
		if (property != null) maxalg                      =   Long.parseLong(property);
		
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_MAXNONSOS                   .getKey()   );
		if (property != null) maxnonSOS                   =   Long.parseLong(property);

		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_RERUN                       .getKey()   );
		if (property != null) rerun                       =   Boolean.parseBoolean(property);
		
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_PARAMODULATION              .getKey()   );
		if (property != null) paramodulation              =   Boolean.parseBoolean(property);
		
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_CASES                       .getKey()   );
		if (property != null) cases                       =   Long.parseLong(property);
		
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_BACKTRACKING                .getKey()   );
		if (property != null) backtracking                =   Boolean.parseBoolean(property);
		
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_PROJ_ORD                    .getKey()   );
		if (property != null) proj_ord                    =   Boolean.parseBoolean(property);
		
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_NSATZ_EADM                  .getKey()   );
		if (property != null) nsatz_eadm                  =   Boolean.parseBoolean(property);
		
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_ICP                         .getKey()   );
		if (property != null) icp                         =   Boolean.parseBoolean(property);
		
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_MATHEMATICA                 .getKey()   );
		if (property != null) mathematica                 =   Boolean.parseBoolean(property);
		
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_Z3                          .getKey()   );
		if (property != null) z3                          =   Boolean.parseBoolean(property);
		
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_QEPCAD                      .getKey()   );
		if (property != null) qepcad                      =   Boolean.parseBoolean(property);
		
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_ICP_SAT                     .getKey()   );
		if (property != null) icp_sat                     =   Boolean.parseBoolean(property);
		
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_UNIV_SAT                    .getKey()   );
		if (property != null) univ_sat                    =   Boolean.parseBoolean(property);
		
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_STRATEGY                    .getKey()   );
		if (property != null) strategy                    =   Long.parseLong(property);
		
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_UNSAFE_DIVISORS             .getKey()   );
		if (property != null) unsafe_divisors             =   Boolean.parseBoolean(property);
		
		property = props.getProperty(                         EPropertyConstant.  METIT_OPTIONS_FULL                        .getKey()   );
		if (property != null) full                        =   Boolean.parseBoolean(property);
		
	}

	public void writeSettings(Properties props) {
		if (!ProofSaver.isInSavingMode()) {
			props.setProperty(
					EPropertyConstant.METIT_OPTIONS_BINARY.getKey(),
					metitBinary.getAbsolutePath()
					);
		}
		props.setProperty(
				EPropertyConstant.METIT_OPTIONS_BACKTRACKING.getKey(),
				"" + backtracking
				);
	}

	/* Methods as elsewhere */

	public File getMetitBinary() {
		return metitBinary;
	}

	public void setMetitBinary(File metitBinary) {
		if (!this.metitBinary.equals(metitBinary)) {
			 this.metitBinary = metitBinary;
			 firePropertyChanged();
		}
	}
	
	public File getMetitAxioms() {
		return metitAxioms;
	}

	public void setMetitAxioms(File metitAxioms) {
		if (!this.metitAxioms.equals(metitAxioms)) {
			 this.metitAxioms = metitAxioms;
			 firePropertyChanged();
		}
	}
	
	/* Option getter/setter methods (numeric options). */

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		if (this.time != time) {
			 this.time  = time;
			 firePropertyChanged();
		}
	}
	
	public long getMaxalg() {
		return maxalg;
	}

	public void setMaxalg(long maxalg) {
		if (this.maxalg != maxalg) {
			 this.maxalg  = maxalg;
			 firePropertyChanged();
		}
	}
	
	public long getMaxweight() {
		return maxweight;
	}

	public void setMaxweight(long maxweight) {
		if (this.maxweight != maxweight) {
			 this.maxweight  = maxweight;
			 firePropertyChanged();
		}
	}
	
	public long getMaxnonSOS() {
		return maxnonSOS;
	}

	public void setMaxnonSOS(long maxnonSOS) {
		if (this.maxnonSOS != maxnonSOS) {
			 this.maxnonSOS  = maxnonSOS;
			 firePropertyChanged();
		}
	}
	
	public long getCases() {
		return cases;
	}

	public void setCases(long cases) {
		if (this.cases != cases) {
			 this.cases  = cases;
			 firePropertyChanged();
		}
	}
	
	/* Strategy */
	
	public long getStrategy() {
		return strategy;
	}

	public void setStrategy(long strategy) {
		if (this.strategy != strategy) {
			 this.strategy  = strategy;
			 firePropertyChanged();
		}
	}
	
	/* Boolean options */
	
	public boolean isAutoInclude() {
		return autoInclude;
	}

	/* Include axioms automatically */
	public void setAutoInclude(boolean autoInclude) {
		if(this.autoInclude != autoInclude) {
			this.autoInclude  = autoInclude;
			firePropertyChanged();
		}
	}	
	
	public boolean isAutoIncludeExtended() {
		return autoIncludeExtended;
	}

	public void setAutoIncludeExtended(boolean autoIncludeExtended) {
		if(this.autoIncludeExtended != autoIncludeExtended) {
			this.autoIncludeExtended  = autoIncludeExtended;
			firePropertyChanged();
		}
	}	
	
	public boolean isAutoIncludeSuperExtended() {
		return autoIncludeSuperExtended;
	}

	public void setAutoIncludeSuperExtended(boolean autoIncludeSuperExtended) {
		if(this.autoIncludeSuperExtended != autoIncludeSuperExtended) {
			this.autoIncludeSuperExtended  = autoIncludeSuperExtended;
			firePropertyChanged();
		}
	}	
	
	/* Paramodulation and rerun */
	
	public boolean isRerun() {
		return rerun;
	}

	public void setRerun(boolean rerun) {
		if(this.rerun != rerun) {
			this.rerun  = rerun;
			firePropertyChanged();
		}
	}	
	
	public boolean isParamodulation() {
		return paramodulation;
	}

	public void setParamodulation(boolean paramodulation) {
		if(this.paramodulation != paramodulation) {
			this.paramodulation  = paramodulation;
			firePropertyChanged();
		}
	}	
	
	/* Projection and Backtracking */
	
	public boolean isProj_ord() {
		return proj_ord;
	}

	public void setProj_ord(boolean proj_ord) {
		if(this.proj_ord != proj_ord) {
			this.proj_ord  = proj_ord;
			firePropertyChanged();
		}
	}	
	
	public boolean isBacktracking() {
		return backtracking;
	}

	public void setBacktracking(boolean backtracking) {
		if(this.backtracking != backtracking) {
			this.backtracking  = backtracking;
			firePropertyChanged();
		}
	}	
	
	/* Nullstellensatz EADM  */
	
	public boolean isNsatz_eadm() {
		return nsatz_eadm;
	}

	public void setNsatz_eadm(boolean nsatz_eadm) {
		if(this.nsatz_eadm != nsatz_eadm) {
			this.nsatz_eadm  = nsatz_eadm;
			firePropertyChanged();
		}
	}	
	
	public boolean isIcp() {
		return icp;
	}

	public void setIcp(boolean icp) {
		if(this.icp != icp) {
			this.icp  = icp;
			if(isIcp()) {
			   /* Mutually exclusive options */
			   this  .mathematica  =  false;
			   this  .qepcad       =  false;
			   this  .z3           =  false;	 
			}
			firePropertyChanged();
		}
	}	
	
	public boolean isMathematica() {
		return mathematica;
	}

	public void setMathematica(boolean mathematica) {
		if(this.mathematica != mathematica) {
			this.mathematica  = mathematica;
         if(mathematica) {
            /* Mutually exclusive options */
            this  .icp          =  false;
            this  .qepcad       =  false;
            this  .z3           =  false;  
         }
			firePropertyChanged();
		}
	}
	
	public boolean isZ3() {
		return z3;
	}

	public void setZ3(boolean z3) {
		if(this.z3 != z3) {
			this.z3  = z3;
         if(z3) {
            /* Mutually exclusive options */
            this  .mathematica  =  false;
            this  .qepcad       =  false;
            this  .icp          =  false;  
         }
			firePropertyChanged();
		}
	}
	
	public boolean isQepcad() {
		return qepcad;
	}

	public void setQepcad(boolean qepcad) {
		if(this.qepcad != qepcad) {
			this.qepcad  = qepcad;
         if(qepcad) {
            /* Mutually exclusive options */
            this  .mathematica  =  false;
            this  .icp          =  false;
            this  .z3           =  false;  
         }
			firePropertyChanged();
		}
	}
	
	public boolean isIcp_sat() {
		return icp_sat;
	}

	public void setIcp_sat(boolean icp_sat) {
		if(this.icp_sat != icp_sat) {
			this.icp_sat  = icp_sat;
			firePropertyChanged();
		}
	}
	
	public boolean isUniv_sat() {
		return univ_sat;
	}

	public void setUniv_sat(boolean univ_sat) {
		if(this.univ_sat != univ_sat) {
			this.univ_sat  = univ_sat;
			firePropertyChanged();
		}
	}
	
	public boolean isUnsafe_divisors() {
		return unsafe_divisors;
	}

	public void setUnsafe_divisors(boolean unsafe_divisors) {
		if(this.unsafe_divisors != unsafe_divisors) {
			this.unsafe_divisors  = unsafe_divisors;
			firePropertyChanged();
		}
	}	
	
	public boolean isFull() {
		return full;
	}

	public void setFull(boolean full) {
		if(this.full != full) {
			this.full  = full;
			firePropertyChanged();
		}
	}	
}